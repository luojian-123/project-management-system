import request from './request'

export function taskListByProject(projectId, tree = false) {
  return request.get(`/task/project/${projectId}`, { params: { tree } })
}

export function taskGet(id) {
  return request.get(`/task/${id}`)
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
