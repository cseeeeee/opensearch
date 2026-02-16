package com.example.booksearch.config;

import org.opensearch.data.client.osc.OpenSearchConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * OpenSearch 연결 설정
 *
 * OpenSearchConfiguration 상속을 통한 클라이언트 자동 구성.
 * 아래 빈이 자동 등록됨:
 * <ul>
 *   <li>OpenSearchClient - OpenSearch 서버 통신용 클라이언트</li>
 *   <li>ElasticsearchOperations - 인덱스/문서 CRUD 작업용</li>
 *   <li>Repository 프록시 구현체 - BookSearchRepository 자동 구현용</li>
 * </ul>
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.example.booksearch.repository")
public class OpenSearchConfig extends OpenSearchConfiguration {

    @Value("${opensearch.uris}")
    private String opensearchUri;

    /**
     * OpenSearch 클라이언트 연결 정보
     *
     * @return 호스트, 포트 등 연결 설정 객체
     */
    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(opensearchUri.replace("http://", ""))
                .build();
    }
}
