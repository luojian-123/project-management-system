package com.pms.repository;

import com.pms.entity.PmCost;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class PmCostRepository {

    private static final String LABEL = "Cost";

    private final Neo4jTemplate template;
    private final IdGenerator idGenerator;

    public PmCostRepository(Neo4jTemplate template, IdGenerator idGenerator) {
        this.template = template;
        this.idGenerator = idGenerator;
    }

    public List<PmCost> selectPage(Long projectId, int offset, int limit) {
        String cypher = "MATCH (n:" + LABEL + ") WHERE ($pid = -1 OR n.projectId = $pid) RETURN n ORDER BY n.id DESC SKIP $offset LIMIT $limit";
        return template.run(cypher, Map.of("pid", projectId != null ? projectId : -1L, "offset", (long) offset, "limit", (long) limit), this::toEntity);
    }

    public long countPage(Long projectId) {
        String cypher = "MATCH (n:" + LABEL + ") WHERE ($pid = -1 OR n.projectId = $pid) RETURN count(n) AS c";
        Long c = template.runSingle(cypher, Map.of("pid", projectId != null ? projectId : -1L), r -> r.get("c").asLong());
        return c != null ? c : 0L;
    }

    public PmCost selectById(Long id) {
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) RETURN n";
        return template.runSingle(cypher, Map.of("id", id), this::toEntity);
    }

    public void insert(PmCost cost) {
        long id = idGenerator.nextId(LABEL);
        cost.setId(id);
        String cypher = "CREATE (n:" + LABEL + " {id: $id, projectId: $projectId, taskId: $taskId, costType: $costType, budgetAmount: $budgetAmount, actualAmount: $actualAmount, occurDate: $occurDate, remark: $remark, createdBy: $createdBy})";
        template.runWrite(cypher, Map.of(
            "id", id,
            "projectId", cost.getProjectId() != null ? cost.getProjectId() : 0L,
            "taskId", cost.getTaskId() != null ? cost.getTaskId() : 0L,
            "costType", nullable(cost.getCostType()),
            "budgetAmount", cost.getBudgetAmount() != null ? cost.getBudgetAmount().toPlainString() : "0",
            "actualAmount", cost.getActualAmount() != null ? cost.getActualAmount().toPlainString() : "0",
            "occurDate", NodeMapper.toDateStr(cost.getOccurDate()),
            "remark", nullable(cost.getRemark()),
            "createdBy", cost.getCreatedBy() != null ? cost.getCreatedBy() : 0L
        ));
    }

    public void updateById(PmCost cost) {
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) SET n.projectId = $projectId, n.taskId = $taskId, n.costType = $costType, n.budgetAmount = $budgetAmount, n.actualAmount = $actualAmount, n.occurDate = $occurDate, n.remark = $remark";
        template.runWrite(cypher, Map.of(
            "id", cost.getId(),
            "projectId", cost.getProjectId() != null ? cost.getProjectId() : 0L,
            "taskId", cost.getTaskId() != null ? cost.getTaskId() : 0L,
            "costType", nullable(cost.getCostType()),
            "budgetAmount", cost.getBudgetAmount() != null ? cost.getBudgetAmount().toPlainString() : "0",
            "actualAmount", cost.getActualAmount() != null ? cost.getActualAmount().toPlainString() : "0",
            "occurDate", NodeMapper.toDateStr(cost.getOccurDate()),
            "remark", nullable(cost.getRemark())
        ));
    }

    public void deleteById(Long id) {
        template.runWrite("MATCH (n:" + LABEL + " {id: $id}) DETACH DELETE n", Map.of("id", id));
    }

    private PmCost toEntity(Record r) {
        if (!r.containsKey("n")) return null;
        Node n = r.get("n").asNode();
        PmCost e = new PmCost();
        e.setId(NodeMapper.getLong(n, "id"));
        e.setProjectId(NodeMapper.getLong(n, "projectId"));
        e.setTaskId(NodeMapper.getLong(n, "taskId"));
        e.setCostType(NodeMapper.getString(n, "costType"));
        e.setBudgetAmount(NodeMapper.getBigDecimal(n, "budgetAmount"));
        e.setActualAmount(NodeMapper.getBigDecimal(n, "actualAmount"));
        e.setOccurDate(NodeMapper.getDate(n, "occurDate"));
        e.setRemark(NodeMapper.getString(n, "remark"));
        e.setCreatedBy(NodeMapper.getLong(n, "createdBy"));
        e.setCreatedAt(NodeMapper.getDateTime(n, "createdAt"));
        e.setUpdatedAt(NodeMapper.getDateTime(n, "updatedAt"));
        return e;
    }

    private static String nullable(String s) { return s == null ? "" : s; }
}
