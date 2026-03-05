package com.pms.service;

import com.pms.common.PageResult;
import com.pms.entity.PmProject;
import com.pms.mapper.PmProjectMapper;
import com.pms.config.WebConfig;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PmProjectService {

    private final PmProjectMapper projectMapper;

    public PmProjectService(PmProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    public PageResult<PmProject> page(String keyword, String status, int page, int size) {
        int offset = (page - 1) * size;
        List<PmProject> list = projectMapper.selectPage(keyword, status, offset, size);
        long total = projectMapper.countPage(keyword, status);
        return new PageResult<>(total, list);
    }

    public PmProject getById(Long id) {
        return projectMapper.selectById(id);
    }

    public void save(PmProject project) {
        if (project.getCreatedBy() == null) project.setCreatedBy(WebConfig.getCurrentUserId());
        if (project.getId() == null) {
            projectMapper.insert(project);
        } else {
            projectMapper.updateById(project);
        }
    }

    public void deleteById(Long id) {
        projectMapper.deleteById(id);
    }
}
