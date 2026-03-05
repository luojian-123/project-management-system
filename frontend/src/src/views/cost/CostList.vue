<template>
  <div class="page">
    <el-card>
      <template #header>成本管理</template>
      <el-form inline class="filter">
        <el-select v-model="projectId" placeholder="选择项目" clearable filterable style="width:220px" @focus="loadProjects">
          <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
        </el-select>
        <el-button type="primary" @click="load" :disabled="!projectId">查询</el-button>
        <el-button type="primary" @click="openForm()" :disabled="!projectId">新增成本</el-button>
      </el-form>
      <el-alert v-if="projectId && summary" type="info" :closable="false" style="margin-bottom:12px">
        本项目汇总：预算合计 <strong>{{ (summary.budgetTotal ?? summary.budgettotal) ?? 0 }}</strong> 元，实际合计 <strong>{{ (summary.actualTotal ?? summary.actualtotal) ?? 0 }}</strong> 元
      </el-alert>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="costType" label="类型" width="100">
          <template #default="{ row }">{{ costTypeMap[row.costType] || row.costType }}</template>
        </el-table-column>
        <el-table-column prop="budgetAmount" label="预算" width="120" />
        <el-table-column prop="actualAmount" label="实际" width="120" />
        <el-table-column prop="occurDate" label="发生日期" width="120" />
        <el-table-column prop="remark" label="备注" min-width="160" />
        <el-table-column label="操作" width="120" fixed="right">
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
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑成本' : '新增成本'" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="关联任务">
          <el-select v-model="form.taskId" placeholder="可选，选择关联任务" clearable filterable style="width:100%" @focus="loadProjectTasks">
            <el-option v-for="t in projectTasks" :key="t.id" :label="t.taskCode + ' ' + t.taskName" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="成本类型" required>
          <el-select v-model="form.costType" style="width:100%">
            <el-option label="人工" value="MANPOWER" />
            <el-option label="材料" value="MATERIAL" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="预算金额">
          <el-input-number v-model="form.budgetAmount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="实际金额">
          <el-input-number v-model="form.actualAmount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="发生日期">
          <el-date-picker v-model="form.occurDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" rows="2" />
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
import { costSummaryByProject, costPageByProject, costSave, costDelete } from '@/api/cost'
import { ElMessage, ElMessageBox } from 'element-plus'

const projectId = ref(null)
const projects = ref([])
const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const dialogVisible = ref(false)
const summary = ref(null)
const projectTasks = ref([])
const form = reactive({
  id: null, projectId: null, taskId: null, costType: 'MANPOWER',
  budgetAmount: 0, actualAmount: 0, occurDate: null, remark: ''
})
const costTypeMap = { MANPOWER: '人工', MATERIAL: '材料', OTHER: '其他' }

async function loadProjects() {
  if (projects.value.length) return
  const res = await projectPage({ page: 1, size: 500 })
  projects.value = res.list || []
}
async function loadProjectTasks() {
  const pid = form.projectId || projectId.value
  if (!pid) { projectTasks.value = []; return }
  projectTasks.value = await taskListByProject(pid, false) || []
}

async function load() {
  if (!projectId.value) return
  loading.value = true
  summary.value = null
  try {
    const [pageRes, sumRes] = await Promise.all([
      costPageByProject(projectId.value, { page: page.value, size: size.value }),
      costSummaryByProject(projectId.value)
    ])
    list.value = pageRes.list || []
    total.value = pageRes.total || 0
    summary.value = sumRes
  } finally {
    loading.value = false
  }
}

function openForm(row) {
  if (row) {
    form.id = row.id
    form.projectId = row.projectId
    form.costType = row.costType || 'MANPOWER'
    form.budgetAmount = row.budgetAmount ?? 0
    form.actualAmount = row.actualAmount ?? 0
    form.occurDate = row.occurDate
    form.remark = row.remark || ''
  } else {
    form.id = null
    form.projectId = projectId.value
    form.costType = 'MANPOWER'
    form.budgetAmount = 0
    form.actualAmount = 0
    form.occurDate = null
    form.remark = ''
  }
  dialogVisible.value = true
}

async function submit() {
  form.projectId = form.projectId || projectId.value
  await costSave(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

function del(row) {
  ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }).then(() => {
    costDelete(row.id).then(() => { ElMessage.success('已删除'); load() })
  }).catch(() => {})
}

onMounted(loadProjects)
</script>

<style scoped>
.filter { margin-bottom: 16px; }
</style>
