<template>
  <el-card>
    <template #header>
      <span>成本管理</span>
      <el-button type="primary" style="float:right" @click="openForm()">新增</el-button>
    </template>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="成本类型/备注" min-width="140">
        <template #default="{ row }">
          <span class="link-name" @click="openForm(row)">{{ row.costType || row.remark || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="projectId" label="项目ID" width="90" />
      <el-table-column prop="budgetAmount" label="预算" width="100" />
      <el-table-column prop="actualAmount" label="实际" width="100" />
      <el-table-column prop="occurDate" label="发生日期" width="120" />
      <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
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
    <el-dialog v-model="dialogVisible" title="成本" width="500px" @close="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="项目ID" prop="projectId">
          <el-input-number v-model="form.projectId" placeholder="项目ID" style="width:100%" />
        </el-form-item>
        <el-form-item label="类型" prop="costType">
          <el-input v-model="form.costType" placeholder="成本类型" />
        </el-form-item>
        <el-form-item label="预算" prop="budgetAmount">
          <el-input-number v-model="form.budgetAmount" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="实际" prop="actualAmount">
          <el-input-number v-model="form.actualAmount" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="发生日期" prop="occurDate">
          <el-date-picker v-model="form.occurDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { costPage, costSave, costUpdate, costDelete } from '@/api/cost'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const dialogVisible = ref(false)
const formRef = ref()
const submitLoading = ref(false)
const form = reactive({
  id: null,
  projectId: null,
  costType: '',
  budgetAmount: null,
  actualAmount: null,
  occurDate: '',
  remark: ''
})
const rules = {}

async function fetchList() {
  loading.value = true
  try {
    const res = await costPage({ page: page.value, size: size.value })
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
  if (row) {
    Object.assign(form, { id: row.id, projectId: row.projectId, costType: row.costType, budgetAmount: row.budgetAmount, actualAmount: row.actualAmount, occurDate: row.occurDate, remark: row.remark })
  } else {
    Object.assign(form, { id: null, projectId: null, costType: '', budgetAmount: null, actualAmount: null, occurDate: '', remark: '' })
  }
  dialogVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate().catch(() => {})
  submitLoading.value = true
  try {
    if (form.id) await costUpdate(form)
    else await costSave(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定删除该成本记录？', '提示', { type: 'warning' })
    .then(async () => {
      await costDelete(row.id)
      ElMessage.success('删除成功')
      fetchList()
    })
    .catch(() => {})
}

onMounted(fetchList)
</script>
