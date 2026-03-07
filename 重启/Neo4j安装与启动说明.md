# Neo4j 安装与启动说明（安装路径：D:\图数据库）

## 1. 安装并启动 Neo4j 5.x（安装地址：D:\图数据库）

### 方式一：使用脚本（推荐）

在 `重启` 目录下打开 PowerShell，执行：

```powershell
.\install-neo4j.ps1
```

脚本会：

- 创建目录 `D:\图数据库`（若不存在）
- 下载 Neo4j 5.15.0 Windows 版并解压到 `D:\图数据库\neo4j-community-5.15.0`
- 询问是否立即启动 Neo4j 控制台

之后每次启动 Neo4j，可执行：

```powershell
.\start-neo4j.ps1
```

### 方式二：手动安装

1. 从 [Neo4j 部署中心](https://neo4j.com/deployment-center/) 下载 **Neo4j 5.x Community** 的 **Windows (zip)**。
2. 解压到 `D:\图数据库`，得到例如 `D:\图数据库\neo4j-community-5.15.0`。
3. 在命令行中进入该目录的 `bin`，执行：
   ```cmd
   D:\图数据库\neo4j-community-5.15.0\bin\neo4j.bat console
   ```

### 首次启动

- 首次运行会要求设置 **neo4j** 用户密码，请设置为 **neo4j123**（与后端配置一致），或之后在 `application.yml` / 环境变量中改为你设置的密码。

---

## 2. 确认 backend/application.yml 中的 Neo4j 配置

以下配置需与 Neo4j 实际一致：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `neo4j.uri` | `bolt://localhost:7687` | Bolt 端口，本机安装一般为该值 |
| `neo4j.username` | `neo4j` | 默认用户名 |
| `neo4j.password` | `${NEO4J_PASSWORD:neo4j123}` | 默认密码 neo4j123，可通过环境变量 `NEO4J_PASSWORD` 覆盖 |

当前 `backend/application.yml` 已按上述配置写好，无需修改即可连接本机 Neo4j（密码为 neo4j123 时）。

---

## 3. 启动后端与前端

1. **先保持 Neo4j 运行**（上述控制台窗口不关闭，或以后台服务方式运行）。
2. 启动后端：在项目根目录或 `backend` 下执行 `mvn spring-boot:run`，或使用 `重启\deploy-local.ps1` 一键部署。
3. 首次启动后端时会自动：
   - 在 Neo4j 中创建所需约束；
   - 初始化管理员账号 **admin / 123456**、ADMIN 角色及默认菜单。
4. 前端无需改代码，照常访问（如 http://localhost:5173），使用 admin/123456 登录即可。

---

## 安装路径汇总

- **Neo4j 安装根目录**：`D:\图数据库`
- **Neo4j 解压目录示例**：`D:\图数据库\neo4j-community-5.15.0`
- **启动脚本**：`D:\图数据库\neo4j-community-5.15.0\bin\neo4j.bat console`
