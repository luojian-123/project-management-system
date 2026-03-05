package com.pms.service;

import com.pms.common.PageResult;
import com.pms.entity.PmRisk;
import com.pms.mapper.PmRiskMapper;
import com.pms.config.WebConfig;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PmRiskService {

    private final PmRiskMapper riskMapper;

    public PmRiskService(PmRiskMapper riskMapper) {
        this.riskMapper = riskMapper;
    }

    public PageResult<PmRisk> page(Long projectId, String status, int page, int size) {
        int offset = (page - 1) * size;
        List<PmRisk> list = riskMapper.selectPage(projectId, status, offset, size);
        long total = riskMapper.countPage(projectId, status);
        return new PageResult<>(total, list);
    }

    public PmRisk getById(Long id) {
        return riskMapper.selectById(id);
    }

    public void save(PmRisk risk) {
        if (risk.getCreatedBy() == null) risk.setCreatedBy(WebConfig.getCurrentUserId());
        if (risk.getId() == null) {
            String maxCode = riskMapper.selectMaxRiskCode();
            int next = maxCode != null ? Integer.parseInt(maxCode.replaceAll("\\D", "")) + 1 : 1;
            risk.setRiskCode("RISK" + next);
            if (risk.getStatus() == null) risk.setStatus("IDENTIFIED");
            riskMapper.insert(risk);
        } else {
            riskMapper.updateById(risk);
        }
    }

    public void deleteById(Long id) {
        riskMapper.deleteById(id);
    }
}
