package com.example.booksearch.repository;

import com.example.booksearch.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 도서 데이터 JPA Repository
 *
 * PostgreSQL에 저장된 도서 원본 데이터 CRUD
 */
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * ISBN 기반 도서 조회
     *
     * @param isbn 조회할 ISBN
     * @return 해당 ISBN의 도서 (Optional)
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * ISBN 존재 여부 확인
     *
     * @param isbn 확인할 ISBN
     * @return 존재 여부
     */
    boolean existsByIsbn(String isbn);

    /**
     * 카테고리별 도서 목록 조회
     *
     * @param category 카테고리
     * @param pageable 페이지 정보
     * @return 해당 카테고리의 도서 목록 (Page)
     */
    Page<Book> findByCategory(String category, Pageable pageable);

    /**
     * 전체 카테고리 목록 중복 제거 조회
     *
     * @return 카테고리 목록
     */
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT b.category FROM Book b WHERE b.category IS NOT NULL ORDER BY b.category")
    List<String> findDistinctCategories();
}
