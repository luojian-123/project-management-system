import request from './request'

export function changePage(params) {
  return request.get('/change/page', { params })
}

export function changeGet(id) {
  return request.get(`/change/${id}`)
}

export function changeSave(data) {
  return request.post('/change', data)
}

export function changeUpdate(data) {
  return request.put('/change', data)
}

export function changeDelete(id) {
  return request.delete(`/change/${id}`)
}
