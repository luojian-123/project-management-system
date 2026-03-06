import request from './request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function getInfo() {
  return request.get('/auth/info')
}

export function register(data) {
  return request.post('/auth/register', data)
}

/** 修改密码：{ oldPassword, newPassword } */
export function changePassword(data) {
  return request.put('/auth/password', data)
}
