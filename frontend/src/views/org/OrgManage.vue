<template>
  <div class="page">
    <div class="page-cards" v-draggable-cards>
      <el-card>
      <template #header>
        <span>组织树</span>
        <div class="org-card-header-actions">
          <el-button type="primary" link @click="loadTree">刷新</el-button>
          <span class="org-tree-tip">根据公司/部门/角色/用户数据自动生成，请在下方库卡片中维护</span>
        </div>
      </template>
      <el-row :gutter="24" class="org-tree-row">
        <el-col :span="8" class="org-tree-col">
          <div class="org-tree-wrap" v-loading="treeLoading" @contextmenu.prevent>
            <div class="org-tree-toolbar">
              <el-button
                v-if="treeExpandAll"
                type="primary"
                link
                size="small"
                class="org-tree-toolbar__btn"
                @click="treeExpandAll = false"
              >收纳</el-button>
              <el-button
                v-else
                type="primary"
                link
                size="small"
                class="org-tree-toolbar__btn"
                @click="treeExpandAll = true"
              >展开</el-button>
            </div>
            <div class="org-tree-content">
            <el-tree
              ref="orgTreeRef"
              :key="'tree-expand-' + treeExpandAll"
              class="org-tree"
              :data="treeData"
              node-key="nodeKey"
              :props="{ label: 'label', children: 'children' }"
              :default-expand-all="treeExpandAll"
              highlight-current
              :expand-on-click-node="false"
              indent="20"
              @node-click="onNodeClick"
            >
              <template #default="{ node, data }">
                <span class="org-tree-node">
                  <span
                    class="org-tree-node__icon-wrap"
                    :class="[
                      `org-tree-node__icon-wrap--${data.type || 'node'}`,
                      data.type === 'company' && data.label === '系统公司' ? 'org-tree-node__icon-wrap--company-system' : ''
                    ]"
                  >
                    <el-icon class="org-tree-node__icon">
                      <OfficeBuilding v-if="data.type === 'company'" />
                      <Grid v-else-if="data.type === 'dept'" />
                      <Avatar v-else-if="data.type === 'role'" />
                      <User v-else-if="data.type === 'user'" />
                      <FolderOpened v-else />
                    </el-icon>
                  </span>
                  <span class="org-tree-node__label">{{ node.label }}</span>
                </span>
              </template>
            </el-tree>
            </div>
          </div>
        </el-col>
        <el-col :span="16" class="org-tree-col">
          <div class="node-detail">
            <div class="node-detail__head">
              <span class="node-detail__title">节点详情</span>
              <div class="node-detail__tabs">
                <el-button
                  :type="detailPanelTab === 'node' ? 'primary' : 'default'"
                  size="small"
                  @click="detailPanelTab = 'node'"
                >节点详情</el-button>
                <el-button
                  v-if="currentNode?.type === 'role'"
                  :type="detailPanelTab === 'permission' ? 'primary' : 'default'"
                  size="small"
                  @click="detailPanelTab = 'permission'; loadRolePermission()"
                >权限配置</el-button>
              </div>
            </div>
            <el-empty v-if="!currentNode" description="请选择左侧节点" />
            <template v-else>
              <div v-show="detailPanelTab === 'node'" class="node-detail__body">
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="类型">{{ typeText(currentNode.type) }}</el-descriptions-item>
                  <el-descriptions-item label="ID">{{ currentNode.id }}</el-descriptions-item>
                  <el-descriptions-item label="名称">{{ currentNode.label }}</el-descriptions-item>
                </el-descriptions>
                </div>
              <div v-show="detailPanelTab === 'permission' && currentNode?.type === 'role'" class="node-detail__body" v-loading="rolePermissionLoading">
                <el-table :data="rolePermissionList" stripe max-height="100%" size="small" border>
                  <el-table-column prop="name" label="菜单名称" min-width="140" />
                  <el-table-column prop="path" label="路径" min-width="120" />
                  <el-table-column prop="assigned" label="已授权" min-width="72" align="center">
                    <template #default="{ row }">{{ row.assigned ? '是' : '否' }}</template>
                  </el-table-column>
                </el-table>
                <el-empty v-if="!rolePermissionLoading && !rolePermissionList.length" description="暂无菜单数据" />
              </div>
            </template>
          </div>
        </el-col>
      </el-row>
      <!-- 公司库/部门库/角色库/用户库 卡片 -->
      <el-row :gutter="16" class="org-lib-row">
        <el-col :xs="12" :sm="12" :md="6">
          <div class="org-lib-card org-lib-card--company" @click="openCompanyLib">
            <el-icon class="org-lib-card__icon"><OfficeBuilding /></el-icon>
            <div class="org-lib-card__num">{{ libraryCounts.company }}</div>
            <div class="org-lib-card__title">公司库</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6">
          <div class="org-lib-card org-lib-card--dept" @click="openDeptLib">
            <el-icon class="org-lib-card__icon"><Grid /></el-icon>
            <div class="org-lib-card__num">{{ libraryCounts.dept }}</div>
            <div class="org-lib-card__title">部门库</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6">
          <div class="org-lib-card org-lib-card--role" @click="openRoleLib">
            <el-icon class="org-lib-card__icon"><Avatar /></el-icon>
            <div class="org-lib-card__num">{{ libraryCounts.role }}</div>
            <div class="org-lib-card__title">角色库</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6">
          <div class="org-lib-card org-lib-card--user" @click="openUserLib">
            <el-icon class="org-lib-card__icon"><User /></el-icon>
            <div class="org-lib-card__num">{{ libraryCounts.user }}</div>
            <div class="org-lib-card__title">用户库</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
    </div>

  <!-- 公司库 抽屉 -->
  <el-drawer
    v-model="companyLibVisible"
    title="公司库"
    direction="rtl"
    size="60%"
    class="org-lib-drawer org-lib-drawer--adaptive"
  >
    <div class="org-lib-drawer__body">
      <div v-if="isAdmin" class="org-lib-drawer__toolbar">
        <el-button type="primary" size="small" @click="openCompanyForm(); companyLibVisible = false">新增公司</el-button>
      </div>
      <el-table :data="companyList" stripe max-height="100%" class="org-lib-table" border>
        <el-table-column prop="companyCode" label="公司编码" min-width="100" />
        <el-table-column label="公司名称" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.companyName || '-' }}
            <el-tag v-if="row.isSystem" type="info" size="small" style="margin-left:6px">系统</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" min-width="72" align="center" />
        <el-table-column v-if="isAdmin" label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-dropdown trigger="click" :disabled="!!row.isSystem" @command="(cmd) => { if (cmd === 'edit') { openCompanyForm(row.id); companyLibVisible = false } else if (cmd === 'del') handleCompanyLibDelete(row) }">
              <el-button type="primary" link :disabled="!!row.isSystem">操作<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit" :disabled="!!row.isSystem">编辑</el-dropdown-item>
                  <el-dropdown-item v-if="!row.isSystem" command="del" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!companyList.length" class="org-lib-drawer__empty">暂无公司数据</div>
    </div>
  </el-drawer>

  <!-- 部门库 抽屉 -->
  <el-drawer v-model="deptLibVisible" title="部门库" direction="rtl" size="60%" class="org-lib-drawer org-lib-drawer--adaptive">
    <div class="org-lib-drawer__body">
      <div v-if="isAdmin" class="org-lib-drawer__toolbar">
        <el-button type="primary" size="small" @click="openDeptForm(null, null); deptLibVisible = false">新增部门</el-button>
      </div>
      <el-table :data="deptOptionsWithCompanyName" stripe max-height="100%" class="org-lib-table" border>
        <el-table-column prop="companyName" label="所属公司" min-width="100" />
        <el-table-column prop="deptCode" label="部门编码" min-width="100" />
        <el-table-column label="部门名称" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.deptName || '-' }}
            <el-tag v-if="row.isSystem" type="info" size="small" style="margin-left:6px">系统</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" min-width="64" align="center" />
        <el-table-column v-if="isAdmin" label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-dropdown trigger="click" :disabled="!!row.isSystem" @command="(cmd) => { if (cmd === 'edit') { openDeptForm(row.id, row.companyId); deptLibVisible = false } else if (cmd === 'del') handleDeptLibDelete(row) }">
              <el-button type="primary" link :disabled="!!row.isSystem">操作<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit" :disabled="!!row.isSystem">编辑</el-dropdown-item>
                  <el-dropdown-item v-if="!row.isSystem" command="del" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!deptOptions.length" class="org-lib-drawer__empty">暂无部门数据</div>
    </div>
  </el-drawer>

  <!-- 角色库 抽屉 -->
  <el-drawer v-model="roleLibVisible" title="角色库" direction="rtl" size="60%" class="org-lib-drawer org-lib-drawer--adaptive">
    <div class="org-lib-drawer__body">
      <div v-if="isAdmin" class="org-lib-drawer__toolbar">
        <el-button type="primary" size="small" @click="openRoleForm(null, []); roleLibVisible = false">新增角色</el-button>
      </div>
      <el-table v-loading="roleLibLoading" :data="roleListData" stripe max-height="100%" class="org-lib-table" border>
        <el-table-column prop="code" label="角色编码" min-width="90" />
        <el-table-column prop="name" label="角色名称" min-width="100" />
        <el-table-column label="所属部门" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ formatRoleDepts(row.deptIds) }}</template>
        </el-table-column>
        <el-table-column label="状态" min-width="72" align="center">
          <template #default="{ row }">{{ row.status === 1 ? '启用' : '禁用' }}</template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" min-width="64" align="center" />
        <el-table-column v-if="isAdmin" label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-dropdown
              trigger="click"
              :disabled="row.code === 'ADMIN'"
              @command="(cmd) => onRoleLibCommand(cmd, row)"
            >
              <el-button type="primary" link :disabled="row.code === 'ADMIN'">操作<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit" :disabled="row.code === 'ADMIN'">编辑</el-dropdown-item>
                  <el-dropdown-item command="permission" :disabled="row.code === 'ADMIN'">权限配置</el-dropdown-item>
                  <el-dropdown-item command="del" divided :disabled="row.code === 'ADMIN'">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!roleLibLoading && !roleListData.length" class="org-lib-drawer__empty">暂无角色数据</div>
    </div>
  </el-drawer>

  <!-- 用户库 抽屉 -->
  <el-drawer v-model="userLibVisible" title="用户库" direction="rtl" size="60%" class="org-lib-drawer org-lib-drawer--adaptive">
    <div class="org-lib-drawer__body">
      <div v-if="isAdmin" class="org-lib-drawer__toolbar">
        <el-button type="primary" size="small" @click="openUserAddForm(); userLibVisible = false">新增用户</el-button>
      </div>
      <el-table v-loading="userLibLoading" :data="userLibList" stripe max-height="400" class="org-lib-table" border>
        <el-table-column prop="username" label="用户名" min-width="90" />
        <el-table-column prop="realName" label="姓名" min-width="80" />
        <el-table-column label="角色" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ formatUserRoles(row) }}</template>
        </el-table-column>
        <el-table-column label="状态" min-width="72" align="center">
          <template #default="{ row }">{{ row.status === 1 ? '启用' : '禁用' }}</template>
        </el-table-column>
        <el-table-column v-if="isAdmin" label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-dropdown
              trigger="click"
              :disabled="row.username === 'admin'"
              @command="(cmd) => { if (cmd === 'edit') { openUserEditFormById(row.id); userLibVisible = false } else if (cmd === 'del') handleUserLibDelete(row) }"
            >
              <el-button type="primary" link :disabled="row.username === 'admin'">操作<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit" :disabled="row.username === 'admin'">编辑</el-dropdown-item>
                  <el-dropdown-item command="del" divided :disabled="row.username === 'admin'">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="userLibPage"
        v-model:page-size="userLibSize"
        :total="userLibTotal"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="org-lib-drawer__pagination"
        @current-change="loadUserLibList"
        @size-change="loadUserLibList"
      />
      <div v-if="!userLibLoading && !userLibList.length" class="org-lib-drawer__empty">暂无用户数据</div>
    </div>
  </el-drawer>

  <!-- 公司 -->
  <el-dialog v-model="companyVisible" title="公司" width="520px" @close="companyRef?.resetFields()">
    <el-form ref="companyRef" :model="companyForm" :rules="companyRules" label-width="90px">
      <el-form-item label="公司编码" prop="companyCode">
        <el-input v-model="companyForm.companyCode" :disabled="!!companyForm.isSystem" placeholder="系统公司编码不可修改" />
      </el-form-item>
      <el-form-item label="公司名称" prop="companyName"><el-input v-model="companyForm.companyName" /></el-form-item>
      <el-form-item label="排序" prop="sortOrder"><el-input-number v-model="companyForm.sortOrder" :min="0" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="companyVisible=false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="submitCompany">确定</el-button>
    </template>
  </el-dialog>

  <!-- 部门 -->
  <el-dialog v-model="deptVisible" title="部门" width="520px" @close="deptRef?.resetFields()">
    <el-form ref="deptRef" :model="deptForm" :rules="deptRules" label-width="90px">
      <el-form-item label="所属公司" prop="companyId">
        <el-select v-model="deptForm.companyId" style="width:100%" placeholder="选择公司" :disabled="!!deptForm.isSystem">
          <el-option v-for="c in companyList" :key="c.id ?? c.companyCode ?? ''" :label="c.companyName || c.companyCode || ''" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="部门编码" prop="deptCode">
        <el-input v-model="deptForm.deptCode" :disabled="!!deptForm.isSystem" placeholder="系统部门编码不可修改" />
      </el-form-item>
      <el-form-item label="部门名称" prop="deptName"><el-input v-model="deptForm.deptName" /></el-form-item>
      <el-form-item label="排序" prop="sortOrder"><el-input-number v-model="deptForm.sortOrder" :min="0" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="deptVisible=false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="submitDept">确定</el-button>
    </template>
  </el-dialog>

  <!-- 角色 -->
  <el-dialog v-model="roleVisible" title="角色" width="520px" @close="roleRef?.resetFields()">
    <el-alert v-if="roleForm.code === 'ADMIN'" type="info" :closable="false" show-icon style="margin-bottom:12px">
      管理员角色默认绑定系统部门且不可取消，拥有所有权限。
    </el-alert>
    <el-form ref="roleRef" :model="roleForm" :rules="roleRules" label-width="90px">
      <el-form-item label="所属部门" prop="deptIds">
        <el-select v-model="roleForm.deptIds" multiple collapse-tags collapse-tags-tooltip style="width:100%" placeholder="可多选部门">
          <el-option v-for="d in deptOptions" :key="d.id ?? d.deptCode ?? ''" :label="d.deptName || d.deptCode || ''" :value="d.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="角色编码" prop="code"><el-input v-model="roleForm.code" /></el-form-item>
      <el-form-item label="角色名称" prop="name"><el-input v-model="roleForm.name" /></el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="roleForm.status" style="width:100%">
          <el-option :value="1" label="启用" />
          <el-option :value="0" label="禁用" />
        </el-select>
      </el-form-item>
      <el-form-item label="排序" prop="sortOrder"><el-input-number v-model="roleForm.sortOrder" :min="0" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="roleVisible=false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="submitRole">确定</el-button>
    </template>
  </el-dialog>

  <!-- 分配用户到当前角色 -->
  <el-dialog v-model="userVisible" title="新增用户（分配至本角色）" width="480px" @close="userForm.userId = null">
    <el-form label-width="90px">
      <el-form-item label="选择用户" required>
        <el-select
          v-model="userForm.userId"
          filterable
          placeholder="搜索用户名/姓名"
          style="width:100%"
          :loading="userListLoading"
          @focus="loadUserList"
        >
          <el-option
            v-for="u in userList"
            :key="u.id"
            :label="(u.realName || u.username) + (u.username ? ' (' + u.username + ')' : '')"
            :value="u.id"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="userVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" :disabled="!userForm.userId" @click="submitUserRole">确定</el-button>
    </template>
  </el-dialog>

  <!-- 编辑/新增用户 -->
  <el-dialog v-model="userEditVisible" :title="userEditForm.id ? '编辑用户' : '新增用户'" width="480px" @close="onUserEditDialogClose">
    <el-form ref="userEditRef" :model="userEditForm" :rules="userEditRules" label-width="90px">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="userEditForm.username" placeholder="用户名" :disabled="!!userEditForm.id" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="userEditForm.password" type="password" :placeholder="userEditForm.id ? '不填则保持原密码' : '请输入密码'" show-password />
      </el-form-item>
      <el-form-item label="姓名" prop="realName">
        <el-input v-model="userEditForm.realName" placeholder="姓名" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="userEditForm.email" placeholder="邮箱" />
      </el-form-item>
      <el-form-item label="手机" prop="phone">
        <el-input v-model="userEditForm.phone" placeholder="手机" />
      </el-form-item>
      <el-form-item label="角色" prop="roleIds">
        <el-select
          v-model="userEditForm.roleIds"
          multiple
          collapse-tags
          collapse-tags-tooltip
          placeholder="选择角色"
          style="width:100%"
          :loading="roleOptionsLoading"
          :disabled="userEditForm.username === 'admin'"
        >
          <el-option v-for="r in roleOptionsForUserForm" :key="r.id" :label="r.name || r.code" :value="r.id" />
        </el-select>
        <span v-if="userEditForm.username === 'admin'" class="admin-status-tip">管理员账号拥有全部权限，不可修改角色</span>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="userEditForm.status" :disabled="userEditForm.username === 'admin'">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">禁用</el-radio>
        </el-radio-group>
        <span v-if="userEditForm.username === 'admin'" class="admin-status-tip">管理员账号不能禁用</span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="userEditVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="submitUserEdit">确定</el-button>
    </template>
  </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { OfficeBuilding, Folder, FolderOpened, Key, User, Grid, Avatar, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import * as orgApi from '@/api/org'
import { roleGet, roleSave, roleUpdate, roleDelete, roleList, roleMenuIds } from '@/api/role'
import { menuTree } from '@/api/menu'
import { userPage, userGet, userSave, userUpdate, userDelete, getUserRoleIds, userAssignRoles } from '@/api/user'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.username === 'admin')

