package com.pms.service;

import com.pms.common.PageResult;
import com.pms.entity.PmIssue;
import com.pms.mapper.PmIssueMapper;
import com.pms.config.WebConfig;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PmIssueService {

    private final PmIssueMapper issueMapper;

    public PmIssueService(PmIssueMapper issueMapper) {
        this.issueMapper = issueMapper;
    }

    public PageResult<PmIssue> page(Long projectId, String status, int page, int size) {
        int offset = (page - 1) * size;
        List<PmIssue> list = issueMapper.selectPage(projectId, status, offset, size);
        long total = issueMapper.countPage(projectId, status);
        return new PageResult<>(total, list);
    }

    public PmIssue getById(Long id) {
        return issueMapper.selectById(id);
    }

    public void save(PmIssue issue) {
        if (issue.getCreatedBy() == null) issue.setCreatedBy(WebConfig.getCurrentUserId());
        if (issue.getId() == null) {
            String maxCode = issueMapper.selectMaxIssueCode();
            int next = maxCode != null ? Integer.parseInt(maxCode.replaceAll("\\D", "")) + 1 : 1;
            issue.setIssueCode("ISS" + next);
            if (issue.getStatus() == null) issue.setStatus("OPEN");
            issueMapper.insert(issue);
        } else {
            issueMapper.updateById(issue);
        }
    }

    public void deleteById(Long id) {
        issueMapper.deleteById(id);
    }
}
