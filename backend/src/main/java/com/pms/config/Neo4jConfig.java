package com.pms.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Neo4j 图数据库配置，支撑千人并发（连接池 80，与方案一致）。
 */
@Configuration
public class Neo4jConfig {

    @Value("${neo4j.uri:bolt://localhost:7687}")
    private String uri;

    @Value("${neo4j.username:neo4j}")
    private String username;

    @Value("${neo4j.password:neo4j123}")
    private String password;

    @Value("${neo4j.pool-size:80}")
    private int poolSize;

    @Bean
    public Driver neo4jDriver() {
        Config config = Config.builder()
            .withMaxConnectionPoolSize(poolSize)
            .build();
        return GraphDatabase.driver(uri, AuthTokens.basic(username, password), config);
    }
}
