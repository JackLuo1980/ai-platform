import request from './request';

export function listLabelDatasets(params?: Record<string, unknown>) {
  return request.get('/api/v1/fastlabel/datasets', { params });
}

export function getLabelDatasets(params?: Record<string, unknown>) {
  return listLabelDatasets(params);
}

export function getLabelDataset(id: string) {
  return request.get(`/api/v1/fastlabel/datasets/${id}`);
}

export function createLabelDataset(data: Record<string, unknown>) {
  return request.post('/api/v1/fastlabel/datasets', data);
}

export function importLabelData(id: string, data: FormData) {
  return request.post(`/api/v1/fastlabel/datasets/${id}/import`, data, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}

export function deleteLabelDataset(id: string) {
  return request.delete(`/api/v1/fastlabel/datasets/${id}`);
}

export function listTasks(params?: Record<string, unknown>) {
  return request.get('/api/v1/fastlabel/tasks', { params });
}

export function getLabelTasks(params?: Record<string, unknown>) {
  return listTasks(params);
}

export function getTask(id: string) {
  return request.get(`/api/v1/fastlabel/tasks/${id}`);
}

export function createTask(data: Record<string, unknown>) {
  return request.post('/api/v1/fastlabel/tasks', data);
}

export function createLabelTask(data: Record<string, unknown>) {
  return createTask(data);
}

export function updateTask(id: string, data: Record<string, unknown>) {
  return request.put(`/api/v1/fastlabel/tasks/${id}`, data);
}

export function assignTask(id: string, data: { assigneeId: string }) {
  return request.put(`/api/v1/fastlabel/tasks/${id}/assign`, data);
}

export function getTaskProgress(id: string) {
  return request.get(`/api/v1/fastlabel/tasks/${id}/progress`);
}

export function listTaskItems(taskId: string, params?: Record<string, unknown>) {
  return request.get(`/api/v1/fastlabel/tasks/${taskId}/items`, { params });
}

export function getItems(params?: Record<string, unknown>) {
  return request.get('/api/v1/fastlabel/items', { params });
}

export function getItem(taskId: string, params?: Record<string, unknown>) {
  return request.get(`/api/v1/fastlabel/tasks/${taskId}/items/current`, { params });
}

export function submitAnnotation(itemId: string, data: Record<string, unknown>) {
  return request.put(`/api/v1/fastlabel/items/${itemId}/annotate`, data);
}

export function reviewItem(itemId: string, data: Record<string, unknown>) {
  return request.put(`/api/v1/fastlabel/items/${itemId}/review`, data);
}

export function annotateItem(taskId: string, itemId: string, data: Record<string, unknown>) {
  return request.put(`/api/v1/fastlabel/tasks/${taskId}/items/${itemId}/annotate`, data);
}

export function skipItem(taskId: string, itemId: string) {
  return request.put(`/api/v1/fastlabel/tasks/${taskId}/items/${itemId}/skip`);
}

export function listExports(params?: Record<string, unknown>) {
  return request.get('/api/v1/fastlabel/exports', { params });
}

export function getExports(params?: Record<string, unknown>) {
  return listExports(params);
}

export function createExport(data: Record<string, unknown>) {
  return request.post('/api/v1/fastlabel/exports', data);
}

export function downloadExport(id: string) {
  return request.get(`/api/v1/fastlabel/exports/${id}/download`, { responseType: 'blob' });
}

export function pushToLab(exportId: string) {
  return request.post(`/api/v1/fastlabel/exports/${exportId}/push-lab`);
}
