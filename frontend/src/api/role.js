import request from './request'

export function roleList() {
  return request.get('/sys/role/list')
}

export function roleGet(id) {
  return request.get(`/sys/role/${id}`)
}

export function roleSave(data) {
  return request.post('/sys/role', data)
}

export function roleUpdate(data) {
  return request.put('/sys/role', data)
}

export function roleDelete(id) {
  return request.delete(`/sys/role/${id}`)
}

export function roleMenuIds(id) {
  return request.get(`/sys/role/${id}/menus`)
}

export function roleAssignMenus(id, menuIds) {
  return request.post(`/sys/role/${id}/menus`, menuIds)
}
