package com.pms.service;

import com.pms.common.OrgTreeNode;
import com.pms.entity.PmCompany;
import com.pms.entity.PmDept;
import com.pms.entity.SysRole;
import com.pms.entity.SysUser;
import com.pms.mapper.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrgTreeService {

    private final PmCompanyMapper companyMapper;
    private final PmDeptMapper deptMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;

    public OrgTreeService(PmCompanyMapper companyMapper,
                          PmDeptMapper deptMapper,
                          SysRoleMapper roleMapper,
                          SysUserMapper userMapper,
                          SysUserRoleMapper userRoleMapper) {
        this.companyMapper = companyMapper;
        this.deptMapper = deptMapper;
        this.roleMapper = roleMapper;
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
    }

    public List<OrgTreeNode> buildTree() {
        List<PmCompany> companies = companyMapper.selectList();
        List<OrgTreeNode> roots = new ArrayList<>();
        for (PmCompany c : companies) {
            OrgTreeNode companyNode = new OrgTreeNode("company", c.getId(), firstNonBlank(c.getCompanyName(), c.getCompanyCode(), "公司"));
            List<PmDept> depts = deptMapper.selectByCompanyId(c.getId());
            for (PmDept d : depts) {
                OrgTreeNode deptNode = new OrgTreeNode("dept", d.getId(), firstNonBlank(d.getDeptName(), d.getDeptCode(), "部门"));
                List<SysRole> roles = roleMapper.selectByDeptId(d.getId());
                for (SysRole r : roles) {
                    OrgTreeNode roleNode = new OrgTreeNode("role", r.getId(), firstNonBlank(r.getName(), r.getCode(), "角色"));
                    List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(r.getId());
                    for (Long uid : userIds) {
                        SysUser u = userMapper.selectById(uid);
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

