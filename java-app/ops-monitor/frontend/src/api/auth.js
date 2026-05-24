import request from './request.js'

export const login = (username, password) => {
  return request.post('/auth/login', { username, password })
}
