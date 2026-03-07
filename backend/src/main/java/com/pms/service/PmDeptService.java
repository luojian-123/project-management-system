package com.pms.service;

import com.pms.entity.PmDept;
import com.pms.repository.PmDeptRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PmDeptService {

    private final PmDeptRepository deptRepository;

    public PmDeptService(PmDeptRepository deptRepository) {
        this.deptRepository = deptRepository;
    }

    public List<PmDept> listByCompanyId(Long companyId) {
        return deptRepository.selectByCompanyId(companyId);
    }

    public PmDept getById(Long id) {
        return deptRepository.selectById(id);
    }

    public void save(PmDept dept) {
        if (dept.getId() == null) {
            deptRepository.insert(dept);
            return;
        }
        PmDept existing = deptRepository.selectById(dept.getId());
        if (existing != null && Boolean.TRUE.equals(existing.getIsSystem())) {
            dept.setCompanyId(existing.getCompanyId());
            dept.setDeptCode(PmDeptRepository.SYS_DEPT_CODE);
            dept.setIsSystem(true);
        }
        deptRepository.updateById(dept);
    }

    public void deleteById(Long id) {
        PmDept d = deptRepository.selectById(id);
        if (d != null && Boolean.TRUE.equals(d.getIsSystem())) {
            throw new IllegalStateException("系统部门不能删除");
        }
        deptRepository.deleteById(id);
    }
}
