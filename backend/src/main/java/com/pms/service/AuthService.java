package com.pms.service;

import com.pms.entity.SysUser;
import com.pms.mapper.SysUserMapper;
import com.pms.mapper.SysMenuMapper;
import com.pms.mapper.SysRoleMapper;
import com.pms.entity.SysMenu;
import com.pms.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(SysUserMapper userMapper, SysMenuMapper menuMapper, SysRoleMapper roleMapper,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.menuMapper = menuMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, Object> login(String username, String password) {
        SysUser user = userMapper.selectByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已禁用");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        List<SysMenu> menus = menuMapper.selectByUserId(user.getId());
        List<Map<String, Object>> menuList = buildMenuTree(menus, 0L);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName() != null ? user.getRealName() : user.getUsername());
        result.put("menus", menuList);
        List<String> roleCodes = roleMapper.selectCodesByUserId(user.getId());
        result.put("roleCodes", roleCodes != null ? roleCodes : List.of());
        return result;
    }

    public Map<String, Object> getInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        List<SysMenu> menus = menuMapper.selectByUserId(userId);
        List<Map<String, Object>> menuList = buildMenuTree(menus, 0L);
        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName() != null ? user.getRealName() : user.getUsername());
        result.put("menus", menuList);
        List<String> roleCodes = roleMapper.selectCodesByUserId(userId);
        result.put("roleCodes", roleCodes != null ? roleCodes : List.of());
        return result;
    }

    public void register(String username, String password, String realName) {
        if (userMapper.selectByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName != null ? realName : username);
        user.setStatus(1);
        userMapper.insert(user);
    }

    /** 修改密码：校验原密码后更新为新密码 */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new RuntimeException("新密码至少6位");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        userMapper.updateById(user);
    }

    private List<Map<String, Object>> buildMenuTree(List<SysMenu> menus, Long parentId) {
        return menus.stream()
                .filter(m -> (m.getParentId() == null && parentId == 0L) || (m.getParentId() != null && m.getParentId().equals(parentId)))
                .map(m -> {
                    Map<String, Object> node = new HashMap<>();
                    node.put("path", m.getPath());
                    node.put("name", m.getName());
                    node.put("icon", m.getIcon());
                    List<Map<String, Object>> children = buildMenuTree(menus, m.getId());
                    if (!children.isEmpty()) node.put("children", children);
                    return node;
                })
                .collect(Collectors.toList());
    }
}
