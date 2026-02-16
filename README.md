# Book Search

OpenSearch + Spring Boot 기반 도서 검색 학습 프로젝트

## 기술 스택

| 구분 | 기술 |
|------|------|
| Backend | Spring Boot 3.4.3, Java 17 |
| Database | PostgreSQL 15 |
| Search Engine | OpenSearch 2.11.0 |
| ORM | Spring Data JPA, Hibernate |
| Template | Thymeleaf |
| Build | Gradle |
| Infra | Docker Compose |

## 프로젝트 구조

```
src/main/java/com/example/booksearch/
├── BookSearchApplication.java      # 메인 애플리케이션
├── controller/
│   ├── AdminController.java        # 관리자 도서 CRUD
│   └── HomeController.java         # 헬스체크 API
├── domain/
│   └── Book.java                   # 도서 JPA 엔티티
├── dto/
│   └── BookRequest.java            # 도서 등록/수정 요청 DTO
├── init/
│   └── DataInitializer.java        # 초기 샘플 데이터 로딩
├── repository/
│   └── BookRepository.java         # 도서 JPA Repository
└── service/
    └── BookService.java            # 도서 비즈니스 로직
```

## 시작하기

### 1. Docker 컨테이너 실행

```bash
docker-compose up -d
```

| 서비스 | 포트 |
|--------|------|
| PostgreSQL | localhost:5433 |
| OpenSearch | localhost:9200 |
| OpenSearch Dashboards | localhost:5601 |

### 2. 프로필별 properties 설정

`application-local.properties` 파일을 `src/main/resources/` 에 생성:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/opensearch
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
```

> `application-local.properties`, `application-dev.properties`는 `.gitignore`에 포함되어 있음

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

또는 IntelliJ에서 `BookSearchApplication.java` 실행

### 4. 접속

| 페이지 | URL |
|--------|-----|
| 관리자 대시보드 | http://localhost:8080/admin |
| 도서 목록 | http://localhost:8080/admin/books |
| 도서 등록 | http://localhost:8080/admin/books/new |
| 헬스체크 | http://localhost:8080/api/health |

## 주요 기능

### 관리자 (구현 완료)
- 도서 등록 / 수정 / 삭제
- 도서 목록 페이징 조회
- 대시보드 (전체 도서 수, 카테고리 목록)
- 애플리케이션 시작 시 샘플 도서 20건 자동 로딩

### 검색 (구현 예정)
- OpenSearch 기반 한국어 형태소 분석 (Nori)
- 키워드 검색, 카테고리 필터, 가격 정렬
- 검색어 하이라이팅, 자동완성
- 패싯 검색 (Aggregation)
