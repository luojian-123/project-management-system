package com.pms.service;

import com.pms.entity.SysRole;
import com.pms.mapper.SysRoleMapper;
import com.pms.mapper.SysRoleMenuMapper;
import com.pms.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;

    public SysRoleService(SysRoleMapper roleMapper, SysRoleMenuMapper roleMenuMapper, SysUserRoleMapper userRoleMapper) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.userRoleMapper = userRoleMapper;
    }

    public List<SysRole> list() {
        return roleMapper.selectList();
    }

    public List<SysRole> listByDeptId(Long deptId) {
        return roleMapper.selectByDeptId(deptId);
    }

    public SysRole getById(Long id) {
        return roleMapper.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(SysRole role) {
        if (role.getStatus() == null) role.setStatus(1);
        if (role.getSortOrder() == null) role.setSortOrder(0);
        if (role.getId() == null) roleMapper.insert(role);
        else roleMapper.updateById(role);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        roleMenuMapper.deleteByRoleId(id);
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

