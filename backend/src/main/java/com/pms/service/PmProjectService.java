package com.pms.service;

import com.pms.common.PageResult;
import com.pms.entity.PmProject;
import com.pms.repository.PmProjectRepository;
import com.pms.config.WebConfig;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PmProjectService {

    private final PmProjectRepository projectRepository;

    public PmProjectService(PmProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public PageResult<PmProject> page(String keyword, String status, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        int offset = (page - 1) * size;
        List<PmProject> list = projectRepository.selectPage(keyword, status, offset, size);
        long total = projectRepository.countPage(keyword, status);
        return new PageResult<>(total, list);
    }

    public PmProject getById(Long id) {
        return projectRepository.selectById(id);
    }

    public void save(PmProject project) {
        if (project.getCreatedBy() == null) project.setCreatedBy(WebConfig.getCurrentUserId());
        if (project.getId() == null) {
            projectRepository.insert(project);
        } else {
            projectRepository.updateById(project);
        }
    }

    public void deleteById(Long id) {
        projectRepository.deleteById(id);
    }
}
