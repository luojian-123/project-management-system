package com.pms.repository;

import com.pms.entity.PmChange;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PmChangeRepository {

    private static final String LABEL = "Change";

    private final Neo4jTemplate template;
    private final IdGenerator idGenerator;

    public PmChangeRepository(Neo4jTemplate template, IdGenerator idGenerator) {
        this.template = template;
        this.idGenerator = idGenerator;
    }

    public List<PmChange> selectPage(Long projectId, String status, int offset, int limit) {
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

    public PmChange selectById(Long id) {
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) RETURN n";
        return template.runSingle(cypher, Map.of("id", id), this::toEntity);
    }

    public String selectMaxChangeNo() {
        String cypher = "MATCH (n:" + LABEL + ") WHERE n.changeNo IS NOT NULL AND n.changeNo <> '' RETURN n.changeNo AS no ORDER BY n.changeNo DESC LIMIT 1";
        return template.runSingle(cypher, null, r -> r.get("no").asString(null));
    }

    public void insert(PmChange change) {
        long id = idGenerator.nextId(LABEL);
        change.setId(id);
        String cypher = "CREATE (n:" + LABEL + " {id: $id, projectId: $projectId, changeNo: $changeNo, title: $title, description: $description, changeType: $changeType, impactScope: $impactScope, status: $status, applicantId: $applicantId, applyTime: $applyTime, flowId: $flowId})";
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("projectId", change.getProjectId() != null ? change.getProjectId() : 0L);
        params.put("changeNo", nullable(change.getChangeNo()));
        params.put("title", nullable(change.getTitle()));
        params.put("description", nullable(change.getDescription()));
        params.put("changeType", nullable(change.getChangeType()));
        params.put("impactScope", nullable(change.getImpactScope()));
        params.put("status", nullable(change.getStatus()));
        params.put("applicantId", change.getApplicantId() != null ? change.getApplicantId() : 0L);
        params.put("applyTime", NodeMapper.toDateTimeStr(change.getApplyTime()));
        params.put("flowId", change.getFlowId() != null ? change.getFlowId() : 0L);
        template.runWrite(cypher, params);
    }

    public void updateById(PmChange change) {
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) SET n.projectId = $projectId, n.changeNo = $changeNo, n.title = $title, n.description = $description, n.changeType = $changeType, n.impactScope = $impactScope, n.status = $status, n.applicantId = $applicantId, n.applyTime = $applyTime, n.flowId = $flowId";
        Map<String, Object> params = new HashMap<>();
        params.put("id", change.getId());
        params.put("projectId", change.getProjectId() != null ? change.getProjectId() : 0L);
        params.put("changeNo", nullable(change.getChangeNo()));
        params.put("title", nullable(change.getTitle()));
        params.put("description", nullable(change.getDescription()));
        params.put("changeType", nullable(change.getChangeType()));
        params.put("impactScope", nullable(change.getImpactScope()));
        params.put("status", nullable(change.getStatus()));
        params.put("applicantId", change.getApplicantId() != null ? change.getApplicantId() : 0L);
        params.put("applyTime", NodeMapper.toDateTimeStr(change.getApplyTime()));
        params.put("flowId", change.getFlowId() != null ? change.getFlowId() : 0L);
        template.runWrite(cypher, params);
    }

    public void deleteById(Long id) {
        template.runWrite("MATCH (n:" + LABEL + " {id: $id}) DETACH DELETE n", Map.of("id", id));
    }

    private PmChange toEntity(Record r) {
        if (!r.containsKey("n")) return null;
        Node n = r.get("n").asNode();
        PmChange e = new PmChange();
        e.setId(NodeMapper.getLong(n, "id"));
        e.setProjectId(NodeMapper.getLong(n, "projectId"));
        e.setChangeNo(NodeMapper.getString(n, "changeNo"));
        e.setTitle(NodeMapper.getString(n, "title"));
        e.setDescription(NodeMapper.getString(n, "description"));
        e.setChangeType(NodeMapper.getString(n, "changeType"));
        e.setImpactScope(NodeMapper.getString(n, "impactScope"));
        e.setStatus(NodeMapper.getString(n, "status"));
        e.setApplicantId(NodeMapper.getLong(n, "applicantId"));
        e.setApplyTime(NodeMapper.getDateTime(n, "applyTime"));
        e.setFlowId(NodeMapper.getLong(n, "flowId"));
        e.setCreatedAt(NodeMapper.getDateTime(n, "createdAt"));
        e.setUpdatedAt(NodeMapper.getDateTime(n, "updatedAt"));
        return e;
    }

    private static String nullable(String s) { return s == null ? "" : s; }
}
