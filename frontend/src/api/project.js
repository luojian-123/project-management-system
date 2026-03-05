import request from './request'

export function projectPage(params) {
  return request.get('/project/page', { params })
}

export function projectGet(id) {
  return request.get(`/project/${id}`)
}

export function projectSave(data) {
  return request.post('/project', data)
}

export function projectUpdate(data) {
  return request.put('/project', data)
}

export function projectDelete(id) {
  return request.delete(`/project/${id}`)
}
