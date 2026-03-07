package com.pms.migration;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 将 MySQL（pms 库）数据迁移到 Neo4j。
 * 运行前：MySQL 与 Neo4j 均需启动；可通过环境变量覆盖连接信息。
 * 运行：mvn compile exec:java -Dexec.mainClass="com.pms.migration.MySqlToNeo4jMigration"
 */
public class MySqlToNeo4jMigration {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter D = DateTimeFormatter.ISO_LOCAL_DATE;

    private final String mysqlUrl;
    private final String mysqlUser;
    private final String mysqlPassword;
    private final String neo4jUri;
    private final String neo4jUser;
    private final String neo4jPassword;

    public MySqlToNeo4jMigration() {
        this.mysqlUrl = System.getenv().getOrDefault("MYSQL_JDBC_URL",
            "jdbc:mysql://localhost:3306/pms?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false");
        this.mysqlUser = System.getenv().getOrDefault("MYSQL_USER", "root");
        this.mysqlPassword = System.getenv().getOrDefault("MYSQL_PASSWORD", "12345678");
        this.neo4jUri = System.getenv().getOrDefault("NEO4J_URI", "bolt://localhost:7687");
        this.neo4jUser = System.getenv().getOrDefault("NEO4J_USER", "neo4j");
        this.neo4jPassword = System.getenv().getOrDefault("NEO4J_PASSWORD", "neo4j123");
    }

    public static void main(String[] args) {
        MySqlToNeo4jMigration m = new MySqlToNeo4jMigration();
        m.run();
    }

