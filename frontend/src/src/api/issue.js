import request from './request'

export function issuePage(params) {
  return request.get('/issue/page', { params })
}

export function issueGet(id) {
  return request.get(`/issue/${id}`)
}

export function issueSave(data) {
  return request.post('/issue', data)
}

export function issueUpdate(data) {
  return request.put('/issue', data)
}

export function issueDelete(id) {
  return request.delete(`/issue/${id}`)
}
