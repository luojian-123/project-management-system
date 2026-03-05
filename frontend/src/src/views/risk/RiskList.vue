<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>风险管理</span>
        <el-button type="primary" style="float:right" @click="openForm()">新增风险</el-button>
      </template>
      <el-form inline class="filter">
        <el-select v-model="projectId" placeholder="项目" clearable filterable style="width:200px" @focus="loadProjects">
          <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
        </el-select>
        <el-select v-model="status" placeholder="状态" clearable style="width:120px">
          <el-option label="识别" value="IDENTIFIED" />
          <el-option label="应对中" value="MITIGATING" />
          <el-option label="已关闭" value="CLOSED" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
      </el-form>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="riskCode" label="编码" width="100" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="probability" label="概率" width="80" />
        <el-table-column prop="impact" label="影响" width="80" />
        <el-table-column prop="riskLevel" label="等级" width="80" />
        <el-table-column prop="ownerName" label="责任人" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">{{ statusMap[row.status] || row.status }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button link type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @current-change="load"
        @size-change="load"
        style="margin-top:16px"
      />
    </el-card>
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑风险' : '新增风险'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="所属项目" required>
          <el-select v-model="form.projectId" placeholder="选择项目" filterable style="width:100%" @focus="loadProjects">
            <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="概率">
          <el-select v-model="form.probability" style="width:100%">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
          </el-select>
        </el-form-item>
        <el-form-item label="影响">
          <el-select v-model="form.impact" style="width:100%">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险等级">
          <el-input v-model="form.riskLevel" placeholder="可自动计算或手工填" />
        </el-form-item>
        <el-form-item label="责任人">
          <el-select v-model="form.ownerId" placeholder="选择责任人" clearable filterable style="width:100%" @focus="loadUsers">
            <el-option v-for="u in users" :key="u.id" :label="u.realName || u.username" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="应对措施">
          <el-input v-model="form.response" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="识别" value="IDENTIFIED" />
            <el-option label="应对中" value="MITIGATING" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { projectPage } from '@/api/project'
import { riskPage, riskSave, riskDelete } from '@/api/risk'
import { userPage } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const projectId = ref(null)
const projects = ref([])
const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const status = ref('')
const dialogVisible = ref(false)
const users = ref([])
const form = reactive({
  id: null, projectId: null, title: '', description: '', probability: 'MEDIUM', impact: 'MEDIUM',
  riskLevel: '', response: '', ownerId: null, status: 'IDENTIFIED'
})
const statusMap = { IDENTIFIED: '识别', MITIGATING: '应对中', CLOSED: '已关闭' }

async function loadProjects() {
  if (projects.value.length) return
  const res = await projectPage({ page: 1, size: 500 })
  projects.value = res.list || []
}
async function loadUsers() {
  if (users.value.length) return
  const res = await userPage({ page: 1, size: 500 })
  users.value = res.list || []
}

async function load() {
  loading.value = true
  try {
    const res = await riskPage({ page: page.value, size: size.value, projectId: projectId.value || undefined, status: status.value || undefined })
    list.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function openForm(row) {
  if (row) {
    form.id = row.id
    form.projectId = row.projectId
    form.title = row.title
    form.description = row.description
    form.probability = row.probability || 'MEDIUM'
    form.impact = row.impact || 'MEDIUM'
    form.riskLevel = row.riskLevel || ''
    form.response = row.response || ''
    form.ownerId = row.ownerId ?? null
    form.status = row.status || 'IDENTIFIED'
  } else {
    form.id = null
    form.projectId = projectId.value || null
    form.title = ''
    form.description = ''
    form.probability = 'MEDIUM'
    form.impact = 'MEDIUM'
    form.riskLevel = ''
    form.response = ''
    form.ownerId = null
    form.status = 'IDENTIFIED'
  }
  dialogVisible.value = true
}

async function submit() {
  if (!form.title?.trim() || !form.projectId) { ElMessage.warning('请选择项目并填写标题'); return }
  await riskSave(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

function del(row) {
  ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }).then(() => {
    riskDelete(row.id).then(() => { ElMessage.success('已删除'); load() })
  }).catch(() => {})
}

onMounted(() => { loadProjects(); load() })
</script>

<style scoped>
.filter { margin-bottom: 16px; }
</style>
