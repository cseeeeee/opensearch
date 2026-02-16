package com.example.booksearch.service;

import com.example.booksearch.domain.Book;
import com.example.booksearch.dto.BookRequestDto;
import com.example.booksearch.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 도서 CRUD 비즈니스 로직 서비스
 *
 * <p>현재는 PostgreSQL(JPA)만 사용하며,
 * 이후 Phase에서 OpenSearch 동기화 로직 추가 예정</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    /**
     * 도서 등록
     *
     * @param request 도서 등록 요청 DTO
     * @return 등록된 도서 엔티티
     */
    @Transactional
    public Book createBook(BookRequestDto request) {
        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .publisher(request.getPublisher())
                .description(request.getDescription())
                .isbn(request.getIsbn())
                .price(request.getPrice())
                .publishedDate(request.getPublishedDate())
                .category(request.getCategory())
                .stockQuantity(request.getStockQuantity())
                .coverImageUrl(request.getCoverImageUrl())
                .build();

        return bookRepository.save(book);
    }

    /**
     * ID 기반 도서 조회
     *
     * @param id 도서 ID
     * @return 도서 엔티티
     * @throws IllegalArgumentException 해당 ID의 도서가 없을 경우
     */
    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("도서를 찾을 수 없습니다. id=" + id));
    }

    /**
     * 전체 도서 목록 페이징 조회
     *
     * @param pageable 페이지 정보
     * @return 도서 목록 (Page)
     */
    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    /**
     * 전체 도서 목록 조회
     *
     * @return 전체 도서 리스트
     */
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    /**
     * 도서 정보 수정
     *
     * @param id      수정할 도서 ID
     * @param request 도서 수정 요청 DTO
     * @return 수정된 도서 엔티티
     */
    @Transactional
    public Book updateBook(Long id, BookRequestDto request) {
        Book book = findById(id);
        book.update(request);
        return book;
    }

    /**
     * 도서 삭제
     *
     * @param id 삭제할 도서 ID
     */
    @Transactional
    public void deleteBook(Long id) {
        Book book = findById(id);
        bookRepository.delete(book);
    }

    /**
     * 전체 도서 수 조회
     *
     * @return 도서 수
     */
    public long count() {
        return bookRepository.count();
    }

    /**
     * 등록된 카테고리 목록 조회
     *
     * @return 카테고리 목록
     */
    public List<String> findCategories() {
        return bookRepository.findDistinctCategories();
    }
}
