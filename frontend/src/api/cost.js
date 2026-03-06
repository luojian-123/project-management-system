import request from './request'

export function costSummaryByProject(projectId) {
  return request.get(`/cost/project/${projectId}/summary`)
}

/** 分页查询：params = { projectId?, page, size } */
export function costPage(params) {
  return request.get('/cost/page', { params })
}

export function costPageByProject(projectId, params) {
  return request.get(`/cost/project/${projectId}/page`, { params })
}

export function costGet(id) {
  return request.get(`/cost/${id}`)
}

export function costSave(data) {
  return request.post('/cost', data)
}

export function costUpdate(data) {
  return request.put('/cost', data)
}

export function costDelete(id) {
  return request.delete(`/cost/${id}`)
}
