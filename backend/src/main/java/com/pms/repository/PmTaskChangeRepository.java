package com.pms.repository;

import com.pms.entity.PmTaskChange;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class PmTaskChangeRepository {

    private static final String LABEL = "TaskChange";

    private final Neo4jTemplate template;
    private final IdGenerator idGenerator;

    public PmTaskChangeRepository(Neo4jTemplate template, IdGenerator idGenerator) {
        this.template = template;
        this.idGenerator = idGenerator;
    }

    public List<PmTaskChange> selectByTaskId(Long taskId) {
        String cypher = "MATCH (n:" + LABEL + " {taskId: $taskId}) RETURN n ORDER BY n.id DESC";
        return template.run(cypher, Map.of("taskId", taskId), this::toEntity);
    }

    public void insert(PmTaskChange change) {
        long id = idGenerator.nextId(LABEL);
        change.setId(id);
        String cypher = "CREATE (n:" + LABEL + " {id: $id, taskId: $taskId, operatorId: $operatorId, operatorName: $operatorName, action: $action, content: $content})";
        template.runWrite(cypher, Map.of(
            "id", id,
            "taskId", change.getTaskId() != null ? change.getTaskId() : 0L,
            "operatorId", change.getOperatorId() != null ? change.getOperatorId() : 0L,
            "operatorName", nullable(change.getOperatorName()),
            "action", nullable(change.getAction()),
            "content", nullable(change.getContent())
        ));
    }

    private PmTaskChange toEntity(Record r) {
        if (!r.containsKey("n")) return null;
        Node n = r.get("n").asNode();
        PmTaskChange e = new PmTaskChange();
        e.setId(NodeMapper.getLong(n, "id"));
        e.setTaskId(NodeMapper.getLong(n, "taskId"));
        e.setOperatorId(NodeMapper.getLong(n, "operatorId"));
        e.setOperatorName(NodeMapper.getString(n, "operatorName"));
        e.setAction(NodeMapper.getString(n, "action"));
        e.setContent(NodeMapper.getString(n, "content"));
        e.setCreatedAt(NodeMapper.getDateTime(n, "createdAt"));
        return e;
    }

    private static String nullable(String s) { return s == null ? "" : s; }
}
