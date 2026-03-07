package com.pms.repository;

import com.pms.entity.PmDept;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class PmDeptRepository {

    private static final String LABEL = "Dept";
    /** 系统部门编码（系统公司下，不可删除） */
    public static final String SYS_DEPT_CODE = "SYS_DEPT";

    private final Neo4jTemplate template;
    private final IdGenerator idGenerator;

    public PmDeptRepository(Neo4jTemplate template, IdGenerator idGenerator) {
        this.template = template;
        this.idGenerator = idGenerator;
    }

    public List<PmDept> selectByCompanyId(Long companyId) {
        String cypher = "MATCH (c:Company {id: $companyId})<-[:BELONGS_TO_COMPANY]-(n:" + LABEL + ") RETURN n ORDER BY n.sortOrder ASC, n.id ASC";
        List<PmDept> list = template.run(cypher, Map.of("companyId", companyId), this::toEntity);
        for (PmDept d : list) d.setCompanyId(companyId);
        return list;
    }

    public PmDept selectByCompanyIdAndCode(Long companyId, String deptCode) {
        if (companyId == null || deptCode == null || deptCode.isEmpty()) return null;
        String cypher = "MATCH (c:Company {id: $companyId})<-[:BELONGS_TO_COMPANY]-(n:" + LABEL + " {deptCode: $deptCode}) RETURN n";
        List<PmDept> list = template.run(cypher, Map.of("companyId", companyId, "deptCode", deptCode), this::toEntity);
        if (list.isEmpty()) return null;
        list.get(0).setCompanyId(companyId);
        return list.get(0);
    }

    public PmDept selectById(Long id) {
        String cypher = "MATCH (n:" + LABEL + " {id: $id})-[:BELONGS_TO_COMPANY]->(c:Company) RETURN n, c.id AS companyId";
        return template.runSingle(cypher, Map.of("id", id), this::toEntityWithCompany);
    }

    public void insert(PmDept dept) {
        long id = idGenerator.nextId(LABEL);
        dept.setId(id);
        boolean sys = Boolean.TRUE.equals(dept.getIsSystem());
        String cypher = "MATCH (c:Company {id: $companyId}) CREATE (n:" + LABEL + " {id: $id, deptCode: $deptCode, deptName: $deptName, sortOrder: $sortOrder, isSystem: $isSystem})-[:BELONGS_TO_COMPANY]->(c)";
        template.runWrite(cypher, Map.of(
            "companyId", dept.getCompanyId(),
            "id", id,
            "deptCode", nullable(dept.getDeptCode()),
            "deptName", nullable(dept.getDeptName()),
            "sortOrder", dept.getSortOrder() != null ? dept.getSortOrder() : 0,
            "isSystem", sys
        ));
    }

    public void updateById(PmDept dept) {
        boolean sys = Boolean.TRUE.equals(dept.getIsSystem());
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) SET n.deptCode = $deptCode, n.deptName = $deptName, n.sortOrder = $sortOrder, n.isSystem = $isSystem";
        template.runWrite(cypher, Map.of(
            "id", dept.getId(),
            "deptCode", nullable(dept.getDeptCode()),
            "deptName", nullable(dept.getDeptName()),
            "sortOrder", dept.getSortOrder() != null ? dept.getSortOrder() : 0,
            "isSystem", sys
        ));
        if (dept.getCompanyId() != null) {
            template.runWrite("MATCH (n:" + LABEL + " {id: $id})-[r:BELONGS_TO_COMPANY]->() DELETE r", Map.of("id", dept.getId()));
            template.runWrite("MATCH (n:" + LABEL + " {id: $id}), (c:Company {id: $companyId}) CREATE (n)-[:BELONGS_TO_COMPANY]->(c)", Map.of("id", dept.getId(), "companyId", dept.getCompanyId()));
        }
    }

    public void deleteById(Long id) {
        template.runWrite("MATCH (n:" + LABEL + " {id: $id}) DETACH DELETE n", Map.of("id", id));
    }

    private PmDept toEntity(Record r) {
        if (!r.containsKey("n")) return null;
        Node n = r.get("n").asNode();
        PmDept e = new PmDept();
        e.setId(NodeMapper.getLong(n, "id"));
        e.setDeptCode(NodeMapper.getString(n, "deptCode"));
        e.setDeptName(NodeMapper.getString(n, "deptName"));
        e.setSortOrder(NodeMapper.getInt(n, "sortOrder"));
        e.setIsSystem(n.containsKey("isSystem") ? n.get("isSystem").asBoolean() : SYS_DEPT_CODE.equals(e.getDeptCode()));
        e.setCreatedAt(NodeMapper.getDateTime(n, "createdAt"));
        e.setUpdatedAt(NodeMapper.getDateTime(n, "updatedAt"));
        return e;
    }

    private PmDept toEntityWithCompany(Record r) {
        PmDept e = toEntity(r);
        if (e != null && r.containsKey("companyId")) e.setCompanyId(r.get("companyId").asLong());
        return e;
    }

    private static String nullable(String s) { return s == null ? "" : s; }
}
