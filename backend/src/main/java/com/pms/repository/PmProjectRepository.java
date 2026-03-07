package com.pms.repository;

import com.pms.entity.PmProject;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class PmProjectRepository {

    private static final String LABEL = "Project";

    private final Neo4jTemplate template;
    private final IdGenerator idGenerator;
    private final SysUserRepository userRepository;

    public PmProjectRepository(Neo4jTemplate template, IdGenerator idGenerator, SysUserRepository userRepository) {
        this.template = template;
        this.idGenerator = idGenerator;
        this.userRepository = userRepository;
    }

    public List<PmProject> selectPage(String keyword, String status, int offset, int limit) {
        String cypher = "MATCH (n:" + LABEL + ") WHERE ($keyword = '' OR (n.projectCode IS NOT NULL AND n.projectCode CONTAINS $keyword) OR (n.projectName IS NOT NULL AND n.projectName CONTAINS $keyword)) AND ($status = '' OR n.status = $status) RETURN n ORDER BY n.id DESC SKIP $offset LIMIT $limit";
        List<PmProject> list = template.run(cypher, Map.of("keyword", keyword != null ? keyword : "", "status", status != null ? status : "", "offset", Math.max(0, offset), "limit", (long) limit), this::toEntity);
        for (PmProject p : list) {
            if (p.getOwnerId() != null && p.getOwnerId() > 0) {
                var u = userRepository.selectById(p.getOwnerId());
                if (u != null) p.setOwnerName(u.getRealName() != null ? u.getRealName() : u.getUsername());
            }
        }
        return list;
    }

    public long countPage(String keyword, String status) {
        String cypher = "MATCH (n:" + LABEL + ") WHERE ($keyword = '' OR (n.projectCode IS NOT NULL AND n.projectCode CONTAINS $keyword) OR (n.projectName IS NOT NULL AND n.projectName CONTAINS $keyword)) AND ($status = '' OR n.status = $status) RETURN count(n) AS c";
        Long c = template.runSingle(cypher, Map.of("keyword", keyword != null ? keyword : "", "status", status != null ? status : ""), r -> r.get("c").asLong());
        return c != null ? c : 0L;
    }

    public PmProject selectById(Long id) {
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) RETURN n";
        return template.runSingle(cypher, Map.of("id", id), this::toEntity);
    }

    public void insert(PmProject project) {
        long id = idGenerator.nextId(LABEL);
        project.setId(id);
        String cypher = "CREATE (n:" + LABEL + " {id: $id, projectCode: $projectCode, projectName: $projectName, description: $description, ownerId: $ownerId, planStart: $planStart, planEnd: $planEnd, status: $status, createdBy: $createdBy})";
        template.runWrite(cypher, Map.of(
            "id", id,
            "projectCode", nullable(project.getProjectCode()),
            "projectName", nullable(project.getProjectName()),
            "description", nullable(project.getDescription()),
            "ownerId", project.getOwnerId() != null ? project.getOwnerId() : 0L,
            "planStart", NodeMapper.toDateStr(project.getPlanStart()),
            "planEnd", NodeMapper.toDateStr(project.getPlanEnd()),
            "status", nullable(project.getStatus()),
            "createdBy", project.getCreatedBy() != null ? project.getCreatedBy() : 0L
        ));
    }

    public void updateById(PmProject project) {
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) SET n.projectCode = $projectCode, n.projectName = $projectName, n.description = $description, n.ownerId = $ownerId, n.planStart = $planStart, n.planEnd = $planEnd, n.status = $status";
        template.runWrite(cypher, Map.of(
            "id", project.getId(),
            "projectCode", nullable(project.getProjectCode()),
            "projectName", nullable(project.getProjectName()),
            "description", nullable(project.getDescription()),
            "ownerId", project.getOwnerId() != null ? project.getOwnerId() : 0L,
            "planStart", NodeMapper.toDateStr(project.getPlanStart()),
            "planEnd", NodeMapper.toDateStr(project.getPlanEnd()),
            "status", nullable(project.getStatus())
        ));
    }

    public void deleteById(Long id) {
        template.runWrite("MATCH (n:" + LABEL + " {id: $id}) DETACH DELETE n", Map.of("id", id));
    }

    private PmProject toEntity(Record r) {
        if (!r.containsKey("n")) return null;
        Node n = r.get("n").asNode();
        PmProject e = new PmProject();
        e.setId(NodeMapper.getLong(n, "id"));
        e.setProjectCode(NodeMapper.getString(n, "projectCode"));
        e.setProjectName(NodeMapper.getString(n, "projectName"));
        e.setDescription(NodeMapper.getString(n, "description"));
        e.setOwnerId(NodeMapper.getLong(n, "ownerId"));
        e.setPlanStart(NodeMapper.getDate(n, "planStart"));
        e.setPlanEnd(NodeMapper.getDate(n, "planEnd"));
        e.setActualStart(NodeMapper.getDate(n, "actualStart"));
        e.setActualEnd(NodeMapper.getDate(n, "actualEnd"));
        e.setStatus(NodeMapper.getString(n, "status"));
        e.setCreatedBy(NodeMapper.getLong(n, "createdBy"));
        e.setCreatedAt(NodeMapper.getDateTime(n, "createdAt"));
        e.setUpdatedAt(NodeMapper.getDateTime(n, "updatedAt"));
        return e;
    }

    private static String nullable(String s) { return s == null ? "" : s; }
}
