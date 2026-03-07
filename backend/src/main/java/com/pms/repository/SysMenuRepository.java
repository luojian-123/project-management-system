package com.pms.repository;

import com.pms.entity.SysMenu;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class SysMenuRepository {

    private static final String LABEL = "Menu";

    private final Neo4jTemplate template;
    private final IdGenerator idGenerator;

    public SysMenuRepository(Neo4jTemplate template, IdGenerator idGenerator) {
        this.template = template;
        this.idGenerator = idGenerator;
    }

    public List<SysMenu> selectByUserId(Long userId) {
        String cypher = "MATCH (u:User {id: $userId})-[:HAS_ROLE]->(r:Role)-[:HAS_MENU]->(n:" + LABEL + ") WHERE n.status = 1 RETURN DISTINCT n ORDER BY n.sortOrder ASC, n.id ASC";
        return template.run(cypher, Map.of("userId", userId), this::toEntity);
    }

    public List<SysMenu> selectChildren(Long parentId) {
        String cypher = "MATCH (n:" + LABEL + ") WHERE n.parentId = $parentId AND n.status = 1 RETURN n ORDER BY n.sortOrder ASC";
        return template.run(cypher, Map.of("parentId", parentId != null ? parentId : 0L), this::toEntity);
    }

    public List<SysMenu> selectAll() {
        String cypher = "MATCH (n:" + LABEL + ") RETURN n ORDER BY n.parentId ASC, n.sortOrder ASC, n.id ASC";
        return template.run(cypher, null, this::toEntity);
    }

    public SysMenu selectById(Long id) {
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) RETURN n";
        return template.runSingle(cypher, Map.of("id", id), this::toEntity);
    }

    public void insert(SysMenu menu) {
        long id = idGenerator.nextId(LABEL);
        menu.setId(id);
        String cypher = "CREATE (n:" + LABEL + " {id: $id, parentId: $parentId, name: $name, path: $path, component: $component, permission: $permission, type: $type, sortOrder: $sortOrder, icon: $icon, status: $status})";
        template.runWrite(cypher, Map.of(
            "id", id,
            "parentId", menu.getParentId() != null ? menu.getParentId() : 0L,
            "name", nullable(menu.getName()),
            "path", nullable(menu.getPath()),
            "component", nullable(menu.getComponent()),
            "permission", nullable(menu.getPermission()),
            "type", menu.getType() != null ? menu.getType() : 0,
            "sortOrder", menu.getSortOrder() != null ? menu.getSortOrder() : 0,
            "icon", nullable(menu.getIcon()),
            "status", menu.getStatus() != null ? menu.getStatus() : 1
        ));
    }

    public void updateById(SysMenu menu) {
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) SET n.parentId = $parentId, n.name = $name, n.path = $path, n.component = $component, n.permission = $permission, n.type = $type, n.sortOrder = $sortOrder, n.icon = $icon, n.status = $status";
        template.runWrite(cypher, Map.of(
            "id", menu.getId(),
            "parentId", menu.getParentId() != null ? menu.getParentId() : 0L,
            "name", nullable(menu.getName()),
            "path", nullable(menu.getPath()),
            "component", nullable(menu.getComponent()),
            "permission", nullable(menu.getPermission()),
            "type", menu.getType() != null ? menu.getType() : 0,
            "sortOrder", menu.getSortOrder() != null ? menu.getSortOrder() : 0,
            "icon", nullable(menu.getIcon()),
            "status", menu.getStatus() != null ? menu.getStatus() : 1
        ));
    }

    public void deleteById(Long id) {
        template.runWrite("MATCH (n:" + LABEL + " {id: $id}) DETACH DELETE n", Map.of("id", id));
    }

    private SysMenu toEntity(Record r) {
        if (!r.containsKey("n")) return null;
        Node n = r.get("n").asNode();
        SysMenu e = new SysMenu();
        e.setId(NodeMapper.getLong(n, "id"));
        e.setParentId(NodeMapper.getLong(n, "parentId"));
        e.setName(NodeMapper.getString(n, "name"));
        e.setPath(NodeMapper.getString(n, "path"));
        e.setComponent(NodeMapper.getString(n, "component"));
        e.setPermission(NodeMapper.getString(n, "permission"));
        e.setType(NodeMapper.getInt(n, "type"));
        e.setSortOrder(NodeMapper.getInt(n, "sortOrder"));
        e.setIcon(NodeMapper.getString(n, "icon"));
        e.setStatus(NodeMapper.getInt(n, "status"));
        return e;
    }

    private static String nullable(String s) { return s == null ? "" : s; }
}