    public void run() {
        System.out.println("=== MySQL -> Neo4j 数据迁移 ===");
        System.out.println("MySQL: " + mysqlUrl);
        System.out.println("Neo4j: " + neo4jUri);

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPassword);
             org.neo4j.driver.Driver neo4j = GraphDatabase.driver(neo4jUri, AuthTokens.basic(neo4jUser, neo4jPassword))) {

            try (Session session = neo4j.session()) {
                // 清空现有图数据（按依赖顺序删除）
                clearNeo4j(session);
            }

            migrateCompany(mysql, neo4j);
            migrateDept(mysql, neo4j);
            migrateRole(mysql, neo4j);
            migrateMenu(mysql, neo4j);
            migrateUser(mysql, neo4j);
            migrateUserRole(mysql, neo4j);
            migrateRoleMenu(mysql, neo4j);
            migrateRoleDept(mysql, neo4j);
            migrateProject(mysql, neo4j);
            migrateTask(mysql, neo4j);
            migrateTaskChange(mysql, neo4j);
            migrateIssue(mysql, neo4j);
            migrateRisk(mysql, neo4j);
            migrateCost(mysql, neo4j);
            migrateChange(mysql, neo4j);
            migrateTodo(mysql, neo4j);

            initSequences(neo4j);

            System.out.println("=== 迁移完成 ===");
        } catch (Exception e) {
            System.err.println("迁移失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void clearNeo4j(Session session) {
        System.out.println("清空 Neo4j 现有数据...");
        session.run("MATCH (n) DETACH DELETE n");
    }

    private static String str(ResultSet rs, String col) throws SQLException {
        String s = null;
        try { s = rs.getString(col); } catch (SQLException e) { /* column may not exist */ }
        return (s == null || s.isEmpty()) ? null : s;
    }

    private static Long lng(ResultSet rs, String col) throws SQLException {
        try { long v = rs.getLong(col); return rs.wasNull() ? null : v; } catch (SQLException e) { return null; }
    }

    private static Integer int_(ResultSet rs, String col) throws SQLException {
        try { int v = rs.getInt(col); return rs.wasNull() ? null : v; } catch (SQLException e) { return null; }
    }

    private static String dateStr(java.sql.Date d) {
        if (d == null) return null;
        return d.toLocalDate().format(D);
    }

    private static String dateStr(java.sql.Timestamp d) {
        if (d == null) return null;
        return d.toLocalDateTime().format(DT);
    }

    private void migrateCompany(Connection mysql, org.neo4j.driver.Driver neo4j) throws SQLException {
        System.out.println("迁移 pm_company...");
        String q = "SELECT id, company_code, company_name, sort_order, created_at, updated_at FROM pm_company";
        try (Statement st = mysql.createStatement(); ResultSet rs = st.executeQuery(q);
             Session s = neo4j.session()) {
            while (rs.next()) {
                long id = rs.getLong("id");
                Map<String, Object> p = new HashMap<>();
                p.put("id", id);
                p.put("companyCode", str(rs, "company_code"));
                p.put("companyName", str(rs, "company_name"));
                p.put("sortOrder", int_(rs, "sort_order") != null ? int_(rs, "sort_order") : 0);
                run(s, "CREATE (n:Company $p)", Map.of("p", p));
            }
        }
    }

    private void migrateDept(Connection mysql, org.neo4j.driver.Driver neo4j) throws SQLException {
        System.out.println("迁移 pm_dept...");
        String q = "SELECT id, company_id, dept_code, dept_name, sort_order, created_at, updated_at FROM pm_dept";
        try (Statement st = mysql.createStatement(); ResultSet rs = st.executeQuery(q);
             Session s = neo4j.session()) {
            while (rs.next()) {
                long id = rs.getLong("id");
                long companyId = rs.getLong("company_id");
                Map<String, Object> p = new HashMap<>();
                p.put("id", id);
                p.put("deptCode", str(rs, "dept_code"));
                p.put("deptName", str(rs, "dept_name"));
                p.put("sortOrder", int_(rs, "sort_order") != null ? int_(rs, "sort_order") : 0);
                run(s, "MATCH (c:Company {id: $cid}) CREATE (n:Dept $p)-[:BELONGS_TO_COMPANY]->(c)", Map.of("cid", companyId, "p", p));
            }
        }
    }

    private void migrateRole(Connection mysql, org.neo4j.driver.Driver neo4j) throws SQLException {
        System.out.println("迁移 sys_role...");
        String q = "SELECT id, code, name, status, dept_id, sort_order FROM sys_role";
        try (Statement st = mysql.createStatement(); ResultSet rs = st.executeQuery(q);
             Session s = neo4j.session()) {
            while (rs.next()) {
                Map<String, Object> p = new HashMap<>();
                p.put("id", rs.getLong("id"));
                p.put("code", str(rs, "code"));
                p.put("name", str(rs, "name"));
                p.put("status", int_(rs, "status") != null ? int_(rs, "status") : 1);
                p.put("sortOrder", int_(rs, "sort_order") != null ? int_(rs, "sort_order") : 0);
                run(s, "CREATE (n:Role $p)", Map.of("p", p));
            }
        }
    }

    private void migrateMenu(Connection mysql, org.neo4j.driver.Driver neo4j) throws SQLException {
        System.out.println("迁移 sys_menu...");
        String q = "SELECT id, parent_id, name, path, component, permission, type, sort_order, icon, status FROM sys_menu";
        try (Statement st = mysql.createStatement(); ResultSet rs = st.executeQuery(q);
             Session s = neo4j.session()) {
            while (rs.next()) {
                Map<String, Object> p = new HashMap<>();
                p.put("id", rs.getLong("id"));
                p.put("parentId", lng(rs, "parent_id") != null ? lng(rs, "parent_id") : 0L);
                p.put("name", str(rs, "name"));
                p.put("path", str(rs, "path"));
                p.put("component", str(rs, "component"));
                p.put("permission", str(rs, "permission"));
                p.put("type", int_(rs, "type") != null ? int_(rs, "type") : 1);
                p.put("sortOrder", int_(rs, "sort_order") != null ? int_(rs, "sort_order") : 0);
                p.put("icon", str(rs, "icon"));
                p.put("status", int_(rs, "status") != null ? int_(rs, "status") : 1);
                run(s, "CREATE (n:Menu $p)", Map.of("p", p));
            }
        }
    }

    private void migrateUser(Connection mysql, org.neo4j.driver.Driver neo4j) throws SQLException {
        System.out.println("迁移 sys_user...");
        String q = "SELECT id, username, password, real_name, email, phone, status, created_at, updated_at FROM sys_user";
        try (Statement st = mysql.createStatement(); ResultSet rs = st.executeQuery(q);
             Session s = neo4j.session()) {
            while (rs.next()) {
                Map<String, Object> p = new HashMap<>();
                p.put("id", rs.getLong("id"));
                p.put("username", str(rs, "username"));
                p.put("password", str(rs, "password"));
                p.put("realName", str(rs, "real_name"));
                p.put("email", str(rs, "email"));
                p.put("phone", str(rs, "phone"));
                p.put("status", int_(rs, "status") != null ? int_(rs, "status") : 1);
                run(s, "CREATE (n:User $p)", Map.of("p", p));
            }
        }
    }

    private void migrateUserRole(Connection mysql, org.neo4j.driver.Driver neo4j) throws SQLException {
        System.out.println("迁移 sys_user_role...");
        migrateRelation(mysql, neo4j, "sys_user_role", "user_id", "role_id", "User", "Role", "HAS_ROLE");
    }

    private void migrateRoleMenu(Connection mysql, org.neo4j.driver.Driver neo4j) throws SQLException {
        System.out.println("迁移 sys_role_menu...");
        migrateRelation(mysql, neo4j, "sys_role_menu", "role_id", "menu_id", "Role", "Menu", "HAS_MENU");
    }

    private void migrateRoleDept(Connection mysql, org.neo4j.driver.Driver neo4j) throws SQLException {
        System.out.println("迁移 sys_role_dept...");
        try {
            migrateRelation(mysql, neo4j, "sys_role_dept", "role_id", "dept_id", "Role", "Dept", "BELONGS_TO_DEPT");
        } catch (SQLException e) {
            if (!e.getMessage().contains("doesn't exist")) throw e;
            System.out.println("  (表 sys_role_dept 不存在，跳过)");
        }
    }

    private void migrateRelation(Connection mysql, org.neo4j.driver.Driver neo4j, String table, String fromCol, String toCol,
                                 String fromLabel, String toLabel, String relType) throws SQLException {
        String q = "SELECT " + fromCol + ", " + toCol + " FROM " + table;
        try (Statement st = mysql.createStatement(); ResultSet rs = st.executeQuery(q);
             Session s = neo4j.session()) {
            while (rs.next()) {
                long a = rs.getLong(fromCol);
                long b = rs.getLong(toCol);
                run(s, "MATCH (a:" + fromLabel + " {id: $a}), (b:" + toLabel + " {id: $b}) CREATE (a)-[:" + relType + "]->(b)", Map.of("a", a, "b", b));
            }
        }
    }

    private void migrateProject(Connection mysql, org.neo4j.driver.Driver neo4j) throws SQLException {
        System.out.println("迁移 pm_project...");
        String q = "SELECT id, project_code, project_name, description, owner_id, plan_start, plan_end, status, created_by, created_at, updated_at FROM pm_project";
        try (Statement st = mysql.createStatement(); ResultSet rs = st.executeQuery(q);
             Session s = neo4j.session()) {
            while (rs.next()) {
                Map<String, Object> p = new HashMap<>();
                p.put("id", rs.getLong("id"));
                p.put("projectCode", str(rs, "project_code"));
                p.put("projectName", str(rs, "project_name"));
                p.put("description", str(rs, "description"));
                p.put("ownerId", lng(rs, "owner_id") != null ? lng(rs, "owner_id") : 0L);
                p.put("planStart", dateStr(rs.getDate("plan_start")));
                p.put("planEnd", dateStr(rs.getDate("plan_end")));
                p.put("status", str(rs, "status"));
                p.put("createdBy", lng(rs, "created_by"));
                p.put("createdAt", dateStr(rs.getTimestamp("created_at")));
                p.put("updatedAt", dateStr(rs.getTimestamp("updated_at")));
                run(s, "CREATE (n:Project $p)", Map.of("p", p));
            }
        }
    }

    private void migrateTask(Connection mysql, org.neo4j.driver.Driver neo4j) throws SQLException {
        System.out.println("迁移 pm_task...");
        String q = "SELECT id, project_id, parent_id, task_code, task_name, assignee_id, plan_start, plan_end, actual_start, actual_end, progress, status, created_at, updated_at FROM pm_task";
        try (Statement st = mysql.createStatement(); ResultSet rs = st.executeQuery(q);
             Session s = neo4j.session()) {
            while (rs.next()) {
                Map<String, Object> p = new HashMap<>();
                p.put("id", rs.getLong("id"));
                p.put("projectId", lng(rs, "project_id") != null ? lng(rs, "project_id") : 0L);
                p.put("parentId", lng(rs, "parent_id") != null ? lng(rs, "parent_id") : 0L);
                p.put("taskCode", str(rs, "task_code"));
                p.put("taskName", str(rs, "task_name"));
                p.put("assigneeId", lng(rs, "assignee_id") != null ? lng(rs, "assignee_id") : 0L);
                p.put("planStart", dateStr(rs.getDate("plan_start")));
                p.put("planEnd", dateStr(rs.getDate("plan_end")));
                p.put("actualStart", dateStr(rs.getDate("actual_start")));
                p.put("actualEnd", dateStr(rs.getDate("actual_end")));
                p.put("progress", int_(rs, "progress") != null ? int_(rs, "progress") : 0);
                p.put("status", str(rs, "status"));
                p.put("createdAt", dateStr(rs.getTimestamp("created_at")));
                p.put("updatedAt", dateStr(rs.getTimestamp("updated_at")));
                run(s, "CREATE (n:Task $p)", Map.of("p", p));
            }
        }
    }

    private void migrateTaskChange(Connection mysql, org.neo4j.driver.Driver neo4j) throws SQLException {
        System.out.println("迁移 pm_task_change...");
        try {
            String q = "SELECT id, task_id, operator_id, operator_name, action, content, created_at FROM pm_task_change";
            try (Statement st = mysql.createStatement(); ResultSet rs = st.executeQuery(q);
                 Session s = neo4j.session()) {
                while (rs.next()) {
                    Map<String, Object> p = new HashMap<>();
                    p.put("id", rs.getLong("id"));
                    p.put("taskId", lng(rs, "task_id"));
                    p.put("operatorId", lng(rs, "operator_id"));
                    p.put("operatorName", str(rs, "operator_name"));
                    p.put("action", str(rs, "action"));
                    p.put("content", str(rs, "content"));
                    p.put("createdAt", dateStr(rs.getTimestamp("created_at")));
                    run(s, "CREATE (n:TaskChange $p)", Map.of("p", p));
                }
            }
        } catch (SQLException e) {
            if (!e.getMessage().contains("doesn't exist")) throw e;
            System.out.println("  (表 pm_task_change 不存在，跳过)");
        }
    }

    private void migrateIssue(Connection mysql, org.neo4j.driver.Driver neo4j) throws SQLException {
        System.out.println("迁移 pm_issue...");
        String q = "SELECT id, project_id, task_id, issue_code, title, description, severity, status, assignee_id, found_date, resolved_date, created_by, created_at, updated_at FROM pm_issue";
        try (Statement st = mysql.createStatement(); ResultSet rs = st.executeQuery(q);
             Session s = neo4j.session()) {
            while (rs.next()) {
                Map<String, Object> p = new HashMap<>();
                p.put("id", rs.getLong("id"));
                p.put("projectId", lng(rs, "project_id"));
                p.put("taskId", lng(rs, "task_id"));
                p.put("issueCode", str(rs, "issue_code"));
                p.put("title", str(rs, "title"));
                p.put("description", str(rs, "description"));
                p.put("severity", str(rs, "severity"));
                p.put("status", str(rs, "status"));
                p.put("assigneeId", lng(rs, "assignee_id"));
                p.put("foundDate", dateStr(rs.getDate("found_date")));
                p.put("resolvedDate", dateStr(rs.getDate("resolved_date")));
                p.put("createdBy", lng(rs, "created_by"));
                p.put("createdAt", dateStr(rs.getTimestamp("created_at")));
                p.put("updatedAt", dateStr(rs.getTimestamp("updated_at")));
                run(s, "CREATE (n:Issue $p)", Map.of("p", p));
            }
        }
    }

    private void migrateRisk(Connection mysql, org.neo4j.driver.Driver neo4j) throws SQLException {
        System.out.println("迁移 pm_risk...");
        String q = "SELECT id, project_id, risk_code, title, description, probability, impact, risk_level, response, owner_id, status, created_by, created_at, updated_at FROM pm_risk";
        try (Statement st = mysql.createStatement(); ResultSet rs = st.executeQuery(q);
             Session s = neo4j.session()) {
            while (rs.next()) {
                Map<String, Object> p = new HashMap<>();
                p.put("id", rs.getLong("id"));
                p.put("projectId", lng(rs, "project_id"));
                p.put("riskCode", str(rs, "risk_code"));
                p.put("title", str(rs, "title"));
                p.put("description", str(rs, "description"));
                p.put("probability", str(rs, "probability"));
                p.put("impact", str(rs, "impact"));
                p.put("riskLevel", str(rs, "risk_level"));
                p.put("response", str(rs, "response"));
                p.put("ownerId", lng(rs, "owner_id"));
                p.put("status", str(rs, "status"));
                p.put("createdBy", lng(rs, "created_by"));
                p.put("createdAt", dateStr(rs.getTimestamp("created_at")));
                p.put("updatedAt", dateStr(rs.getTimestamp("updated_at")));
                run(s, "CREATE (n:Risk $p)", Map.of("p", p));
            }
        }
    }

    private void migrateCost(Connection mysql, org.neo4j.driver.Driver neo4j) throws SQLException {
        System.out.println("迁移 pm_cost...");
        String q = "SELECT id, project_id, task_id, cost_type, budget_amount, actual_amount, occur_date, remark, created_by, created_at, updated_at FROM pm_cost";
        try (Statement st = mysql.createStatement(); ResultSet rs = st.executeQuery(q);
             Session s = neo4j.session()) {
            while (rs.next()) {
                Map<String, Object> p = new HashMap<>();
                p.put("id", rs.getLong("id"));
                p.put("projectId", lng(rs, "project_id"));
                p.put("taskId", lng(rs, "task_id"));
                p.put("costType", str(rs, "cost_type"));
                p.put("budgetAmount", rs.getBigDecimal("budget_amount") != null ? rs.getBigDecimal("budget_amount").toPlainString() : "0");
                p.put("actualAmount", rs.getBigDecimal("actual_amount") != null ? rs.getBigDecimal("actual_amount").toPlainString() : "0");
                p.put("occurDate", dateStr(rs.getDate("occur_date")));
                p.put("remark", str(rs, "remark"));
                p.put("createdBy", lng(rs, "created_by"));
                p.put("createdAt", dateStr(rs.getTimestamp("created_at")));
                p.put("updatedAt", dateStr(rs.getTimestamp("updated_at")));
                run(s, "CREATE (n:Cost $p)", Map.of("p", p));
            }
        }
    }

    private void migrateChange(Connection mysql, org.neo4j.driver.Driver neo4j) throws SQLException {
        System.out.println("迁移 pm_change...");
        String q = "SELECT id, project_id, change_no, title, description, change_type, impact_scope, status, applicant_id, apply_time, flow_id, created_at, updated_at FROM pm_change";
        try (Statement st = mysql.createStatement(); ResultSet rs = st.executeQuery(q);
             Session s = neo4j.session()) {
            while (rs.next()) {
                Map<String, Object> p = new HashMap<>();
                p.put("id", rs.getLong("id"));
                p.put("projectId", lng(rs, "project_id"));
                p.put("changeNo", str(rs, "change_no"));
                p.put("title", str(rs, "title"));
                p.put("description", str(rs, "description"));
                p.put("changeType", str(rs, "change_type"));
                p.put("impactScope", str(rs, "impact_scope"));
                p.put("status", str(rs, "status"));
                p.put("applicantId", lng(rs, "applicant_id"));
                p.put("applyTime", dateStr(rs.getTimestamp("apply_time")));
                p.put("flowId", lng(rs, "flow_id"));
                p.put("createdAt", dateStr(rs.getTimestamp("created_at")));
                p.put("updatedAt", dateStr(rs.getTimestamp("updated_at")));
                run(s, "CREATE (n:Change $p)", Map.of("p", p));
            }
        }
    }

    private void migrateTodo(Connection mysql, org.neo4j.driver.Driver neo4j) throws SQLException {
        System.out.println("迁移 pm_todo...");
        String q = "SELECT id, user_id, title, biz_type, biz_id, priority, due_date, status, created_at, updated_at FROM pm_todo";
        try (Statement st = mysql.createStatement(); ResultSet rs = st.executeQuery(q);
             Session s = neo4j.session()) {
            while (rs.next()) {
                Map<String, Object> p = new HashMap<>();
                p.put("id", rs.getLong("id"));
                p.put("userId", lng(rs, "user_id"));
                p.put("title", str(rs, "title"));
                p.put("bizType", str(rs, "biz_type"));
                p.put("bizId", lng(rs, "biz_id"));
                p.put("priority", str(rs, "priority"));
                p.put("dueDate", dateStr(rs.getDate("due_date")));
                p.put("status", str(rs, "status"));
                p.put("createdAt", dateStr(rs.getTimestamp("created_at")));
                p.put("updatedAt", dateStr(rs.getTimestamp("updated_at")));
                run(s, "CREATE (n:Todo $p)", Map.of("p", p));
            }
        }
    }

    private void run(Session s, String cypher, Map<String, Object> params) {
        if (params == null || params.isEmpty())
            s.run(cypher);
        else
            s.run(cypher, params);
    }

    private void initSequences(org.neo4j.driver.Driver neo4j) {
        System.out.println("初始化 Sequence 节点（供后续插入用）...");
        String[] labels = {"User", "Role", "Menu", "Company", "Dept", "Project", "Task", "TaskChange", "Issue", "Risk", "Cost", "Change", "Todo"};
        try (Session s = neo4j.session()) {
            for (String label : labels) {
                String name = "seq_" + label;
                Result r = s.run("MATCH (n:" + label + ") RETURN max(n.id) AS m");
                long next = 1;
                if (r.hasNext()) {
                    var v = r.single().get("m");
                    if (!v.isNull()) next = v.asLong() + 1;
                }
                s.run("MERGE (s:Sequence {name: $name}) ON CREATE SET s.nextId = $next ON MATCH SET s.nextId = $next", Map.of("name", name, "next", next));
            }
        }
    }
}
