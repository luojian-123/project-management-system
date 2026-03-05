package com.pms.service;

import com.pms.common.PageResult;
import com.pms.entity.PmChange;
import com.pms.mapper.PmChangeMapper;
import com.pms.config.WebConfig;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PmChangeService {

    private final PmChangeMapper changeMapper;

    public PmChangeService(PmChangeMapper changeMapper) {
        this.changeMapper = changeMapper;
    }

    public PageResult<PmChange> page(Long projectId, String status, int page, int size) {
        int offset = (page - 1) * size;
        List<PmChange> list = changeMapper.selectPage(projectId, status, offset, size);
        long total = changeMapper.countPage(projectId, status);
        return new PageResult<>(total, list);
    }

    public PmChange getById(Long id) {
        return changeMapper.selectById(id);
    }

    public void save(PmChange change) {
        if (change.getApplicantId() == null) change.setApplicantId(WebConfig.getCurrentUserId());
        if (change.getId() == null) {
            String maxNo = changeMapper.selectMaxChangeNo();
            int next = maxNo != null ? Integer.parseInt(maxNo.replaceAll("\\D", "")) + 1 : 1;
            change.setChangeNo("CHG" + next);
            if (change.getStatus() == null) change.setStatus("DRAFT");
            changeMapper.insert(change);
        } else {
            changeMapper.updateById(change);
        }
    }

    public void deleteById(Long id) {
        changeMapper.deleteById(id);
    }
}
