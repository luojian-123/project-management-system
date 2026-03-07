package com.pms.repository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class SysRoleDeptRepository {

    private final Neo4jTemplate template;

    public SysRoleDeptRepository(Neo4jTemplate template) {
        this.template = template;
    }

    public List<Long> selectDeptIdsByRoleId(Long roleId) {
        String cypher = "MATCH (r:Role {id: $roleId})-[:BELONGS_TO_DEPT]->(d:Dept) RETURN d.id AS id ORDER BY d.id";
        return template.run(cypher, Map.of("roleId", roleId), r -> r.get("id").asLong());
    }

    public List<Long> selectRoleIdsByDeptId(Long deptId) {
        String cypher = "MATCH (r:Role)-[:BELONGS_TO_DEPT]->(d:Dept {id: $deptId}) RETURN r.id AS id";
        return template.run(cypher, Map.of("deptId", deptId), r -> r.get("id").asLong());
    }

    public void deleteByRoleId(Long roleId) {
        template.runWrite("MATCH (r:Role {id: $roleId})-[rel:BELONGS_TO_DEPT]->() DELETE rel", Map.of("roleId", roleId));
    }

    public void insert(Long roleId, Long deptId) {
        template.runWrite(
            "MATCH (r:Role {id: $roleId}), (d:Dept {id: $deptId}) CREATE (r)-[:BELONGS_TO_DEPT]->(d)",
            Map.of("roleId", roleId, "deptId", deptId)
        );
    }
}
