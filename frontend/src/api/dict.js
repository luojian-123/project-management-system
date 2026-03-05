import request from './request'

export function dictTypeList() {
  return request.get('/sys/dict/type/list')
}

export function dictItemList(typeCode) {
  return request.get(`/sys/dict/item/${typeCode}`)
}

export function dictTypeSave(data) {
  return request.post('/sys/dict/type', data)
}

export function dictTypeDelete(id) {
  return request.delete(`/sys/dict/type/${id}`)
}

export function dictItemSave(data) {
  return request.post('/sys/dict/item', data)
}

export function dictItemDelete(id) {
  return request.delete(`/sys/dict/item/${id}`)
}
