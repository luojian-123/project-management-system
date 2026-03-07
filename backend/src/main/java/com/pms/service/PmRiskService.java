package com.pms.service;

import com.pms.common.PageResult;
import com.pms.entity.PmRisk;
import com.pms.repository.PmRiskRepository;
import com.pms.config.WebConfig;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PmRiskService {

    private final PmRiskRepository riskRepository;

    public PmRiskService(PmRiskRepository riskRepository) {
        this.riskRepository = riskRepository;
    }

    public PageResult<PmRisk> page(Long projectId, String status, int page, int size) {
        int offset = (page - 1) * size;
        List<PmRisk> list = riskRepository.selectPage(projectId, status, offset, size);
        long total = riskRepository.countPage(projectId, status);
        return new PageResult<>(total, list);
    }

    public PmRisk getById(Long id) {
        return riskRepository.selectById(id);
    }

    public void save(PmRisk risk) {
        if (risk.getCreatedBy() == null) risk.setCreatedBy(WebConfig.getCurrentUserId());
        if (risk.getId() == null) {
            String maxCode = riskRepository.selectMaxRiskCode();
            int next = maxCode != null ? Integer.parseInt(maxCode.replaceAll("\\D", "")) + 1 : 1;
            risk.setRiskCode("RISK" + next);
            if (risk.getStatus() == null) risk.setStatus("IDENTIFIED");
            riskRepository.insert(risk);
        } else {
            riskRepository.updateById(risk);
        }
    }

    public void deleteById(Long id) {
        riskRepository.deleteById(id);
    }
}
