import request from './request';

export function login(data: { username: string; password: string }) {
  return request.post('/api/v1/auth/login', data);
}

export function logout() {
  return request.post('/api/v1/auth/logout');
}

export function getProfile() {
  return request.get('/api/v1/profile');
}

export function updateProfile(data: Record<string, unknown>) {
  return request.put('/api/v1/profile', data);
}

export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return request.put('/api/v1/profile/password', data);
}

export function listTenants(params?: Record<string, unknown>) {
  return request.get('/api/v1/tenants', { params });
}

export function getTenant(id: string) {
  return request.get(`/api/v1/tenants/${id}`);
}

export function createTenant(data: Record<string, unknown>) {
  return request.post('/api/v1/tenants', data);
}

export function updateTenant(id: string, data: Record<string, unknown>) {
  return request.put(`/api/v1/tenants/${id}`, data);
}

export function deleteTenant(id: string) {
  return request.delete(`/api/v1/tenants/${id}`);
}

export function toggleTenantStatus(id: string, enabled: boolean) {
  return request.put(`/api/v1/tenants/${id}/status`, { enabled });
}

export function updateTenantQuota(id: string, data: Record<string, unknown>) {
  return request.put(`/api/v1/tenants/${id}/quota`, data);
}

export function listUsers(params?: Record<string, unknown>) {
  return request.get('/api/v1/users', { params });
}

export function getUser(id: string) {
  return request.get(`/api/v1/users/${id}`);
}

export function createUser(data: Record<string, unknown>) {
  return request.post('/api/v1/users', data);
}

export function updateUser(id: string, data: Record<string, unknown>) {
  return request.put(`/api/v1/users/${id}`, data);
}

export function deleteUser(id: string) {
  return request.delete(`/api/v1/users/${id}`);
}

export function assignRoles(userId: string, roleIds: string[]) {
  return request.put(`/api/v1/users/${userId}/roles`, { roleIds });
}

export function listRoles(params?: Record<string, unknown>) {
  return request.get('/api/v1/roles', { params });
}

export function getRole(id: string) {
  return request.get(`/api/v1/roles/${id}`);
}

export function createRole(data: Record<string, unknown>) {
  return request.post('/api/v1/roles', data);
}

export function updateRole(id: string, data: Record<string, unknown>) {
  return request.put(`/api/v1/roles/${id}`, data);
}

export function deleteRole(id: string) {
  return request.delete(`/api/v1/roles/${id}`);
}

export function getRolePermissions(roleId: string) {
  return request.get(`/api/v1/roles/${roleId}/permissions`);
}

export function updateRolePermissions(roleId: string, permissions: string[]) {
  return request.put(`/api/v1/roles/${roleId}/permissions`, { permissions });
}

export function listProjects(params?: Record<string, unknown>) {
  return request.get('/api/v1/projects', { params });
}

export function getProject(id: string) {
  return request.get(`/api/v1/projects/${id}`);
}

export function createProject(data: Record<string, unknown>) {
  return request.post('/api/v1/projects', data);
}

export function updateProject(id: string, data: Record<string, unknown>) {
  return request.put(`/api/v1/projects/${id}`, data);
}

export function deleteProject(id: string) {
  return request.delete(`/api/v1/projects/${id}`);
}

export function listProjectMembers(projectId: string) {
  return request.get(`/api/v1/projects/${projectId}/members`);
}

export function addProjectMember(projectId: string, data: Record<string, unknown>) {
  return request.post(`/api/v1/projects/${projectId}/members`, data);
}

export function removeProjectMember(projectId: string, userId: string) {
  return request.delete(`/api/v1/projects/${projectId}/members/${userId}`);
}

export function listAuditLogs(params?: Record<string, unknown>) {
  return request.get('/api/v1/audit-logs', { params });
}

export function listMessages(params?: Record<string, unknown>) {
  return request.get('/api/v1/messages', { params });
}

export function markMessageRead(id: string) {
  return request.put(`/api/v1/messages/${id}/read`);
}

export function markMessageUnread(id: string) {
  return request.put(`/api/v1/messages/${id}/unread`);
}

export function deleteMessage(id: string) {
  return request.delete(`/api/v1/messages/${id}`);
}

export function getLicense() {
  return request.get('/api/v1/license');
}

export function activateLicense(data: { key: string }) {
  return request.post('/api/v1/license/activate', data);
}

export function listTeams(params?: Record<string, unknown>) {
  return request.get('/api/v1/teams', { params });
}

export function createTeam(data: Record<string, unknown>) {
  return request.post('/api/v1/teams', data);
}

export function updateTeam(id: string, data: Record<string, unknown>) {
  return request.put(`/api/v1/teams/${id}`, data);
}

export function deleteTeam(id: string) {
  return request.delete(`/api/v1/teams/${id}`);
}
