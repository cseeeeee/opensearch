package com.example.booksearch.init;

import com.example.booksearch.domain.Book;
import com.example.booksearch.dto.BookRequestDto;
import com.example.booksearch.service.BookIndexService;
import com.example.booksearch.service.BookService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/**
 * 애플리케이션 시작 시 초기 도서 데이터 로딩 및 OpenSearch 인덱싱
 *
 * src/main/resources/data/books.json 파일을 읽어
 * DB에 데이터가 없을 경우 자동으로 샘플 도서 등록
 * 기동 시마다 OpenSearch 인덱스 존재를 보장하고,
 * DB에 있지만 OpenSearch에 없는 데이터를 벌크 인덱싱한다
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final BookService bookService;
    private final BookIndexService bookIndexService;

    /**
     * 애플리케이션 기동 시 초기 데이터 로딩 및 OpenSearch 동기화
     *
     * 1) OpenSearch 인덱스 생성 보장
     * 2) DB에 도서 데이터가 없으면 books.json에서 초기 데이터 로딩
     * 3) DB 전체 도서를 OpenSearch에 벌크 인덱싱
     *
     * @param args 애플리케이션 실행 인자
     * @throws Exception JSON 파싱 또는 DB 저장 실패 시
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 1) OpenSearch 인덱스 생성 보장
        bookIndexService.createIndexIfNotExists();

        // 2) DB에 데이터가 없으면 JSON에서 초기 로딩
        if (bookService.count() == 0) {
            log.info("초기 도서 데이터 로딩 시작...");

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            ClassPathResource resource = new ClassPathResource("data/books.json");
            try (InputStream is = resource.getInputStream()) {
                List<BookRequestDto> requests = mapper.readValue(is, new TypeReference<>() {});

                for (BookRequestDto request : requests) {
                    bookService.createBook(request);
                }

                log.info("초기 도서 데이터 {}건 로딩 완료", requests.size());
            }
        } else {
            log.info("기존 도서 데이터 {}건 존재, 초기 데이터 로딩 스킵", bookService.count());
        }

        // 3) DB 전체 도서를 OpenSearch에 벌크 인덱싱 (누락 데이터 동기화)
        List<Book> allBooks = bookService.findAll();
        if (!allBooks.isEmpty()) {
            bookIndexService.bulkIndexBooks(allBooks);
        }
    }
}