const orgTreeRef = ref(null)
const treeData = ref([])
const currentNode = ref(null)
const submitLoading = ref(false)
const treeLoading = ref(false)
/** true=展开所有层级，false=收纳只显示第一层级 */
const treeExpandAll = ref(true)
/** 右侧面板 tab：'node' 节点详情，'permission' 权限配置（仅角色节点） */
const detailPanelTab = ref('node')
const rolePermissionList = ref([])
const rolePermissionLoading = ref(false)

const companyList = ref([])
const deptOptions = ref([])

const libraryCounts = reactive({ company: 0, dept: 0, role: 0, user: 0 })
const companyLibVisible = ref(false)
const deptLibVisible = ref(false)
const roleLibVisible = ref(false)
const userLibVisible = ref(false)
const roleListData = ref([])
const roleLibLoading = ref(false)
const userLibList = ref([])
const userLibTotal = ref(0)
const userLibPage = ref(1)
const userLibSize = ref(20)
const userLibLoading = ref(false)

const deptOptionsWithCompanyName = computed(() => {
  const companies = companyList.value
  return deptOptions.value.map(d => {
    const c = companies.find(c => c.id === d.companyId)
    return { ...d, companyName: c?.companyName || c?.companyCode || '-' }
  })
})

function getDeptNameById(deptId) {
  if (deptId == null) return '-'
  const d = deptOptions.value.find(x => x.id === deptId)
  return d ? (d.deptName || d.deptCode || '-') : '-'
}

