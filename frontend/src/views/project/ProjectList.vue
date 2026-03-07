<template>
  <div class="page">
    <div class="page-cards" v-draggable-cards>
      <el-card>
    <template #header>
      <span>项目管理</span>
      <div style="display:flex; gap:8px; align-items:center">
        <el-input v-model="keyword" placeholder="项目编号/名称" clearable style="width:180px" @keyup.enter="fetchList" />
        <el-select v-model="status" placeholder="状态" clearable style="width:100px" @change="fetchList">
          <el-option label="规划" value="planning" />
          <el-option label="进行中" value="running" />
          <el-option label="已结项" value="closed" />
        </el-select>
        <el-button type="primary" @click="fetchList">查询</el-button>
        <el-button v-if="isAdmin" type="primary" @click="openForm()">新增项目</el-button>
      </div>
    </template>
    <el-table v-loading="loading" :data="list" stripe border>
      <el-table-column prop="projectCode" label="项目编号" min-width="100" />
      <el-table-column label="项目名称" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">
          <span class="link-name" @click="goDetail(row)">{{ row.projectName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="customerName" label="客户" min-width="100" />
      <el-table-column prop="ownerName" label="负责人" min-width="80" />
      <el-table-column prop="planStart" label="计划开始" min-width="108" />
      <el-table-column prop="planEnd" label="计划结束" min-width="108" />
      <el-table-column label="状态" min-width="84">
        <template #default="{ row }">{{ projectStatusMap[row?.status] ?? row?.status ?? '-' }}</template>
      </el-table-column>
      <el-table-column v-if="isAdmin" label="操作" width="100" fixed="right" align="center">
        <template #default="{ row }">
          <div class="table-actions-cell">
            <el-dropdown trigger="click" @command="(cmd) => cmd === 'edit' ? openForm(row) : handleDelete(row)">
              <el-button type="primary" link>操作<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">编辑</el-dropdown-item>
                  <el-dropdown-item command="del" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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
    <el-dialog v-model="dialogVisible" title="项目" width="560px" @close="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="项目编号" prop="projectCode">
          <el-input v-model="form.projectCode" placeholder="项目编号" />
        </el-form-item>
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="form.projectName" placeholder="项目名称" />
        </el-form-item>
        <el-form-item label="客户" prop="customerName">
          <el-input v-model="form.customerName" placeholder="客户" />
        </el-form-item>
        <el-form-item label="计划开始" prop="planStart">
          <el-date-picker v-model="form.planStart" type="date" value-format="YYYY-MM-DD" placeholder="计划开始" style="width:100%" />
        </el-form-item>
        <el-form-item label="计划结束" prop="planEnd">
          <el-date-picker v-model="form.planEnd" type="date" value-format="YYYY-MM-DD" placeholder="计划结束" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="状态" style="width:100%">
            <el-option label="规划" value="planning" />
            <el-option label="进行中" value="running" />
            <el-option label="已结项" value="closed" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" rows="3" placeholder="描述" />
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { projectPage, projectSave, projectUpdate, projectDelete } from '@/api/project'

const router = useRouter()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.username === 'admin')
const loading = ref(false)
const projectStatusMap = {
  planning: '规划',
  PLANNING: '规划',
  running: '进行中',
  RUNNING: '进行中',
  closed: '已结项',
  CLOSED: '已结项'
}
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const keyword = ref('')
const status = ref('')
const dialogVisible = ref(false)
const formRef = ref()
const submitLoading = ref(false)
const form = reactive({
  id: null,
  projectCode: '',
  projectName: '',
  customerName: '',
  description: '',
  planStart: '',
  planEnd: '',
  status: 'planning'
})
const rules = {
  projectCode: [{ required: true, message: '请输入项目编号', trigger: 'blur' }],
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }]
}

async function fetchList() {
  loading.value = true
  try {
    const res = await projectPage({ page: page.value, size: size.value, keyword: keyword.value || undefined, status: status.value || undefined })
    list.value = res?.list ?? []
    total.value = res?.total ?? 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function goDetail(row) {
  router.push(`/project/${row.id}`)
}

function openForm(row) {
  if (row) {
    Object.assign(form, {
      id: row.id,
      projectCode: row.projectCode,
      projectName: row.projectName,
      customerName: row.customerName,
      description: row.description,
      planStart: row.planStart,
      planEnd: row.planEnd,
      status: row.status
    })
  } else {
    Object.assign(form, {
      id: null,
      projectCode: '',
      projectName: '',
      customerName: '',
      description: '',
      planStart: '',
      planEnd: '',
      status: 'planning'
    })
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
  submitLoading.value = true
  try {
    if (form.id) await projectUpdate(form)
    else await projectSave(form)
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
  ElMessageBox.confirm('确定删除该项目？', '提示', { type: 'warning' })
    .then(async () => {
      await projectDelete(row.id)
      ElMessage.success('删除成功')
      fetchList()
    })
    .catch(() => {})
}

onMounted(fetchList)
</script>
