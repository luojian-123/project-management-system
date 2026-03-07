package com.pms.repository;

import org.neo4j.driver.Record;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class SysUserRoleRepository {

    private final Neo4jTemplate template;

    public SysUserRoleRepository(Neo4jTemplate template) {
        this.template = template;
    }

    public List<Long> selectRoleIdsByUserId(Long userId) {
        String cypher = "MATCH (u:User {id: $userId})-[:HAS_ROLE]->(r:Role) RETURN r.id AS id";
        return template.run(cypher, Map.of("userId", userId), r -> r.get("id").asLong());
    }

    public List<Long> selectUserIdsByRoleId(Long roleId) {
        String cypher = "MATCH (u:User)-[:HAS_ROLE]->(r:Role {id: $roleId}) RETURN u.id AS id";
        return template.run(cypher, Map.of("roleId", roleId), r -> r.get("id").asLong());
    }

    public void deleteByUserId(Long userId) {
        template.runWrite("MATCH (u:User {id: $userId})-[r:HAS_ROLE]->() DELETE r", Map.of("userId", userId));
    }

    public void deleteByRoleId(Long roleId) {
        template.runWrite("MATCH ()-[r:HAS_ROLE]->(ro:Role {id: $roleId}) DELETE r", Map.of("roleId", roleId));
    }

    public void insert(Long userId, Long roleId) {
        template.runWrite(
            "MATCH (u:User {id: $userId}), (ro:Role {id: $roleId}) CREATE (u)-[:HAS_ROLE]->(ro)",
            Map.of("userId", userId, "roleId", roleId)
        );
    }
}
