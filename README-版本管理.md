# 项目版本管理说明

**本仓库路径**：`D:\PMS系统代码\代码仓`  
**Git 程序路径**：`D:\PMS系统代码\git`（使用前可将 `D:\PMS系统代码\git\bin` 加入系统 PATH，或直接用完整路径调用）

## 一、初始化（已完成）

仓库已初始化并完成首次提交与标签 `v1.0.0`。无需再次执行 `git init`。

## 二、日常提交

在 `D:\PMS系统代码\代码仓` 下打开终端。若未将 Git 加入 PATH，请使用完整路径：

```bash
cd D:\PMS系统代码\代码仓
D:\PMS系统代码\git\bin\git.exe status
```

或先设置 PATH 后直接使用 `git`：

```bash
$env:Path = "D:\PMS系统代码\git\bin;" + $env:Path
cd D:\PMS系统代码\代码仓
git status
```

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

## 五、远程备份（可选）

若使用 GitHub / Gitee / 公司 Git 服务器：

```bash
# 添加远程仓库
git remote add origin <远程仓库地址>

# 首次推送并带上标签
git push -u origin main
git push origin --tags
```

## 六、.gitignore 说明

根目录已添加 `.gitignore`，会忽略：

- `frontend/node_modules/`、`frontend/dist/`
- `backend/target/`
- 各类环境变量、日志、IDE 配置等

这样提交时不会把依赖和构建产物纳入版本，仓库更干净。
