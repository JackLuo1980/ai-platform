import request from './request';

export function listClusters(params?: Record<string, unknown>) {
  return request.get('/api/operation/clusters', { params });
}

export function getCluster(id: string) {
  return request.get(`/api/operation/clusters/${id}`);
}

export function createCluster(data: Record<string, unknown>) {
  return request.post('/api/operation/clusters', data);
}

export function updateCluster(id: string, data: Record<string, unknown>) {
  return request.put(`/api/operation/clusters/${id}`, data);
}

export function deleteCluster(id: string) {
  return request.delete(`/api/operation/clusters/${id}`);
}

export function getClusterStatus(id: string) {
  return request.get(`/api/operation/clusters/${id}/status`);
}

export function listResourcePools(params?: Record<string, unknown>) {
  return request.get('/api/operation/pools', { params });
}

export function getResourcePool(id: string) {
  return request.get(`/api/operation/pools/${id}`);
}

export function createResourcePool(data: Record<string, unknown>) {
  return request.post('/api/operation/pools', data);
}

export function updateResourcePool(id: string, data: Record<string, unknown>) {
  return request.put(`/api/operation/pools/${id}`, data);
}

export function deleteResourcePool(id: string) {
  return request.delete(`/api/operation/pools/${id}`);
}

export function getResourcePoolUsage(id: string) {
  return request.get(`/api/operation/pools/${id}/usage`);
}

export function listQuotas(params?: Record<string, unknown>) {
  return request.get('/api/operation/quotas', { params });
}

export function updateQuota(id: string, data: Record<string, unknown>) {
  return request.put(`/api/operation/quotas/${id}`, data);
}

export function getQuotaUsage(id: string) {
  return request.get(`/api/operation/quotas/${id}/usage`);
}

export function listImages(params?: Record<string, unknown>) {
  return request.get('/api/operation/images', { params });
}

export function getImage(id: string) {
  return request.get(`/api/operation/images/${id}`);
}

export function createImage(data: Record<string, unknown>) {
  return request.post('/api/operation/images', data);
}

export function updateImage(id: string, data: Record<string, unknown>) {
  return request.put(`/api/operation/images/${id}`, data);
}

export function deleteImage(id: string) {
  return request.delete(`/api/operation/images/${id}`);
}

export function listEnvironments(params?: Record<string, unknown>) {
  return request.get('/api/operation/environments', { params });
}

export function getEnvironment(id: string) {
  return request.get(`/api/operation/environments/${id}`);
}

export function createEnvironment(data: Record<string, unknown>) {
  return request.post('/api/operation/environments', data);
}

export function updateEnvironment(id: string, data: Record<string, unknown>) {
  return request.put(`/api/operation/environments/${id}`, data);
}

export function deleteEnvironment(id: string) {
  return request.delete(`/api/operation/environments/${id}`);
}
