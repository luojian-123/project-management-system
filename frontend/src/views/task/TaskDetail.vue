<template>
  <div class="page">
    <div class="page-cards" v-draggable-cards>
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
      <template #header>
        <span>交付物</span>
        <el-button type="primary" size="small" @click="openDeliverableForm()">新增</el-button>
      </template>
      <div>
        <el-table v-loading="deliverablesLoading" :data="deliverables" stripe border>
          <el-table-column prop="name" label="名称" min-width="140" show-overflow-tooltip />
          <el-table-column prop="type" label="类型" min-width="88">
            <template #default="{ row }">{{ deliverableTypeMap[row.type] || row.type || '-' }}</template>
          </el-table-column>
          <el-table-column label="链接/路径" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <a v-if="row.url" :href="row.url" target="_blank" rel="noopener" class="link-name">{{ row.url }}</a>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
          <el-table-column label="上传文档" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">{{ row.attachmentPath ? (row.attachmentName || '已上传') : '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right" align="center">
            <template #default="{ row }">
              <el-dropdown trigger="click" @command="(cmd) => onDeliverableCommand(cmd, row)">
                <el-button type="primary" link>操作<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="row.attachmentPath" command="preview">预览</el-dropdown-item>
                    <el-dropdown-item v-if="row.attachmentPath" command="download">下载</el-dropdown-item>
                    <el-dropdown-item command="edit" :divided="!!row.attachmentPath">编辑</el-dropdown-item>
                    <el-dropdown-item command="del" divided>删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!deliverablesLoading && !deliverables.length" description="暂无交付物" :image-size="80" />
      </div>
    </el-card>

    <el-card v-if="detail" style="margin-top:16px">
      <template #header><span>变更记录</span></template>
      <div>
        <el-table v-loading="changesLoading" :data="changes" stripe border>
          <el-table-column prop="createdAt" label="时间" min-width="140">
            <template #default="scope">{{ formatTime(scope.row.createdAt) }}</template>
          </el-table-column>
          <el-table-column prop="operatorName" label="操作人" min-width="100" />
          <el-table-column prop="action" label="操作类型" min-width="90" />
          <el-table-column prop="content" label="变更内容" min-width="200" show-overflow-tooltip />
        </el-table>
        <el-empty v-if="!changesLoading && !changes.length" description="暂无变更记录" :image-size="80" />
      </div>
    </el-card>
    </div>

    <el-dialog v-model="previewVisible" :title="'预览：' + (previewName || '')" width="90%" top="3vh" class="doc-preview-dialog" @closed="revokePreviewUrl">
      <div v-if="previewType === 'pdf'" class="preview-body preview-pdf">
        <iframe :src="previewUrl" class="preview-iframe" title="文档预览" />
      </div>
      <div v-else-if="previewType === 'image'" class="preview-body preview-image">
        <img :src="previewUrl" :alt="previewName" class="preview-img" />
      </div>
      <div v-else-if="previewType === 'text'" class="preview-body preview-text">
        <pre class="preview-pre">{{ previewText }}</pre>
      </div>
      <div v-else class="preview-body preview-other">
        <p>该类型暂不支持在线预览，请下载后查看。</p>
        <el-button type="primary" @click="downloadCurrentPreview">下载</el-button>
      </div>
    </el-dialog>

    <el-dialog v-model="deliverableFormVisible" :title="deliverableForm.id ? '编辑交付物' : '新增交付物'" width="480px" @closed="deliverableFormRef?.resetFields(); deliverableFileList = []">
      <el-form ref="deliverableFormRef" :model="deliverableForm" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="deliverableForm.name" placeholder="交付物名称" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="deliverableForm.type" placeholder="请选择" clearable style="width:100%">
            <el-option label="文档" value="文档" />
            <el-option label="代码" value="代码" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="链接/路径">
          <el-input v-model="deliverableForm.url" placeholder="URL 或文件路径" />
        </el-form-item>
        <el-form-item label="上传文档">
          <el-upload
            :action="uploadAction"
            :http-request="uploadDeliverableRequest"
            :show-file-list="true"
            :limit="1"
            :on-remove="onDeliverableFileRemove"
            :file-list="deliverableFileList"
          >
            <el-button type="primary" size="small">选择文件</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="deliverableForm.remark" type="textarea" :rows="2" placeholder="备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deliverableFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="deliverableSubmitting" @click="submitDeliverable">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { taskGet, taskGetChanges, taskGetDeliverables, taskSaveDeliverable, taskDeleteDeliverable } from '@/api/task'
