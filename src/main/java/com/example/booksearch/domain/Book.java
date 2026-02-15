package com.example.booksearch.domain;

import com.example.booksearch.dto.BookRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 도서 정보 JPA Entity
 *
 * <p>PostgreSQL에 저장되는 도서 원본 데이터이며,
 * OpenSearch 인덱스와 동기화하여 검색에 활용</p>
 */
@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 도서명 */
    @Column(nullable = false)
    private String title;

    /** 저자 */
    @Column(nullable = false)
    private String author;

    /** 출판사 */
    private String publisher;

    /** 도서 소개 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** ISBN */
    @Column(unique = true)
    private String isbn;

    /** 가격 (원) */
    private Integer price;

    /** 출판일 */
    private LocalDate publishedDate;

    /** 카테고리 (예: IT, 문학, 경제) */
    private String category;

    /** 재고 수량 */
    private Integer stockQuantity;

    /** 표지 이미지 URL */
    private String coverImageUrl;

    /** 등록 일시 */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /** 수정 일시 */
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Book 엔티티 생성용 빌더
     *
     * @param title         도서명
     * @param author        저자
     * @param publisher     출판사
     * @param description   도서 소개
     * @param isbn          ISBN
     * @param price         가격
     * @param publishedDate 출판일
     * @param category      카테고리
     * @param stockQuantity 재고 수량
     * @param coverImageUrl 표지 이미지 URL
     */
    @Builder
    public Book(String title, String author, String publisher, String description,
                String isbn, Integer price, LocalDate publishedDate, String category,
                Integer stockQuantity, String coverImageUrl) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.description = description;
        this.isbn = isbn;
        this.price = price;
        this.publishedDate = publishedDate;
        this.category = category;
        this.stockQuantity = stockQuantity;
        this.coverImageUrl = coverImageUrl;
    }

    /**
     * 도서 정보 수정
     *
     * @param request 도서 수정 요청 DTO
     */
    public void update(BookRequest request) {
        this.title = request.getTitle();
        this.author = request.getAuthor();
        this.publisher = request.getPublisher();
        this.description = request.getDescription();
        this.isbn = request.getIsbn();
        this.price = request.getPrice();
        this.publishedDate = request.getPublishedDate();
        this.category = request.getCategory();
        this.stockQuantity = request.getStockQuantity();
        this.coverImageUrl = request.getCoverImageUrl();
    }
}
