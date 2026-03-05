import request from './request'

export function todoPage(params) {
  return request.get('/todo/page', { params })
}

export function todoGet(id) {
  return request.get(`/todo/${id}`)
}

export function todoSave(data) {
  return request.post('/todo', data)
}

export function todoUpdate(data) {
  return request.put('/todo', data)
}

export function todoDelete(id) {
  return request.delete(`/todo/${id}`)
}
