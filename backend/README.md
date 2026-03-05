# PMS 后端

- **技术栈**：Spring Boot 3.2、MyBatis、MySQL 8、JWT、Lombok
- **JDK**：17

## 功能模块

| 模块     | 路径前缀 | 说明 |
|----------|----------|------|
| 认证     | /auth    | 登录、注册、获取当前用户信息与菜单 |
| 个人待办 | /todo    | 分页按责任人(userId)筛选，增删改查 |
| 项目管理 | /project | 分页、详情、增删改 |
| 成本     | /cost    | 分页、详情、增删改 |
| 问题     | /issue   | 分页、详情、增删改 |
| 变更     | /change  | 分页、详情、增删改 |
| 风险     | /risk    | 分页、详情、增删改 |

## 运行前准备

1. 创建数据库并执行 `../sql/schema.sql`
2. 修改 `src/main/resources/application.yml` 中的数据库连接与 JWT 配置

## 构建与运行

```bash
mvn clean package -DskipTests
java -jar target/pms-backend-1.0.0.jar
```

或直接运行：

```bash
mvn spring-boot:run
```

- 默认端口：8080，上下文路径：/api
- 首次启动会自动创建管理员账号：**admin / 123456**
