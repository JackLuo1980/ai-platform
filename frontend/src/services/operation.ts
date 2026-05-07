import request from './request';

export function listClusters(params?: Record<string, unknown>) {
  return request.get('/api/v1/clusters', { params });
}

export function getCluster(id: string) {
  return request.get('/api/v1/clusters/' + id);
}

export function createCluster(data: Record<string, unknown>) {
  return request.post('/api/v1/clusters', data);
}

export function updateCluster(id: string, data: Record<string, unknown>) {
  return request.put('/api/v1/clusters/' + id, data);
}

export function deleteCluster(id: string) {
  return request.delete('/api/v1/clusters/' + id);
}

export function getClusterStatus(id: string) {
  return request.get('/api/v1/clusters/' + id + '/status');
}

export function listResourcePools(params?: Record<string, unknown>) {
  return request.get('/api/v1/resource-pools', { params });
}

export function getResourcePool(id: string) {
  return request.get('/api/v1/resource-pools/' + id);
}

export function createResourcePool(data: Record<string, unknown>) {
  return request.post('/api/v1/resource-pools', data);
}

export function updateResourcePool(id: string, data: Record<string, unknown>) {
  return request.put('/api/v1/resource-pools/' + id, data);
}

export function deleteResourcePool(id: string) {
  return request.delete('/api/v1/resource-pools/' + id);
}

export function getResourcePoolUsage(id: string) {
  return request.get('/api/v1/resource-pools/' + id + '/usage');
}

export function listQuotas(params?: Record<string, unknown>) {
  return request.get('/api/v1/resource-quotas', { params });
}

export function updateQuota(id: string, data: Record<string, unknown>) {
  return request.put('/api/v1/resource-quotas/' + id, data);
}

export function getQuotaUsage(id: string) {
  return request.get('/api/v1/resource-quotas/' + id + '/usage');
}

export function listImages(params?: Record<string, unknown>) {
  return request.get('/api/v1/images', { params });
}

export function getImage(id: string) {
  return request.get('/api/v1/images/' + id);
}

export function createImage(data: Record<string, unknown>) {
  return request.post('/api/v1/images', data);
}

export function updateImage(id: string, data: Record<string, unknown>) {
  return request.put('/api/v1/images/' + id, data);
}

export function deleteImage(id: string) {
  return request.delete('/api/v1/images/' + id);
}

export function listEnvironments(params?: Record<string, unknown>) {
  return request.get('/api/v1/environments', { params });
}

export function getEnvironment(id: string) {
  return request.get('/api/v1/environments/' + id);
}

export function createEnvironment(data: Record<string, unknown>) {
  return request.post('/api/v1/environments', data);
}

export function updateEnvironment(id: string, data: Record<string, unknown>) {
  return request.put('/api/v1/environments/' + id, data);
}

export function deleteEnvironment(id: string) {
  return request.delete('/api/v1/environments/' + id);
}
