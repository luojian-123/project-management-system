import request from './request'

/** 按责任人分页查询任务（当前登录用户作为负责人的项目任务） */
export function taskPageByAssignee(params) {
  return request.get('/task/by-assignee', { params })
}

export function taskListByProject(projectId, tree = false) {
  return request.get(`/task/project/${projectId}`, { params: { tree } })
}

export function taskGet(id) {
  return request.get(`/task/${id}`)
}

/** 任务变更记录 */
export function taskGetChanges(id) {
  return request.get(`/task/${id}/changes`)
}

/** 任务交付物列表 */
export function taskGetDeliverables(taskId) {
  return request.get(`/task/${taskId}/deliverables`)
}

/** 新增或更新交付物（有 id 为更新） */
export function taskSaveDeliverable(data) {
  return request.post('/task/deliverables', data)
}

/** 删除交付物 */
export function taskDeleteDeliverable(deliverableId) {
  return request.delete(`/task/deliverables/${deliverableId}`)
}

export function taskSave(data) {
  return request.post('/task', data)
}

export function taskUpdate(data) {
  return request.put('/task', data)
}

export function taskDependencies(id, dependTaskIds) {
  return request.post(`/task/${id}/dependencies`, dependTaskIds)
}

export function taskGetDependencies(id) {
  return request.get(`/task/${id}/dependencies`)
}

export function taskDelete(id) {
  return request.delete(`/task/${id}`)
}
