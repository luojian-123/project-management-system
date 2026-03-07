package com.pms.repository;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 执行 Cypher 的模板类，读写均通过 Driver 会话。
 */
@Component
public class Neo4jTemplate {

    private final Driver driver;

    public Neo4jTemplate(Driver driver) {
        this.driver = driver;
    }

    public <T> List<T> run(String cypher, Map<String, Object> params, Function<Record, T> mapper) {
        try (Session session = driver.session(SessionConfig.builder().withDefaultAccessMode(org.neo4j.driver.AccessMode.READ).build())) {
            Result result = params == null || params.isEmpty()
                ? session.run(cypher)
                : session.run(cypher, params);
            List<T> list = new ArrayList<>();
            while (result.hasNext()) {
                Record r = result.next();
                T t = mapper.apply(r);
                if (t != null) list.add(t);
            }
            return list;
        }
    }

    public <T> T runSingle(String cypher, Map<String, Object> params, Function<Record, T> mapper) {
        List<T> list = run(cypher, params, mapper);
        return list.isEmpty() ? null : list.get(0);
    }

    public void runWrite(String cypher, Map<String, Object> params) {
        try (Session session = driver.session(SessionConfig.builder().withDefaultAccessMode(org.neo4j.driver.AccessMode.WRITE).build())) {
            if (params == null || params.isEmpty())
                session.run(cypher);
            else
                session.run(cypher, params);
        }
    }

    public <T> T runWriteAndReturn(String cypher, Map<String, Object> params, Function<Record, T> mapper) {
        try (Session session = driver.session(SessionConfig.builder().withDefaultAccessMode(org.neo4j.driver.AccessMode.WRITE).build())) {
            Result result = params == null || params.isEmpty()
                ? session.run(cypher)
                : session.run(cypher, params);
            if (!result.hasNext()) return null;
            return mapper.apply(result.next());
        }
    }

    /** 在写事务中执行多句 Cypher（同一事务） */
    public void runWriteTx(List<String> cyphers, List<Map<String, Object>> paramsList) {
        if (cyphers == null || cyphers.isEmpty()) return;
        try (Session session = driver.session(SessionConfig.builder().withDefaultAccessMode(org.neo4j.driver.AccessMode.WRITE).build())) {
            session.executeWrite(tx -> {
                for (int i = 0; i < cyphers.size(); i++) {
                    Map<String, Object> params = (paramsList != null && i < paramsList.size()) ? paramsList.get(i) : null;
                    if (params == null || params.isEmpty())
                        tx.run(cyphers.get(i));
                    else
                        tx.run(cyphers.get(i), params);
                }
                return null;
            });
        }
    }
}
