package com.pms.service;

import com.pms.common.PageResult;
import com.pms.entity.PmTask;
import com.pms.entity.PmTaskChange;
import com.pms.mapper.PmTaskMapper;
import com.pms.mapper.PmTaskChangeMapper;
import com.pms.mapper.SysUserMapper;
import com.pms.entity.SysUser;
import com.pms.config.WebConfig;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PmTaskService {

    private final PmTaskMapper taskMapper;
    private final PmTaskChangeMapper taskChangeMapper;
    private final SysUserMapper userMapper;

    public PmTaskService(PmTaskMapper taskMapper, PmTaskChangeMapper taskChangeMapper, SysUserMapper userMapper) {
        this.taskMapper = taskMapper;
        this.taskChangeMapper = taskChangeMapper;
        this.userMapper = userMapper;
    }

    /** 按责任人分页查询任务（当前登录用户） */
    public PageResult<PmTask> pageByAssignee(Long assigneeId, int page, int size) {
        if (assigneeId == null) {
            return new PageResult<>(0L, List.of());
        }
        int offset = (page - 1) * size;
        List<PmTask> list = taskMapper.selectPageByAssignee(assigneeId, offset, size);
        long total = taskMapper.countPageByAssignee(assigneeId);
        return new PageResult<>(total, list);
    }

    public PmTask getById(Long id) {
        return taskMapper.selectById(id);
    }

    /** 任务详情（含项目名、负责人名） */
    public PmTask getDetailById(Long id) {
        return taskMapper.selectByIdWithNames(id);
    }

    /** 按项目查询任务列表（含负责人名）；tree 为 true 时组装为树形（children） */
    public List<PmTask> listByProject(Long projectId, boolean tree) {
        if (projectId == null) return List.of();
        List<PmTask> list = taskMapper.selectByProjectId(projectId);
        if (!tree || list.isEmpty()) return list;
        return buildTaskTree(list, 0L);
    }

    private List<PmTask> buildTaskTree(List<PmTask> flat, Long parentId) {
        List<PmTask> result = new java.util.ArrayList<>();
        for (PmTask t : flat) {
            Long pid = t.getParentId();
            if (pid == null) pid = 0L;
            if (!pid.equals(parentId)) continue;
            result.add(t);
            List<PmTask> children = buildTaskTree(flat, t.getId());
            if (!children.isEmpty()) t.setChildren(children);
        }
        return result;
    }

    public List<PmTaskChange> listChangesByTaskId(Long taskId) {
        return taskChangeMapper.selectByTaskId(taskId);
    }

    public void save(PmTask task) {
        if (task.getId() == null) {
            if (task.getStatus() == null) task.setStatus("TODO");
            if (task.getProgress() == null) task.setProgress(0);
            if (task.getParentId() == null) task.setParentId(0L);
            taskMapper.insert(task);
            logChange(task.getId(), "创建", "创建任务");
        } else {
            taskMapper.updateById(task);
            logChange(task.getId(), "更新", "更新任务信息");
        }
    }

    public void deleteById(Long id) {
        taskMapper.deleteById(id);
    }

    private void logChange(Long taskId, String action, String content) {
        Long uid = WebConfig.getCurrentUserId();
        String operatorName = null;
        if (uid != null) {
            SysUser u = userMapper.selectById(uid);
            if (u != null) operatorName = u.getRealName() != null ? u.getRealName() : u.getUsername();
        }
        PmTaskChange change = new PmTaskChange();
        change.setTaskId(taskId);
        change.setOperatorId(uid);
        change.setOperatorName(operatorName);
        change.setAction(action);
        change.setContent(content);
        taskChangeMapper.insert(change);
    }
}