import { uploadDeliverableFile, downloadFile, previewFile } from '@/api/file'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const changesLoading = ref(false)
const deliverablesLoading = ref(false)
const detail = ref(null)
const changes = ref([])
const deliverables = ref([])
const deliverableFormVisible = ref(false)
const deliverableFormRef = ref(null)
const deliverableSubmitting = ref(false)
const deliverableForm = ref({ name: '', type: '', url: '', remark: '', attachmentPath: '', attachmentName: '' })
const deliverableFileList = ref([])
const uploadAction = '#'
const deliverableTypeMap = { 文档: '文档', 代码: '代码', 其他: '其他' }
const previewVisible = ref(false)
const previewUrl = ref('')
const previewType = ref('other')
const previewName = ref('')
const previewText = ref('')
const previewRow = ref(null)

/** 根据文件名推断交付物类型：文档 / 代码 / 其他 */
function inferDeliverableType(filename) {
  if (!filename || typeof filename !== 'string') return '其他'
  const ext = filename.split('.').pop()?.toLowerCase() || ''
  const docExt = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'md', 'rtf', 'odt', 'ods', 'odp', 'csv', 'vsd', 'drawio', 'wps', 'pages', 'numbers', 'key']
  const codeExt = ['js', 'ts', 'jsx', 'tsx', 'vue', 'java', 'py', 'go', 'rs', 'c', 'cpp', 'h', 'hpp', 'cs', 'php', 'rb', 'swift', 'kt', 'scala', 'sql', 'html', 'htm', 'css', 'scss', 'sass', 'less', 'json', 'xml', 'yml', 'yaml', 'sh', 'bat', 'ps1', 'gradle', 'pom', 'r', 'm', 'mm', 'pl', 'lua', 'groovy', 'dart', 'fs', 'vb', 'rb', 'ex', 'exs', 'clj', 'scm', 'go', 'mod', 'sum']
  if (docExt.includes(ext)) return '文档'
  if (codeExt.includes(ext)) return '代码'
  return '其他'
}

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
  deliverables.value = []
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

async function fetchDeliverables() {
  if (!id.value || !detail.value) return
  deliverablesLoading.value = true
  try {
    const res = await taskGetDeliverables(id.value)
    deliverables.value = Array.isArray(res) ? res : []
  } catch {
    deliverables.value = []
  } finally {
    deliverablesLoading.value = false
  }
}

function openDeliverableForm(row) {
  if (row) {
    deliverableForm.value = {
      id: row.id,
      taskId: id.value,
      name: row.name || '',
      type: row.type || '',
      url: row.url || '',
      remark: row.remark || '',
      attachmentPath: row.attachmentPath || '',
      attachmentName: row.attachmentName || ''
    }
    deliverableFileList.value = row.attachmentName ? [{ name: row.attachmentName }] : []
  } else {
    deliverableForm.value = { taskId: id.value, name: '', type: '', url: '', remark: '', attachmentPath: '', attachmentName: '' }
    deliverableFileList.value = []
  }
  deliverableFormVisible.value = true
}

async function uploadDeliverableRequest({ file, onSuccess, onError }) {
  try {
    const res = await uploadDeliverableFile(file)
    deliverableForm.value.attachmentPath = res.path
    deliverableForm.value.attachmentName = res.name
    deliverableFileList.value = [{ name: res.name }]
    // 名称自动识别为文档名称，类型根据扩展名自动识别
    deliverableForm.value.name = res.name
    deliverableForm.value.type = inferDeliverableType(res.name)
    onSuccess(res)
  } catch (e) {
    onError(e)
  }
}

