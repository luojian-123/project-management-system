package com.pms.repository;

import com.pms.entity.PmRisk;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PmRiskRepository {

    private static final String LABEL = "Risk";

    private final Neo4jTemplate template;
    private final IdGenerator idGenerator;

    public PmRiskRepository(Neo4jTemplate template, IdGenerator idGenerator) {
        this.template = template;
        this.idGenerator = idGenerator;
    }

    public List<PmRisk> selectPage(Long projectId, String status, int offset, int limit) {
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

    public PmRisk selectById(Long id) {
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) RETURN n";
        return template.runSingle(cypher, Map.of("id", id), this::toEntity);
    }

    public String selectMaxRiskCode() {
        String cypher = "MATCH (n:" + LABEL + ") WHERE n.riskCode IS NOT NULL AND n.riskCode <> '' RETURN n.riskCode AS code ORDER BY n.riskCode DESC LIMIT 1";
        return template.runSingle(cypher, null, r -> r.get("code").asString(null));
    }

    public void insert(PmRisk risk) {
        long id = idGenerator.nextId(LABEL);
        risk.setId(id);
        String cypher = "CREATE (n:" + LABEL + " {id: $id, projectId: $projectId, riskCode: $riskCode, title: $title, description: $description, probability: $probability, impact: $impact, riskLevel: $riskLevel, response: $response, ownerId: $ownerId, status: $status, createdBy: $createdBy})";
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("projectId", risk.getProjectId() != null ? risk.getProjectId() : 0L);
        params.put("riskCode", nullable(risk.getRiskCode()));
        params.put("title", nullable(risk.getTitle()));
        params.put("description", nullable(risk.getDescription()));
        params.put("probability", nullable(risk.getProbability()));
        params.put("impact", nullable(risk.getImpact()));
        params.put("riskLevel", nullable(risk.getRiskLevel()));
        params.put("response", nullable(risk.getResponse()));
        params.put("ownerId", risk.getOwnerId() != null ? risk.getOwnerId() : 0L);
        params.put("status", nullable(risk.getStatus()));
        params.put("createdBy", risk.getCreatedBy() != null ? risk.getCreatedBy() : 0L);
        template.runWrite(cypher, params);
    }

    public void updateById(PmRisk risk) {
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) SET n.projectId = $projectId, n.riskCode = $riskCode, n.title = $title, n.description = $description, n.probability = $probability, n.impact = $impact, n.riskLevel = $riskLevel, n.response = $response, n.ownerId = $ownerId, n.status = $status";
        Map<String, Object> params = new HashMap<>();
        params.put("id", risk.getId());
        params.put("projectId", risk.getProjectId() != null ? risk.getProjectId() : 0L);
        params.put("riskCode", nullable(risk.getRiskCode()));
        params.put("title", nullable(risk.getTitle()));
        params.put("description", nullable(risk.getDescription()));
        params.put("probability", nullable(risk.getProbability()));
        params.put("impact", nullable(risk.getImpact()));
        params.put("riskLevel", nullable(risk.getRiskLevel()));
        params.put("response", nullable(risk.getResponse()));
        params.put("ownerId", risk.getOwnerId() != null ? risk.getOwnerId() : 0L);
        params.put("status", nullable(risk.getStatus()));
        template.runWrite(cypher, params);
    }

    public void deleteById(Long id) {
        template.runWrite("MATCH (n:" + LABEL + " {id: $id}) DETACH DELETE n", Map.of("id", id));
    }

    private PmRisk toEntity(Record r) {
        if (!r.containsKey("n")) return null;
        Node n = r.get("n").asNode();
        PmRisk e = new PmRisk();
        e.setId(NodeMapper.getLong(n, "id"));
        e.setProjectId(NodeMapper.getLong(n, "projectId"));
        e.setRiskCode(NodeMapper.getString(n, "riskCode"));
        e.setTitle(NodeMapper.getString(n, "title"));
        e.setDescription(NodeMapper.getString(n, "description"));
        e.setProbability(NodeMapper.getString(n, "probability"));
        e.setImpact(NodeMapper.getString(n, "impact"));
        e.setRiskLevel(NodeMapper.getString(n, "riskLevel"));
        e.setResponse(NodeMapper.getString(n, "response"));
        e.setOwnerId(NodeMapper.getLong(n, "ownerId"));
        e.setStatus(NodeMapper.getString(n, "status"));
        e.setCreatedBy(NodeMapper.getLong(n, "createdBy"));
        e.setCreatedAt(NodeMapper.getDateTime(n, "createdAt"));
        e.setUpdatedAt(NodeMapper.getDateTime(n, "updatedAt"));
        return e;
    }

    private static String nullable(String s) { return s == null ? "" : s; }
}
