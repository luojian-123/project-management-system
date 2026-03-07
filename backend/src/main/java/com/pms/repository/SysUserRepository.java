package com.pms.repository;

import com.pms.entity.SysUser;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class SysUserRepository {

    private static final String LABEL = "User";

    private final Neo4jTemplate template;
    private final IdGenerator idGenerator;

    public SysUserRepository(Neo4jTemplate template, IdGenerator idGenerator) {
        this.template = template;
        this.idGenerator = idGenerator;
    }

    public SysUser selectByUsername(String username) {
        String cypher = "MATCH (n:" + LABEL + " {username: $username}) RETURN n";
        return template.runSingle(cypher, Map.of("username", username), this::toEntity);
    }

    public SysUser selectById(Long id) {
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) RETURN n";
        return template.runSingle(cypher, Map.of("id", id), this::toEntity);
    }

    public List<SysUser> selectPage(String keyword, long offset, long limit) {
        String cypher = "MATCH (n:" + LABEL + ") WHERE ($keyword = '' OR n.username CONTAINS $keyword OR n.realName CONTAINS $keyword) RETURN n ORDER BY n.id DESC SKIP $offset LIMIT $limit";
        return template.run(cypher, Map.of("keyword", keyword != null ? keyword : "", "offset", offset, "limit", limit), this::toEntity);
    }

    public long countPage(String keyword) {
        String cypher = "MATCH (n:" + LABEL + ") WHERE ($keyword = '' OR n.username CONTAINS $keyword OR n.realName CONTAINS $keyword) RETURN count(n) AS c";
        Long c = template.runSingle(cypher, Map.of("keyword", keyword != null ? keyword : ""), r -> r.get("c").asLong());
        return c != null ? c : 0L;
    }

    public void insert(SysUser user) {
        long id = idGenerator.nextId(LABEL);
        user.setId(id);
        String cypher = "CREATE (n:" + LABEL + " {id: $id, username: $username, password: $password, realName: $realName, email: $email, phone: $phone, status: $status})";
        template.runWrite(cypher, Map.of(
            "id", id,
            "username", nullable(user.getUsername()),
            "password", nullable(user.getPassword()),
            "realName", nullable(user.getRealName()),
            "email", nullable(user.getEmail()),
            "phone", nullable(user.getPhone()),
            "status", user.getStatus() != null ? user.getStatus() : 1
        ));
    }

    public void updateById(SysUser user) {
        boolean setPassword = user.getPassword() != null && !user.getPassword().isEmpty();
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) SET n.username = $username, n.realName = $realName, n.email = $email, n.phone = $phone, n.status = $status" + (setPassword ? ", n.password = $password" : "");
        Map<String, Object> params = new java.util.HashMap<>(Map.of(
            "id", user.getId(),
            "username", nullable(user.getUsername()),
            "realName", nullable(user.getRealName()),
            "email", nullable(user.getEmail()),
            "phone", nullable(user.getPhone()),
            "status", user.getStatus() != null ? user.getStatus() : 1
        ));
        if (setPassword) params.put("password", user.getPassword());
        template.runWrite(cypher, params);
    }

    public void deleteById(Long id) {
        template.runWrite("MATCH (n:" + LABEL + " {id: $id}) DETACH DELETE n", Map.of("id", id));
    }

    private SysUser toEntity(Record r) {
        if (!r.containsKey("n")) return null;
        Node n = r.get("n").asNode();
        SysUser e = new SysUser();
        e.setId(NodeMapper.getLong(n, "id"));
        e.setUsername(NodeMapper.getString(n, "username"));
        e.setPassword(NodeMapper.getString(n, "password"));
        e.setRealName(NodeMapper.getString(n, "realName"));
        e.setEmail(NodeMapper.getString(n, "email"));
        e.setPhone(NodeMapper.getString(n, "phone"));
        e.setStatus(NodeMapper.getInt(n, "status"));
        e.setCreatedAt(NodeMapper.getDateTime(n, "createdAt"));
        e.setUpdatedAt(NodeMapper.getDateTime(n, "updatedAt"));
        return e;
    }

    private static String nullable(String s) { return s == null ? "" : s; }
}