function formatRoleDepts(deptIds) {
  if (!Array.isArray(deptIds) || !deptIds.length) return '-'
  return deptIds.map(id => getDeptNameById(id)).filter(Boolean).join('、') || '-'
}

function openCompanyLib() {
  companyLibVisible.value = true
}

function openDeptLib() {
  deptLibVisible.value = true
}

async function openRoleLib() {
  roleLibVisible.value = true
  roleLibLoading.value = true
  try {
    const res = await roleList()
    roleListData.value = Array.isArray(res) ? res : (res?.data ?? res?.list ?? [])
  } catch {
    roleListData.value = []
  } finally {
    roleLibLoading.value = false
  }
}

function openUserLib() {
  userLibVisible.value = true
  loadUserLibList()
}

const userLibRoleMap = ref({})

function formatUserRoles(row) {
  if (row.roleNamesDisplay) return row.roleNamesDisplay
  if (row.roleNames && typeof row.roleNames === 'string') return row.roleNames
  if (row.roleName) return row.roleName
  if (Array.isArray(row.roles) && row.roles.length) return row.roles.map(r => r.name || r.roleName || r).join('，')
  return '-'
}

async function loadUserLibList() {
  userLibLoading.value = true
  try {
    const res = await userPage({ page: userLibPage.value, size: userLibSize.value })
    const list = res?.list ?? []
    userLibTotal.value = res?.total ?? 0
    if (list.length) {
      try {
        const roles = await roleList()
        const roleArr = Array.isArray(roles) ? roles : (roles?.data ?? roles?.list ?? [])
        roleArr.forEach(r => { userLibRoleMap.value[r.id] = r.name || r.code || '' })
      } catch {
        userLibRoleMap.value = {}
      }
      for (const u of list) {
        try {
          const ids = await getUserRoleIds(u.id)
          const arr = Array.isArray(ids) ? ids : []
          u.roleNamesDisplay = arr.length ? arr.map(id => userLibRoleMap.value[id] || id).filter(Boolean).join('，') || '-' : '-'
        } catch {
          u.roleNamesDisplay = '-'
        }
      }
    }
    userLibList.value = list
  } catch {
    userLibList.value = []
    userLibTotal.value = 0
  } finally {
    userLibLoading.value = false
  }
}

