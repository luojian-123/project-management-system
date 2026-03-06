<template>
  <div class="page">
    <el-card v-loading="loading">
      <template #header>
        <span>任务详情</span>
        <el-button type="primary" @click="goBack">返回</el-button>
      </template>
      <div v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务编码">{{ detail.taskCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="任务名称">{{ detail.taskName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所属项目">
            <router-link v-if="detail.projectId" :to="`/project/${detail.projectId}`" class="link-name">{{ detail.projectName || '-' }}</router-link>
            <span v-else>{{ detail.projectName || '-' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="负责人">{{ detail.assigneeName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="计划开始">{{ detail.planStart || '-' }}</el-descriptions-item>
          <el-descriptions-item label="计划结束">{{ detail.planEnd || '-' }}</el-descriptions-item>
          <el-descriptions-item label="实际开始">{{ detail.actualStart || '-' }}</el-descriptions-item>
          <el-descriptions-item label="实际结束">{{ detail.actualEnd || '-' }}</el-descriptions-item>
          <el-descriptions-item label="进度">{{ detail.progress != null ? detail.progress + '%' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusText(detail.status) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(detail.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatTime(detail.updatedAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div v-else-if="!loading" style="text-align:center; padding: 24px 0;">
        <el-empty description="任务不存在或已删除" />
        <el-button type="primary" style="margin-top:12px" @click="goBack">返回</el-button>
      </div>
    </el-card>

    <el-card v-if="detail" style="margin-top:16px">
      <template #header><span>变更记录</span></template>
      <div>
        <el-table v-loading="changesLoading" :data="changes" stripe>
          <el-table-column prop="createdAt" label="时间" width="180">
            <template #default="scope">{{ formatTime(scope.row.createdAt) }}</template>
          </el-table-column>
          <el-table-column prop="operatorName" label="操作人" width="120" />
          <el-table-column prop="action" label="操作类型" width="100" />
          <el-table-column prop="content" label="变更内容" min-width="200" show-overflow-tooltip />
        </el-table>
        <el-empty v-if="!changesLoading && !changes.length" description="暂无变更记录" :image-size="80" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { taskGet, taskGetChanges } from '@/api/task'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const changesLoading = ref(false)
const detail = ref(null)
const changes = ref([])

const id = computed(() => {
  const p = route.params.id
  return p != null && p !== '' ? String(p) : null
})

const statusMap = { TODO: '待办', IN_PROGRESS: '进行中', DONE: '已完成', CANCELLED: '已取消' }
function statusText(s) {
  return statusMap[s] || s || '-'
}

function formatTime(val) {
  if (!val) return '-'
  const d = new Date(val)
  return isNaN(d.getTime()) ? val : d.toLocaleString('zh-CN')
}

async function fetchDetail() {
  if (!id.value) return
  loading.value = true
  detail.value = null
  changes.value = []
  try {
    const res = await taskGet(id.value)
    detail.value = res && typeof res === 'object' ? res : null
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

async function fetchChanges() {
  if (!id.value || !detail.value) return
  changesLoading.value = true
  try {
    const res = await taskGetChanges(id.value)
    changes.value = Array.isArray(res) ? res : []
  } catch {
    changes.value = []
  } finally {
    changesLoading.value = false
  }
}

function goBack() {
  if (window.history.length > 1) router.back()
  else router.push('/todo')
}

watch(id, (newId) => {
  if (!newId) return
  fetchDetail().then(() => {
    if (detail.value) fetchChanges()
  })
}, { immediate: true })
</script>

<style scoped>
.page { max-width: 100%; }
.link-name { color: var(--el-color-primary); text-decoration: none; cursor: pointer; }
.link-name:hover { text-decoration: underline; }
</style>
