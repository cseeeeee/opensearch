package com.example.booksearch.init;

import com.example.booksearch.dto.BookRequestDto;
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
 * 애플리케이션 시작 시 초기 도서 데이터 로딩 컴포넌트
 *
 * src/main/resources/data/books.json 파일을 읽어
 * DB에 데이터가 없을 경우 자동으로 샘플 도서 등록
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final BookService bookService;

    /**
     * 애플리케이션 기동 시 초기 데이터 로딩 실행
     *
     * DB에 도서 데이터가 없으면 books.json에서 초기 데이터 로딩
     *
     * @param args 애플리케이션 실행 인자
     * @throws Exception JSON 파싱 또는 DB 저장 실패 시
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (bookService.count() > 0) {
            log.info("기존 도서 데이터 {}건 존재. 초기 데이터 로딩 스킵.", bookService.count());
            return;
        }

        log.info("초기 도서 데이터 로딩 시작...");

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        ClassPathResource resource = new ClassPathResource("data/books.json");
        try (InputStream is = resource.getInputStream()) {
            List<BookRequestDto> requests = mapper.readValue(is, new TypeReference<>() {});

            for (BookRequestDto request : requests) {
                bookService.createBook(request);
            }

            log.info("초기 도서 데이터 {}건 로딩 완료.", requests.size());
        }
    }
}
