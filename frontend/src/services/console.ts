import request from './request';

export function login(data: { username: string; password: string }) {
  return request.post('/auth/login', data);
}

export function logout() {
  return request.post('/auth/logout');
}

export function getProfile() {
  return request.get('/api/console/profile');
}

export function updateProfile(data: Record<string, unknown>) {
  return request.put('/api/console/profile', data);
}

export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return request.put('/api/console/profile/password', data);
}

export function listTenants(params?: Record<string, unknown>) {
  return request.get('/api/console/tenants', { params });
}

export function getTenant(id: string) {
  return request.get(`/api/console/tenants/${id}`);
}

export function createTenant(data: Record<string, unknown>) {
  return request.post('/api/console/tenants', data);
}

export function updateTenant(id: string, data: Record<string, unknown>) {
  return request.put(`/api/console/tenants/${id}`, data);
}

export function deleteTenant(id: string) {
  return request.delete(`/api/console/tenants/${id}`);
}

export function toggleTenantStatus(id: string, enabled: boolean) {
  return request.put(`/api/console/tenants/${id}/status`, { enabled });
}

export function updateTenantQuota(id: string, data: Record<string, unknown>) {
  return request.put(`/api/console/tenants/${id}/quota`, data);
}

export function listUsers(params?: Record<string, unknown>) {
  return request.get('/api/console/users', { params });
}

export function getUser(id: string) {
  return request.get(`/api/console/users/${id}`);
}

export function createUser(data: Record<string, unknown>) {
  return request.post('/api/console/users', data);
}

export function updateUser(id: string, data: Record<string, unknown>) {
  return request.put(`/api/console/users/${id}`, data);
}

export function deleteUser(id: string) {
  return request.delete(`/api/console/users/${id}`);
}

export function assignRoles(userId: string, roleIds: string[]) {
  return request.put(`/api/console/users/${userId}/roles`, { roleIds });
}

export function listRoles(params?: Record<string, unknown>) {
  return request.get('/api/console/roles', { params });
}

export function getRole(id: string) {
  return request.get(`/api/console/roles/${id}`);
}

export function createRole(data: Record<string, unknown>) {
  return request.post('/api/console/roles', data);
}

export function updateRole(id: string, data: Record<string, unknown>) {
  return request.put(`/api/console/roles/${id}`, data);
}

export function deleteRole(id: string) {
  return request.delete(`/api/console/roles/${id}`);
}

export function getRolePermissions(roleId: string) {
  return request.get(`/api/console/roles/${roleId}/permissions`);
}

export function updateRolePermissions(roleId: string, permissions: string[]) {
  return request.put(`/api/console/roles/${roleId}/permissions`, { permissions });
}

export function listProjects(params?: Record<string, unknown>) {
  return request.get('/api/console/projects', { params });
}

export function getProject(id: string) {
  return request.get(`/api/console/projects/${id}`);
}

export function createProject(data: Record<string, unknown>) {
  return request.post('/api/console/projects', data);
}

export function updateProject(id: string, data: Record<string, unknown>) {
  return request.put(`/api/console/projects/${id}`, data);
}

export function deleteProject(id: string) {
  return request.delete(`/api/console/projects/${id}`);
}

export function listProjectMembers(projectId: string) {
  return request.get(`/api/console/projects/${projectId}/members`);
}

export function addProjectMember(projectId: string, data: Record<string, unknown>) {
  return request.post(`/api/console/projects/${projectId}/members`, data);
}

export function removeProjectMember(projectId: string, userId: string) {
  return request.delete(`/api/console/projects/${projectId}/members/${userId}`);
}

export function listAuditLogs(params?: Record<string, unknown>) {
  return request.get('/api/console/audit-logs', { params });
}

export function listMessages(params?: Record<string, unknown>) {
  return request.get('/api/console/messages', { params });
}

export function markMessageRead(id: string) {
  return request.put(`/api/console/messages/${id}/read`);
}

export function markMessageUnread(id: string) {
  return request.put(`/api/console/messages/${id}/unread`);
}

export function deleteMessage(id: string) {
  return request.delete(`/api/console/messages/${id}`);
}

export function getLicense() {
  return request.get('/api/console/license');
}

export function activateLicense(data: { key: string }) {
  return request.post('/api/console/license/activate', data);
}

export function listTeams(params?: Record<string, unknown>) {
  return request.get('/api/console/teams', { params });
}

export function createTeam(data: Record<string, unknown>) {
  return request.post('/api/console/teams', data);
}

export function updateTeam(id: string, data: Record<string, unknown>) {
  return request.put(`/api/console/teams/${id}`, data);
}

export function deleteTeam(id: string) {
  return request.delete(`/api/console/teams/${id}`);
}
