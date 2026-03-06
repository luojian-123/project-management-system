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
            "CREATE TABLE IF NOT EXISTS pm_company (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "company_code VARCHAR(64)," +
            "company_name VARCHAR(128) NOT NULL," +
            "sort_order INT DEFAULT 0," +
            "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
            ")"
        );
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS pm_dept (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "company_id BIGINT NOT NULL," +
            "dept_code VARCHAR(64)," +
            "dept_name VARCHAR(128) NOT NULL," +
            "sort_order INT DEFAULT 0," +
            "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
            ")"
        );
        try { jdbcTemplate.execute("ALTER TABLE sys_role ADD COLUMN dept_id BIGINT NULL"); } catch (Exception ignored) {}
        try { jdbcTemplate.execute("ALTER TABLE sys_role ADD COLUMN sort_order INT DEFAULT 0"); } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS sys_role_dept (role_id BIGINT NOT NULL, dept_id BIGINT NOT NULL, PRIMARY KEY (role_id, dept_id))"
            );
            jdbcTemplate.update(
                "INSERT IGNORE INTO sys_role_dept (role_id, dept_id) SELECT id, dept_id FROM sys_role WHERE dept_id IS NOT NULL"
            );
        } catch (Exception ignored) {}

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
        // 初始化系统角色「财务」：对成本管理有增删改查权限（拥有成本管理菜单）
        try {
            Integer financeExists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_role WHERE code = 'FINANCE'", Integer.class);
            if (financeExists == null || financeExists == 0) {
                jdbcTemplate.update("INSERT INTO sys_role (code, name, status, sort_order) VALUES ('FINANCE', '财务', 1, 10)");
            }
            Integer costMenuExists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_menu WHERE path = '/cost'", Integer.class);
            if (costMenuExists == null || costMenuExists == 0) {
                jdbcTemplate.update("INSERT INTO sys_menu (parent_id, name, path, component, permission, type, sort_order, icon, status) VALUES (0, '成本管理', '/cost', NULL, NULL, 1, 3, 'Money', 1)");
            }
            jdbcTemplate.update(
                "INSERT IGNORE INTO sys_role_menu (role_id, menu_id) SELECT r.id, m.id FROM sys_role r, sys_menu m WHERE r.code = 'FINANCE' AND m.path = '/cost'"
            );
        } catch (Exception e) {
            // 表或数据可能尚未就绪，忽略
        }
    }
}
