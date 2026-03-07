package com.pms.config;

import com.pms.entity.SysMenu;
import com.pms.entity.SysRole;
import com.pms.entity.SysUser;
import com.pms.repository.*;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Neo4j 图库初始化：创建约束/索引，并写入默认管理员、角色与菜单。
 */
@Component
public class InitDataRunner implements CommandLineRunner {

    private final Driver neo4jDriver;
    private final PasswordEncoder passwordEncoder;
    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final SysMenuRepository menuRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final SysRoleMenuRepository roleMenuRepository;
    private final SysRoleDeptRepository roleDeptRepository;
    private final com.pms.service.OrgBootstrapService orgBootstrapService;

    public InitDataRunner(Driver neo4jDriver,
                          PasswordEncoder passwordEncoder,
                          SysUserRepository userRepository,
                          SysRoleRepository roleRepository,
                          SysMenuRepository menuRepository,
                          SysUserRoleRepository userRoleRepository,
                          SysRoleMenuRepository roleMenuRepository,
                          SysRoleDeptRepository roleDeptRepository,
                          com.pms.service.OrgBootstrapService orgBootstrapService) {
        this.neo4jDriver = neo4jDriver;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleMenuRepository = roleMenuRepository;
        this.roleDeptRepository = roleDeptRepository;
        this.orgBootstrapService = orgBootstrapService;
    }

    @Override
    public void run(String[] args) {
        try (Session session = neo4jDriver.session()) {
            // 创建唯一约束，便于按 username / code / path 查询
            runIgnoreFail(session, "CREATE CONSTRAINT user_username IF NOT EXISTS FOR (u:User) REQUIRE u.username IS UNIQUE");
            runIgnoreFail(session, "CREATE CONSTRAINT role_code IF NOT EXISTS FOR (r:Role) REQUIRE r.code IS UNIQUE");
        }
        initAdminAndMenus();
        orgBootstrapService.ensureSystemCompanyExists();
        initFinanceRole();
    }

    private static void runIgnoreFail(Session session, String cypher) {
        try {
            session.run(cypher);
        } catch (Exception ignored) {
            // 约束已存在或版本不支持 IF NOT EXISTS 时忽略
        }
    }

    private void initAdminAndMenus() {
        if (userRepository.selectByUsername("admin") != null) {
            return;
        }
        // 1) 创建 ADMIN 角色（先于用户，保证存在）
        SysRole adminRole = new SysRole();
        adminRole.setCode("ADMIN");
        adminRole.setName("管理员");
        adminRole.setStatus(1);
        adminRole.setSortOrder(0);
        roleRepository.insert(adminRole);
        Long adminRoleId = adminRole.getId();

        // 2) 创建 admin 用户
        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setRealName("管理员");
        admin.setStatus(1);
        userRepository.insert(admin);
        Long adminUserId = admin.getId();

        // 3) 用户绑定 ADMIN 角色
        if (adminUserId != null && adminRoleId != null) {
            userRoleRepository.insert(adminUserId, adminRoleId);
        }

        // 4) 默认菜单（与前端路由一致）
        String[][] menus = {
            {"首页", "/", null, "1", "0"},
            {"组织管理", "/org", null, "1", "1"},
            {"项目管理", "/project", null, "1", "2"},
            {"成本管理", "/cost", null, "1", "3"},
            {"问题管理", "/issue", null, "1", "4"},
            {"风险管理", "/risk", null, "1", "5"},
            {"变更管理", "/change", null, "1", "6"},
            {"待办任务", "/todo", null, "1", "7"},
        };
        for (String[] m : menus) {
            SysMenu menu = new SysMenu();
            menu.setName(m[0]);
            menu.setPath(m[1]);
            menu.setComponent(m[2]);
            menu.setType(m[3] != null ? Integer.parseInt(m[3]) : 1);
            menu.setSortOrder(m[4] != null ? Integer.parseInt(m[4]) : 0);
            menu.setStatus(1);
            menuRepository.insert(menu);
            if (adminRoleId != null && menu.getId() != null) {
                roleMenuRepository.insert(adminRoleId, menu.getId());
            }
        }
    }

    private void initFinanceRole() {
        List<SysRole> roles = roleRepository.selectList();
        boolean hasFinance = roles.stream().anyMatch(r -> "FINANCE".equals(r.getCode()));
        if (hasFinance) return;

        SysRole finance = new SysRole();
        finance.setCode("FINANCE");
        finance.setName("财务");
        finance.setStatus(1);
        finance.setSortOrder(10);
        roleRepository.insert(finance);
        Long financeRoleId = finance.getId();

        List<SysMenu> allMenus = menuRepository.selectAll();
        SysMenu costMenu = allMenus.stream().filter(m -> "/cost".equals(m.getPath())).findFirst().orElse(null);
        if (financeRoleId != null && costMenu != null && costMenu.getId() != null) {
            roleMenuRepository.insert(financeRoleId, costMenu.getId());
        }
    }
}
