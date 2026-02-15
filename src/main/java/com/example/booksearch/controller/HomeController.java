package com.example.booksearch.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 홈 및 공통 API 컨트롤러
 */
@RestController
public class HomeController {

    /**
     * 애플리케이션 헬스체크 API
     *
     * @return 상태 정보를 담은 Map (status, application)
     */
    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "application", "book-search"
        );
    }
}
