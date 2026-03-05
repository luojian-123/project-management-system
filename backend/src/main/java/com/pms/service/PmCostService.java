package com.pms.service;

import com.pms.common.PageResult;
import com.pms.entity.PmCost;
import com.pms.mapper.PmCostMapper;
import com.pms.config.WebConfig;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PmCostService {

    private final PmCostMapper costMapper;

    public PmCostService(PmCostMapper costMapper) {
        this.costMapper = costMapper;
    }

    public PageResult<PmCost> page(Long projectId, int page, int size) {
        int offset = (page - 1) * size;
        List<PmCost> list = costMapper.selectPage(projectId, offset, size);
        long total = costMapper.countPage(projectId);
        return new PageResult<>(total, list);
    }

    public PmCost getById(Long id) {
        return costMapper.selectById(id);
    }

    public void save(PmCost cost) {
        if (cost.getCreatedBy() == null) cost.setCreatedBy(WebConfig.getCurrentUserId());
        if (cost.getId() == null) {
            costMapper.insert(cost);
        } else {
            costMapper.updateById(cost);
        }
    }

    public void deleteById(Long id) {
        costMapper.deleteById(id);
    }
}
