package com.pms.service;

import com.pms.common.PageResult;
import com.pms.entity.PmIssue;
import com.pms.repository.PmIssueRepository;
import com.pms.config.WebConfig;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PmIssueService {

    private final PmIssueRepository issueRepository;

    public PmIssueService(PmIssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    public PageResult<PmIssue> page(Long projectId, String status, int page, int size) {
        int offset = (page - 1) * size;
        List<PmIssue> list = issueRepository.selectPage(projectId, status, offset, size);
        long total = issueRepository.countPage(projectId, status);
        return new PageResult<>(total, list);
    }

    public PmIssue getById(Long id) {
        return issueRepository.selectById(id);
    }

    public void save(PmIssue issue) {
        if (issue.getCreatedBy() == null) issue.setCreatedBy(WebConfig.getCurrentUserId());
        if (issue.getId() == null) {
            String maxCode = issueRepository.selectMaxIssueCode();
            int next = maxCode != null ? Integer.parseInt(maxCode.replaceAll("\\D", "")) + 1 : 1;
            issue.setIssueCode("ISS" + next);
            if (issue.getStatus() == null) issue.setStatus("OPEN");
            issueRepository.insert(issue);
        } else {
            issueRepository.updateById(issue);
        }
    }

    public void deleteById(Long id) {
        issueRepository.deleteById(id);
    }
}
