package com.pms.service;

import com.pms.entity.PmCompany;
import com.pms.entity.PmDept;
import com.pms.entity.SysRole;
import com.pms.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 组织基础数据保障：确保系统公司、系统部门存在，且管理员角色绑定系统部门。
 * 在启动时与首次拉取公司列表时调用，避免系统公司未展示。
 */
@Service
public class OrgBootstrapService {

    private final PmCompanyRepository companyRepository;
    private final PmDeptRepository deptRepository;
    private final SysRoleRepository roleRepository;
    private final SysRoleDeptRepository roleDeptRepository;

    public OrgBootstrapService(PmCompanyRepository companyRepository,
                              PmDeptRepository deptRepository,
                              SysRoleRepository roleRepository,
                              SysRoleDeptRepository roleDeptRepository) {
        this.companyRepository = companyRepository;
        this.deptRepository = deptRepository;
        this.roleRepository = roleRepository;
        this.roleDeptRepository = roleDeptRepository;
    }

    /** 若系统公司不存在则创建（系统部门、ADMIN 绑定一并补齐） */
    public void ensureSystemCompanyExists() {
        PmCompany sysCompany = companyRepository.selectByCode(PmCompanyRepository.SYS_COMPANY_CODE);
        if (sysCompany == null) {
            sysCompany = new PmCompany();
            sysCompany.setCompanyCode(PmCompanyRepository.SYS_COMPANY_CODE);
            sysCompany.setCompanyName("系统公司");
            sysCompany.setSortOrder(-1);
            sysCompany.setIsSystem(true);
            companyRepository.insert(sysCompany);
        }
        if (sysCompany.getId() == null) return;

        PmDept sysDept = deptRepository.selectByCompanyIdAndCode(sysCompany.getId(), PmDeptRepository.SYS_DEPT_CODE);
        if (sysDept == null) {
            sysDept = new PmDept();
            sysDept.setCompanyId(sysCompany.getId());
            sysDept.setDeptCode(PmDeptRepository.SYS_DEPT_CODE);
            sysDept.setDeptName("系统管理部");
            sysDept.setSortOrder(0);
            sysDept.setIsSystem(true);
            deptRepository.insert(sysDept);
        }
        if (sysDept.getId() == null) return;

        SysRole adminRole = roleRepository.selectByCode("ADMIN");
        if (adminRole != null && adminRole.getId() != null) {
            List<Long> deptIds = roleDeptRepository.selectDeptIdsByRoleId(adminRole.getId());
            if (deptIds == null || !deptIds.contains(sysDept.getId())) {
                roleDeptRepository.insert(adminRole.getId(), sysDept.getId());
            }
        }
    }
}
