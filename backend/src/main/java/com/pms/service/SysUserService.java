package com.pms.service;

import com.pms.common.PageResult;
import com.pms.entity.SysUser;
import com.pms.repository.SysUserRepository;
import com.pms.repository.SysUserRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysUserService {

    private final SysUserRepository userRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public SysUserService(SysUserRepository userRepository, SysUserRoleRepository userRoleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResult<SysUser> page(String keyword, int page, int size) {
        long offset = (long) (page - 1) * size;
        List<SysUser> list = userRepository.selectPage(keyword, offset, size);
        long total = userRepository.countPage(keyword);
        return new PageResult<>(total, list);
    }

    public SysUser getById(Long id) {
        return userRepository.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(SysUser user) {
        if (user.getId() == null) {
            if (user.getUsername() == null || user.getUsername().isEmpty()) throw new RuntimeException("用户名不能为空");
            if (userRepository.selectByUsername(user.getUsername()) != null) throw new RuntimeException("用户名已存在");
            if (user.getPassword() == null || user.getPassword().isEmpty()) throw new RuntimeException("密码不能为空");
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            if (user.getStatus() == null) user.setStatus(1);
            userRepository.insert(user);
        } else {
            SysUser exist = userRepository.selectById(user.getId());
            if (exist == null) throw new RuntimeException("用户不存在");
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                user.setPassword(exist.getPassword());
            }
            if ("admin".equals(exist.getUsername()) && user.getStatus() != null && user.getStatus() == 0) {
                user.setStatus(1);
            }
            userRepository.updateById(user);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        SysUser user = userRepository.selectById(id);
        if (user != null && "admin".equals(user.getUsername())) {
            throw new RuntimeException("管理员账号不能删除");
        }
        userRoleRepository.deleteByUserId(id);
        userRepository.deleteById(id);
    }

    public List<Long> getRoleIdsByUserId(Long userId) {
        return userRoleRepository.selectRoleIdsByUserId(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void setRoles(Long userId, List<Long> roleIds) {
        userRoleRepository.deleteByUserId(userId);
        if (roleIds != null) {
            for (Long rid : roleIds) {
                userRoleRepository.insert(userId, rid);
            }
        }
    }
}

