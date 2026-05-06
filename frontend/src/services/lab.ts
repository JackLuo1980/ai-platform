import request from './request';

export function listDataSources(params?: Record<string, unknown>) {
  return request.get('/api/lab/datasources', { params });
}

export function getDataSource(id: string) {
  return request.get(`/api/lab/datasources/${id}`);
}

export function createDataSource(data: Record<string, unknown>) {
  return request.post('/api/lab/datasources', data);
}

export function updateDataSource(id: string, data: Record<string, unknown>) {
  return request.put(`/api/lab/datasources/${id}`, data);
}

export function deleteDataSource(id: string) {
  return request.delete(`/api/lab/datasources/${id}`);
}

export function testDataSourceConnection(id: string) {
  return request.post(`/api/lab/datasources/${id}/test`);
}

export function listDatasets(params?: Record<string, unknown>) {
  return request.get('/api/lab/datasets', { params });
}

export function getDataset(id: string) {
  return request.get(`/api/lab/datasets/${id}`);
}

export function createDataset(data: FormData) {
  return request.post('/api/lab/datasets', data, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}

export function updateDataset(id: string, data: Record<string, unknown>) {
  return request.put(`/api/lab/datasets/${id}`, data);
}

export function deleteDataset(id: string) {
  return request.delete(`/api/lab/datasets/${id}`);
}

export function getDatasetPreview(id: string, params?: Record<string, unknown>) {
  return request.get(`/api/lab/datasets/${id}/preview`, { params });
}

export function getDatasetStats(id: string) {
  return request.get(`/api/lab/datasets/${id}/stats`);
}

export function getDatasetVersions(id: string) {
  return request.get(`/api/lab/datasets/${id}/versions`);
}

export function listOperators(params?: Record<string, unknown>) {
  return request.get('/api/lab/operators', { params });
}

export function getOperator(id: string) {
  return request.get(`/api/lab/operators/${id}`);
}

export function createOperator(data: Record<string, unknown>) {
  return request.post('/api/lab/operators', data);
}

export function updateOperator(id: string, data: Record<string, unknown>) {
  return request.put(`/api/lab/operators/${id}`, data);
}

export function deleteOperator(id: string) {
  return request.delete(`/api/lab/operators/${id}`);
}

export function listWorkflows(params?: Record<string, unknown>) {
  return request.get('/api/lab/workflows', { params });
}

export function getWorkflow(id: string) {
  return request.get(`/api/lab/workflows/${id}`);
}

export function createWorkflow(data: Record<string, unknown>) {
  return request.post('/api/lab/workflows', data);
}

export function updateWorkflow(id: string, data: Record<string, unknown>) {
  return request.put(`/api/lab/workflows/${id}`, data);
}

export function deleteWorkflow(id: string) {
  return request.delete(`/api/lab/workflows/${id}`);
}

export function runWorkflow(id: string) {
  return request.post(`/api/lab/workflows/${id}/run`);
}

export function listExperiments(params?: Record<string, unknown>) {
  return request.get('/api/lab/experiments', { params });
}

export function getExperiment(id: string) {
  return request.get(`/api/lab/experiments/${id}`);
}

export function createExperiment(data: Record<string, unknown>) {
  return request.post('/api/lab/experiments', data);
}

export function deleteExperiment(id: string) {
  return request.delete(`/api/lab/experiments/${id}`);
}

export function getExperimentMetrics(id: string) {
  return request.get(`/api/lab/experiments/${id}/metrics`);
}

export function listArchives(params?: Record<string, unknown>) {
  return request.get('/api/lab/archives', { params });
}

export function approveArchive(id: string) {
  return request.put(`/api/lab/archives/${id}/approve`);
}

export function rejectArchive(id: string, reason: string) {
  return request.put(`/api/lab/archives/${id}/reject`, { reason });
}

export function listFeatureGroups(params?: Record<string, unknown>) {
  return request.get('/api/lab/features', { params });
}

export function getFeatureGroup(id: string) {
  return request.get(`/api/lab/features/${id}`);
}

export function createFeatureGroup(data: Record<string, unknown>) {
  return request.post('/api/lab/features', data);
}

export function updateFeatureGroup(id: string, data: Record<string, unknown>) {
  return request.put(`/api/lab/features/${id}`, data);
}

export function deleteFeatureGroup(id: string) {
  return request.delete(`/api/lab/features/${id}`);
}

export function triggerFeatureComputation(id: string) {
  return request.post(`/api/lab/features/${id}/compute`);
}

export function listFiles(params?: Record<string, unknown>) {
  return request.get('/api/lab/files', { params });
}

export function uploadFile(data: FormData) {
  return request.post('/api/lab/files', data, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}

export function downloadFile(id: string) {
  return request.get(`/api/lab/files/${id}/download`, { responseType: 'blob' });
}

export function moveFile(id: string, data: { targetPath: string }) {
  return request.put(`/api/lab/files/${id}/move`, data);
}

export function copyFile(id: string, data: { targetPath: string }) {
  return request.put(`/api/lab/files/${id}/copy`, data);
}

export function deleteFile(id: string) {
  return request.delete(`/api/lab/files/${id}`);
}

export function getDataQualityReport(datasetId: string) {
  return request.get(`/api/lab/datasets/${datasetId}/quality`);
}

export function listMarketplaceOperators(params?: Record<string, unknown>) {
  return request.get('/api/lab/marketplace', { params });
}

export function installMarketplaceOperator(id: string) {
  return request.post(`/api/lab/marketplace/${id}/install`);
}
