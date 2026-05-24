import request from './request.js'

export const getServers = () => request.get('/servers')
export const addServer = (data) => request.post('/servers', data)
export const deleteServer = (id) => request.delete(`/servers/${id}`)
