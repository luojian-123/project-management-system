package com.pms.service;

import com.pms.common.PageResult;
import com.pms.entity.PmCost;
import com.pms.repository.PmCostRepository;
import com.pms.config.WebConfig;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PmCostService {

    private final PmCostRepository costRepository;

    public PmCostService(PmCostRepository costRepository) {
        this.costRepository = costRepository;
    }

    public PageResult<PmCost> page(Long projectId, int page, int size) {
        int offset = (page - 1) * size;
        List<PmCost> list = costRepository.selectPage(projectId, offset, size);
        long total = costRepository.countPage(projectId);
        return new PageResult<>(total, list);
    }

    public PmCost getById(Long id) {
        return costRepository.selectById(id);
    }

    public void save(PmCost cost) {
        if (cost.getCreatedBy() == null) cost.setCreatedBy(WebConfig.getCurrentUserId());
        if (cost.getId() == null) {
            costRepository.insert(cost);
        } else {
            costRepository.updateById(cost);
        }
    }

    public void deleteById(Long id) {
        costRepository.deleteById(id);
    }
}
