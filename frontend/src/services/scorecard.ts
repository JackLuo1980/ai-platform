import request from './request';

export function listVariables(params?: Record<string, unknown>) {
  return request.get('/api/scorecard/variables', { params });
}

export function getVariable(id: string) {
  return request.get(`/api/scorecard/variables/${id}`);
}

export function createVariable(data: Record<string, unknown>) {
  return request.post('/api/scorecard/variables', data);
}

export function updateVariable(id: string, data: Record<string, unknown>) {
  return request.put(`/api/scorecard/variables/${id}`, data);
}

export function deleteVariable(id: string) {
  return request.delete(`/api/scorecard/variables/${id}`);
}

export function calculateIV(id: string) {
  return request.post(`/api/scorecard/variables/${id}/iv`);
}

export function getBinningDetail(variableId: string) {
  return request.get(`/api/scorecard/variables/${variableId}/binning`);
}

export function updateBinning(variableId: string, data: Record<string, unknown>) {
  return request.put(`/api/scorecard/variables/${variableId}/binning`, data);
}

export function getWOEChart(variableId: string) {
  return request.get(`/api/scorecard/variables/${variableId}/woe`);
}

export function listScorecardModels(params?: Record<string, unknown>) {
  return request.get('/api/scorecard/models', { params });
}

export function getScorecardModel(id: string) {
  return request.get(`/api/scorecard/models/${id}`);
}

export function createScorecardModel(data: Record<string, unknown>) {
  return request.post('/api/scorecard/models', data);
}

export function updateScorecardModel(id: string, data: Record<string, unknown>) {
  return request.put(`/api/scorecard/models/${id}`, data);
}

export function deleteScorecardModel(id: string) {
  return request.delete(`/api/scorecard/models/${id}`);
}

export function trainScorecardModel(id: string) {
  return request.post(`/api/scorecard/models/${id}/train`);
}

export function getScorecardModelScore(id: string) {
  return request.get(`/api/scorecard/models/${id}/score`);
}

export function onlineScore(modelId: string, data: Record<string, unknown>) {
  return request.post(`/api/scorecard/models/${modelId}/score/online`, data);
}

export function createBatchScoring(modelId: string, data: Record<string, unknown>) {
  return request.post(`/api/scorecard/models/${modelId}/score/batch`, data);
}

export function getBatchScoringResult(id: string) {
  return request.get(`/api/scorecard/score/batch/${id}`);
}
