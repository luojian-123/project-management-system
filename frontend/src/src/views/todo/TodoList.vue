<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>个人待办</span>
        <el-button type="primary" style="float:right" @click="openForm()">新增</el-button>
      </template>
      <el-form inline class="filter">
        <el-select v-model="status" placeholder="状态" clearable style="width:120px">
          <el-option label="待办" value="PENDING" />
          <el-option label="进行中" value="IN_PROGRESS" />
          <el-option label="已完成" value="DONE" />
        </el-select>
        <el-select v-model="priority" placeholder="优先级" clearable style="width:100px">
          <el-option label="高" value="HIGH" />
          <el-option label="中" value="NORMAL" />
          <el-option label="低" value="LOW" />
        </el-select>
        <el-date-picker v-model="dueDateFrom" type="date" value-format="YYYY-MM-DD" placeholder="截止从" clearable style="width:130px" />
        <el-date-picker v-model="dueDateTo" type="date" value-format="YYYY-MM-DD" placeholder="截止至" clearable style="width:130px" />
        <el-button type="primary" @click="load">查询</el-button>
      </el-form>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="bizType" label="类型" width="90">
          <template #default="{ row }">{{ bizTypeMap[row.bizType] || row.bizType || '-' }}</template>
        </el-table-column>
        <el-table-column prop="bizId" label="关联ID" width="90" />
        <el-table-column prop="priority" label="优先级" width="90">
          <template #default="{ row }">{{ priorityMap[row.priority] || row.priority }}</template>
        </el-table-column>
        <el-table-column prop="dueDate" label="截止日期" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">{{ statusMap[row.status] || row.status }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button link type="primary" @click="done(row)" v-if="row.status !== 'DONE'">完成</el-button>
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
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑待办' : '新增待办'" width="500px" @close="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="标题" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.bizType" placeholder="类型" clearable style="width:100%">
            <el-option label="任务" value="task" />
            <el-option label="问题" value="issue" />
            <el-option label="变更" value="change" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联业务ID">
          <el-input-number v-model="form.bizId" placeholder="关联业务ID" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="form.priority" placeholder="优先级" style="width:100%">
            <el-option label="高" value="HIGH" />
            <el-option label="中" value="NORMAL" />
            <el-option label="低" value="LOW" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker v-model="form.dueDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="状态" style="width:100%">
            <el-option label="待办" value="PENDING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="DONE" />
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
import { todoPage, todoSave, todoDelete } from '@/api/todo'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const status = ref('')
const priority = ref('')
const dueDateFrom = ref('')
const dueDateTo = ref('')
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, title: '', bizType: '', bizId: null, priority: 'NORMAL', dueDate: null, status: 'PENDING' })

const priorityMap = { HIGH: '高', NORMAL: '中', LOW: '低' }
const bizTypeMap = { task: '任务', issue: '问题', change: '变更', other: '其他' }
const statusMap = { PENDING: '待办', IN_PROGRESS: '进行中', DONE: '已完成' }

async function load() {
  loading.value = true
  try {
    const res = await todoPage({ page: page.value, size: size.value, status: status.value || undefined, priority: priority.value || undefined, dueDateFrom: dueDateFrom.value || undefined, dueDateTo: dueDateTo.value || undefined })
    list.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function openForm(row) {
  if (row) {
    form.id = row.id
    form.title = row.title
    form.bizType = row.bizType || ''
    form.bizId = row.bizId ?? null
    form.priority = row.priority || 'NORMAL'
    form.dueDate = row.dueDate
    form.status = row.status || 'PENDING'
  } else {
    form.id = null
    form.title = ''
    form.bizType = ''
    form.bizId = null
    form.priority = 'NORMAL'
    form.dueDate = null
    form.status = 'PENDING'
  }
  dialogVisible.value = true
}

async function submit() {
  if (!form.title?.trim()) { ElMessage.warning('请输入标题'); return }
  await todoSave(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

function done(row) {
  todoSave({ ...row, status: 'DONE' }).then(() => { ElMessage.success('已标记完成'); load() })
}

function del(row) {
  ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }).then(() => {
    todoDelete(row.id).then(() => { ElMessage.success('已删除'); load() })
  }).catch(() => {})
}

onMounted(load)
</script>

<style scoped>
.filter { margin-bottom: 12px; }
</style>