function openUserEditFormById(userId) {
  loadRoleOptionsForUserForm()
  userGet(userId).then(async (d) => {
    const u = d?.data ?? d
    if (u) {
      let roleIds = []
      try {
        const ids = await getUserRoleIds(u.id)
        roleIds = Array.isArray(ids) ? ids : []
      } catch {
        roleIds = []
      }
      Object.assign(userEditForm, { id: u.id, username: u.username ?? '', password: '', realName: u.realName ?? '', email: u.email ?? '', phone: u.phone ?? '', status: u.status ?? 1, roleIds })
      if (userEditForm.username === 'admin') userEditForm.status = 1
    }
    userEditVisible.value = true
  }).catch(() => {})
}

function onRoleLibCommand(cmd, row) {
  if (row.code === 'ADMIN') return
  if (cmd === 'edit') {
    openRoleForm(row.id, row.deptIds)
    roleLibVisible.value = false
  } else if (cmd === 'permission') {
    currentNode.value = { type: 'role', id: row.id, label: row.name || row.code }
    detailPanelTab.value = 'permission'
    loadRolePermission()
    roleLibVisible.value = false
    // 左侧树定位到该角色节点并展开
    nextTick(() => {
      orgTreeRef.value?.setCurrentKey('role_' + row.id)
      // 若当前未展开全部，先展开以便看到选中角色
      if (!treeExpandAll.value) treeExpandAll.value = true
    })
  } else if (cmd === 'del') {
    handleRoleLibDelete(row)
  }
}

async function handleCompanyLibDelete(row) {
  if (row.isSystem) {
    ElMessage.warning('系统公司不能删除')
    return
  }
  try {
    await ElMessageBox.confirm(`确定删除公司「${row.companyName || row.companyCode}」？`, '提示', { type: 'warning' })
    await orgApi.companyDelete(row.id)
    ElMessage.success('删除成功')
    companyLibVisible.value = false
    await loadTree()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.message || '删除失败')
  }
}

async function handleDeptLibDelete(row) {
  if (row.isSystem) {
    ElMessage.warning('系统部门不能删除')
    return
  }
  try {
    await ElMessageBox.confirm(`确定删除部门「${row.deptName || row.deptCode}」？`, '提示', { type: 'warning' })
    await orgApi.deptDelete(row.id)
    ElMessage.success('删除成功')
    deptLibVisible.value = false
    await loadTree()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.message || '删除失败')
  }
}

