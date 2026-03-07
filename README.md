# project-management-system

项目管理系统，包含前端、后端、本地一键部署脚本、前后端重启脚本，以及局域网访问辅助脚本。

## 项目简介

本项目用于项目管理场景，当前代码仓库已包含：

- 前端页面与交互逻辑
- 后端接口与权限控制
- 组织管理、任务、问题、风险、成本、变更、待办等模块
- 本地开发 / 本地部署所需脚本
- 局域网访问辅助脚本

## 技术栈

### 前端

- Vue 3
- Vite
- Element Plus
- Pinia
- Vue Router

### 后端

- Spring Boot
- **Neo4j 图数据库**（替代 MySQL + MyBatis，支撑约 1000 人并发）
- JWT / Spring Security

### 脚本与运行环境

- PowerShell
- Node.js / npm
- Java
- Maven

## 目录说明

- `frontend`：前端项目（Vite + Vue 3）
- `backend`：后端项目（Spring Boot）
- `重启`：一键部署、重启脚本、局域网访问脚本
- `sql`：数据库结构与补丁脚本
- `docs`：项目相关说明文档

## 环境要求

建议本机具备以下环境：

- Node.js 18+
- npm 9+
- Java 17+
- Maven 3.8+
- **Neo4j 5.x**（图数据库，后端唯一数据源）
- Windows PowerShell

## 后端启动前：Neo4j

后端已切换为 **Neo4j 图数据库**，不再使用 MySQL。启动后端前请：

1. **安装并启动 Neo4j 5.x**（安装地址：**D:\图数据库**）
   - 推荐：在 `重启` 目录执行 `.\install-neo4j.ps1`，将自动下载并解压到 `D:\图数据库`，之后可用 `.\start-neo4j.ps1` 启动。
   - 详细步骤见 **`重启/Neo4j安装与启动说明.md`**。
   - 默认 Bolt 端口 `7687`。
2. **确认 `backend/application.yml`** 中 Neo4j 与实例一致：
   - `neo4j.uri`: `bolt://localhost:7687`
   - `neo4j.username`: `neo4j`
   - `neo4j.password`: 默认 `neo4j123`（或通过环境变量 `NEO4J_PASSWORD` 覆盖）。
3. **启动后端**：首次启动会自动建约束并初始化管理员账号（**admin / 123456**）、ADMIN 角色及默认菜单；**前端无需改代码**，照常访问。

千人并发架构与配置说明见：**`docs/图数据库与千人并发方案.md`**。

### 从 MySQL 迁移数据到 Neo4j

若曾有数据在 MySQL（pms 库）中并需迁到 Neo4j，可做**一次性迁移**：先启动 MySQL 与 Neo4j，在 `backend` 目录执行（使用 `-Pmigration` 才会引入 MySQL 驱动）：

```bash
mvn -Pmigration compile exec:java -Dexec.mainClass="com.pms.migration.MySqlToNeo4jMigration"
```

日常运行与构建**不需要 MySQL**，仅需 Neo4j。连接信息可通过环境变量覆盖，详见 **`docs/数据迁移-MySQL到Neo4j.md`**。

## 本地启动方式

推荐优先使用 `重启` 目录中的 PowerShell 脚本。

### 方式一：一键部署并启动前后端

进入脚本目录：

```powershell
cd "D:\PMS系统代码\重启"
```

首次如遇脚本执行限制，可先执行：

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

执行一键部署脚本：

```powershell
.\deploy-local.ps1
```

该脚本会自动完成：

1. 释放 `8080` 和 `5173` 端口
2. 构建后端 `mvn clean package -DskipTests`
3. 检查前端依赖，如无 `node_modules` 则自动执行 `npm install`
4. 启动后端服务
5. 启动前端服务

### 方式二：分别启动前后端

#### 1. 启动后端

```powershell
cd "D:\PMS系统代码\backend"
mvn clean package -DskipTests
java -jar target\pms-backend-1.0.0.jar
```

#### 2. 启动前端

```powershell
cd "D:\PMS系统代码\frontend"
npm install
npm run dev
```

## 重启脚本说明

进入脚本目录：

```powershell
cd "D:\PMS系统代码\重启"
```

可使用以下脚本：

```powershell
.\deploy-local.ps1
.\restart-backend.ps1
.\restart-frontend.ps1
.\restart-all.ps1
.\生成局域网链接.ps1
```

脚本说明：

- `deploy-local.ps1`：本地一键部署并启动前后端
- `restart-backend.ps1`：仅重启后端
- `restart-frontend.ps1`：仅重启前端
- `restart-all.ps1`：同时重启前后端
- `生成局域网链接.ps1`：生成局域网访问地址，并复制前端访问地址到剪贴板

## 本地访问方式

服务启动后，默认访问地址如下：

- 前端页面：`http://localhost:5173/`
- 后端接口：`http://localhost:8080/api`

## 局域网访问方式

确保前后端已经启动后，在 `重启` 目录执行：

```powershell
.\生成局域网链接.ps1
```

该脚本会自动：

- 获取当前机器局域网 IP
- 生成前端局域网访问地址
- 生成后端局域网接口地址
- 自动复制前端地址到剪贴板

生成结果格式如下：

- 前端：`http://你的局域网IP:5173/`
- 后端：`http://你的局域网IP:8080/api`

**同局域网如何登录：**

1. **在运行本系统的电脑上**：先启动前后端（如执行 `.\restart-all.ps1` 或分别启动后端与前端）。
2. **获取本机局域网 IP**：在 `重启` 目录执行 `.\生成局域网链接.ps1`，会输出并复制前端地址；或用 `ipconfig` 查看 IPv4 地址（如 `192.168.1.100`）。
3. **在同局域网的其他设备上**（手机、平板、同事电脑等）：打开浏览器，输入 **`http://你的本机IP:5173`**（例如 `http://192.168.1.100:5173`）。
4. **登录**：使用与本地相同的账号密码（如默认管理员 **admin / 123456**），无需额外配置。

说明：前端请求会通过运行前端的电脑转发到后端，因此只要该电脑上前后端都正常启动，同网设备用上述地址即可正常登录使用。

注意事项：

- 访问设备需要与当前电脑处于同一局域网
- Windows 防火墙如果阻止端口访问，需要放行 `5173` 和 `8080`
- 若前端可访问但接口异常，请确认后端是否已正常启动

## 常见问题

### 1. PowerShell 不允许执行脚本

执行：

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### 2. 后端启动失败，提示找不到 jar

请先执行：

```powershell
cd "D:\PMS系统代码\backend"
mvn clean package -DskipTests
```

### 3. 前端启动失败，提示缺少依赖

请先执行：

```powershell
cd "D:\PMS系统代码\frontend"
npm install
```

### 4. 端口被占用

可直接执行：

```powershell
cd "D:\PMS系统代码\重启"
.\restart-all.ps1
```

## Git 仓库

### 本地仓库

- 本地路径：`D:\PMS系统代码`

### GitHub 仓库

- 仓库地址：[https://github.com/luojian-123/project-management-system](https://github.com/luojian-123/project-management-system)
- 当前默认远程：`origin`

## 维护说明

后续代码默认同步到以下两个位置：

- 本地：`D:\PMS系统代码`
- GitHub：`https://github.com/luojian-123/project-management-system`