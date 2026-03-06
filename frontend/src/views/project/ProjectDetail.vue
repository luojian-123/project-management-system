<template>
  <div class="page">
    <el-card v-loading="projectLoading">
      <template #header>
        <span>项目：{{ project?.projectName }}</span>
        <el-button link type="primary" @click="$router.push('/project')">返回列表</el-button>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="项目编码">{{ project?.projectCode }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ projectStatusLabel(project?.status) }}</el-descriptions-item>
        <el-descriptions-item label="计划开始">{{ project?.planStart }}</el-descriptions-item>
        <el-descriptions-item label="计划结束">{{ project?.planEnd }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ project?.description || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top:16px">
      <template #header>
        <span>任务管理</span>
        <el-button type="primary" size="small" @click="openTaskForm()">新增任务</el-button>
      </template>
      <el-table :data="taskList" v-loading="taskLoading" row-key="id" default-expand-all>
        <el-table-column prop="taskCode" label="任务编码" width="100" />
        <el-table-column label="任务名称" min-width="200">
          <template #default="{ row }">
            <router-link :to="`/task/${row.id}`" class="task-name-link">{{ row.taskName }}</router-link>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="负责人" width="100" />
        <el-table-column prop="planStart" label="计划开始" width="110" />
        <el-table-column prop="planEnd" label="计划结束" width="110" />
        <el-table-column prop="actualStart" label="实际开始" width="110" />
        <el-table-column prop="actualEnd" label="实际结束" width="110" />
        <el-table-column prop="progress" label="进度" width="80">
          <template #default="{ row }">{{ row.progress }}%</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">{{ taskStatusMap[row.status] || row.status }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openTaskForm(row)">编辑</el-button>
            <el-button link type="danger" @click="delTask(row)" v-if="!row.children?.length">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card style="margin-top:16px" v-if="taskList.length">
      <template #header>甘特图</template>
      <div class="gantt-wrap">
        <div class="gantt-head">
          <div class="gantt-col name">任务</div>
          <div class="gantt-col range">时间轴</div>
        </div>
        <div class="gantt-body">
          <div class="dependency-layer" v-if="dependencyLines.length">
            <svg xmlns="http://www.w3.org/2000/svg" :viewBox="`0 0 100 ${flatTasks.length * 40}`" preserveAspectRatio="none">
              <line v-for="(line, i) in dependencyLines" :key="i" :x1="line.fromPct" :y1="line.fromY" :x2="line.toPct" :y2="line.toY" stroke="#999" stroke-width="0.5" />
            </svg>
          </div>
          <div v-for="(t, idx) in flatTasks" :key="t.id" class="gantt-row">
            <div class="gantt-col name">
              <router-link :to="`/task/${t.id}`" class="task-name-link">{{ t.taskCode }} {{ t.taskName }}</router-link>
            </div>
            <div class="gantt-col range">
              <div
                v-if="t.planStart && t.planEnd"
                class="bar"
                :style="barStyle(t)"
                draggable="false"
                @mousedown.prevent="onBarMouseDown($event, t)"
              />
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="taskDialogVisible" :title="taskForm.id ? '编辑任务' : '新增任务'" width="520px">
      <el-form ref="taskFormRef" :model="taskForm" label-width="90px">
        <el-form-item label="任务名称" required>
          <el-input v-model="taskForm.taskName" placeholder="任务名称" />
        </el-form-item>
        <el-form-item label="父任务">
          <el-select v-model="taskForm.parentId" placeholder="无（顶级）" clearable style="width:100%">
            <el-option v-for="t in flatTasksForSelect" :key="t.id" :label="t.taskCode + ' ' + t.taskName" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="taskForm.assigneeId" placeholder="选择负责人" clearable filterable style="width:100%" @focus="loadTaskUsers">
            <el-option v-for="u in taskUsers" :key="u.id" :label="u.realName || u.username" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划开始">
          <el-date-picker v-model="taskForm.planStart" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="计划结束">
          <el-date-picker v-model="taskForm.planEnd" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="实际开始">
          <el-date-picker v-model="taskForm.actualStart" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="实际结束">
          <el-date-picker v-model="taskForm.actualEnd" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="前置依赖">
          <el-select v-model="taskForm.dependTaskIds" placeholder="选择依赖的前置任务" clearable multiple filterable style="width:100%">
            <el-option v-for="t in flatTasksForSelect" :key="t.id" :label="t.taskCode + ' ' + t.taskName" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="进度">
          <el-slider v-model="taskForm.progress" :min="0" :max="100" show-input />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="taskForm.status" style="width:100%">
            <el-option label="待办" value="TODO" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="DONE" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTask">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { projectGet } from '@/api/project'
import { taskListByProject, taskSave, taskDelete, taskGetDependencies, taskDependencies } from '@/api/task'
import { userPage } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const projectId = computed(() => Number(route.params.id))

const project = ref(null)
const projectLoading = ref(false)
const taskList = ref([])
const taskLoading = ref(false)
const taskDialogVisible = ref(false)
const taskFormRef = ref()
const taskUsers = ref([])
const taskForm = reactive({
  id: null, projectId: null, parentId: 0, taskName: '', assigneeId: null, planStart: null, planEnd: null, actualStart: null, actualEnd: null, progress: 0, status: 'TODO', dependTaskIds: []
})
const projectStatusLabels = {
  planning: '规划', PLANNING: '规划', running: '进行中', RUNNING: '进行中', closed: '已结项', CLOSED: '已结项',
  IN_PROGRESS: '进行中', PAUSED: '已暂停'
}
const taskStatusMap = { TODO: '待办', IN_PROGRESS: '进行中', DONE: '已完成', CANCELLED: '已取消' }
function projectStatusLabel(s) {
  if (s == null || s === '') return '-'
  return projectStatusLabels[s] ?? s
}

function flatten(tasks, out = []) {
  for (const t of tasks || []) {
    out.push(t)
    if (t.children?.length) flatten(t.children, out)
  }
  return out
}

const flatTasks = computed(() => flatten(taskList.value))
const flatTasksForSelect = computed(() => flatTasks.value.filter(t => t.id !== taskForm.id))

const ROW_H = 40
function barPct(t) {
  if (!t?.planStart || !t?.planEnd) return { left: 0, end: 0 }
  const start = ganttStart.value.getTime()
  const end = ganttEnd.value.getTime()
  const tStart = new Date(t.planStart).getTime()
  const tEnd = new Date(t.planEnd).getTime()
  const left = ((tStart - start) / (end - start)) * 100
  const width = ((tEnd - tStart) / (end - start)) * 100
  return { left: Math.max(0, left), end: Math.min(100, left + width) }
}
const dependencyLines = computed(() => {
  const flat = flatTasks.value
  const lines = []
  for (let i = 0; i < flat.length; i++) {
    const t = flat[i]
    const depIds = t.dependTaskIds || []
    const toPct = barPct(t).left
    const toY = i * ROW_H + ROW_H / 2
    for (const depId of depIds) {
      const j = flat.findIndex(x => x.id === depId)
      if (j < 0) continue
      const from = barPct(flat[j])
      const fromY = j * ROW_H + ROW_H / 2
      lines.push({ fromPct: from.end, fromY, toPct, toY })
        }
  }
  return lines
})

const ganttStart = computed(() => {
  const dates = flatTasks.value.flatMap(t => [t.planStart, t.planEnd]).filter(Boolean)
  if (!dates.length) return new Date()
  return new Date(Math.min(...dates.map(d => new Date(d))))
})
const ganttEnd = computed(() => {
  const dates = flatTasks.value.flatMap(t => [t.planStart, t.planEnd]).filter(Boolean)
  if (!dates.length) return new Date()
  return new Date(Math.max(...dates.map(d => new Date(d))))
})
const totalDays = computed(() => {
  const s = ganttStart.value.getTime()
  const e = ganttEnd.value.getTime()
  return Math.max(1, Math.ceil((e - s) / (24 * 3600 * 1000)))
})

function barStyle(t) {
  if (!t.planStart || !t.planEnd) return { display: 'none' }
  const start = ganttStart.value.getTime()
  const end = ganttEnd.value.getTime()
  const tStart = new Date(t.planStart).getTime()
  const tEnd = new Date(t.planEnd).getTime()
  const left = ((tStart - start) / (end - start)) * 100
  const width = ((tEnd - tStart) / (end - start)) * 100
  return { left: Math.max(0, left) + '%', width: Math.min(100 - left, width) + '%', cursor: 'move' }
}

function onBarMouseDown(e, t) {
  const rangeEl = e.target.closest('.gantt-col.range')
  if (!rangeEl || !t.planStart || !t.planEnd) return
  const startX = e.clientX
  const startPlanStart = new Date(t.planStart).getTime()
  const startPlanEnd = new Date(t.planEnd).getTime()
  const rangeStart = ganttStart.value.getTime()
  const rangeEnd = ganttEnd.value.getTime()
  const totalMs = rangeEnd - rangeStart
  const onMove = (e2) => {
    const deltaX = e2.clientX - startX
    const rangeWidth = rangeEl.offsetWidth
    const deltaMs = (deltaX / rangeWidth) * totalMs
    const newStart = Math.max(rangeStart, Math.min(rangeEnd, startPlanStart + deltaMs))
    const newEnd = Math.max(rangeStart, Math.min(rangeEnd, startPlanEnd + deltaMs))
    if (newEnd <= newStart) return
    t.planStart = new Date(newStart).toISOString().slice(0, 10)
    t.planEnd = new Date(newEnd).toISOString().slice(0, 10)
  }
  const onUp = async () => {
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
    try {
      await taskSave({ id: t.id, projectId: t.projectId, parentId: t.parentId ?? 0, taskName: t.taskName, assigneeId: t.assigneeId, planStart: t.planStart, planEnd: t.planEnd, actualStart: t.actualStart, actualEnd: t.actualEnd, progress: t.progress ?? 0, status: t.status ?? 'TODO' })
      ElMessage.success('已更新计划时间')
    } catch (err) {
      loadTasks()
    }
  }
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
}

async function loadProject() {
  if (!projectId.value) return
  projectLoading.value = true
  try {
    project.value = await projectGet(projectId.value)
  } finally {
    projectLoading.value = false
  }
}

async function loadTasks() {
  if (!projectId.value) return
  taskLoading.value = true
  try {
    taskList.value = await taskListByProject(projectId.value, true)
  } finally {
    taskLoading.value = false
  }
}

async function loadTaskUsers() {
  if (taskUsers.value.length) return
  const res = await userPage({ page: 1, size: 500 })
  taskUsers.value = res.list || []
}

async function openTaskForm(row) {
  if (row) {
    taskForm.id = row.id
    taskForm.projectId = projectId.value
    taskForm.parentId = row.parentId || 0
    taskForm.taskName = row.taskName
    taskForm.assigneeId = row.assigneeId ?? null
    taskForm.planStart = row.planStart
    taskForm.planEnd = row.planEnd
    taskForm.actualStart = row.actualStart ?? null
    taskForm.actualEnd = row.actualEnd ?? null
    taskForm.progress = row.progress ?? 0
    taskForm.status = row.status || 'TODO'
    taskForm.dependTaskIds = (await taskGetDependencies(row.id)) || []
  } else {
    taskForm.id = null
    taskForm.projectId = projectId.value
    taskForm.parentId = 0
    taskForm.taskName = ''
    taskForm.assigneeId = null
    taskForm.planStart = null
    taskForm.planEnd = null
    taskForm.actualStart = null
    taskForm.actualEnd = null
    taskForm.progress = 0
    taskForm.status = 'TODO'
    taskForm.dependTaskIds = []
  }
  taskDialogVisible.value = true
}

async function submitTask() {
  if (!taskForm.taskName?.trim()) { ElMessage.warning('请输入任务名称'); return }
  const payload = { id: taskForm.id, projectId: taskForm.projectId, parentId: taskForm.parentId || 0, taskName: taskForm.taskName, assigneeId: taskForm.assigneeId, planStart: taskForm.planStart, planEnd: taskForm.planEnd, actualStart: taskForm.actualStart, actualEnd: taskForm.actualEnd, progress: taskForm.progress, status: taskForm.status }
  const saved = await taskSave(payload)
  const taskId = saved?.id || taskForm.id
  if (taskId && taskForm.dependTaskIds?.length) await taskDependencies(taskId, taskForm.dependTaskIds)
  ElMessage.success('保存成功')
  taskDialogVisible.value = false
  loadTasks()
}

function delTask(row) {
  ElMessageBox.confirm('确定删除该任务？', '提示', { type: 'warning' }).then(() => {
    taskDelete(row.id).then(() => { ElMessage.success('已删除'); loadTasks() })
  }).catch(() => {})
}

watch(projectId, () => { loadProject(); loadTasks() }, { immediate: true })
onMounted(() => { loadProject(); loadTasks() })
</script>

<style scoped>
.task-name-link { color: var(--el-color-primary); text-decoration: none; }
.task-name-link:hover { text-decoration: underline; }
.gantt-wrap { overflow-x: auto; }
.gantt-head, .gantt-row { display: flex; min-width: 800px; }
.gantt-col.name { width: 240px; flex-shrink: 0; padding: 8px; border: 1px solid #eee; }
.gantt-col.range { flex: 1; min-width: 400px; position: relative; padding: 8px; border: 1px solid #eee; }
.gantt-row .bar { position: absolute; top: 50%; transform: translateY(-50%); height: 20px; background: #409eff; border-radius: 4px; }
.gantt-body { position: relative; }
.dependency-layer { position: absolute; left: 240px; top: 0; right: 0; bottom: 0; pointer-events: none; z-index: 0; }
.gantt-row { position: relative; z-index: 1; }
.gantt-row .bar { z-index: 1; }
.dependency-layer svg { width: 100%; height: 100%; display: block; }
</style>
