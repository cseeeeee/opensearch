package com.example.booksearch.dto;

import com.example.booksearch.domain.Book;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 도서 등록/수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class BookRequest {

    /** 도서명 */
    private String title;

    /** 저자 */
    private String author;

    /** 출판사 */
    private String publisher;

    /** 도서 소개 */
    private String description;

    /** ISBN */
    private String isbn;

    /** 가격 (원) */
    private Integer price;

    /** 출판일 */
    private LocalDate publishedDate;

    /** 카테고리 */
    private String category;

    /** 재고 수량 */
    private Integer stockQuantity;

    /** 표지 이미지 URL */
    private String coverImageUrl;

    @Builder
    public BookRequest(String title, String author, String publisher, String description,
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
     * Book 엔티티 → BookRequest 변환 팩토리 메서드
     *
     * @param book 변환할 도서 엔티티
     * @return 도서 요청 DTO
     */
    public static BookRequest from(Book book) {
        return BookRequest.builder()
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .description(book.getDescription())
                .isbn(book.getIsbn())
                .price(book.getPrice())
                .publishedDate(book.getPublishedDate())
                .category(book.getCategory())
                .stockQuantity(book.getStockQuantity())
                .coverImageUrl(book.getCoverImageUrl())
                .build();
    }
}
