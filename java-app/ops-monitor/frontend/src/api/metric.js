import request from './request.js'

export const getCurrentMetrics = () => request.get('/metrics/current')
export const getLatestMetric = (serverId) => request.get(`/metrics/${serverId}/latest`)
export const getRecentMetrics = (serverId) => request.get(`/metrics/${serverId}/recent`)