function onDeliverableFileRemove() {
  deliverableForm.value.attachmentPath = ''
  deliverableForm.value.attachmentName = ''
  deliverableFileList.value = []
}

function getPreviewType(filename) {
  if (!filename) return 'other'
  const ext = filename.split('.').pop()?.toLowerCase() || ''
  if (['pdf'].includes(ext)) return 'pdf'
  if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg', 'bmp'].includes(ext)) return 'image'
  if (['txt', 'md', 'json', 'xml', 'html', 'htm', 'css', 'js', 'ts', 'vue', 'java', 'py', 'c', 'cpp', 'h', 'sql', 'yml', 'yaml', 'sh', 'bat'].includes(ext)) return 'text'
  return 'other'
}

async function openPreview(row) {
  if (!row.attachmentPath) return
  try {
    const blob = await previewFile(row.attachmentPath)
    const type = getPreviewType(row.attachmentName || '')
    previewRow.value = row
    previewName.value = row.attachmentName || '文档'
    previewType.value = type
    previewText.value = ''
    if (type === 'text') {
      previewText.value = await new Response(blob).text()
    } else {
      previewUrl.value = URL.createObjectURL(blob)
    }
    previewVisible.value = true
  } catch (e) {
    ElMessage.error('预览失败')
  }
}

function revokePreviewUrl() {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
  }
  previewRow.value = null
}

function downloadCurrentPreview() {
  if (previewRow.value) downloadDeliverableFile(previewRow.value)
}

function onDeliverableCommand(cmd, row) {
  if (cmd === 'preview') openPreview(row)
  else if (cmd === 'download') downloadDeliverableFile(row)
  else if (cmd === 'edit') openDeliverableForm(row)
  else if (cmd === 'del') delDeliverable(row)
}

async function downloadDeliverableFile(row) {
  if (!row.attachmentPath) return
  try {
    const blob = await downloadFile(row.attachmentPath)
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = row.attachmentName || '下载'
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    ElMessage.error('下载失败')
  }
}

async function submitDeliverable() {
  const form = deliverableForm.value
  if (!form.name?.trim()) {
    ElMessage.warning('请输入交付物名称')
    return
  }
  deliverableSubmitting.value = true
  try {
    await taskSaveDeliverable({ ...form, taskId: id.value })
    ElMessage.success(form.id ? '更新成功' : '添加成功')
    deliverableFormVisible.value = false
    fetchDeliverables()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    deliverableSubmitting.value = false
  }
}

async function delDeliverable(row) {
  try {
    await ElMessageBox.confirm('确定删除该交付物？', '提示', { type: 'warning' })
    await taskDeleteDeliverable(row.id)
    ElMessage.success('已删除')
    fetchDeliverables()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.message || '删除失败')
  }
}

function goBack() {
  if (window.history.length > 1) router.back()
  else router.push('/todo')
}

watch(id, (newId) => {
  if (!newId) return
  fetchDetail().then(() => {
    if (detail.value) {
      fetchChanges()
      fetchDeliverables()
    }
  })
}, { immediate: true })
</script>

<style scoped>
.page { max-width: 100%; }
.link-name { color: var(--el-color-primary); text-decoration: none; cursor: pointer; }
.link-name:hover { text-decoration: underline; }
</style>
<style>
.doc-preview-dialog .el-dialog__body { padding: 12px; max-height: 84vh; overflow: auto; }
.preview-body { min-height: 60vh; display: flex; justify-content: center; align-items: flex-start; }
.preview-pdf .preview-iframe { width: 100%; height: 80vh; border: none; }
.preview-image .preview-img { max-width: 100%; max-height: 80vh; object-fit: contain; }
.preview-text { justify-content: flex-start; }
.preview-text .preview-pre { white-space: pre-wrap; word-break: break-word; padding: 12px; margin: 0; font-size: 13px; max-height: 80vh; overflow: auto; width: 100%; }
.preview-other { flex-direction: column; gap: 12px; padding: 24px; }
</style>
