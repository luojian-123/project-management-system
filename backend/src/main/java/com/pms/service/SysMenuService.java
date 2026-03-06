package com.pms.service;

import com.pms.entity.SysMenu;
import com.pms.mapper.SysMenuMapper;
import com.pms.mapper.SysRoleMenuMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SysMenuService {

    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    public SysMenuService(SysMenuMapper menuMapper, SysRoleMenuMapper roleMenuMapper) {
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    public List<SysMenu> tree() {
        List<SysMenu> all = menuMapper.selectAll();
        return buildTree(all, 0L);
    }

    private List<SysMenu> buildTree(List<SysMenu> all, Long parentId) {
        List<SysMenu> out = new ArrayList<>();
        for (SysMenu m : all) {
            Long pid = m.getParentId() == null ? 0L : m.getParentId();
            if (pid.equals(parentId)) {
                m.setChildren(buildTree(all, m.getId()));
                out.add(m);
            }
        }
        return out;
    }

    public SysMenu getById(Long id) {
        return menuMapper.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(SysMenu menu) {
        if (menu.getStatus() == null) menu.setStatus(1);
        if (menu.getSortOrder() == null) menu.setSortOrder(0);
        if (menu.getParentId() == null) menu.setParentId(0L);
        if (menu.getId() == null) menuMapper.insert(menu);
        else menuMapper.updateById(menu);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        roleMenuMapper.deleteByMenuId(id);
        menuMapper.deleteById(id);
    }
}

