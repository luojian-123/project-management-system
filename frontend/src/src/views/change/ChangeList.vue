<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>变更管理</span>
        <el-button type="primary" style="float:right" @click="openForm()">新增变更</el-button>
      </template>
      <el-form inline class="filter">
        <el-select v-model="projectId" placeholder="项目" clearable filterable style="width:200px" @focus="loadProjects">
          <el-option v-for="p in projects" :key="p.id" :label="p.projectName" :value="p.id" />
        </el-select>
        <el-select v-model="status" placeholder="状态" clearable style="width:120px">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="待审批" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
      </el-form>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="changeNo" label="变更单号" width="120" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="changeType" label="变更类型" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">{{ statusMap[row.status] || row.status }}</template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="applyTime" label="申请时间" width="160" />
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
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑变更' : '新增变更'" width="560px">
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
        <el-form-item label="变更类型">
          <el-input v-model="form.changeType" placeholder="变更类型" />
        </el-form-item>
        <el-form-item label="影响范围">
          <el-input v-model="form.impactScope" type="textarea" rows="2" />
        </el-form-item>
        <el-form-item label="审批流">
          <el-select v-model="form.flowId" placeholder="可选，选择审批流" clearable filterable style="width:100%" @focus="loadFlows">
            <el-option v-for="f in flows" :key="f.id" :label="f.name + ' (' + f.code + ')'" :value="f.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
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
import { changePage, changeSave, changeDelete } from '@/api/change'
import { approvalFlowList } from '@/api/config'
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
const flows = ref([])
const form = reactive({
  id: null, projectId: null, title: '', description: '', changeType: '', impactScope: '', flowId: null, status: 'DRAFT'
})
const statusMap = { DRAFT: '草稿', PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回' }

async function loadProjects() {
  if (projects.value.length) return
  const res = await projectPage({ page: 1, size: 500 })
  projects.value = res.list || []
}
async function loadFlows() {
  if (flows.value.length) return
  flows.value = await approvalFlowList() || []
}

async function load() {
  loading.value = true
  try {
    const res = await changePage({ page: page.value, size: size.value, projectId: projectId.value || undefined, status: status.value || undefined })
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
    form.changeType = row.changeType || ''
    form.impactScope = row.impactScope || ''
    form.flowId = row.flowId ?? null
    form.status = row.status || 'DRAFT'
  } else {
    form.id = null
    form.projectId = projectId.value || null
    form.title = ''
    form.description = ''
    form.changeType = ''
    form.impactScope = ''
    form.flowId = null
    form.status = 'DRAFT'
  }
  dialogVisible.value = true
}

async function submit() {
  if (!form.title?.trim() || !form.projectId) { ElMessage.warning('请选择项目并填写标题'); return }
  await changeSave(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

function del(row) {
  ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }).then(() => {
    changeDelete(row.id).then(() => { ElMessage.success('已删除'); load() })
  }).catch(() => {})
}

onMounted(() => { loadProjects(); load() })
</script>

<style scoped>
.filter { margin-bottom: 16px; }
</style>
