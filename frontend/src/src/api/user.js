import request from './request'

export function userPage(params) {
  return request.get('/sys/user/page', { params })
}

export function userGet(id) {
  return request.get(`/sys/user/${id}`)
}

export function userSave(data) {
  return request.post('/sys/user', data)
}

export function userUpdate(data) {
  return request.put('/sys/user', data)
}

export function userDelete(id) {
  return request.delete(`/sys/user/${id}`)
}

export function getUserRoleIds(id) {
  return request.get(`/sys/user/${id}/roles`)
}

export function userAssignRoles(id, roleIds) {
  return request.post(`/sys/user/${id}/roles`, roleIds)
}
