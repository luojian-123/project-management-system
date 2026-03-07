package com.pms.repository;

import com.pms.entity.SysRole;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class SysRoleRepository {

    private static final String LABEL = "Role";

    private final Neo4jTemplate template;
    private final IdGenerator idGenerator;
    private final SysRoleDeptRepository roleDeptRepository;

    public SysRoleRepository(Neo4jTemplate template, IdGenerator idGenerator, SysRoleDeptRepository roleDeptRepository) {
        this.template = template;
        this.idGenerator = idGenerator;
        this.roleDeptRepository = roleDeptRepository;
    }

    public List<SysRole> selectList() {
        String cypher = "MATCH (n:" + LABEL + ") RETURN n ORDER BY n.sortOrder ASC, n.id ASC";
        List<SysRole> list = template.run(cypher, null, this::toEntity);
        for (SysRole r : list) if (r.getId() != null) r.setDeptIds(roleDeptRepository.selectDeptIdsByRoleId(r.getId()));
        return list;
    }

    public List<SysRole> selectByDeptId(Long deptId) {
        String cypher = "MATCH (d:Dept {id: $deptId})<-[:BELONGS_TO_DEPT]-(n:" + LABEL + ") RETURN n ORDER BY n.sortOrder ASC, n.id ASC";
        List<SysRole> list = template.run(cypher, Map.of("deptId", deptId), this::toEntity);
        for (SysRole r : list) if (r.getId() != null) r.setDeptIds(roleDeptRepository.selectDeptIdsByRoleId(r.getId()));
        return list;
    }

    public SysRole selectByCode(String code) {
        if (code == null || code.isEmpty()) return null;
        String cypher = "MATCH (n:" + LABEL + " {code: $code}) RETURN n";
        SysRole r = template.runSingle(cypher, Map.of("code", code), this::toEntity);
        if (r != null && r.getId() != null) r.setDeptIds(roleDeptRepository.selectDeptIdsByRoleId(r.getId()));
        return r;
    }

    public SysRole selectById(Long id) {
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) RETURN n";
        SysRole r = template.runSingle(cypher, Map.of("id", id), this::toEntity);
        if (r != null && id != null) r.setDeptIds(roleDeptRepository.selectDeptIdsByRoleId(id));
        return r;
    }

    public List<String> selectCodesByUserId(Long userId) {
        String cypher = "MATCH (u:User {id: $userId})-[:HAS_ROLE]->(r:" + LABEL + ") RETURN r.code AS code";
        return template.run(cypher, Map.of("userId", userId), rec -> rec.get("code").asString(null));
    }

    public void insert(SysRole role) {
        long id = idGenerator.nextId(LABEL);
        role.setId(id);
        String cypher = "CREATE (n:" + LABEL + " {id: $id, code: $code, name: $name, status: $status, sortOrder: $sortOrder})";
        template.runWrite(cypher, Map.of(
            "id", id,
            "code", nullable(role.getCode()),
            "name", nullable(role.getName()),
            "status", role.getStatus() != null ? role.getStatus() : 1,
            "sortOrder", role.getSortOrder() != null ? role.getSortOrder() : 0
        ));
    }

    public void updateById(SysRole role) {
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) SET n.code = $code, n.name = $name, n.status = $status, n.sortOrder = $sortOrder";
        template.runWrite(cypher, Map.of(
            "id", role.getId(),
            "code", nullable(role.getCode()),
            "name", nullable(role.getName()),
            "status", role.getStatus() != null ? role.getStatus() : 1,
            "sortOrder", role.getSortOrder() != null ? role.getSortOrder() : 0
        ));
    }

    public void deleteById(Long id) {
        template.runWrite("MATCH (n:" + LABEL + " {id: $id}) DETACH DELETE n", Map.of("id", id));
    }

    private SysRole toEntity(Record r) {
        if (!r.containsKey("n")) return null;
        Node n = r.get("n").asNode();
        SysRole e = new SysRole();
        e.setId(NodeMapper.getLong(n, "id"));
        e.setCode(NodeMapper.getString(n, "code"));
        e.setName(NodeMapper.getString(n, "name"));
        e.setStatus(NodeMapper.getInt(n, "status"));
        e.setSortOrder(NodeMapper.getInt(n, "sortOrder"));
        return e;
    }

    private static String nullable(String s) { return s == null ? "" : s; }
}
