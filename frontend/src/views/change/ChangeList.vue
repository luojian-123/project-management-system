<template>
  <div class="page">
    <el-card>
    <template #header>
      <span>变更管理</span>
      <el-button type="primary" @click="openForm()">新增</el-button>
    </template>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="标题" min-width="160">
        <template #default="{ row }">
          <span class="link-name" @click="openForm(row)">{{ row.title || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="projectId" label="项目ID" width="90" />
      <el-table-column prop="changeType" label="类型" width="100" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">{{ changeStatusMap[row?.status] ?? row?.status ?? '-' }}</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column label="操作" width="80" fixed="right" align="center">
        <template #default="{ row }">
          <el-dropdown trigger="click" @command="(cmd) => cmd === 'edit' ? openForm(row) : handleDelete(row)">
            <el-button type="primary" link>操作<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit">编辑</el-dropdown-item>
                <el-dropdown-item command="del" divided>删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
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
    <el-dialog v-model="dialogVisible" title="变更" width="500px" @close="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="标题" />
        </el-form-item>
        <el-form-item label="项目ID" prop="projectId">
          <el-input-number v-model="form.projectId" placeholder="项目ID" style="width:100%" />
        </el-form-item>
        <el-form-item label="类型" prop="changeType">
          <el-input v-model="form.changeType" placeholder="变更类型" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="状态" style="width:100%">
            <el-option label="待审批" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
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
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { changePage, changeSave, changeUpdate, changeDelete } from '@/api/change'

const loading = ref(false)
const changeStatusMap = { pending: '待审批', approved: '已通过', rejected: '已拒绝' }
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const dialogVisible = ref(false)
const formRef = ref()
const submitLoading = ref(false)
const form = reactive({ id: null, title: '', projectId: null, changeType: '', status: 'pending' })
const rules = { title: [{ required: true, message: '请输入标题', trigger: 'blur' }] }

async function fetchList() {
  loading.value = true
  try {
    const res = await changePage({ page: page.value, size: size.value })
    list.value = res?.list ?? []
    total.value = res?.total ?? 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function openForm(row) {
  if (row) Object.assign(form, { id: row.id, title: row.title, projectId: row.projectId, changeType: row.changeType, status: row.status })
  else Object.assign(form, { id: null, title: '', projectId: null, changeType: '', status: 'pending' })
  dialogVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (form.id) await changeUpdate(form)
    else await changeSave(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定删除该变更？', '提示', { type: 'warning' })
    .then(async () => {
      await changeDelete(row.id)
      ElMessage.success('删除成功')
      fetchList()
    })
    .catch(() => {})
}

onMounted(fetchList)
</script>
