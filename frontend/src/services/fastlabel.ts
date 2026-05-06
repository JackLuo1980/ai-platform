import request from './request';

export function listLabelDatasets(params?: Record<string, unknown>) {
  return request.get('/api/fastlabel/datasets', { params });
}

export function getLabelDataset(id: string) {
  return request.get(`/api/fastlabel/datasets/${id}`);
}

export function createLabelDataset(data: Record<string, unknown>) {
  return request.post('/api/fastlabel/datasets', data);
}

export function importLabelData(id: string, data: FormData) {
  return request.post(`/api/fastlabel/datasets/${id}/import`, data, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}

export function deleteLabelDataset(id: string) {
  return request.delete(`/api/fastlabel/datasets/${id}`);
}

export function listTasks(params?: Record<string, unknown>) {
  return request.get('/api/fastlabel/tasks', { params });
}

export function getTask(id: string) {
  return request.get(`/api/fastlabel/tasks/${id}`);
}

export function createTask(data: Record<string, unknown>) {
  return request.post('/api/fastlabel/tasks', data);
}

export function updateTask(id: string, data: Record<string, unknown>) {
  return request.put(`/api/fastlabel/tasks/${id}`, data);
}

export function assignTask(id: string, data: { assigneeId: string }) {
  return request.put(`/api/fastlabel/tasks/${id}/assign`, data);
}

export function getTaskProgress(id: string) {
  return request.get(`/api/fastlabel/tasks/${id}/progress`);
}

export function listTaskItems(taskId: string, params?: Record<string, unknown>) {
  return request.get(`/api/fastlabel/tasks/${taskId}/items`, { params });
}

export function getItem(taskId: string, itemId: string) {
  return request.get(`/api/fastlabel/tasks/${taskId}/items/${itemId}`);
}

export function annotateItem(taskId: string, itemId: string, data: Record<string, unknown>) {
  return request.put(`/api/fastlabel/tasks/${taskId}/items/${itemId}/annotate`, data);
}

export function skipItem(taskId: string, itemId: string) {
  return request.put(`/api/fastlabel/tasks/${taskId}/items/${itemId}/skip`);
}

export function listExports(params?: Record<string, unknown>) {
  return request.get('/api/fastlabel/exports', { params });
}

export function createExport(data: Record<string, unknown>) {
  return request.post('/api/fastlabel/exports', data);
}

export function downloadExport(id: string) {
  return request.get(`/api/fastlabel/exports/${id}/download`, { responseType: 'blob' });
}

export function pushToLab(exportId: string) {
  return request.post(`/api/fastlabel/exports/${exportId}/push-lab`);
}

export function listLabelTeams(params?: Record<string, unknown>) {
  return request.get('/api/fastlabel/teams', { params });
}

export function createLabelTeam(data: Record<string, unknown>) {
  return request.post('/api/fastlabel/teams', data);
}

export function updateLabelTeam(id: string, data: Record<string, unknown>) {
  return request.put(`/api/fastlabel/teams/${id}`, data);
}

export function deleteLabelTeam(id: string) {
  return request.delete(`/api/fastlabel/teams/${id}`);
}
