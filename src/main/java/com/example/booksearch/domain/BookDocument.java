package com.example.booksearch.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * OpenSearch 검색 인덱스용 도서 문서
 *
 * Book 엔티티와 동일한 데이터를 검색 최적화 형태로 저장.
 * Nori 한글 형태소 분석기 적용으로 한국어 Full-text 검색 지원.
 *
 * 인덱스 매핑 전략:
 * <ul>
 *   <li>text (korean 분석기): title, author, description → Full-text 검색 대상</li>
 *   <li>keyword: publisher, isbn, category → 정확한 값 필터링</li>
 *   <li>integer: price, stockQuantity → 범위 검색</li>
 *   <li>date: publishedDate, createdAt, updatedAt → 범위 검색 및 정렬</li>
 * </ul>
 */
@Document(indexName = "books")
@Setting(settingPath = "/opensearch/book-index-settings.json")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookDocument {

    /** Book 엔티티 PK와 동일한 값 (동기화 키) */
    @Id
    private Long id;

    /** 도서명 - Nori 형태소 분석 Full-text 검색 대상 */
    @Field(type = FieldType.Text, analyzer = "korean", searchAnalyzer = "korean_search")
    private String title;

    /** 저자 - Nori 형태소 분석 Full-text 검색 대상 */
    @Field(type = FieldType.Text, analyzer = "korean")
    private String author;

    /** 출판사 - keyword 정확한 값 필터링 */
    @Field(type = FieldType.Keyword)
    private String publisher;

    /** 도서 소개 - Nori 형태소 분석 Full-text 검색 대상 */
    @Field(type = FieldType.Text, analyzer = "korean")
    private String description;

    /** ISBN - keyword 정확한 값 매칭 */
    @Field(type = FieldType.Keyword)
    private String isbn;

    /** 가격 - 범위 검색 (minPrice ~ maxPrice) */
    @Field(type = FieldType.Integer)
    private Integer price;

    /** 출판일 - 범위 검색 및 정렬 */
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd")
    private LocalDate publishedDate;

    /** 카테고리 - 필터링 및 Aggregation */
    @Field(type = FieldType.Keyword)
    private String category;

    /** 재고 수량 - 범위 필터링 */
    @Field(type = FieldType.Integer)
    private Integer stockQuantity;

    /** 표지 이미지 URL - 검색 제외, 표시 전용 */
    @Field(type = FieldType.Keyword, index = false)
    private String coverImageUrl;

    /** 등록 일시 - 정렬용 */
    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    /** 수정 일시 - 동기화 추적용 */
    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;

    @Builder
    public BookDocument(Long id, String title, String author, String publisher,
                        String description, String isbn, Integer price,
                        LocalDate publishedDate, String category, Integer stockQuantity,
                        String coverImageUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
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
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * JPA Book 엔티티 → OpenSearch BookDocument 변환 팩토리 메서드
     *
     * Book의 PK(id)를 OpenSearch 문서의 _id로 그대로 사용하여
     * 양쪽 저장소 간 동기화 보장.
     *
     * @param book JPA Book 엔티티
     * @return OpenSearch 인덱싱용 BookDocument
     */
    public static BookDocument from(Book book) {
        return BookDocument.builder()
                .id(book.getId())
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
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}
