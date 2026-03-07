package com.pms.service;

import com.pms.common.OrgTreeNode;
import com.pms.entity.PmCompany;
import com.pms.entity.PmDept;
import com.pms.entity.SysRole;
import com.pms.entity.SysUser;
import com.pms.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrgTreeService {

    private final PmCompanyRepository companyRepository;
    private final PmDeptRepository deptRepository;
    private final SysRoleRepository roleRepository;
    private final SysUserRepository userRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final OrgBootstrapService orgBootstrapService;

    public OrgTreeService(PmCompanyRepository companyRepository,
                          PmDeptRepository deptRepository,
                          SysRoleRepository roleRepository,
                          SysUserRepository userRepository,
                          SysUserRoleRepository userRoleRepository,
                          OrgBootstrapService orgBootstrapService) {
        this.companyRepository = companyRepository;
        this.deptRepository = deptRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.orgBootstrapService = orgBootstrapService;
    }

    public List<OrgTreeNode> buildTree() {
        orgBootstrapService.ensureSystemCompanyExists();
        List<PmCompany> companies = companyRepository.selectList();
        List<OrgTreeNode> roots = new ArrayList<>();
        for (PmCompany c : companies) {
            OrgTreeNode companyNode = new OrgTreeNode("company", c.getId(), firstNonBlank(c.getCompanyName(), c.getCompanyCode(), "公司"));
            List<PmDept> depts = deptRepository.selectByCompanyId(c.getId());
            for (PmDept d : depts) {
                OrgTreeNode deptNode = new OrgTreeNode("dept", d.getId(), firstNonBlank(d.getDeptName(), d.getDeptCode(), "部门"));
                List<SysRole> roles = roleRepository.selectByDeptId(d.getId());
                for (SysRole r : roles) {
                    OrgTreeNode roleNode = new OrgTreeNode("role", r.getId(), firstNonBlank(r.getName(), r.getCode(), "角色"));
                    List<Long> userIds = userRoleRepository.selectUserIdsByRoleId(r.getId());
                    for (Long uid : userIds) {
                        SysUser u = userRepository.selectById(uid);
                        if (u != null) {
                            roleNode.getChildren().add(new OrgTreeNode("user", u.getId(), firstNonBlank(u.getRealName(), u.getUsername(), "用户")));
                        }
                    }
                    deptNode.getChildren().add(roleNode);
                }
                companyNode.getChildren().add(deptNode);
            }
            roots.add(companyNode);
        }
        return roots;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) return v;
        }
        return null;
    }
}

