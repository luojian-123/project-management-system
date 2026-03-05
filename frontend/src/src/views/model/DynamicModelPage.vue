<template>
  <div class="page">
    <el-card v-loading="configLoading">
      <template v-if="formConfig" #header>
        <span>{{ formConfig.formName }}</span>
        <el-button type="primary" style="float:right" @click="openForm()">新增</el-button>
      </template>
      <template v-if="!formConfig && !configLoading">
        <el-empty description="未找到该对象的表单配置，请在系统配置-表单配置中维护表单编码与接口路径" />
      </template>
      <template v-else-if="formConfig">
        <el-table :data="list" v-loading="loading">
          <el-table-column v-for="col in listColumns" :key="col.fieldName" :prop="col.fieldName" :label="col.fieldLabel" :width="col.listWidth || undefined" :min-width="col.listWidth ? undefined : 120" />
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
      </template>
    </el-card>
    <el-dialog v-if="formConfig" v-model="dialogVisible" :title="formData.id ? '编辑' : '新增'" width="520px">
      <el-form :model="formData" label-width="100px">
        <el-form-item v-for="f in formFields" :key="f.fieldName" :label="f.fieldLabel" :required="f.required === 1">
          <el-input v-if="f.fieldType === 'text'" v-model="formData[f.fieldName]" :placeholder="f.fieldLabel" />
          <el-input-number v-else-if="f.fieldType === 'number'" v-model="formData[f.fieldName]" style="width:100%" />
          <el-date-picker v-else-if="f.fieldType === 'date'" v-model="formData[f.fieldName]" type="date" value-format="YYYY-MM-DD" style="width:100%" />
          <el-select v-else-if="f.fieldType === 'select'" v-model="formData[f.fieldName]" :placeholder="f.fieldLabel" clearable style="width:100%">
            <el-option v-for="opt in getSelectOptions(f)" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
          <el-input v-else v-model="formData[f.fieldName]" :placeholder="f.fieldLabel" />
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { modelConfig } from '@/api/config'
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const formCode = computed(() => route.params.formCode)

const configLoading = ref(true)
const formConfig = ref(null)
const formFields = ref([])
const listColumns = computed(() => {
  return formFields.value
    .filter(f => f.showInList === 1)
    .sort((a, b) => (a.listOrder || 0) - (b.listOrder || 0))
    .map(f => ({ ...f, listWidth: f.listWidth ? parseInt(f.listWidth, 10) : undefined }))
})

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const dialogVisible = ref(false)
const formData = reactive({})

async function loadConfig() {
  if (!formCode.value) return
  configLoading.value = true
  try {
    const res = await modelConfig(formCode.value)
    formConfig.value = res.form
    formFields.value = (res.fields || []).sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
    const initial = {}
    formFields.value.forEach(f => { initial[f.fieldName] = null })
    Object.assign(formData, initial)
  } catch (e) {
    formConfig.value = null
    formFields.value = []
  } finally {
    configLoading.value = false
  }
}

function getListPath() {
  const p = formConfig.value?.apiListPath
  return p ? (p.startsWith('/') ? p : '/' + p) : ''
}

function getSavePath() {
  const p = formConfig.value?.apiSavePath
  return p ? (p.startsWith('/') ? p : '/' + p) : ''
}

async function load() {
  const path = getListPath()
  if (!path) return
  loading.value = true
  try {
    const res = await request.get(path, { params: { page: page.value, size: size.value } })
    list.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function openForm(row) {
  formFields.value.forEach(f => { formData[f.fieldName] = row ? row[f.fieldName] ?? null : null })
  if (row) formData.id = row.id
  else formData.id = null
  dialogVisible.value = true
}

function getSelectOptions(field) {
  try {
    const j = field.extraJson && JSON.parse(field.extraJson)
    if (j && Array.isArray(j.options)) return j.options
  } catch (_) {}
  return []
}

async function submit() {
  const path = getSavePath()
  if (!path) { ElMessage.warning('未配置保存接口'); return }
  const payload = { ...formData }
  const id = payload.id
  delete payload.id
  try {
    if (id) await request.put(path, { ...payload, id })
    else await request.post(path, payload)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch (_) {}
}

function del(row) {
  const path = getSavePath()
  if (!path) return
  ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }).then(() => {
    request.delete(path + '/' + row.id).then(() => {
      ElMessage.success('已删除')
      load()
    })
  }).catch(() => {})
}

watch(formCode, loadConfig, { immediate: true })
watch(formConfig, (cfg) => { if (cfg) load() }, { immediate: true })
onMounted(() => { if (formConfig.value) load() })
</script>

<style scoped>
.page { min-height: 200px; }
</style>
