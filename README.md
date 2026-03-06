# project-management-system

项目管理系统，包含前端与后端代码、本地一键部署脚本、重启脚本和局域网访问辅助脚本。

## 目录说明

- `frontend`：前端项目（Vite + Vue 3）
- `backend`：后端项目（Spring Boot）
- `重启`：本地一键部署、前后端重启、局域网链接生成脚本

## 本地一键部署

推荐直接使用 `重启` 目录中的 PowerShell 脚本。

### 1. 进入脚本目录

```powershell
cd "D:\PMS系统代码\重启"
```

### 2. 首次如遇执行限制

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### 3. 一键部署并启动前后端

```powershell
.\deploy-local.ps1
```

这个脚本会执行：

1. 释放 `8080` 和 `5173` 端口
2. 编译后端 `mvn clean package -DskipTests`
3. 检查并安装前端依赖 `npm install`
4. 启动后端服务
5. 启动前端服务

## 其他常用脚本

```powershell
.\restart-backend.ps1
.\restart-frontend.ps1
.\restart-all.ps1
.\生成局域网链接.ps1
```

## 访问地址

- 前端：`http://localhost:5173/`
- 后端：`http://localhost:8080/api`

## 远程仓库

- GitHub: [https://github.com/luojian-123/project-management-system](https://github.com/luojian-123/project-management-system)