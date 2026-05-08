import request from './request';

export function listModels(params?: Record<string, unknown>) {
  return request.get('/api/v1/inference/models', { params });
}

export function getModels(params?: Record<string, unknown>) {
  return listModels(params);
}

export function getModel(id: string) {
  return request.get(`/api/v1/inference/models/${id}`);
}

export function importModel(data: Record<string, unknown>) {
  return request.post('/api/v1/inference/models/import', data);
}

export function approveModel(id: string, data?: Record<string, unknown>) {
  return request.put(`/api/v1/inference/models/${id}/approve`, data);
}

export function deleteModel(id: string) {
  return request.delete(`/api/v1/inference/models/${id}`);
}

export function listModelVersions(modelId: string) {
  return request.get(`/api/v1/inference/models/${modelId}/versions`);
}

export function createModelVersion(modelId: string, data: Record<string, unknown>) {
  return request.post(`/api/v1/inference/models/${modelId}/versions`, data);
}

export function listOnlineServices(params?: Record<string, unknown>) {
  return request.get('/api/v1/inference/online-services', { params });
}

export function getOnlineServices(params?: Record<string, unknown>) {
  return listOnlineServices(params);
}

export function getOnlineService(id: string) {
  return request.get(`/api/v1/inference/online-services/${id}`);
}

export function deployOnlineService(data: Record<string, unknown>) {
  return request.post('/api/v1/inference/online-services', data);
}

export function deployOnline(data: Record<string, unknown>) {
  return deployOnlineService(data);
}

export function updateOnlineService(id: string, data: Record<string, unknown>) {
  return request.put(`/api/v1/inference/online-services/${id}`, data);
}

export function stopOnlineService(id: string) {
  return request.put(`/api/v1/inference/online-services/${id}/stop`);
}

export function stopOnline(id: string) {
  return stopOnlineService(id);
}

export function predictOnlineService(id: string, data: Record<string, unknown>) {
  return request.post(`/api/v1/inference/online-services/${id}/predict`, data);
}

export function predictOnline(id: string, data: Record<string, unknown>) {
  return predictOnlineService(id, data);
}

export function listBatchServices(params?: Record<string, unknown>) {
  return request.get('/api/v1/inference/batch-services', { params });
}

export function getBatchServices(params?: Record<string, unknown>) {
  return listBatchServices(params);
}

export function getBatchService(id: string) {
  return request.get(`/api/v1/inference/batch-services/${id}`);
}

export function createBatchService(data: Record<string, unknown>) {
  return request.post('/api/v1/inference/batch-services', data);
}

export function createBatch(data: Record<string, unknown>) {
  return createBatchService(data);
}

export function startBatchService(id: string) {
  return request.put(`/api/v1/inference/batch-services/${id}/start`);
}

export function startBatch(id: string) {
  return startBatchService(id);
}

export function stopBatchService(id: string) {
  return request.put(`/api/v1/inference/batch-services/${id}/stop`);
}

export function stopBatch(id: string) {
  return stopBatchService(id);
}

export function getBatchServiceResult(id: string) {
  return request.get(`/api/v1/inference/batch-services/${id}/result`, { responseType: 'blob' });
}

export function downloadResults(id: string) {
  return getBatchServiceResult(id);
}

export function getMonitoringMetrics(params?: Record<string, unknown>) {
  return request.get('/api/v1/inference/monitor/metrics', { params });
}

export function getServiceMetrics(params?: Record<string, unknown>) {
  return getMonitoringMetrics(params);
}

export function getMonitorLogs(params?: Record<string, unknown>) {
  return request.get('/api/v1/inference/monitor/logs', { params });
}

export function listDriftReports(params?: Record<string, unknown>) {
  return request.get('/api/v1/inference/drift', { params });
}

export function getDriftReports(params?: Record<string, unknown>) {
  return listDriftReports(params);
}

export function getDriftDetail(id: string) {
  return request.get(`/api/v1/inference/drift/${id}`);
}

export function getDriftTrend(params?: Record<string, unknown>) {
  return request.get('/api/v1/inference/drift/trend', { params });
}

export function listEvaluations(params?: Record<string, unknown>) {
  return request.get('/api/v1/inference/evaluation', { params });
}

export function getEvaluations(params?: Record<string, unknown>) {
  return listEvaluations(params);
}

export function getEvaluation(id: string) {
  return request.get(`/api/v1/inference/evaluation/${id}`);
}

export function createEvaluation(data: Record<string, unknown>) {
  return request.post('/api/v1/inference/evaluation', data);
}

export function runEvaluation(data: Record<string, unknown>) {
  return createEvaluation(data);
}

export function getEvaluationReport(id: string) {
  return request.get(`/api/v1/inference/evaluation/${id}/report`);
}

export function executeBackflow(data: Record<string, unknown>) {
  return request.post('/api/v1/inference/backflow/execute', data);
}

export function listBackflowTasks(params?: Record<string, unknown>) {
  return request.get('/api/v1/inference/backflow/tasks', { params });
}

export function getBackflowTask(id: string) {
  return request.get(`/api/v1/inference/backflow/tasks/${id}`);
}
