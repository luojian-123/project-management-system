<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>组织树</span>
        <div style="display:flex; gap:8px; align-items:center">
          <el-button type="primary" link @click="loadTree">刷新</el-button>
          <!-- 用户为最底层节点，不显示新增 -->
          <el-button v-if="currentNode?.type && currentNode.type !== 'role' && currentNode.type !== 'user'" type="primary" @click="openCompanyForm()">新增公司</el-button>
          <el-button v-if="currentNode?.type === 'company'" type="primary" @click="openDeptForm(null, currentNode.id)">新增部门</el-button>
          <el-button v-if="currentNode?.type === 'dept'" type="primary" @click="openRoleForm(null, currentNode.id)">新增角色</el-button>
          <el-button v-if="currentNode?.type === 'role'" type="primary" @click="openUserForm()">新增用户</el-button>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="8">
          <div v-loading="treeLoading" @contextmenu.prevent>
            <el-tree
              :data="treeData"
              node-key="nodeKey"
              :props="{ label: 'label', children: 'children' }"
              default-expand-all
              highlight-current
              :expand-on-click-node="false"
              @node-click="onNodeClick"
            />
          </div>
        </el-col>
        <el-col :span="16">
          <div class="node-detail">
            <div class="node-detail__title">节点详情</div>
            <el-empty v-if="!currentNode" description="请选择左侧节点" />
            <template v-else>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="类型">{{ typeText(currentNode.type) }}</el-descriptions-item>
                <el-descriptions-item label="ID">{{ currentNode.id }}</el-descriptions-item>
                <el-descriptions-item label="名称">{{ currentNode.label }}</el-descriptions-item>
              </el-descriptions>
              <div v-if="currentNode.type && ['company','dept','role','user'].includes(currentNode.type)" style="margin-top:12px; display:flex; gap:8px; flex-wrap:wrap">
                <el-button v-if="currentNode.type === 'company'" type="primary" @click="openCompanyForm(currentNode.id)">编辑公司</el-button>
                <el-button v-if="currentNode.type === 'company'" type="danger" @click="doDeleteCompany">删除公司</el-button>
                <el-button v-if="currentNode.type === 'dept'" type="primary" @click="openDeptForm(currentNode.id)">编辑部门</el-button>
                <el-button v-if="currentNode.type === 'dept'" type="danger" @click="doDeleteDept">删除部门</el-button>
                <el-button v-if="currentNode.type === 'role'" type="primary" @click="openRoleForm(currentNode.id)">编辑角色</el-button>
                <el-button v-if="currentNode.type === 'role'" type="danger" @click="doDeleteRole">删除角色</el-button>
                <el-button v-if="currentNode.type === 'user'" type="primary" @click="openUserEditForm()">编辑用户</el-button>
                <el-button v-if="currentNode.type === 'user'" type="danger" :disabled="isCurrentUserAdmin" @click="doDeleteUser">删除用户</el-button>
              </div>
            </template>
          </div>
        </el-col>
      </el-row>
    </el-card>

  <!-- 公司 -->
  <el-dialog v-model="companyVisible" title="公司" width="520px" @close="companyRef?.resetFields()">
    <el-form ref="companyRef" :model="companyForm" :rules="companyRules" label-width="90px">
      <el-form-item label="公司编码" prop="companyCode"><el-input v-model="companyForm.companyCode" /></el-form-item>
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
        <el-select v-model="deptForm.companyId" style="width:100%" placeholder="选择公司">
          <el-option v-for="c in companyList" :key="c.id ?? c.companyCode ?? ''" :label="c.companyName || c.companyCode || ''" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="部门编码" prop="deptCode"><el-input v-model="deptForm.deptCode" /></el-form-item>
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
    <el-form ref="roleRef" :model="roleForm" :rules="roleRules" label-width="90px">
      <el-form-item label="所属部门" prop="deptId">
        <el-select v-model="roleForm.deptId" style="width:100%" placeholder="选择部门">
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

  <!-- 编辑用户（组织树用户节点） -->
  <el-dialog v-model="userEditVisible" title="编辑用户" width="480px" @close="userEditRef?.resetFields()">
    <el-form ref="userEditRef" :model="userEditForm" label-width="90px">
      <el-form-item label="用户名"><el-input v-model="userEditForm.username" disabled /></el-form-item>
      <el-form-item label="新密码"><el-input v-model="userEditForm.password" type="password" placeholder="不填则保持原密码" show-password /></el-form-item>
      <el-form-item label="姓名"><el-input v-model="userEditForm.realName" placeholder="姓名" /></el-form-item>
      <el-form-item label="邮箱"><el-input v-model="userEditForm.email" placeholder="邮箱" /></el-form-item>
      <el-form-item label="手机"><el-input v-model="userEditForm.phone" placeholder="手机" /></el-form-item>
      <el-form-item label="状态">
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as orgApi from '@/api/org'
import { roleGet, roleSave, roleUpdate, roleDelete } from '@/api/role'
import { userPage, userGet, userUpdate, userDelete, getUserRoleIds, userAssignRoles } from '@/api/user'

