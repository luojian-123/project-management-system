package com.pms.repository;

import com.pms.entity.PmTaskDeliverable;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class PmTaskDeliverableRepository {

    private static final String LABEL = "TaskDeliverable";

    private final Neo4jTemplate template;
    private final IdGenerator idGenerator;

    public PmTaskDeliverableRepository(Neo4jTemplate template, IdGenerator idGenerator) {
        this.template = template;
        this.idGenerator = idGenerator;
    }

    public List<PmTaskDeliverable> selectByTaskId(Long taskId) {
        String cypher = "MATCH (n:" + LABEL + " {taskId: $taskId}) RETURN n ORDER BY n.sortOrder ASC, n.id ASC";
        return template.run(cypher, Map.of("taskId", taskId != null ? taskId : 0L), this::toEntity);
    }

    public void insert(PmTaskDeliverable d) {
        long id = idGenerator.nextId(LABEL);
        d.setId(id);
        String cypher = "CREATE (n:" + LABEL + " {id: $id, taskId: $taskId, name: $name, type: $type, url: $url, attachmentPath: $attachmentPath, attachmentName: $attachmentName, remark: $remark, sortOrder: $sortOrder})";
        template.runWrite(cypher, Map.of(
            "id", id,
            "taskId", d.getTaskId() != null ? d.getTaskId() : 0L,
            "name", nullable(d.getName()),
            "type", nullable(d.getType()),
            "url", nullable(d.getUrl()),
            "attachmentPath", nullable(d.getAttachmentPath()),
            "attachmentName", nullable(d.getAttachmentName()),
            "remark", nullable(d.getRemark()),
            "sortOrder", d.getSortOrder() != null ? d.getSortOrder() : 0
        ));
    }

    public void update(PmTaskDeliverable d) {
        if (d.getId() == null) return;
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) SET n.name = $name, n.type = $type, n.url = $url, n.attachmentPath = $attachmentPath, n.attachmentName = $attachmentName, n.remark = $remark, n.sortOrder = $sortOrder";
        template.runWrite(cypher, Map.of(
            "id", d.getId(),
            "name", nullable(d.getName()),
            "type", nullable(d.getType()),
            "url", nullable(d.getUrl()),
            "attachmentPath", nullable(d.getAttachmentPath()),
            "attachmentName", nullable(d.getAttachmentName()),
            "remark", nullable(d.getRemark()),
            "sortOrder", d.getSortOrder() != null ? d.getSortOrder() : 0
        ));
    }

    public void deleteById(Long id) {
        if (id == null) return;
        String cypher = "MATCH (n:" + LABEL + " {id: $id}) DELETE n";
        template.runWrite(cypher, Map.of("id", id));
    }

    private PmTaskDeliverable toEntity(Record r) {
        if (!r.containsKey("n")) return null;
        Node n = r.get("n").asNode();
        PmTaskDeliverable e = new PmTaskDeliverable();
        e.setId(NodeMapper.getLong(n, "id"));
        e.setTaskId(NodeMapper.getLong(n, "taskId"));
        e.setName(NodeMapper.getString(n, "name"));
        e.setType(NodeMapper.getString(n, "type"));
        e.setUrl(NodeMapper.getString(n, "url"));
        e.setAttachmentPath(NodeMapper.getString(n, "attachmentPath"));
        e.setAttachmentName(NodeMapper.getString(n, "attachmentName"));
        e.setRemark(NodeMapper.getString(n, "remark"));
        e.setSortOrder(NodeMapper.getInt(n, "sortOrder"));
        return e;
    }

    private static String nullable(String s) { return s == null ? "" : s; }
}
