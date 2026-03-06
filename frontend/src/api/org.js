import request from './request'

export function orgTree() {
  return request.get('/org/tree')
}

export function companyList() {
  return request.get('/org/company/list')
}

export function companyGet(id) {
  return request.get(`/org/company/${id}`)
}

export function companySave(data) {
  return request.post('/org/company', data)
}

export function companyUpdate(data) {
  return request.put('/org/company', data)
}

export function companyDelete(id) {
  return request.delete(`/org/company/${id}`)
}

export function deptList(companyId) {
  return request.get('/org/dept/list', { params: { companyId } })
}

export function deptGet(id) {
  return request.get(`/org/dept/${id}`)
}

export function deptSave(data) {
  return request.post('/org/dept', data)
}

export function deptUpdate(data) {
  return request.put('/org/dept', data)
}

export function deptDelete(id) {
  return request.delete(`/org/dept/${id}`)
}
