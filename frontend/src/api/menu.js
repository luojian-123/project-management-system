import request from './request'

export function menuTree(admin) {
  return request.get('/sys/menu/tree', { params: admin ? { admin: true } : {} })
}

export function menuSave(data) {
  return request.post('/sys/menu', data)
}

export function menuDelete(id) {
  return request.delete(`/sys/menu/${id}`)
}
