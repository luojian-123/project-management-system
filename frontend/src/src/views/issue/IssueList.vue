<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>问题管理</span>
        <el-button type="primary" style="float:right" @click="openForm()">新增问题</el-button>
      </template>
      <el-form inline class="filter">
        <el-select v-model="projectId" placeholder="项目" clearable filterable style="width:200px" @focus="loadProjects">
          <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
        </el-select>
        <el-select v-model="status" placeholder="状态" clearable style="width:120px">
          <el-option label="待处理" value="OPEN" />
          <el-option label="处理中" value="IN_PROGRESS" />
          <el-option label="已解决" value="RESOLVED" />
          <el-option label="已关闭" value="CLOSED" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
      </el-form>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="issueCode" label="编码" width="100" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="severity" label="严重程度" width="100">
          <template #default="{ row }">{{ severityMap[row.severity] || row.severity }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">{{ statusMap[row.status] || row.status }}</template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="责任人" width="100" />
        <el-table-column prop="foundDate" label="发现日期" width="110" />
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
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑问题' : '新增问题'" width="560px">
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
        <el-form-item label="关联任务">
          <el-select v-model="form.taskId" placeholder="可选，选择关联任务" clearable filterable style="width:100%" @focus="loadProjectTasks">
            <el-option v-for="t in projectTasks" :key="t.id" :label="t.taskCode + ' ' + t.taskName" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="责任人">
          <el-select v-model="form.assigneeId" placeholder="选择责任人" clearable filterable style="width:100%" @focus="loadUsers">
            <el-option v-for="u in users" :key="u.id" :label="u.realName || u.username" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="严重程度">
          <el-select v-model="form.severity" style="width:100%">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="严重" value="CRITICAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="待处理" value="OPEN" />
            <el-option label="处理中" value="IN_PROGRESS" />
            <el-option label="已解决" value="RESOLVED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="发现日期">
          <el-date-picker v-model="form.foundDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="解决日期">
          <el-date-picker v-model="form.resolvedDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
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
import { taskListByProject } from '@/api/task'
import { issuePage, issueSave, issueDelete } from '@/api/issue'
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
const projectTasks = ref([])
const form = reactive({
  id: null, projectId: null, taskId: null, title: '', description: '', assigneeId: null, severity: 'MEDIUM', status: 'OPEN',
  foundDate: null, resolvedDate: null
})
const severityMap = { LOW: '低', MEDIUM: '中', HIGH: '高', CRITICAL: '严重' }
const statusMap = { OPEN: '待处理', IN_PROGRESS: '处理中', RESOLVED: '已解决', CLOSED: '已关闭' }

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
async function loadProjectTasks() {
  if (!form.projectId) { projectTasks.value = []; return }
  projectTasks.value = await taskListByProject(form.projectId, false) || []
}

async function load() {
  loading.value = true
  try {
    const res = await issuePage({ page: page.value, size: size.value, projectId: projectId.value || undefined, status: status.value || undefined })
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
    form.taskId = row.taskId ?? null
    form.title = row.title
    form.description = row.description
    form.assigneeId = row.assigneeId ?? null
    form.severity = row.severity || 'MEDIUM'
    form.status = row.status || 'OPEN'
    form.foundDate = row.foundDate
    form.resolvedDate = row.resolvedDate
  } else {
    form.id = null
    form.projectId = projectId.value || null
    form.taskId = null
    form.title = ''
    form.description = ''
    form.assigneeId = null
    form.severity = 'MEDIUM'
    form.status = 'OPEN'
    form.foundDate = null
    form.resolvedDate = null
  }
  projectTasks.value = []
  dialogVisible.value = true
}

async function submit() {
  if (!form.title?.trim() || !form.projectId) { ElMessage.warning('请选择项目并填写标题'); return }
  await issueSave(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

function del(row) {
  ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }).then(() => {
    issueDelete(row.id).then(() => { ElMessage.success('已删除'); load() })
  }).catch(() => {})
}

onMounted(() => { loadProjects(); load() })
</script>

<style scoped>
.filter { margin-bottom: 16px; }
</style>
