<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>项目管理</span>
        <el-button type="primary" style="float:right" @click="openForm()">新增项目</el-button>
      </template>
      <el-form inline class="filter">
        <el-input v-model="keyword" placeholder="编码/名称" clearable style="width:180px" />
        <el-select v-model="status" placeholder="状态" clearable style="width:120px">
          <el-option label="规划中" value="PLANNING" />
          <el-option label="进行中" value="IN_PROGRESS" />
          <el-option label="已暂停" value="PAUSED" />
          <el-option label="已关闭" value="CLOSED" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
      </el-form>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="projectCode" label="项目编码" width="120" />
        <el-table-column prop="projectName" label="项目名称" min-width="160" />
        <el-table-column prop="ownerName" label="负责人" width="100" />
        <el-table-column prop="planStart" label="计划开始" width="110" />
        <el-table-column prop="planEnd" label="计划结束" width="110" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">{{ statusMap[row.status] || row.status }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push('/project/' + row.id)">详情</el-button>
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
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑项目' : '新增项目'" width="560px">
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="项目编码" required>
          <el-input v-model="form.projectCode" placeholder="项目编码" />
        </el-form-item>
        <el-form-item label="项目名称" required>
          <el-input v-model="form.projectName" placeholder="项目名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="form.ownerId" placeholder="选择负责人" clearable filterable style="width:100%" @focus="loadUsers">
            <el-option v-for="u in users" :key="u.id" :label="u.realName || u.username" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划开始">
          <el-date-picker v-model="form.planStart" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="计划结束">
          <el-date-picker v-model="form.planEnd" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="规划中" value="PLANNING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已暂停" value="PAUSED" />
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
import { projectPage, projectSave, projectUpdate, projectDelete } from '@/api/project'
import { userPage } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const keyword = ref('')
const status = ref('')
const dialogVisible = ref(false)
const formRef = ref()
const users = ref([])
const form = reactive({
  id: null, projectCode: '', projectName: '', description: '', ownerId: null,
  planStart: null, planEnd: null, status: 'PLANNING'
})
const statusMap = { PLANNING: '规划中', IN_PROGRESS: '进行中', PAUSED: '已暂停', CLOSED: '已关闭' }

async function load() {
  loading.value = true
  try {
    const res = await projectPage({ page: page.value, size: size.value, keyword: keyword.value || undefined, status: status.value || undefined })
    list.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function loadUsers() {
  if (users.value.length) return
  const res = await userPage({ page: 1, size: 500 })
  users.value = res.list || []
}

function openForm(row) {
  if (row) {
    form.id = row.id
    form.projectCode = row.projectCode
    form.projectName = row.projectName
    form.description = row.description
    form.ownerId = row.ownerId ?? null
    form.planStart = row.planStart
    form.planEnd = row.planEnd
    form.status = row.status || 'PLANNING'
  } else {
    form.id = null
    form.projectCode = ''
    form.projectName = ''
    form.description = ''
    form.ownerId = null
    form.planStart = null
    form.planEnd = null
    form.status = 'PLANNING'
  }
  dialogVisible.value = true
}

async function submit() {
  if (!form.projectCode?.trim() || !form.projectName?.trim()) { ElMessage.warning('请填写编码和名称'); return }
  if (form.id) await projectUpdate(form)
  else await projectSave(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

function del(row) {
  ElMessageBox.confirm('确定删除该项目？', '提示', { type: 'warning' }).then(() => {
    projectDelete(row.id).then(() => { ElMessage.success('已删除'); load() })
  }).catch(() => {})
}

onMounted(load)
</script>

<style scoped>
.filter { margin-bottom: 16px; }
</style>
