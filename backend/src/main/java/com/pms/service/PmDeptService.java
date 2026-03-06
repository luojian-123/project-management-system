package com.pms.service;

import com.pms.entity.PmDept;
import com.pms.mapper.PmDeptMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PmDeptService {

    private final PmDeptMapper deptMapper;

    public PmDeptService(PmDeptMapper deptMapper) {
        this.deptMapper = deptMapper;
    }

    public List<PmDept> listByCompanyId(Long companyId) {
        return deptMapper.selectByCompanyId(companyId);
    }

    public PmDept getById(Long id) {
        return deptMapper.selectById(id);
    }

    public void save(PmDept dept) {
        if (dept.getId() == null) deptMapper.insert(dept);
        else deptMapper.updateById(dept);
    }

    public void deleteById(Long id) {
        deptMapper.deleteById(id);
    }
}

