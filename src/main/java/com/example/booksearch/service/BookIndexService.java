package com.example.booksearch.service;

import com.example.booksearch.domain.Book;
import com.example.booksearch.domain.BookDocument;
import com.example.booksearch.repository.BookSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * OpenSearch 인덱스 관리 및 문서 CRUD 서비스
 *
 * 인덱스 생성/삭제, 단건 문서 CRUD, 벌크 인덱싱을 담당한다
 * BookService에서 PostgreSQL 저장 후 호출하여 OpenSearch 동기화를 수행한다
 *
 * OpenSearch 장애 시에도 PostgreSQL 작업에 영향을 주지 않도록
 * 각 메서드에서 예외를 catch하여 로그 경고로 처리한다
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookIndexService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final BookSearchRepository bookSearchRepository;

    /** 벌크 인덱싱 시 한 번에 처리할 문서 수 */
    private static final int BULK_CHUNK_SIZE = 100;

    // ── 인덱스 관리 ─────────────────────────────────────

    /**
     * 인덱스 존재 여부 확인
     *
     * @return 인덱스 존재 시 true
     */
    public boolean indexExists() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(BookDocument.class);
        return indexOps.exists();
    }

    /**
     * 인덱스 생성 (이미 존재하면 스킵)
     *
     * BookDocument의 @Document, @Setting, @Mapping 어노테이션 기반으로
     * 인덱스 설정(Nori 분석기)과 매핑을 함께 생성한다
     */
    public void createIndexIfNotExists() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(BookDocument.class);

        if (indexOps.exists()) {
            log.info("OpenSearch 인덱스 'books'가 이미 존재합니다.");
            return;
        }

        boolean created = indexOps.createWithMapping();
        if (created) {
            log.info("OpenSearch 인덱스 'books' 생성 완료 (Nori 분석기 포함)");
        } else {
            log.warn("OpenSearch 인덱스 'books' 생성 실패");
        }
    }

    /**
     * 인덱스 삭제 후 재생성
     *
     * 매핑 변경 시 사용, 기존 문서가 모두 삭제되므로
     * 재생성 후 bulkIndexBooks()로 데이터를 다시 인덱싱해야 한다
     */
    public void recreateIndex() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(BookDocument.class);

        if (indexOps.exists()) {
            indexOps.delete();
            log.info("OpenSearch 인덱스 'books' 삭제 완료");
        }

        boolean created = indexOps.createWithMapping();
        if (created) {
            log.info("OpenSearch 인덱스 'books' 재생성 완료");
        } else {
            log.warn("OpenSearch 인덱스 'books' 재생성 실패");
        }
    }

    // ── 단건 문서 CRUD ──────────────────────────────────

    /**
     * 단건 문서 인덱싱 (생성/수정 겸용)
     *
     * 동일 ID의 문서가 있으면 덮어쓴다 (upsert 동작)
     *
     * @param book 인덱싱할 도서 엔티티
     */
    public void indexBook(Book book) {
        try {
            BookDocument document = BookDocument.from(book);
            bookSearchRepository.save(document);
            log.debug("도서 인덱싱 완료: id={}, title={}", book.getId(), book.getTitle());
        } catch (Exception e) {
            log.warn("도서 인덱싱 실패: id={}, title={}, error={}",
                    book.getId(), book.getTitle(), e.getMessage());
        }
    }

    /**
     * 단건 문서 삭제
     *
     * @param bookId 삭제할 도서 ID
     */
    public void deleteBook(Long bookId) {
        try {
            bookSearchRepository.deleteById(bookId);
            log.debug("도서 인덱스 삭제 완료: id={}", bookId);
        } catch (Exception e) {
            log.warn("도서 인덱스 삭제 실패: id={}, error={}", bookId, e.getMessage());
        }
    }

    // ── 검색 ─────────────────────────────────────────────

    /**
     * 키워드 기반 도서 검색 (multi_match)
     *
     * title, author, description 필드를 대상으로 Nori 형태소 분석 검색 수행
     *
     * @param keyword  검색 키워드
     * @param pageable 페이지 정보
     * @return 검색 결과 (Page)
     */
    public Page<BookDocument> searchBooks(String keyword, Pageable pageable) {
        String queryString = String.format("""
                {
                  "multi_match": {
                    "query": "%s",
                    "fields": ["title^3", "author^2", "description"],
                    "type": "best_fields"
                  }
                }
                """, keyword.replace("\"", "\\\""));

        var query = new org.springframework.data.elasticsearch.core.query.StringQuery(queryString);
        query.setPageable(pageable);

        SearchHits<BookDocument> searchHits = elasticsearchOperations.search(
                query, BookDocument.class);

        List<BookDocument> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();

        return PageableExecutionUtils.getPage(content, pageable, searchHits::getTotalHits);
    }

    // ── 벌크 인덱싱 ─────────────────────────────────────

    /**
     * 전체 도서 벌크 인덱싱
     *
     * 대량 데이터를 {@value BULK_CHUNK_SIZE}건 단위로 분할하여 처리한다
     * ElasticsearchOperations.bulkIndex()를 사용하여 네트워크 왕복을 최소화한다
     *
     * @param books 인덱싱할 도서 엔티티 목록
     */
    public void bulkIndexBooks(List<Book> books) {
        if (books == null || books.isEmpty()) {
            log.info("인덱싱할 도서가 없습니다.");
            return;
        }

        log.info("벌크 인덱싱 시작: 총 {}건", books.size());
        int successCount = 0;

        for (int i = 0; i < books.size(); i += BULK_CHUNK_SIZE) {
            int end = Math.min(i + BULK_CHUNK_SIZE, books.size());
            List<Book> chunk = books.subList(i, end);

            try {
                List<IndexQuery> queries = chunk.stream()
                        .map(book -> new IndexQueryBuilder()
                                .withId(String.valueOf(book.getId()))
                                .withObject(BookDocument.from(book))
                                .build())
                        .toList();

                elasticsearchOperations.bulkIndex(queries, IndexCoordinates.of("books"));
                successCount += chunk.size();
                log.debug("벌크 인덱싱 청크 완료: {}-{} / {}", i + 1, end, books.size());
            } catch (Exception e) {
                log.warn("벌크 인덱싱 청크 실패: {}-{}, error={}", i + 1, end, e.getMessage());
            }
        }

        log.info("벌크 인덱싱 완료: 성공 {}건 / 총 {}건", successCount, books.size());
    }
}
