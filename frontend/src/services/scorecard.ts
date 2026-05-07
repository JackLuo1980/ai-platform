import request from './request';

export function listVariables(params?: Record<string, unknown>) {
  return request.get('/api/v1/scorecard/variables', { params });
}

export function getVariables(params?: Record<string, unknown>) {
  return listVariables(params);
}

export function getVariable(id: string) {
  return request.get(`/api/v1/scorecard/variables/${id}`);
}

export function createVariable(data: Record<string, unknown>) {
  return request.post('/api/v1/scorecard/variables', data);
}

export function updateVariable(id: string, data: Record<string, unknown>) {
  return request.put(`/api/v1/scorecard/variables/${id}`, data);
}

export function deleteVariable(id: string) {
  return request.delete(`/api/v1/scorecard/variables/${id}`);
}

export function calculateIV(id: string) {
  return request.post(`/api/v1/scorecard/variables/${id}/iv`);
}

export function analyzeVariables(data: Record<string, unknown>) {
  return request.post('/api/v1/scorecard/variables/analyze', data);
}

export function getBinningDetail(variableId: string) {
  return request.get(`/api/v1/scorecard/variables/${variableId}/binning`);
}

export function getBinningResult(variableId: string) {
  return getBinningDetail(variableId);
}

export function updateBinning(variableId: string, data: Record<string, unknown>) {
  return request.put(`/api/v1/scorecard/variables/${variableId}/binning`, data);
}

export function runBinning(variableId: string, data: Record<string, unknown>) {
  return request.post(`/api/v1/scorecard/variables/${variableId}/binning/auto`, data);
}

export function adjustBinning(variableId: string, data: Record<string, unknown>) {
  return updateBinning(variableId, data);
}

export function getWOEChart(variableId: string) {
  return request.get(`/api/v1/scorecard/variables/${variableId}/woe`);
}

export function listScorecardModels(params?: Record<string, unknown>) {
  return request.get('/api/v1/scorecard/models', { params });
}

export function getScModels(params?: Record<string, unknown>) {
  return listScorecardModels(params);
}

export function getScorecardModel(id: string) {
  return request.get(`/api/v1/scorecard/models/${id}`);
}

export function createScorecardModel(data: Record<string, unknown>) {
  return request.post('/api/v1/scorecard/models', data);
}

export function updateScorecardModel(id: string, data: Record<string, unknown>) {
  return request.put(`/api/v1/scorecard/models/${id}`, data);
}

export function deleteScorecardModel(id: string) {
  return request.delete(`/api/v1/scorecard/models/${id}`);
}

export function trainScorecardModel(id: string) {
  return request.post(`/api/v1/scorecard/models/${id}/train`);
}

export function trainScModel(data: Record<string, unknown>) {
  return request.post('/api/v1/scorecard/models/train', data);
}

export function getScModelReport(id: string) {
  return request.get(`/api/v1/scorecard/models/${id}/report`);
}

export function getScorecardModelScore(id: string) {
  return request.get(`/api/v1/scorecard/models/${id}/score`);
}

export function onlineScore(data: Record<string, unknown>) {
  return request.post('/api/v1/scorecard/scoring/online', data);
}

export function batchScore(data: Record<string, unknown>) {
  return request.post('/api/v1/scorecard/scoring/batch', data);
}

export function createBatchScoring(modelId: string, data: Record<string, unknown>) {
  return request.post(`/api/v1/scorecard/models/${modelId}/score/batch`, data);
}

export function getBatchScoringResult(id: string) {
  return request.get(`/api/v1/scorecard/score/batch/${id}`);
}

export function getScoringRules(params?: Record<string, unknown>) {
  return request.get('/api/v1/scorecard/rules', { params });
}

export function getScoringHistory(modelId: string) {
  return request.get(`/api/v1/scorecard/scoring/${modelId}/results`);
}

export function deployToInference(modelId: string) {
  return request.post(`/api/v1/scorecard/models/${modelId}/deploy`);
}

export function getScorecardRules(modelId: string) {
  return request.get(`/api/v1/scorecard/rules/model/${modelId}`);
}
