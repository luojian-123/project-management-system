package com.pms.repository;

import com.pms.entity.PmTodo;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class PmTodoRepository {

    private static final String LABEL = "Todo";

    private final Neo4jTemplate template;
    private final IdGenerator idGenerator;

    public PmTodoRepository(Neo4jTemplate template, IdGenerator idGenerator) {
        this.template = template;
        this.idGenerator = idGenerator;
    }

    public List<PmTodo> selectPage(Long userId, int offset, int limit) {
        String cypher = "MATCH (n:" + LABEL + ") WHERE ($uid = -1 OR n.userId = $uid) RETURN n ORDER BY n.id DESC SKIP $offset LIMIT $limit";
        return template.run(cypher, Map.of("uid", userId != null ? userId : -1L, "offset", (long) offset, "limit", (long) limit), this::toEntity);
    }

    public long countPage(Long userId) {
        String cypher = "MATCH (n:" + LABEL + ") WHERE ($uid = -1 OR n.userId = $uid) RETURN count(n) AS c";
        Long c = template.runSingle(cypher, Map.of("uid", userId != null ? userId : -1L), r -> r.get("c").asLong());
        return c != null ? c : 0L;
    }

    public PmTodo selectById(Long id) {
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) RETURN n";
        return template.runSingle(cypher, Map.of("id", id), this::toEntity);
    }

    public void insert(PmTodo todo) {
        long id = idGenerator.nextId(LABEL);
        todo.setId(id);
        String cypher = "CREATE (n:" + LABEL + " {id: $id, userId: $userId, title: $title, bizType: $bizType, bizId: $bizId, priority: $priority, dueDate: $dueDate, status: $status})";
        template.runWrite(cypher, Map.of(
            "id", id,
            "userId", todo.getUserId() != null ? todo.getUserId() : 0L,
            "title", nullable(todo.getTitle()),
            "bizType", nullable(todo.getBizType()),
            "bizId", todo.getBizId() != null ? todo.getBizId() : 0L,
            "priority", nullable(todo.getPriority()),
            "dueDate", NodeMapper.toDateStr(todo.getDueDate()),
            "status", nullable(todo.getStatus())
        ));
    }

    public void updateById(PmTodo todo) {
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) SET n.userId = $userId, n.title = $title, n.bizType = $bizType, n.bizId = $bizId, n.priority = $priority, n.dueDate = $dueDate, n.status = $status";
        template.runWrite(cypher, Map.of(
            "id", todo.getId(),
            "userId", todo.getUserId() != null ? todo.getUserId() : 0L,
            "title", nullable(todo.getTitle()),
            "bizType", nullable(todo.getBizType()),
            "bizId", todo.getBizId() != null ? todo.getBizId() : 0L,
            "priority", nullable(todo.getPriority()),
            "dueDate", NodeMapper.toDateStr(todo.getDueDate()),
            "status", nullable(todo.getStatus())
        ));
    }

    public void deleteById(Long id) {
        template.runWrite("MATCH (n:" + LABEL + " {id: $id}) DETACH DELETE n", Map.of("id", id));
    }

    private PmTodo toEntity(Record r) {
        if (!r.containsKey("n")) return null;
        Node n = r.get("n").asNode();
        PmTodo e = new PmTodo();
        e.setId(NodeMapper.getLong(n, "id"));
        e.setUserId(NodeMapper.getLong(n, "userId"));
        e.setTitle(NodeMapper.getString(n, "title"));
        e.setBizType(NodeMapper.getString(n, "bizType"));
        e.setBizId(NodeMapper.getLong(n, "bizId"));
        e.setPriority(NodeMapper.getString(n, "priority"));
        e.setDueDate(NodeMapper.getDate(n, "dueDate"));
        e.setStatus(NodeMapper.getString(n, "status"));
        e.setCreatedAt(NodeMapper.getDateTime(n, "createdAt"));
        e.setUpdatedAt(NodeMapper.getDateTime(n, "updatedAt"));
        return e;
    }

    private static String nullable(String s) { return s == null ? "" : s; }
}
