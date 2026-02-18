package com.example.booksearch.repository;

import com.example.booksearch.domain.BookDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * OpenSearch 도서 검색 Repository
 *
 * Spring Data의 메서드 이름 기반 쿼리 자동 생성 활용.
 * 단순 조건 검색용이며, 복합 검색(multi_match, bool, aggregation)은
 * BookIndexService에서 ElasticsearchOperations로 직접 처리.
 */
public interface BookSearchRepository extends ElasticsearchRepository<BookDocument, Long> {

    /**
     * 카테고리별 도서 문서 검색
     *
     * OpenSearch term 쿼리로 변환됨:
     * {"query": {"term": {"category": "IT"}}}
     *
     * @param category 카테고리 (keyword 타입 정확 매칭)
     * @param pageable 페이지 정보
     * @return 해당 카테고리 도서 목록
     */
    Page<BookDocument> findByCategory(String category, Pageable pageable);

    /**
     * 가격 범위 도서 문서 검색
     *
     * OpenSearch range 쿼리로 변환됨:
     * {"query": {"range": {"price": {"gte": 10000, "lte": 30000}}}}
     *
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @param pageable 페이지 정보
     * @return 가격 범위 내 도서 목록
     */
    Page<BookDocument> findByPriceBetween(Integer minPrice, Integer maxPrice, Pageable pageable);

    /**
     * 카테고리 + 가격 범위 복합 조건 검색
     *
     * OpenSearch bool 쿼리로 변환됨:
     * {"query": {"bool": {"must": [
     *   {"term": {"category": "IT"}},
     *   {"range": {"price": {"gte": 10000, "lte": 30000}}}
     * ]}}}
     *
     * @param category 카테고리
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @param pageable 페이지 정보
     * @return 조건에 맞는 도서 목록
     */
    Page<BookDocument> findByCategoryAndPriceBetween(
            String category, Integer minPrice, Integer maxPrice, Pageable pageable);

    /**
     * 출판사별 도서 문서 검색
     *
     * @param publisher 출판사명 (keyword 타입 정확 매칭)
     * @return 해당 출판사 도서 목록
     */
    List<BookDocument> findByPublisher(String publisher);
}
