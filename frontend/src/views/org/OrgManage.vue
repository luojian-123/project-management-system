<template>
  <div class="page">
    <el-row :gutter="16">
    <el-col :span="8">
      <el-card shadow="never">
        <template #header>
          <span>组织树</span>
          <div>
            <el-button type="primary" link @click="loadTree">刷新</el-button>
          </div>
        </template>
        <div v-loading="treeLoading">
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
      </el-card>
    </el-col>
    <el-col :span="16">
      <el-card shadow="never">
        <template #header>
          <span>节点详情</span>
          <div style="display:flex; gap:8px">
            <el-button type="primary" @click="openCompanyForm()">新增公司</el-button>
            <el-button v-if="currentNode?.type === 'company'" type="primary" @click="openDeptForm(null, currentNode.id)">新增部门</el-button>
            <el-button v-if="currentNode?.type === 'dept'" type="primary" @click="openRoleForm(null, currentNode.id)">新增角色</el-button>
          </div>
        </template>

        <el-empty v-if="!currentNode" description="请选择左侧节点" />

        <template v-else>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="类型">{{ typeText(currentNode.type) }}</el-descriptions-item>
            <el-descriptions-item label="ID">{{ currentNode.id }}</el-descriptions-item>
            <el-descriptions-item label="名称">{{ currentNode.label }}</el-descriptions-item>
          </el-descriptions>
          <div style="margin-top:12px; display:flex; gap:8px; flex-wrap:wrap">
            <el-button v-if="currentNode.type === 'company'" type="primary" @click="openCompanyForm(currentNode.id)">编辑公司</el-button>
            <el-button v-if="currentNode.type === 'company'" type="danger" @click="doDeleteCompany">删除公司</el-button>
            <el-button v-if="currentNode.type === 'dept'" type="primary" @click="openDeptForm(currentNode.id)">编辑部门</el-button>
            <el-button v-if="currentNode.type === 'dept'" type="danger" @click="doDeleteDept">删除部门</el-button>
            <el-button v-if="currentNode.type === 'role'" type="primary" @click="openRoleForm(currentNode.id)">编辑角色</el-button>
            <el-button v-if="currentNode.type === 'role'" type="danger" @click="doDeleteRole">删除角色</el-button>
          </div>
        </template>
      </el-card>
    </el-col>
  </el-row>

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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as orgApi from '@/api/org'
import { roleGet, roleSave, roleUpdate, roleDelete } from '@/api/role'

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

function onNodeClick(node) {
  currentNode.value = node
}

// 公司表单
const companyVisible = ref(false)
const companyRef = ref(null)
const companyForm = reactive({ id: null, companyCode: '', companyName: '', sortOrder: 0 })
const companyRules = { companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }] }

function openCompanyForm(id) {
  Object.assign(companyForm, { id: id || null, companyCode: '', companyName: '', sortOrder: 0 })
  if (id) {
    orgApi.companyGet(id).then(d => Object.assign(companyForm, d || {}))
  }
  companyVisible.value = true
}

async function submitCompany() {
  await companyRef.value?.validate()
  submitLoading.value = true
  try {
    if (companyForm.id) await orgApi.companyUpdate(companyForm)
    else await orgApi.companySave(companyForm)
    ElMessage.success('保存成功')
    companyVisible.value = false
    await loadTree()
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
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

function openDeptForm(id, companyId) {
  Object.assign(deptForm, { id: id || null, companyId: companyId || null, deptCode: '', deptName: '', sortOrder: 0 })
  if (id) orgApi.deptGet(id).then(d => Object.assign(deptForm, d || {}))
  deptVisible.value = true
}

async function submitDept() {
  await deptRef.value?.validate()
  submitLoading.value = true
  try {
    if (deptForm.id) await orgApi.deptUpdate(deptForm)
    else await orgApi.deptSave(deptForm)
    ElMessage.success('保存成功')
    deptVisible.value = false
    await loadTree()
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

function openRoleForm(id, deptId) {
  Object.assign(roleForm, { id: id || null, deptId: deptId || null, code: '', name: '', status: 1, sortOrder: 0 })
  if (id) roleGet(id).then(d => Object.assign(roleForm, d || {}))
  roleVisible.value = true
}

async function submitRole() {
  await roleRef.value?.validate()
  submitLoading.value = true
  try {
    if (roleForm.id) await roleUpdate(roleForm)
    else await roleSave(roleForm)
    ElMessage.success('保存成功')
    roleVisible.value = false
    await loadTree()
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