const treeData = ref([])
const currentNode = ref(null)
const submitLoading = ref(false)
const treeLoading = ref(false)

const companyList = ref([])
const deptOptions = ref([])

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
  if (node?.type === 'user' && node.id) {
    userGet(node.id).then((d) => {
      const u = d?.data ?? d
      currentUserDetail.value = u || null
    }).catch(() => { currentUserDetail.value = null })
  } else {
    currentUserDetail.value = null
  }
}

const isCurrentUserAdmin = computed(() => currentUserDetail.value?.username === 'admin')

// 公司表单
const companyVisible = ref(false)
const companyRef = ref(null)
const companyForm = reactive({ id: null, companyCode: '', companyName: '', sortOrder: 0 })
const companyRules = {
  companyCode: [{ required: true, message: '请输入公司编码', trigger: 'blur' }],
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }]
}

function openCompanyForm(id) {
  Object.assign(companyForm, { id: id || null, companyCode: '', companyName: '', sortOrder: 0 })
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
const deptForm = reactive({ id: null, companyId: null, deptCode: '', deptName: '', sortOrder: 0 })
const deptRules = {
  companyId: [{ required: true, message: '请选择公司', trigger: 'change' }],
  deptCode: [{ required: true, message: '请输入部门编码', trigger: 'blur' }],
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

function openDeptForm(id, companyId) {
  Object.assign(deptForm, { id: id || null, companyId: companyId || null, deptCode: '', deptName: '', sortOrder: 0 })
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
const roleForm = reactive({ id: null, deptId: null, code: '', name: '', status: 1, sortOrder: 0 })
const roleRules = {
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const userVisible = ref(false)
const userForm = reactive({ userId: null })
const userList = ref([])
const userListLoading = ref(false)

const userEditVisible = ref(false)
const userEditRef = ref(null)
const userEditForm = reactive({ id: null, username: '', password: '', realName: '', email: '', phone: '', status: 1 })

function openUserEditForm() {
  if (currentNode.value?.type !== 'user' || !currentNode.value?.id) return
  userGet(currentNode.value.id).then((d) => {
    const u = d?.data ?? d
    if (u) {
      Object.assign(userEditForm, { id: u.id, username: u.username ?? '', password: '', realName: u.realName ?? '', email: u.email ?? '', phone: u.phone ?? '', status: u.status ?? 1 })
      if (userEditForm.username === 'admin') userEditForm.status = 1
    }
  }).catch(() => {})
  userEditVisible.value = true
}

async function submitUserEdit() {
  submitLoading.value = true
  try {
    const payload = { ...userEditForm }
    if (!payload.password?.trim()) delete payload.password
    if (payload.username === 'admin') payload.status = 1
    await userUpdate(payload)
    ElMessage.success('保存成功')
    userEditVisible.value = false
    if (currentUserDetail.value) Object.assign(currentUserDetail.value, userEditForm)
    await loadTree()
  } catch (e) {
    const msg = e?.response?.data?.message ?? e?.message ?? e?.msg ?? '保存失败'
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

function openRoleForm(id, deptId) {
  Object.assign(roleForm, { id: id || null, deptId: deptId || null, code: '', name: '', status: 1, sortOrder: 0 })
  if (id) roleGet(id).then(d => Object.assign(roleForm, d || {}))
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
.node-detail__title {
  font-weight: 500;
  font-size: 0.9375rem;
  color: #000;
  margin-bottom: 12px;
}
.admin-status-tip {
  margin-left: 8px;
  font-size: 0.8125rem;
  color: var(--text-secondary, #262626);
}
</style>
