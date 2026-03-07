package com.pms.service;

import com.pms.entity.SysMenu;
import com.pms.repository.SysMenuRepository;
import com.pms.repository.SysRoleMenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SysMenuService {

    private final SysMenuRepository menuRepository;
    private final SysRoleMenuRepository roleMenuRepository;

    public SysMenuService(SysMenuRepository menuRepository, SysRoleMenuRepository roleMenuRepository) {
        this.menuRepository = menuRepository;
        this.roleMenuRepository = roleMenuRepository;
    }

    public List<SysMenu> tree() {
        List<SysMenu> all = menuRepository.selectAll();
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
        return menuRepository.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(SysMenu menu) {
        if (menu.getStatus() == null) menu.setStatus(1);
        if (menu.getSortOrder() == null) menu.setSortOrder(0);
        if (menu.getParentId() == null) menu.setParentId(0L);
        if (menu.getId() == null) menuRepository.insert(menu);
        else menuRepository.updateById(menu);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        roleMenuRepository.deleteByMenuId(id);
        menuRepository.deleteById(id);
    }
}

