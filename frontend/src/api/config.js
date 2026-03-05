import request from './request'

export function approvalFlowList() {
  return request.get('/sys/config/approval/flow/list')
}

export function approvalFlowGet(id) {
  return request.get(`/sys/config/approval/flow/${id}`)
}

export function approvalFlowNodes(id) {
  return request.get(`/sys/config/approval/flow/${id}/nodes`)
}

export function approvalFlowSave(data) {
  return request.post('/sys/config/approval/flow', data)
}

export function approvalFlowDelete(id) {
  return request.delete(`/sys/config/approval/flow/${id}`)
}

export function formConfigList() {
  return request.get('/sys/config/form/list')
}

export function formConfigGet(id) {
  return request.get(`/sys/config/form/${id}`)
}

export function formConfigFields(id) {
  return request.get(`/sys/config/form/${id}/fields`)
}

export function formConfigSave(data) {
  return request.post('/sys/config/form', data)
}

export function formConfigDelete(id) {
  return request.delete(`/sys/config/form/${id}`)
}

/** 获取所有可建模对象（用于菜单与动态页关联） */
export function modelList() {
  return request.get('/sys/config/model/list')
}

/** 按对象编码获取模型配置（表单+字段），供动态列表/表单渲染 */
export function modelConfig(formCode) {
  return request.get(`/sys/config/model/${formCode}`)
}

export function tabConfigList(bizType) {
  return request.get('/sys/config/tab/list', { params: bizType ? { bizType } : {} })
}

export function tabConfigSave(data) {
  return request.post('/sys/config/tab', data)
}

export function tabConfigDelete(id) {
  return request.delete(`/sys/config/tab/${id}`)
}
