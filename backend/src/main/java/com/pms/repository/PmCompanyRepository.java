package com.pms.repository;

import com.pms.entity.PmCompany;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class PmCompanyRepository {

    private static final String LABEL = "Company";

    private final Neo4jTemplate template;
    private final IdGenerator idGenerator;

    public PmCompanyRepository(Neo4jTemplate template, IdGenerator idGenerator) {
        this.template = template;
        this.idGenerator = idGenerator;
    }

    /** 系统公司编码，默认顶层不可删 */
    public static final String SYS_COMPANY_CODE = "SYS";

    public List<PmCompany> selectList() {
        String cypher = "MATCH (n:" + LABEL + ") RETURN n ORDER BY n.sortOrder ASC, n.id ASC";
        return template.run(cypher, null, this::toEntity);
    }

    public PmCompany selectByCode(String companyCode) {
        if (companyCode == null || companyCode.isEmpty()) return null;
        String cypher = "MATCH (n:" + LABEL + " {companyCode: $code}) RETURN n";
        return template.runSingle(cypher, Map.of("code", companyCode), this::toEntity);
    }

    public PmCompany selectById(Long id) {
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) RETURN n";
        return template.runSingle(cypher, Map.of("id", id), this::toEntity);
    }

    public void insert(PmCompany company) {
        long id = idGenerator.nextId(LABEL);
        company.setId(id);
        boolean sys = Boolean.TRUE.equals(company.getIsSystem());
        String cypher = "CREATE (n:" + LABEL + " {id: $id, companyCode: $companyCode, companyName: $companyName, sortOrder: $sortOrder, isSystem: $isSystem})";
        template.runWrite(cypher, Map.of(
            "id", id,
            "companyCode", nullable(company.getCompanyCode()),
            "companyName", nullable(company.getCompanyName()),
            "sortOrder", company.getSortOrder() != null ? company.getSortOrder() : 0,
            "isSystem", sys
        ));
    }

    public void updateById(PmCompany company) {
        boolean sys = Boolean.TRUE.equals(company.getIsSystem());
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) SET n.companyCode = $companyCode, n.companyName = $companyName, n.sortOrder = $sortOrder, n.isSystem = $isSystem";
        template.runWrite(cypher, Map.of(
            "id", company.getId(),
            "companyCode", nullable(company.getCompanyCode()),
            "companyName", nullable(company.getCompanyName()),
            "sortOrder", company.getSortOrder() != null ? company.getSortOrder() : 0,
            "isSystem", sys
        ));
    }

    public void deleteById(Long id) {
        template.runWrite("MATCH (n:" + LABEL + " {id: $id}) DETACH DELETE n", Map.of("id", id));
    }

    private PmCompany toEntity(Record r) {
        if (!r.containsKey("n")) return null;
        Node n = r.get("n").asNode();
        PmCompany e = new PmCompany();
        e.setId(NodeMapper.getLong(n, "id"));
        e.setCompanyCode(NodeMapper.getString(n, "companyCode"));
        e.setCompanyName(NodeMapper.getString(n, "companyName"));
        e.setSortOrder(NodeMapper.getInt(n, "sortOrder"));
        e.setIsSystem(n.containsKey("isSystem") ? n.get("isSystem").asBoolean() : SYS_COMPANY_CODE.equals(e.getCompanyCode()));
        e.setCreatedAt(NodeMapper.getDateTime(n, "createdAt"));
        e.setUpdatedAt(NodeMapper.getDateTime(n, "updatedAt"));
        return e;
    }

    private static String nullable(String s) { return s == null ? "" : s; }
}
