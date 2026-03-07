package com.pms.repository;

import com.pms.entity.PmTask;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PmTaskRepository {

    private static final String LABEL = "Task";

    private final Neo4jTemplate template;
    private final IdGenerator idGenerator;
    private final SysUserRepository userRepository;
    private final PmProjectRepository projectRepository;

    public PmTaskRepository(Neo4jTemplate template, IdGenerator idGenerator, SysUserRepository userRepository, PmProjectRepository projectRepository) {
        this.template = template;
        this.idGenerator = idGenerator;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    public List<PmTask> selectPageByAssignee(Long assigneeId, int offset, int limit) {
        String cypher = "MATCH (n:" + LABEL + ") WHERE ($aid = -1 OR n.assigneeId = $aid) RETURN n ORDER BY n.id DESC SKIP $offset LIMIT $limit";
        Map<String, Object> params = new java.util.HashMap<>(Map.of("offset", (long) offset, "limit", (long) limit));
        params.put("aid", assigneeId != null ? assigneeId : -1L);
        List<PmTask> list = template.run(cypher, params, this::toEntity);
        fillNames(list);
        return list;
    }

    public long countPageByAssignee(Long assigneeId) {
        String cypher = "MATCH (n:" + LABEL + ") WHERE ($aid = -1 OR n.assigneeId = $aid) RETURN count(n) AS c";
        Long c = template.runSingle(cypher, Map.of("aid", assigneeId != null ? assigneeId : -1L), r -> r.get("c").asLong());
        return c != null ? c : 0L;
    }

    public PmTask selectById(Long id) {
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) RETURN n";
        return template.runSingle(cypher, Map.of("id", id), this::toEntity);
    }

    public PmTask selectByIdWithNames(Long id) {
        PmTask t = selectById(id);
        if (t != null) fillNames(List.of(t));
        return t;
    }

    public List<PmTask> selectByProjectId(Long projectId) {
        String cypher = "MATCH (n:" + LABEL + " {projectId: $projectId}) RETURN n ORDER BY n.parentId ASC, n.id ASC";
        List<PmTask> list = template.run(cypher, Map.of("projectId", projectId), this::toEntity);
        fillNames(list);
        return list;
    }

    public void insert(PmTask task) {
        long id = idGenerator.nextId(LABEL);
        task.setId(id);
        String cypher = "CREATE (n:" + LABEL + " {id: $id, projectId: $projectId, parentId: $parentId, taskCode: $taskCode, taskName: $taskName, assigneeId: $assigneeId, planStart: $planStart, planEnd: $planEnd, actualStart: $actualStart, actualEnd: $actualEnd, progress: $progress, status: $status})";
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("projectId", task.getProjectId() != null ? task.getProjectId() : 0L);
        params.put("parentId", task.getParentId() != null ? task.getParentId() : 0L);
        params.put("taskCode", nullable(task.getTaskCode()));
        params.put("taskName", nullable(task.getTaskName()));
        params.put("assigneeId", task.getAssigneeId() != null ? task.getAssigneeId() : 0L);
        params.put("planStart", NodeMapper.toDateStr(task.getPlanStart()));
        params.put("planEnd", NodeMapper.toDateStr(task.getPlanEnd()));
        params.put("actualStart", NodeMapper.toDateStr(task.getActualStart()));
        params.put("actualEnd", NodeMapper.toDateStr(task.getActualEnd()));
        params.put("progress", task.getProgress() != null ? task.getProgress() : 0);
        params.put("status", nullable(task.getStatus()));
        template.runWrite(cypher, params);
    }

    public void updateById(PmTask task) {
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) SET n.projectId = $projectId, n.parentId = $parentId, n.taskCode = $taskCode, n.taskName = $taskName, n.assigneeId = $assigneeId, n.planStart = $planStart, n.planEnd = $planEnd, n.actualStart = $actualStart, n.actualEnd = $actualEnd, n.progress = $progress, n.status = $status";
        Map<String, Object> params = new HashMap<>();
        params.put("id", task.getId());
        params.put("projectId", task.getProjectId() != null ? task.getProjectId() : 0L);
        params.put("parentId", task.getParentId() != null ? task.getParentId() : 0L);
        params.put("taskCode", nullable(task.getTaskCode()));
        params.put("taskName", nullable(task.getTaskName()));
        params.put("assigneeId", task.getAssigneeId() != null ? task.getAssigneeId() : 0L);
        params.put("planStart", NodeMapper.toDateStr(task.getPlanStart()));
        params.put("planEnd", NodeMapper.toDateStr(task.getPlanEnd()));
        params.put("actualStart", NodeMapper.toDateStr(task.getActualStart()));
        params.put("actualEnd", NodeMapper.toDateStr(task.getActualEnd()));
        params.put("progress", task.getProgress() != null ? task.getProgress() : 0);
        params.put("status", nullable(task.getStatus()));
        template.runWrite(cypher, params);
    }

    public void deleteById(Long id) {
        template.runWrite("MATCH (n:" + LABEL + " {id: $id}) DETACH DELETE n", Map.of("id", id));
    }

    private void fillNames(List<PmTask> list) {
        for (PmTask t : list) {
            if (t.getAssigneeId() != null) {
                var u = userRepository.selectById(t.getAssigneeId());
                if (u != null) t.setAssigneeName(u.getRealName() != null ? u.getRealName() : u.getUsername());
            }
            if (t.getProjectId() != null) {
                var p = projectRepository.selectById(t.getProjectId());
                if (p != null) t.setProjectName(p.getProjectName());
            }
        }
    }

    private PmTask toEntity(Record r) {
        if (!r.containsKey("n")) return null;
        Node n = r.get("n").asNode();
        PmTask e = new PmTask();
        e.setId(NodeMapper.getLong(n, "id"));
        e.setProjectId(NodeMapper.getLong(n, "projectId"));
        e.setParentId(NodeMapper.getLong(n, "parentId"));
        e.setTaskCode(NodeMapper.getString(n, "taskCode"));
        e.setTaskName(NodeMapper.getString(n, "taskName"));
        e.setAssigneeId(NodeMapper.getLong(n, "assigneeId"));
        e.setPlanStart(NodeMapper.getDate(n, "planStart"));
        e.setPlanEnd(NodeMapper.getDate(n, "planEnd"));
        e.setActualStart(NodeMapper.getDate(n, "actualStart"));
        e.setActualEnd(NodeMapper.getDate(n, "actualEnd"));
        e.setProgress(NodeMapper.getInt(n, "progress"));
        e.setStatus(NodeMapper.getString(n, "status"));
        e.setCreatedAt(NodeMapper.getDateTime(n, "createdAt"));
        e.setUpdatedAt(NodeMapper.getDateTime(n, "updatedAt"));
        return e;
    }

    private static String nullable(String s) { return s == null ? "" : s; }
}
