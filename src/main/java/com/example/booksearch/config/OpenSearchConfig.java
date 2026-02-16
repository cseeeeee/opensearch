package com.example.booksearch.config;

import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.opensearch.data.client.osc.OpenSearchConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.opensearch.repository.config.EnableOpenSearchRepositories;

@Configuration
@EnableOpenSearchRepositories(basePackages = "com.example.booksearch.repository")
public class OpenSearchConfig extends OpenSearchConfiguration {

    @Value("${opensearch.uris}")
    private String opensearchUri;

    @Override
    public String[] opensearchClientHosts() {
        return new String[]{opensearchUri};
    }

    @Bean
    public RestClient opensearchRestClient() {
        return RestClient.builder(HttpHost.create(opensearchUri)).build();
    }

    @Bean
    public OpenSearchClient openSearchClient(RestClient restClient) {
        RestClientTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );
        return new OpenSearchClient(transport);
    }
}
