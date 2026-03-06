package com.pms.service;

import com.pms.entity.SysRole;
import com.pms.mapper.SysRoleMapper;
import com.pms.mapper.SysRoleDeptMapper;
import com.pms.mapper.SysRoleMenuMapper;
import com.pms.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;

    public SysRoleService(SysRoleMapper roleMapper, SysRoleDeptMapper roleDeptMapper,
                          SysRoleMenuMapper roleMenuMapper, SysUserRoleMapper userRoleMapper) {
        this.roleMapper = roleMapper;
        this.roleDeptMapper = roleDeptMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.userRoleMapper = userRoleMapper;
    }

    public List<SysRole> list() {
        List<SysRole> roles = roleMapper.selectList();
        for (SysRole r : roles) {
            if (r.getId() != null)
                r.setDeptIds(roleDeptMapper.selectDeptIdsByRoleId(r.getId()));
        }
        return roles;
    }

    public List<SysRole> listByDeptId(Long deptId) {
        List<Long> roleIds = roleDeptMapper.selectRoleIdsByDeptId(deptId);
        List<SysRole> result = new ArrayList<>();
        for (Long rid : roleIds) {
            SysRole r = roleMapper.selectById(rid);
            if (r != null) {
                r.setDeptIds(roleDeptMapper.selectDeptIdsByRoleId(rid));
                result.add(r);
            }
        }
        return result;
    }

    public SysRole getById(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role != null && id != null)
            role.setDeptIds(roleDeptMapper.selectDeptIdsByRoleId(id));
        return role;
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(SysRole role) {
        if (role.getStatus() == null) role.setStatus(1);
        if (role.getSortOrder() == null) role.setSortOrder(0);
        if (role.getDeptIds() != null && !role.getDeptIds().isEmpty())
            role.setDeptId(role.getDeptIds().get(0));
        if (role.getId() == null) roleMapper.insert(role);
        else roleMapper.updateById(role);
        if (role.getId() != null) {
            roleDeptMapper.deleteByRoleId(role.getId());
            if (role.getDeptIds() != null) {
                for (Long deptId : role.getDeptIds()) {
                    if (deptId != null)
                        roleDeptMapper.insert(role.getId(), deptId);
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        roleMenuMapper.deleteByRoleId(id);
        roleDeptMapper.deleteByRoleId(id);
        userRoleMapper.deleteByRoleId(id);
        roleMapper.deleteById(id);
    }

    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return roleMenuMapper.selectMenuIdsByRoleId(roleId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void setMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.deleteByRoleId(roleId);
        if (menuIds != null) {
            for (Long mid : menuIds) {
                roleMenuMapper.insert(roleId, mid);
            }
        }
    }
}

