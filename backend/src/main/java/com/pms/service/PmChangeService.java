package com.pms.service;

import com.pms.common.PageResult;
import com.pms.entity.PmChange;
import com.pms.repository.PmChangeRepository;
import com.pms.config.WebConfig;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PmChangeService {

    private final PmChangeRepository changeRepository;

    public PmChangeService(PmChangeRepository changeRepository) {
        this.changeRepository = changeRepository;
    }

    public PageResult<PmChange> page(Long projectId, String status, int page, int size) {
        int offset = (page - 1) * size;
        List<PmChange> list = changeRepository.selectPage(projectId, status, offset, size);
        long total = changeRepository.countPage(projectId, status);
        return new PageResult<>(total, list);
    }

    public PmChange getById(Long id) {
        return changeRepository.selectById(id);
    }

    public void save(PmChange change) {
        if (change.getApplicantId() == null) change.setApplicantId(WebConfig.getCurrentUserId());
        if (change.getId() == null) {
            String maxNo = changeRepository.selectMaxChangeNo();
            int next = maxNo != null ? Integer.parseInt(maxNo.replaceAll("\\D", "")) + 1 : 1;
            change.setChangeNo("CHG" + next);
            if (change.getStatus() == null) change.setStatus("DRAFT");
            changeRepository.insert(change);
        } else {
            changeRepository.updateById(change);
        }
    }

    public void deleteById(Long id) {
        changeRepository.deleteById(id);
    }
}
