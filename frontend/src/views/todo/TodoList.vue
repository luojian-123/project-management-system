<template>
  <div class="page">
    <div class="page-cards" v-draggable-cards>
      <el-card>
    <template #header>
      <div class="card-header">
        <div>
          <span>我的任务</span>
          <span class="header-desc">展示您作为责任人的任务数据</span>
        </div>
      </div>
    </template>
    <el-table v-loading="loading" :data="list" stripe border>
      <el-table-column label="来源" min-width="84">
        <template #default="{ row }">{{ row._source === 'task' ? '项目任务' : '待办' }}</template>
      </el-table-column>
      <el-table-column label="任务名称" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row._source === 'todo'" class="link-name" @click="openForm(row)">{{ row.title || '-' }}</span>
          <router-link v-else-if="row.id" :to="`/task/${row.id}`" class="link-name">{{ row.taskName || '-' }}</router-link>
          <span v-else>{{ row.taskName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="项目" min-width="100">
        <template #default="{ row }">{{ row._source === 'task' ? (row.projectName || '-') : '-' }}</template>
      </el-table-column>
      <el-table-column label="任务类型" min-width="100">
        <template #default="{ row }">{{ row._source === 'todo' ? (taskTypeMap[row.bizType] || row.bizType || '-') : '任务' }}</template>
      </el-table-column>
      <el-table-column label="优先级" min-width="88">
        <template #default="{ row }">{{ row._source === 'todo' ? (priorityMap[row.priority] || '-') : '-' }}</template>
      </el-table-column>
      <el-table-column label="截止/计划日期" min-width="132">
        <template #default="{ row }">{{ row._source === 'todo' ? row.dueDate : row.planEnd }}</template>
      </el-table-column>
      <el-table-column label="状态" min-width="84">
        <template #default="{ row }">{{ row._source === 'todo' ? (statusMap[row.status] || row.status) : (taskStatusMap[row.status] || row.status) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right" align="center">
        <template #default="{ row }">
          <div class="table-actions-cell">
            <template v-if="row._source === 'todo'">
              <el-dropdown v-if="isAdmin" trigger="click" @command="(cmd) => cmd === 'edit' ? openForm(row) : handleDelete(row)">
                <el-button type="primary" link>操作<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit">编辑</el-dropdown-item>
                    <el-dropdown-item command="del" divided>删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <span v-else>-</span>
            </template>
            <router-link v-else-if="row.id" :to="`/task/${row.id}`">
              <el-button type="primary" link>查看</el-button>
            </router-link>
            <span v-else>-</span>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="page"
      v-model:page-size="size"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      style="margin-top:16px"
      @current-change="fetchList"
      @size-change="fetchList"
    />
    <el-dialog v-model="dialogVisible" title="任务" width="500px" @close="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="任务名称" prop="title">
          <el-input v-model="form.title" placeholder="任务名称" />
        </el-form-item>
        <el-form-item label="类型" prop="bizType">
          <el-select v-model="form.bizType" placeholder="类型" style="width:100%">
            <el-option label="任务" value="task" />
            <el-option label="问题" value="issue" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="form.priority" placeholder="优先级" style="width:100%">
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期" prop="dueDate">
          <el-date-picker v-model="form.dueDate" type="date" value-format="YYYY-MM-DD" placeholder="截止日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="状态" style="width:100%">
            <el-option label="待办" value="pending" />
            <el-option label="进行中" value="doing" />
            <el-option label="已完成" value="done" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'
import { todoPage, todoSave, todoUpdate, todoDelete } from '@/api/todo'
import { taskPageByAssignee } from '@/api/task'

const router = useRouter()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.username === 'admin')
const currentUserId = computed(() => {
  const id = userStore.userId
  if (id != null) return id
  try {
    const u = JSON.parse(localStorage.getItem('user') || '{}')
    return u.userId != null ? u.userId : null
  } catch {
    return null
  }
})

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const dialogVisible = ref(false)
const formRef = ref()
const submitLoading = ref(false)
const form = reactive({ id: null, userId: null, title: '', bizType: 'task', priority: 'medium', dueDate: '', status: 'pending' })
const rules = {
  title: [{ required: true, message: '请输入任务名称', trigger: 'blur' }]
}
const taskTypeMap = { task: '任务', issue: '问题', other: '其他' }
const priorityMap = { high: '高', medium: '中', low: '低' }
const statusMap = { pending: '待办', doing: '进行中', done: '已完成' }
const taskStatusMap = { TODO: '待办', IN_PROGRESS: '进行中', DONE: '已完成', CANCELLED: '已取消' }

async function fetchList() {
  loading.value = true
  try {
    const [todoRes, taskRes] = await Promise.all([
      todoPage({ page: page.value, size: size.value }),
      taskPageByAssignee({ page: page.value, size: size.value }).catch(() => ({ list: [], total: 0 }))
    ])
    const todoList = (todoRes?.list ?? []).map(r => ({ ...r, _source: 'todo' }))
    const taskList = (taskRes?.list ?? []).map(r => ({ ...r, _source: 'task' }))
    list.value = [...todoList, ...taskList]
    total.value = (todoRes?.total ?? 0) + (taskRes?.total ?? 0)
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function goTaskDetail(row) {
  if (row._source === 'task' && row.id) router.push(`/task/${row.id}`)
  else if (row.projectId) router.push(`/project/${row.projectId}`)
}

function openForm(row) {
  if (row && row._source === 'todo' && !isAdmin.value) return
  if (row) {
    Object.assign(form, { id: row.id, userId: row.userId, title: row.title, bizType: row.bizType, priority: row.priority, dueDate: row.dueDate, status: row.status })
  } else {
    Object.assign(form, { id: null, userId: currentUserId.value, title: '', bizType: 'task', priority: 'medium', dueDate: '', status: 'pending' })
  }
  dialogVisible.value = true
}

async function submitForm() {
  try {
    await formRef.value?.validate()
  } catch {
    ElMessage.warning('请完善必填项后再提交')
    return
  }
  if (!form.id && form.userId == null) form.userId = currentUserId.value
  submitLoading.value = true
  try {
    if (form.id) await todoUpdate(form)
    else await todoSave(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } catch (e) {
    const msg = e?.response?.data?.message ?? e?.message ?? e?.msg ?? '保存失败，请检查网络或后端'
    ElMessage.error(msg)
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定删除该待办？', '提示', {
    type: 'warning'
  }).then(async () => {
    await todoDelete(row.id)
    ElMessage.success('删除成功')
    fetchList()
  }).catch(() => {})
}

onMounted(async () => {
  if (currentUserId.value == null) {
    try {
      await userStore.fetchInfo()
    } catch (_) {}
  }
  fetchList()
})
</script>

<style scoped>
.card-header { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 8px; }
.header-desc { font-size: 0.8125rem; color: #64748b; margin-left: 10px; font-weight: normal; }
.link-name { color: var(--el-color-primary); text-decoration: none; cursor: pointer; }
.link-name:hover { text-decoration: underline; }
a.link-name { display: inline-block; }
</style>
