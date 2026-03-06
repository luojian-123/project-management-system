package com.pms.service;

import com.pms.common.PageResult;
import com.pms.entity.SysUser;
import com.pms.mapper.SysUserMapper;
import com.pms.mapper.SysUserRoleMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysUserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public SysUserService(SysUserMapper userMapper, SysUserRoleMapper userRoleMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResult<SysUser> page(String keyword, int page, int size) {
        long offset = (long) (page - 1) * size;
        List<SysUser> list = userMapper.selectPage(keyword, offset, size);
        long total = userMapper.countPage(keyword);
        return new PageResult<>(total, list);
    }

    public SysUser getById(Long id) {
        return userMapper.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(SysUser user) {
        if (user.getId() == null) {
            if (user.getUsername() == null || user.getUsername().isEmpty()) throw new RuntimeException("用户名不能为空");
            if (userMapper.selectByUsername(user.getUsername()) != null) throw new RuntimeException("用户名已存在");
            if (user.getPassword() == null || user.getPassword().isEmpty()) throw new RuntimeException("密码不能为空");
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            if (user.getStatus() == null) user.setStatus(1);
            userMapper.insert(user);
        } else {
            SysUser exist = userMapper.selectById(user.getId());
            if (exist == null) throw new RuntimeException("用户不存在");
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                user.setPassword(exist.getPassword());
            }
            if ("admin".equals(exist.getUsername()) && user.getStatus() != null && user.getStatus() == 0) {
                user.setStatus(1);
            }
            userMapper.updateById(user);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user != null && "admin".equals(user.getUsername())) {
            throw new RuntimeException("管理员账号不能删除");
        }
        userRoleMapper.deleteByUserId(id);
        userMapper.deleteById(id);
    }

    public List<Long> getRoleIdsByUserId(Long userId) {
        return userRoleMapper.selectRoleIdsByUserId(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void setRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteByUserId(userId);
        if (roleIds != null) {
            for (Long rid : roleIds) {
                userRoleMapper.insert(userId, rid);
            }
        }
    }
}

