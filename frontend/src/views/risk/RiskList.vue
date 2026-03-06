<template>
  <div class="page">
    <PageCard>
      <template #header><span>风险管理</span></template>
      <template #actions>
        <el-button type="primary" @click="openForm()">新增</el-button>
      </template>
      <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="标题" min-width="160">
        <template #default="{ row }">
          <span class="link-name" @click="openForm(row)">{{ row.title || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="projectId" label="项目ID" width="90" />
      <el-table-column prop="level" label="等级" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">{{ riskStatusMap[row?.status] ?? row?.status ?? '-' }}</template>
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
    <el-dialog v-model="dialogVisible" title="风险" width="500px" @close="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="标题" />
        </el-form-item>
        <el-form-item label="项目ID" prop="projectId">
          <el-input-number v-model="form.projectId" placeholder="项目ID" style="width:100%" />
        </el-form-item>
        <el-form-item label="等级" prop="level">
          <el-select v-model="form.level" placeholder="等级" style="width:100%">
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="状态" style="width:100%">
            <el-option label="识别" value="identified" />
            <el-option label="应对中" value="mitigating" />
            <el-option label="已关闭" value="closed" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
    </PageCard>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { riskPage, riskSave, riskUpdate, riskDelete } from '@/api/risk'

const loading = ref(false)
const riskStatusMap = { identified: '识别', mitigating: '应对中', closed: '已关闭' }
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const dialogVisible = ref(false)
const formRef = ref()
const submitLoading = ref(false)
const form = reactive({ id: null, title: '', projectId: null, level: 'medium', status: 'identified' })
const rules = { title: [{ required: true, message: '请输入标题', trigger: 'blur' }] }

async function fetchList() {
  loading.value = true
  try {
    const res = await riskPage({ page: page.value, size: size.value })
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
  if (row) Object.assign(form, { id: row.id, title: row.title, projectId: row.projectId, level: row.level, status: row.status })
  else Object.assign(form, { id: null, title: '', projectId: null, level: 'medium', status: 'identified' })
  dialogVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (form.id) await riskUpdate(form)
    else await riskSave(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定删除该风险？', '提示', { type: 'warning' })
    .then(async () => {
      await riskDelete(row.id)
      ElMessage.success('删除成功')
      fetchList()
    })
    .catch(() => {})
}

onMounted(fetchList)
</script>
