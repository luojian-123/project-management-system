package com.pms.repository;

import com.pms.entity.PmIssue;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PmIssueRepository {

    private static final String LABEL = "Issue";

    private final Neo4jTemplate template;
    private final IdGenerator idGenerator;

    public PmIssueRepository(Neo4jTemplate template, IdGenerator idGenerator) {
        this.template = template;
        this.idGenerator = idGenerator;
    }

    public List<PmIssue> selectPage(Long projectId, String status, int offset, int limit) {
        String cypher = "MATCH (n:" + LABEL + ") WHERE ($pid = -1 OR n.projectId = $pid) AND ($status = '' OR n.status = $status) RETURN n ORDER BY n.id DESC SKIP $offset LIMIT $limit";
        Map<String, Object> params = new java.util.HashMap<>(Map.of("status", status != null ? status : "", "offset", (long) offset, "limit", (long) limit));
        params.put("pid", projectId != null ? projectId : -1L);
        return template.run(cypher, params, this::toEntity);
    }

    public long countPage(Long projectId, String status) {
        String cypher = "MATCH (n:" + LABEL + ") WHERE ($pid = -1 OR n.projectId = $pid) AND ($status = '' OR n.status = $status) RETURN count(n) AS c";
        Map<String, Object> params = Map.of("pid", projectId != null ? projectId : -1L, "status", status != null ? status : "");
        Long c = template.runSingle(cypher, params, r -> r.get("c").asLong());
        return c != null ? c : 0L;
    }

    public PmIssue selectById(Long id) {
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) RETURN n";
        return template.runSingle(cypher, Map.of("id", id), this::toEntity);
    }

    public String selectMaxIssueCode() {
        String cypher = "MATCH (n:" + LABEL + ") WHERE n.issueCode IS NOT NULL AND n.issueCode <> '' RETURN n.issueCode AS code ORDER BY n.issueCode DESC LIMIT 1";
        return template.runSingle(cypher, null, r -> r.get("code").asString(null));
    }

    public void insert(PmIssue issue) {
        long id = idGenerator.nextId(LABEL);
        issue.setId(id);
        String cypher = "CREATE (n:" + LABEL + " {id: $id, projectId: $projectId, taskId: $taskId, issueCode: $issueCode, title: $title, description: $description, severity: $severity, status: $status, assigneeId: $assigneeId, foundDate: $foundDate, resolvedDate: $resolvedDate, createdBy: $createdBy})";
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("projectId", issue.getProjectId() != null ? issue.getProjectId() : 0L);
        params.put("taskId", issue.getTaskId() != null ? issue.getTaskId() : 0L);
        params.put("issueCode", nullable(issue.getIssueCode()));
        params.put("title", nullable(issue.getTitle()));
        params.put("description", nullable(issue.getDescription()));
        params.put("severity", nullable(issue.getSeverity()));
        params.put("status", nullable(issue.getStatus()));
        params.put("assigneeId", issue.getAssigneeId() != null ? issue.getAssigneeId() : 0L);
        params.put("foundDate", NodeMapper.toDateStr(issue.getFoundDate()));
        params.put("resolvedDate", NodeMapper.toDateStr(issue.getResolvedDate()));
        params.put("createdBy", issue.getCreatedBy() != null ? issue.getCreatedBy() : 0L);
        template.runWrite(cypher, params);
    }

    public void updateById(PmIssue issue) {
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) SET n.projectId = $projectId, n.taskId = $taskId, n.issueCode = $issueCode, n.title = $title, n.description = $description, n.severity = $severity, n.status = $status, n.assigneeId = $assigneeId, n.foundDate = $foundDate, n.resolvedDate = $resolvedDate";
        Map<String, Object> params = new HashMap<>();
        params.put("id", issue.getId());
        params.put("projectId", issue.getProjectId() != null ? issue.getProjectId() : 0L);
        params.put("taskId", issue.getTaskId() != null ? issue.getTaskId() : 0L);
        params.put("issueCode", nullable(issue.getIssueCode()));
        params.put("title", nullable(issue.getTitle()));
        params.put("description", nullable(issue.getDescription()));
        params.put("severity", nullable(issue.getSeverity()));
        params.put("status", nullable(issue.getStatus()));
        params.put("assigneeId", issue.getAssigneeId() != null ? issue.getAssigneeId() : 0L);
        params.put("foundDate", NodeMapper.toDateStr(issue.getFoundDate()));
        params.put("resolvedDate", NodeMapper.toDateStr(issue.getResolvedDate()));
        template.runWrite(cypher, params);
    }

    public void deleteById(Long id) {
        template.runWrite("MATCH (n:" + LABEL + " {id: $id}) DETACH DELETE n", Map.of("id", id));
    }

    private PmIssue toEntity(Record r) {
        if (!r.containsKey("n")) return null;
        Node n = r.get("n").asNode();
        PmIssue e = new PmIssue();
        e.setId(NodeMapper.getLong(n, "id"));
        e.setProjectId(NodeMapper.getLong(n, "projectId"));
        e.setTaskId(NodeMapper.getLong(n, "taskId"));
        e.setIssueCode(NodeMapper.getString(n, "issueCode"));
        e.setTitle(NodeMapper.getString(n, "title"));
        e.setDescription(NodeMapper.getString(n, "description"));
        e.setSeverity(NodeMapper.getString(n, "severity"));
        e.setStatus(NodeMapper.getString(n, "status"));
        e.setAssigneeId(NodeMapper.getLong(n, "assigneeId"));
        e.setFoundDate(NodeMapper.getDate(n, "foundDate"));
        e.setResolvedDate(NodeMapper.getDate(n, "resolvedDate"));
        e.setCreatedBy(NodeMapper.getLong(n, "createdBy"));
        e.setCreatedAt(NodeMapper.getDateTime(n, "createdAt"));
        e.setUpdatedAt(NodeMapper.getDateTime(n, "updatedAt"));
        return e;
    }

    private static String nullable(String s) { return s == null ? "" : s; }
}
