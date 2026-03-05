package com.pms.config;

import com.pms.entity.SysUser;
import com.pms.mapper.SysUserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InitDataRunner implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public InitDataRunner(SysUserMapper userMapper, PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String[] args) {
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS pm_task (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "project_id BIGINT NOT NULL," +
            "parent_id BIGINT DEFAULT 0," +
            "task_code VARCHAR(64)," +
            "task_name VARCHAR(255) NOT NULL," +
            "assignee_id BIGINT," +
            "plan_start DATE," +
            "plan_end DATE," +
            "actual_start DATE," +
            "actual_end DATE," +
            "progress INT DEFAULT 0," +
            "status VARCHAR(32) DEFAULT 'TODO'," +
            "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
            ")"
        );
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS pm_task_change (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "task_id BIGINT NOT NULL," +
            "operator_id BIGINT," +
            "operator_name VARCHAR(64)," +
            "action VARCHAR(32) NOT NULL," +
            "content TEXT," +
            "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ")"
        );
        if (userMapper.selectByUsername("admin") == null) {
            SysUser admin = new SysUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRealName("管理员");
            admin.setStatus(1);
            userMapper.insert(admin);
            Long userId = admin.getId();
            if (userId != null) {
                jdbcTemplate.update("INSERT IGNORE INTO sys_user_role (user_id, role_id) SELECT ?, id FROM sys_role WHERE code = 'ADMIN' LIMIT 1", userId);
                jdbcTemplate.update("INSERT IGNORE INTO sys_role_menu (role_id, menu_id) SELECT (SELECT id FROM sys_role WHERE code = 'ADMIN' LIMIT 1), id FROM sys_menu");
            }
        }
    }
}
