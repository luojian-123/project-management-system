# 项目版本管理说明

## 一、初始化 Git 仓库（仅需一次）

在项目根目录 `project-management-system` 下打开终端，执行：

```bash
cd C:\Users\lj\project-management-system
git init
```

## 二、日常提交

```bash
# 查看修改
git status

# 添加要纳入版本的文件（全部）
git add .

# 或只添加部分
git add frontend/src
git add backend/src

# 提交到本地仓库，并写清本次改动
git commit -m "描述本次修改，例如：全局中文与思源黑体、操作改为下拉、名称可跳详情"
```

## 三、版本号管理（推荐）

用**标签（tag）**标记可发布版本，便于回溯和发布：

```bash
# 打标签（语义化版本：主版本.次版本.修订号）
git tag -a v1.0.0 -m "首次版本：登录、项目管理、待办、成本/问题/变更/风险、系统配置、思源黑体与全局中文"

# 查看所有标签
git tag -l

# 之后每次发版可递增，例如
git tag -a v1.1.0 -m "操作下拉、名称跳转详情、仅思源黑体"
```

## 四、查看与回退

```bash
# 查看提交历史
git log --oneline

# 回退到某个提交（保留工作区修改）
git reset --soft <提交hash>

# 回退到某个提交（丢弃之后所有修改，慎用）
git reset --hard <提交hash>

# 回到某个标签对应的版本
git checkout v1.0.0
```

## 五、部署到远程（GitHub / Gitee）

### 1. 在网站上创建仓库

- **GitHub**：https://github.com/new → 仓库名如 `project-management-system`，不勾选「Initialize with README」
- **Gitee**：https://gitee.com/projects/new → 同上

### 2. 关联并推送

在项目根目录执行（把 `<远程仓库地址>` 换成你的实际地址）：

```bash
# 添加远程仓库
git remote add origin <远程仓库地址>
# 示例：git remote add origin https://github.com/你的用户名/project-management-system.git

# 当前分支为 master，首次推送并设置上游
git push -u origin master

# 若远程默认分支是 main，可先改本地分支名再推送：
# git branch -M main
# git push -u origin main
```

### 3. 之后日常推送

```bash
git add .
git commit -m "本次修改说明"
git push
```

## 六、.gitignore 说明

根目录已添加 `.gitignore`，会忽略：

- `frontend/node_modules/`、`frontend/dist/`
- `backend/target/`
- 各类环境变量、日志、IDE 配置等

这样提交时不会把依赖和构建产物纳入版本，仓库更干净。
