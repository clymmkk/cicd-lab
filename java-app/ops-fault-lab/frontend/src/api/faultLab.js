import http from './http';

export function fetchScenarios() {
  return http.get('/scenarios');
}

export function fetchScenario(code) {
  return http.get(`/scenarios/${code}`);
}

export function triggerScenario(code, payload) {
  return http.post(`/scenarios/${code}/trigger`, payload || {});
}

export function stopScenario(code, payload) {
  return http.post(`/scenarios/${code}/stop`, payload || {});
}

export function fetchDashboard() {
  return http.get('/dashboard');
}

export function fetchFaultLogs(params) {
  return http.get('/fault-logs', { params });
}

export function fetchOperationLogs() {
  return http.get('/operation-logs');
}
