import request from './request';

export function listModels(params?: Record<string, unknown>) {
  return request.get('/api/inference/models', { params });
}

export function getModel(id: string) {
  return request.get(`/api/inference/models/${id}`);
}

export function importModel(data: Record<string, unknown>) {
  return request.post('/api/inference/models/import', data);
}

export function approveModel(id: string) {
  return request.put(`/api/inference/models/${id}/approve`);
}

export function deleteModel(id: string) {
  return request.delete(`/api/inference/models/${id}`);
}

export function listModelVersions(modelId: string) {
  return request.get(`/api/inference/models/${modelId}/versions`);
}

export function createModelVersion(modelId: string, data: Record<string, unknown>) {
  return request.post(`/api/inference/models/${modelId}/versions`, data);
}

export function listOnlineServices(params?: Record<string, unknown>) {
  return request.get('/api/inference/online-services', { params });
}

export function getOnlineService(id: string) {
  return request.get(`/api/inference/online-services/${id}`);
}

export function deployOnlineService(data: Record<string, unknown>) {
  return request.post('/api/inference/online-services', data);
}

export function updateOnlineService(id: string, data: Record<string, unknown>) {
  return request.put(`/api/inference/online-services/${id}`, data);
}

export function stopOnlineService(id: string) {
  return request.put(`/api/inference/online-services/${id}/stop`);
}

export function predictOnlineService(id: string, data: Record<string, unknown>) {
  return request.post(`/api/inference/online-services/${id}/predict`, data);
}

export function listBatchServices(params?: Record<string, unknown>) {
  return request.get('/api/inference/batch-services', { params });
}

export function getBatchService(id: string) {
  return request.get(`/api/inference/batch-services/${id}`);
}

export function createBatchService(data: Record<string, unknown>) {
  return request.post('/api/inference/batch-services', data);
}

export function startBatchService(id: string) {
  return request.put(`/api/inference/batch-services/${id}/start`);
}

export function stopBatchService(id: string) {
  return request.put(`/api/inference/batch-services/${id}/stop`);
}

export function getBatchServiceResult(id: string) {
  return request.get(`/api/inference/batch-services/${id}/result`, { responseType: 'blob' });
}

export function getMonitoringMetrics(params?: Record<string, unknown>) {
  return request.get('/api/inference/monitoring/metrics', { params });
}

export function getServiceMetrics(serviceId: string, params?: Record<string, unknown>) {
  return request.get(`/api/inference/monitoring/services/${serviceId}/metrics`, { params });
}

export function listDriftReports(params?: Record<string, unknown>) {
  return request.get('/api/inference/drift', { params });
}

export function getDriftDetail(id: string) {
  return request.get(`/api/inference/drift/${id}`);
}

export function createDriftReport(data: Record<string, unknown>) {
  return request.post('/api/inference/drift', data);
}

export function listEvaluations(params?: Record<string, unknown>) {
  return request.get('/api/inference/evaluations', { params });
}

export function getEvaluation(id: string) {
  return request.get(`/api/inference/evaluations/${id}`);
}

export function createEvaluation(data: Record<string, unknown>) {
  return request.post('/api/inference/evaluations', data);
}

export function getEvaluationReport(id: string) {
  return request.get(`/api/inference/evaluations/${id}/report`);
}

export function listQuantizations(params?: Record<string, unknown>) {
  return request.get('/api/inference/quantize', { params });
}

export function createQuantization(data: Record<string, unknown>) {
  return request.post('/api/inference/quantize', data);
}

export function getQuantizationResult(id: string) {
  return request.get(`/api/inference/quantize/${id}`);
}