async function handleRoleLibDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除角色「${row.name || row.code}」？`, '提示', { type: 'warning' })
    await roleDelete(row.id)
    ElMessage.success('删除成功')
    roleLibVisible.value = false
    await loadTree()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.message || '删除失败')
  }
}

async function handleUserLibDelete(row) {
  if (row.username === 'admin') {
    ElMessage.warning('管理员账号不能删除')
    return
  }
  try {
    await ElMessageBox.confirm(`确定删除用户「${row.realName || row.username}」？`, '提示', { type: 'warning' })
    await userDelete(row.id)
    ElMessage.success('删除成功')
    await loadUserLibList()
    await loadTree()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.message || '删除失败')
  }
}

async function loadLibraryCounts() {
  libraryCounts.company = companyList.value.length
  libraryCounts.dept = deptOptions.value.length
  try {
    const roles = await roleList()
    const roleArr = Array.isArray(roles) ? roles : (roles?.data ?? roles?.list ?? [])
    libraryCounts.role = roleArr.length
  } catch {
    libraryCounts.role = 0
  }
  try {
    const userRes = await userPage({ page: 1, size: 1 })
    libraryCounts.user = userRes?.total ?? 0
  } catch {
    libraryCounts.user = 0
  }
}

function typeText(t) {
  return ({ company: '公司', dept: '部门', role: '角色', user: '用户' }[t] || t)
}

/** 规范树节点：保证 nodeKey、label、children 存在，避免 el-tree 报错导致白屏 */
function normalizeTree(nodes) {
  if (!nodes) return []
  const list = Array.isArray(nodes) ? nodes : (nodes?.children ?? nodes?.data ?? [])
  if (!Array.isArray(list)) return []
  return list.filter(n => n != null && typeof n === 'object').map((n, i) => ({
    ...n,
    nodeKey: n.nodeKey || (n.type != null && n.id != null ? `${n.type}_${n.id}` : `n_${i}_${Math.random().toString(36).slice(2)}`),
    label: n.label ?? n.name ?? String(n.id ?? ''),
    children: normalizeTree(n.children)
  }))
}

async function loadTree() {
  treeLoading.value = true
  try {
    const res = await orgApi.orgTree()
    const raw = Array.isArray(res) ? res : (res?.data ?? res?.children ?? [])
    treeData.value = normalizeTree(raw)
    const cl = await orgApi.companyList()
    companyList.value = (Array.isArray(cl) ? cl : []).filter(c => c != null && (c.id != null || c.companyCode != null))
    const allDepts = []
    for (const c of companyList.value) {
      try {
        const dl = await orgApi.deptList(c.id)
        allDepts.push(...(Array.isArray(dl) ? dl : []))
      } catch {
        // 单公司部门列表失败不影响其他
      }
    }
    deptOptions.value = allDepts.filter(d => d != null && (d.id != null || d.deptCode != null))
    await loadLibraryCounts()
  } catch (e) {
    treeData.value = []
    companyList.value = []
    deptOptions.value = []
    const msg = e?.message || e?.msg || '加载组织树失败，请检查网络或后端服务'
    ElMessage.error(msg)
    console.error('[OrgManage] loadTree error:', e)
  } finally {
    treeLoading.value = false
  }
}

const currentUserDetail = ref(null)

function onNodeClick(node) {
  currentNode.value = node
  detailPanelTab.value = 'node'
  if (node?.type === 'user' && node.id) {
    userGet(node.id).then((d) => {
      const u = d?.data ?? d
      currentUserDetail.value = u || null
    }).catch(() => { currentUserDetail.value = null })
  } else {
    currentUserDetail.value = null
  }
}

function flattenMenuTree(nodes, list = []) {
  if (!Array.isArray(nodes)) return list
  for (const n of nodes) {
    list.push({ id: n.id, name: n.name || n.label || '-', path: n.path || '-', assigned: false })
    if (n.children?.length) flattenMenuTree(n.children, list)
  }
  return list
}

async function loadRolePermission() {
  const roleId = currentNode.value?.id
  if (currentNode.value?.type !== 'role' || !roleId) {
    rolePermissionList.value = []
    return
  }
  rolePermissionLoading.value = true
  rolePermissionList.value = []
  try {
    const [menuIdsRes, treeRes] = await Promise.all([
      roleMenuIds(roleId),
      menuTree(true)
    ])
    const ids = Array.isArray(menuIdsRes) ? menuIdsRes : (menuIdsRes?.data ?? menuIdsRes?.list ?? [])
    const tree = Array.isArray(treeRes) ? treeRes : (treeRes?.data ?? treeRes ?? [])
    const setId = new Set(ids.map(String))
    const flat = flattenMenuTree(tree)
    flat.forEach((m) => { m.assigned = setId.has(String(m.id)) })
    rolePermissionList.value = flat
  } catch {
    rolePermissionList.value = []
  } finally {
    rolePermissionLoading.value = false
  }
}

const isCurrentUserAdmin = computed(() => currentUserDetail.value?.username === 'admin')

// 公司表单
const companyVisible = ref(false)
const companyRef = ref(null)
const companyForm = reactive({ id: null, companyCode: '', companyName: '', sortOrder: 0, isSystem: false })
const companyRules = {
  companyCode: [{ required: true, message: '请输入公司编码', trigger: 'blur' }],
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }]
}

function openCompanyForm(id) {
  Object.assign(companyForm, { id: id || null, companyCode: '', companyName: '', sortOrder: 0, isSystem: false })
  if (id) {
    orgApi.companyGet(id).then(d => Object.assign(companyForm, d || {}))
  }
  companyVisible.value = true
}

async function submitCompany() {
  try {
    await companyRef.value?.validate()
  } catch {
    ElMessage.warning('请完善必填项后再提交')
    return
  }
  submitLoading.value = true
  try {
    if (companyForm.id) await orgApi.companyUpdate(companyForm)
    else await orgApi.companySave(companyForm)
    ElMessage.success('保存成功')
    companyVisible.value = false
    await loadTree()
  } catch (e) {
    const msg = e?.response?.data?.message ?? e?.message ?? e?.msg ?? '保存失败，请检查网络或后端'
    ElMessage.error(msg)
  } finally {
    submitLoading.value = false
  }
}

// 部门表单
const deptVisible = ref(false)
const deptRef = ref(null)
const deptForm = reactive({ id: null, companyId: null, deptCode: '', deptName: '', sortOrder: 0, isSystem: false })
const deptRules = {
  companyId: [{ required: true, message: '请选择公司', trigger: 'change' }],
  deptCode: [{ required: true, message: '请输入部门编码', trigger: 'blur' }],
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

function openDeptForm(id, companyId) {
  Object.assign(deptForm, { id: id || null, companyId: companyId || null, deptCode: '', deptName: '', sortOrder: 0, isSystem: false })
  if (id) orgApi.deptGet(id).then(d => Object.assign(deptForm, d || {}))
  deptVisible.value = true
}

async function submitDept() {
  try {
    await deptRef.value?.validate()
  } catch {
    ElMessage.warning('请完善必填项后再提交')
    return
  }
  submitLoading.value = true
  try {
    if (deptForm.id) await orgApi.deptUpdate(deptForm)
    else await orgApi.deptSave(deptForm)
    ElMessage.success('保存成功')
    deptVisible.value = false
    await loadTree()
  } catch (e) {
    const msg = e?.response?.data?.message ?? e?.message ?? e?.msg ?? '保存失败，请检查网络或后端'
    ElMessage.error(msg)
  } finally {
    submitLoading.value = false
  }
}

// 角色表单
const roleVisible = ref(false)
const roleRef = ref(null)
const roleForm = reactive({ id: null, deptIds: [], code: '', name: '', status: 1, sortOrder: 0 })
const roleRules = {
  deptIds: [{ required: true, message: '请至少选择一个部门', trigger: 'change', validator: (_r, v, cb) => { if (!Array.isArray(v) || v.length === 0) cb(new Error('请至少选择一个部门')); else cb(); } }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const userVisible = ref(false)
const userForm = reactive({ userId: null })
const userList = ref([])
const userListLoading = ref(false)

const userEditVisible = ref(false)
const userEditRef = ref(null)
const userEditForm = reactive({ id: null, username: '', password: '', realName: '', email: '', phone: '', status: 1, roleIds: [] })
const roleOptionsForUserForm = ref([])
const roleOptionsLoading = ref(false)
const userEditRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
}

async function loadRoleOptionsForUserForm() {
  roleOptionsLoading.value = true
  try {
    const res = await roleList()
    roleOptionsForUserForm.value = Array.isArray(res) ? res : (res?.data ?? res?.list ?? [])
  } catch {
    roleOptionsForUserForm.value = []
  } finally {
    roleOptionsLoading.value = false
  }
}

function openUserAddForm() {
  Object.assign(userEditForm, { id: null, username: '', password: '', realName: '', email: '', phone: '', status: 1, roleIds: [] })
  loadRoleOptionsForUserForm()
  userEditVisible.value = true
}

function onUserEditDialogClose() {
  userEditRef.value?.resetFields()
  Object.assign(userEditForm, { id: null, username: '', password: '', realName: '', email: '', phone: '', status: 1, roleIds: [] })
}

function openUserEditForm() {
  if (currentNode.value?.type !== 'user' || !currentNode.value?.id) return
  loadRoleOptionsForUserForm()
  userGet(currentNode.value.id).then(async (d) => {
    const u = d?.data ?? d
    if (u) {
      let roleIds = []
      try {
        const ids = await getUserRoleIds(u.id)
        roleIds = Array.isArray(ids) ? ids : []
      } catch {
        roleIds = []
      }
      Object.assign(userEditForm, { id: u.id, username: u.username ?? '', password: '', realName: u.realName ?? '', email: u.email ?? '', phone: u.phone ?? '', status: u.status ?? 1, roleIds })
      if (userEditForm.username === 'admin') userEditForm.status = 1
    }
  }).catch(() => {})
  userEditVisible.value = true
}

async function submitUserEdit() {
  try {
    await userEditRef.value?.validate()
  } catch {
    return
  }
  if (!userEditForm.id && !userEditForm.password?.trim()) {
    ElMessage.warning('新增用户请输入密码')
    return
  }
  submitLoading.value = true
  try {
    const roleIds = Array.isArray(userEditForm.roleIds) ? [...userEditForm.roleIds] : []
    const payload = { ...userEditForm }
    delete payload.roleIds
    if (!payload.password?.trim()) delete payload.password
    if (payload.username === 'admin') payload.status = 1
    let savedUserId = payload.id
    const isAdmin = userEditForm.username === 'admin'
    if (userEditForm.id) {
      await userUpdate(payload)
      if (!isAdmin) await userAssignRoles(savedUserId, roleIds)
    } else {
      if (!payload.password?.trim()) {
        ElMessage.warning('新增用户请输入密码')
        return
      }
      const res = await userSave(payload)
      savedUserId = res?.data?.id ?? res?.id
      if (savedUserId && roleIds.length && !isAdmin) await userAssignRoles(savedUserId, roleIds)
    }
    ElMessage.success('保存成功')
    userEditVisible.value = false
    if (currentUserDetail.value) Object.assign(currentUserDetail.value, userEditForm)
    await loadTree()
    loadUserLibList().catch(() => {})
  } catch (e) {
    if (e?.message === 'cancel') return
    const msg = e?.response?.data?.message ?? e?.message ?? e?.msg ?? '保存失败'
    ElMessage.error(msg)
  } finally {
    submitLoading.value = false
  }
}

/** 在树中查找某节点的父节点 */
function findParentInTree(nodes, nodeKey) {
  if (!Array.isArray(nodes) || !nodeKey) return null
  for (const n of nodes) {
    const children = n.children
    if (Array.isArray(children) && children.some(c => c.nodeKey === nodeKey)) return n
    const found = findParentInTree(children || [], nodeKey)
    if (found) return found
  }
  return null
}

/** 移除：从当前角色下移除用户（取消关联），树中不再显示该用户在此角色下 */
async function doRemoveUserFromTree() {
  if (currentNode.value?.type !== 'user' || !currentNode.value?.id) return
  const userNode = currentNode.value
  const parent = findParentInTree(treeData.value, userNode.nodeKey)
  if (!parent || parent.type !== 'role') {
    ElMessage.warning('无法获取所属角色，无法移除')
    return
  }
  try {
    await ElMessageBox.confirm(`确定将「${userNode.label}」从角色下移除？移除后该用户在此角色下不再显示。`, '移除确认', { type: 'warning' })
  } catch {
    return
  }
  submitLoading.value = true
  try {
    const curRoleIds = await getUserRoleIds(userNode.id)
    const ids = Array.isArray(curRoleIds) ? curRoleIds : []
    const newRoleIds = ids.filter(id => id !== parent.id)
    await userAssignRoles(userNode.id, newRoleIds)
    ElMessage.success('已移除')
    currentNode.value = null
    currentUserDetail.value = null
    await loadTree()
  } catch (e) {
    const msg = e?.response?.data?.message ?? e?.message ?? e?.msg ?? '移除失败'
    ElMessage.error(msg)
  } finally {
    submitLoading.value = false
  }
}

async function doDeleteUser() {
  if (currentNode.value?.type !== 'user' || !currentNode.value?.id) return
  if (isCurrentUserAdmin.value) {
    ElMessage.warning('管理员账号不能删除')
    return
  }
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '删除确认', { type: 'warning' })
  } catch {
    return
  }
  submitLoading.value = true
  try {
    await userDelete(currentNode.value.id)
    ElMessage.success('删除成功')
    currentNode.value = null
    currentUserDetail.value = null
    await loadTree()
  } catch (e) {
    const msg = e?.response?.data?.message ?? e?.message ?? e?.msg ?? '删除失败'
    ElMessage.error(msg)
  } finally {
    submitLoading.value = false
  }
}

function openUserForm() {
  if (currentNode.value?.type !== 'role') return
  userForm.userId = null
  userList.value = []
  userVisible.value = true
}

async function loadUserList() {
  if (userList.value.length > 0) return
  userListLoading.value = true
  try {
    const res = await userPage({ page: 1, size: 500 })
    userList.value = res?.list ?? []
  } catch {
    userList.value = []
  } finally {
    userListLoading.value = false
  }
}

async function submitUserRole() {
  if (!userForm.userId || currentNode.value?.type !== 'role') return
  const roleId = currentNode.value.id
  submitLoading.value = true
  try {
    const curRoleIds = await getUserRoleIds(userForm.userId)
    const ids = Array.isArray(curRoleIds) ? curRoleIds : []
    if (ids.includes(roleId)) {
      ElMessage.warning('该用户已拥有本角色')
      return
    }
    await userAssignRoles(userForm.userId, [...ids, roleId])
    ElMessage.success('已分配用户到本角色')
    userVisible.value = false
    await loadTree()
  } catch (e) {
    const msg = e?.response?.data?.message ?? e?.message ?? e?.msg ?? '分配失败，请检查网络或后端'
    ElMessage.error(msg)
  } finally {
    submitLoading.value = false
  }
}

function openRoleForm(id, deptIds) {
  Object.assign(roleForm, { id: id || null, deptIds: Array.isArray(deptIds) ? [...deptIds] : (deptIds != null ? [deptIds] : []), code: '', name: '', status: 1, sortOrder: 0 })
  if (id) roleGet(id).then(d => { if (d) Object.assign(roleForm, { ...d, deptIds: Array.isArray(d.deptIds) ? d.deptIds : (d.deptId != null ? [d.deptId] : []) }) })
  roleVisible.value = true
}

async function submitRole() {
  try {
    await roleRef.value?.validate()
  } catch {
    ElMessage.warning('请完善必填项后再提交')
    return
  }
  submitLoading.value = true
  try {
    if (roleForm.id) await roleUpdate(roleForm)
    else await roleSave(roleForm)
    ElMessage.success('保存成功')
    roleVisible.value = false
    await loadTree()
  } catch (e) {
    const msg = e?.response?.data?.message ?? e?.message ?? e?.msg ?? '角色保存失败，请检查网络或后端'
    ElMessage.error(msg)
  } finally {
    submitLoading.value = false
  }
}

async function doDeleteCompany() {
  if (!currentNode.value?.id) return
  try {
    await ElMessageBox.confirm('确定要删除该公司吗？其下部门、角色等将受影响。', '删除确认', { type: 'warning' })
  } catch {
    return
  }
  submitLoading.value = true
  try {
    await orgApi.companyDelete(currentNode.value.id)
    ElMessage.success('删除成功')
    currentNode.value = null
    await loadTree()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message ?? e?.message ?? '删除失败')
  } finally {
    submitLoading.value = false
  }
}

async function doDeleteDept() {
  if (!currentNode.value?.id) return
  try {
    await ElMessageBox.confirm('确定要删除该部门吗？其下角色将受影响。', '删除确认', { type: 'warning' })
  } catch {
    return
  }
  submitLoading.value = true
  try {
    await orgApi.deptDelete(currentNode.value.id)
    ElMessage.success('删除成功')
    currentNode.value = null
    await loadTree()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message ?? e?.message ?? '删除失败')
  } finally {
    submitLoading.value = false
  }
}

async function doDeleteRole() {
  if (!currentNode.value?.id) return
  try {
    await ElMessageBox.confirm('确定要删除该角色吗？', '删除确认', { type: 'warning' })
  } catch {
    return
  }
  submitLoading.value = true
  try {
    await roleDelete(currentNode.value.id)
    ElMessage.success('删除成功')
    currentNode.value = null
    await loadTree()
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  try {
    loadTree()
  } catch (e) {
    console.error('[OrgManage] onMounted error:', e)
    treeData.value = []
  }
})
</script>

<style scoped>
/* 组织树与节点详情等高：行内两列拉伸一致，内容区填满列高 */
.org-tree-row {
  display: flex;
  align-items: stretch;
}
.org-tree-row .org-tree-col {
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.org-tree-wrap {
  padding: 14px 16px;
  background: linear-gradient(180deg, #fafbfc 0%, #f5f7fa 100%);
  border: 1px solid var(--border-light, #e5e5e5);
  border-radius: 10px;
  min-height: 200px;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.org-tree-toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  margin-bottom: 10px;
  flex-shrink: 0;
}
.org-tree-toolbar__btn {
  flex-shrink: 0;
}
.org-tree-content {
  flex: 1;
  min-height: 0;
  overflow: auto;
}
:deep(.org-tree) {
  background: transparent;
}
:deep(.org-tree .el-tree-node) {
  margin-bottom: 2px;
}
:deep(.org-tree .el-tree-node__content) {
  height: 36px;
  padding-left: 12px;
  border-radius: 8px;
  transition: background 0.2s ease, color 0.2s ease;
}
:deep(.org-tree .el-tree-node__content:hover) {
  background: rgba(14, 165, 233, 0.08);
}
:deep(.org-tree .el-tree-node.is-current > .el-tree-node__content) {
  background: linear-gradient(90deg, rgba(14, 165, 233, 0.14), rgba(124, 58, 237, 0.08));
  color: var(--text-primary);
  font-weight: 500;
}
:deep(.org-tree .el-tree-node__expand-icon) {
  color: var(--neutral-500);
  font-size: 14px;
}
:deep(.org-tree .el-tree-node.is-current > .el-tree-node__content .el-tree-node__expand-icon) {
  color: var(--primary-start);
}
.org-tree-node {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  font-size: 0.875rem;
}
.org-tree-node__icon-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  flex-shrink: 0;
  border: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s ease;
}
.org-tree-node__icon-wrap:hover {
  transform: translateY(-1px) scale(1.05);
}
.org-tree-node__icon-wrap--company {
  background: linear-gradient(155deg, #7dd3fc 0%, #0ea5e9 45%, #0284c7 100%);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.4);
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.1);
}
.org-tree-node__icon-wrap--company:hover {
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.12);
}
.org-tree-node__icon-wrap--company-system {
  background: linear-gradient(155deg, #86efac 0%, #22c55e 45%, #16a34a 100%) !important;
  color: #fff;
  border-color: rgba(255, 255, 255, 0.4);
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.1);
}
.org-tree-node__icon-wrap--company-system:hover {
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.12);
}
.org-tree-node__icon-wrap--dept {
  background: linear-gradient(155deg, #c4b5fd 0%, #a78bfa 45%, #7c3aed 100%);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.4);
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.1);
}
.org-tree-node__icon-wrap--dept:hover {
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.12);
}
.org-tree-node__icon-wrap--role {
  background: linear-gradient(155deg, #f9a8d4 0%, #f472b6 45%, #db2777 100%);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.4);
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.1);
}
.org-tree-node__icon-wrap--role:hover {
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.12);
}
.org-tree-node__icon-wrap--user {
  background: linear-gradient(155deg, #b8c5d6 0%, #94a3b8 45%, #475569 100%);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.4);
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.1);
}
.org-tree-node__icon-wrap--user:hover {
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.12);
}
.org-tree-node__icon-wrap--node {
  background: linear-gradient(155deg, #e2e8f0 0%, #cbd5e1 45%, #94a3b8 100%);
  color: #fff;
  border-color: rgba(0, 0, 0, 0.06);
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.08);
}
.org-tree-node__icon-wrap--node:hover {
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}
.org-tree-node__icon {
  font-size: 0.8125rem;
  width: 1em;
  height: 1em;
}
.org-tree-node__label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.node-detail {
  position: relative;
  border-left: 2px solid #cbd5e1;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.06);
  padding: 14px 0 14px 20px;
  min-height: 200px;
  flex: 1;
  display: flex;
  flex-direction: column;
}
.org-card-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.org-tree-tip {
  font-size: 0.75rem;
  color: var(--text-secondary);
}
.node-detail__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}
.node-detail__title {
  font-weight: 600;
  font-size: 0.875rem;
  color: var(--text-primary);
  letter-spacing: 0.02em;
}
.node-detail__tabs {
  display: flex;
  align-items: center;
  gap: 8px;
}
.node-detail__body {
  margin-top: 4px;
}
.node-detail :deep(.el-descriptions) {
  --el-descriptions-item-bordered-padding: 8px 12px;
}
.node-detail :deep(.el-descriptions__label),
.node-detail :deep(.el-descriptions__content) {
  font-size: 0.8125rem;
}
.admin-status-tip {
  margin-left: 8px;
  font-size: 0.8125rem;
  color: var(--text-secondary, #262626);
}

.org-lib-row {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--border-light, #e5e5e5);
}
.org-lib-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px 12px;
  border-radius: 10px;
  border: 1px solid var(--border-light, #e5e5e5);
  background: #fafbfc;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}
.org-lib-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}
.org-lib-card--company,
.org-lib-card--dept,
.org-lib-card--role,
.org-lib-card--user {
  cursor: pointer;
}
.org-lib-card__icon {
  width: 36px;
  height: 36px;
  margin-bottom: 8px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 1.125rem;
}
.org-lib-card__num {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}
.org-lib-card__title {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  margin-top: 4px;
}
.org-lib-card--company .org-lib-card__icon {
  background: linear-gradient(155deg, #7dd3fc 0%, #0ea5e9 50%, #0284c7 100%);
  border: 1px solid rgba(255, 255, 255, 0.45);
  box-shadow:
    0 2px 6px rgba(14, 165, 233, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}
.org-lib-card--dept .org-lib-card__icon {
  background: linear-gradient(155deg, #c4b5fd 0%, #a78bfa 50%, #7c3aed 100%);
  border: 1px solid rgba(255, 255, 255, 0.45);
  box-shadow:
    0 2px 6px rgba(139, 92, 246, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}
.org-lib-card--role .org-lib-card__icon {
  background: linear-gradient(155deg, #f9a8d4 0%, #f472b6 50%, #db2777 100%);
  border: 1px solid rgba(255, 255, 255, 0.45);
  box-shadow:
    0 2px 6px rgba(236, 72, 153, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}
.org-lib-card--user .org-lib-card__icon {
  background: linear-gradient(155deg, #b8c5d6 0%, #94a3b8 50%, #475569 100%);
  border: 1px solid rgba(255, 255, 255, 0.45);
  box-shadow:
    0 2px 6px rgba(100, 116, 139, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.35);
}

/* 抽屉宽度自适应：按视口 60%，且不小于列宽总和避免挤压 */
.org-lib-drawer--adaptive {
  min-width: 560px;
}
/* 抽屉页四边一点点圆角 */
.org-lib-drawer.el-drawer {
  border-radius: 8px;
  overflow: hidden;
}
.org-lib-drawer__body {
  padding: 0 8px;
}
/* 表格操作列防穿模：与左侧列留出间距，避免与排序列数字重叠 */
.org-lib-table .el-table__body-wrapper td .org-lib-drawer__actions {
  margin-left: 4px;
}
.org-lib-drawer__actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
  flex-wrap: nowrap;
}
.org-lib-drawer__actions .el-button {
  flex-shrink: 0;
}
.org-lib-drawer__toolbar {
  margin-bottom: 12px;
}
.org-lib-drawer__empty {
  text-align: center;
  color: var(--text-secondary);
  font-size: 0.875rem;
  padding: 32px 0;
}
.org-lib-drawer__pagination {
  margin-top: 16px;
  justify-content: flex-start;
}
</style>
