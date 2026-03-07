import request from './request'

/** 上传交付物文档，返回 { path, name } */
export function uploadDeliverableFile(file) {
  const form = new FormData()
  form.append('file', file)
  return request.post('/file/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** 下载文档（blob），path 为上传返回的 path */
export function downloadFile(path) {
  return request.get('/file/download', { params: { path }, responseType: 'blob' })
}

/** 在线预览文档（blob，Content-Type 由后端按扩展名设置） */
export function previewFile(path) {
  return request.get('/file/preview', { params: { path }, responseType: 'blob' })
}
