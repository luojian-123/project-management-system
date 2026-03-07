package com.pms.service;

import com.pms.entity.PmDept;
import com.pms.entity.SysRole;
import com.pms.repository.PmCompanyRepository;
import com.pms.repository.PmDeptRepository;
import com.pms.repository.SysRoleRepository;
import com.pms.repository.SysRoleDeptRepository;
import com.pms.repository.SysRoleMenuRepository;
import com.pms.repository.SysUserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class SysRoleService {

    private final SysRoleRepository roleRepository;
    private final SysRoleDeptRepository roleDeptRepository;
    private final SysRoleMenuRepository roleMenuRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final PmCompanyRepository companyRepository;
    private final PmDeptRepository deptRepository;

    public SysRoleService(SysRoleRepository roleRepository, SysRoleDeptRepository roleDeptRepository,
                          SysRoleMenuRepository roleMenuRepository, SysUserRoleRepository userRoleRepository,
                          PmCompanyRepository companyRepository, PmDeptRepository deptRepository) {
        this.roleRepository = roleRepository;
        this.roleDeptRepository = roleDeptRepository;
        this.roleMenuRepository = roleMenuRepository;
        this.userRoleRepository = userRoleRepository;
        this.companyRepository = companyRepository;
        this.deptRepository = deptRepository;
    }

    public List<SysRole> list() {
        return roleRepository.selectList();
    }

    public List<SysRole> listByDeptId(Long deptId) {
        return roleRepository.selectByDeptId(deptId);
    }

    public SysRole getById(Long id) {
        return roleRepository.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(SysRole role) {
        if (role.getStatus() == null) role.setStatus(1);
        if (role.getSortOrder() == null) role.setSortOrder(0);
        if (role.getDeptIds() != null && !role.getDeptIds().isEmpty())
            role.setDeptId(role.getDeptIds().get(0));
        if (role.getId() == null) roleRepository.insert(role);
        else roleRepository.updateById(role);
        if (role.getId() != null) {
            roleDeptRepository.deleteByRoleId(role.getId());
            Set<Long> deptIds = new LinkedHashSet<>(role.getDeptIds() != null ? role.getDeptIds() : List.of());
            if ("ADMIN".equals(role.getCode())) {
                var sysCompany = companyRepository.selectByCode(PmCompanyRepository.SYS_COMPANY_CODE);
                if (sysCompany != null && sysCompany.getId() != null) {
                    PmDept sysDept = deptRepository.selectByCompanyIdAndCode(sysCompany.getId(), PmDeptRepository.SYS_DEPT_CODE);
                    if (sysDept != null && sysDept.getId() != null) {
                        deptIds.add(sysDept.getId());
                    }
                }
            }
            for (Long deptId : deptIds) {
                if (deptId != null)
                    roleDeptRepository.insert(role.getId(), deptId);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        roleMenuRepository.deleteByRoleId(id);
        roleDeptRepository.deleteByRoleId(id);
        userRoleRepository.deleteByRoleId(id);
        roleRepository.deleteById(id);
    }

    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return roleMenuRepository.selectMenuIdsByRoleId(roleId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void setMenus(Long roleId, List<Long> menuIds) {
        roleMenuRepository.deleteByRoleId(roleId);
        if (menuIds != null) {
            for (Long mid : menuIds) {
                roleMenuRepository.insert(roleId, mid);
            }
        }
    }
}

