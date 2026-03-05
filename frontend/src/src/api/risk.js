import request from './request'

export function riskPage(params) {
  return request.get('/risk/page', { params })
}

export function riskGet(id) {
  return request.get(`/risk/${id}`)
}

export function riskSave(data) {
  return request.post('/risk', data)
}

export function riskUpdate(data) {
  return request.put('/risk', data)
}

export function riskDelete(id) {
  return request.delete(`/risk/${id}`)
}
