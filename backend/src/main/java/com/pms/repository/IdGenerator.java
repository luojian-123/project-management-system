package com.pms.repository;

import org.neo4j.driver.Record;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 图库节点业务 ID 序列（按标签递增）。
 */
@Component
public class IdGenerator {

    private final Neo4jTemplate template;

    public IdGenerator(Neo4jTemplate template) {
        this.template = template;
    }

    /**
     * 获取并递增指定标签的下一个 ID（在写事务中执行，保证唯一）。
     */
    public long nextId(String label) {
        String cypher = "MERGE (s:Sequence {name: $name}) ON CREATE SET s.nextId = 1 ON MATCH SET s.nextId = s.nextId + 1 WITH s RETURN s.nextId AS id";
        Long id = template.runWriteAndReturn(cypher, Map.of("name", "seq_" + label), r -> r.get("id").asLong());
        return id != null ? id : 1L;
    }
}
