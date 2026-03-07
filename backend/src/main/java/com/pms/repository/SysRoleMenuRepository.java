package com.pms.repository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class SysRoleMenuRepository {

    private final Neo4jTemplate template;

    public SysRoleMenuRepository(Neo4jTemplate template) {
        this.template = template;
    }

    public List<Long> selectMenuIdsByRoleId(Long roleId) {
        String cypher = "MATCH (r:Role {id: $roleId})-[:HAS_MENU]->(m:Menu) RETURN m.id AS id";
        return template.run(cypher, Map.of("roleId", roleId), r -> r.get("id").asLong());
    }

    public void deleteByRoleId(Long roleId) {
        template.runWrite("MATCH (r:Role {id: $roleId})-[rel:HAS_MENU]->() DELETE rel", Map.of("roleId", roleId));
    }

    public void deleteByMenuId(Long menuId) {
        template.runWrite("MATCH ()-[rel:HAS_MENU]->(m:Menu {id: $menuId}) DELETE rel", Map.of("menuId", menuId));
    }

    public void insert(Long roleId, Long menuId) {
        template.runWrite(
            "MATCH (r:Role {id: $roleId}), (m:Menu {id: $menuId}) CREATE (r)-[:HAS_MENU]->(m)",
            Map.of("roleId", roleId, "menuId", menuId)
        );
    }
}
