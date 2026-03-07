# 项目管理系统 - MySQL 到 Neo4j 数据迁移说明

将现有 MySQL（pms 库）中的业务数据迁移到 Neo4j，迁移后后端**仅连接 Neo4j，不再依赖 MySQL**。  
**说明**：项目默认已移除 MySQL 依赖；仅在需要“从 MySQL 再做一次迁移”时，才需启用迁移 profile 并保证 MySQL 可用。

---

## 一、前置条件

1. **MySQL**：已安装并运行，且 `pms` 库中已有需要迁移的数据（用户、角色、菜单、组织、项目、任务、问题、风险、成本、变更、待办等）。
2. **Neo4j**：已安装并运行（如 `D:\neo4j\neo4j-community-5.15.0\bin\neo4j.bat console`），默认 Bolt 端口 7687。
3. **本机**：已安装 Java 17+、Maven，可编译并运行后端工程。

---

## 二、迁移前注意

- 迁移会**先清空 Neo4j 中当前所有节点与关系**，再将 MySQL 数据写入。若 Neo4j 中已有重要数据，请先备份。
- 迁移**不会**修改 MySQL 数据，仅读取。
- 建议在**业务低峰**执行，避免迁移过程中 MySQL 数据被大量修改。

---

## 三、运行迁移

### 1. 使用默认连接信息（本机）

MySQL 默认：`jdbc:mysql://localhost:3306/pms`，用户 `root`，密码 `12345678`。  
Neo4j 默认：`bolt://localhost:7687`，用户 `neo4j`，密码 `neo4j123`。

在 `backend` 目录执行（需先启用 `migration` profile，以引入 MySQL 驱动）：

```bash
cd D:\PMS系统代码\backend
mvn -Pmigration compile exec:java -Dexec.mainClass="com.pms.migration.MySqlToNeo4jMigration"
```

### 2. 使用环境变量覆盖连接信息

若 MySQL 或 Neo4j 的地址/账号/密码与默认不同，可设置环境变量后再执行上述命令：

| 环境变量 | 含义 | 默认值 |
|----------|------|--------|
| `MYSQL_JDBC_URL` | MySQL JDBC 地址 | `jdbc:mysql://localhost:3306/pms?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false` |
| `MYSQL_USER` | MySQL 用户名 | `root` |
| `MYSQL_PASSWORD` | MySQL 密码 | `12345678` |
| `NEO4J_URI` | Neo4j Bolt 地址 | `bolt://localhost:7687` |
| `NEO4J_USER` | Neo4j 用户名 | `neo4j` |
| `NEO4J_PASSWORD` | Neo4j 密码 | `neo4j123` |

**Windows PowerShell 示例：**

```powershell
$env:MYSQL_PASSWORD = "你的MySQL密码"
$env:NEO4J_PASSWORD = "你的Neo4j密码"
cd D:\PMS系统代码\backend
mvn compile exec:java -Dexec.mainClass="com.pms.migration.MySqlToNeo4jMigration"
```

---

## 四、迁移内容概览

| 顺序 | MySQL 表 | Neo4j 节点/关系 |
|------|----------|------------------|
| 1 | pm_company | 节点 Label: Company |
| 2 | pm_dept | 节点 Label: Dept，关系 BELONGS_TO_COMPANY → Company |
| 3 | sys_role | 节点 Label: Role |
| 4 | sys_menu | 节点 Label: Menu |
| 5 | sys_user | 节点 Label: User |
| 6 | sys_user_role | 关系 User -[:HAS_ROLE]-> Role |
| 7 | sys_role_menu | 关系 Role -[:HAS_MENU]-> Menu |
| 8 | sys_role_dept | 关系 Role -[:BELONGS_TO_DEPT]-> Dept（表存在时） |
| 9 | pm_project | 节点 Label: Project |
| 10 | pm_task | 节点 Label: Task |
| 11 | pm_task_change | 节点 Label: TaskChange（表存在时） |
| 12 | pm_issue | 节点 Label: Issue |
| 13 | pm_risk | 节点 Label: Risk |
| 14 | pm_cost | 节点 Label: Cost |
| 15 | pm_change | 节点 Label: Change |
| 16 | pm_todo | 节点 Label: Todo |
| - | - | 最后初始化各 Label 的 Sequence，供后续插入使用 |

主键 `id` 会原样写入 Neo4j 节点属性，保证与现有业务一致。

---

## 五、迁移后

1. 确认 Neo4j 中已有对应节点与关系（可用 Neo4j Browser 或 `cypher-shell` 查询）。
2. 启动后端（只连 Neo4j），用原 MySQL 中的账号（如 admin）登录验证。
3. 确认无误后，可停止 MySQL 或不再将后端指向 MySQL。

---

## 六、常见问题

- **报错 “Unknown database 'pms'”**：请先在 MySQL 中执行 `CREATE DATABASE pms;` 并建表、导入数据后再迁移。
- **报错 “Connection refused” 到 Neo4j**：请确认 Neo4j 已启动且 Bolt 端口（默认 7687）可访问。
- **某表不存在被跳过**：如 `sys_role_dept`、`pm_task_change` 在库中不存在，迁移会跳过并打日志，不影响其他表。
