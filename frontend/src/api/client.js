import axios from 'axios';
import {
  DEMO_ORGS,
  DEMO_PROJECTS,
  DEMO_SPRINTS,
  DEMO_TASKS,
  DEMO_RESOURCES,
  DEMO_NOTIFICATIONS,
  DEMO_AUDIT_LOGS
} from './mockData';

const apiClient = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor to inject Token & Tenant Headers
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token');
    const orgId = localStorage.getItem('active_org_id');
    const userId = localStorage.getItem('user_id');

    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    if (orgId) {
      config.headers['X-Organization-Id'] = orgId;
    }
    if (userId) {
      config.headers['X-User-Id'] = userId;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// High-level API Service Object
export const api = {
  // =========================================
  // AUTHENTICATION
  // =========================================
  register: async (userData) => {
    try {
      const res = await apiClient.post('/auth/register', userData);
      return { success: true, data: res.data?.data || res.data };
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || err.message || 'Registration failed';
      return { success: false, error: msg };
    }
  },

  login: async (credentials) => {
    try {
      const res = await apiClient.post('/auth/login', credentials);
      return { success: true, data: res.data?.data || res.data };
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || err.message || 'Invalid credentials';
      return { success: false, error: msg };
    }
  },

  // =========================================
  // ORGANIZATIONS (TENANTS)
  // =========================================
  getOrganizations: async () => {
    try {
      const res = await apiClient.get('/v1/organizations');
      const items = res.data?.data?.content || res.data?.data || [];
      return Array.isArray(items) && items.length > 0 ? items : (Array.isArray(items) ? items : DEMO_ORGS);
    } catch {
      return DEMO_ORGS;
    }
  },

  createOrganization: async (orgData) => {
    try {
      const res = await apiClient.post('/v1/organizations', orgData);
      return { success: true, data: res.data?.data || res.data };
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || err.message || 'Failed to create organization';
      return { success: false, error: msg };
    }
  },

  // =========================================
  // PROJECTS
  // =========================================
  getProjects: async () => {
    try {
      const res = await apiClient.get('/v1/projects');
      const items = res.data?.data?.content || res.data?.data || [];
      return Array.isArray(items) ? items : DEMO_PROJECTS;
    } catch {
      return DEMO_PROJECTS;
    }
  },

  getProjectById: async (id) => {
    try {
      const res = await apiClient.get(`/v1/projects/${id}`);
      return res.data?.data || null;
    } catch {
      return null;
    }
  },

  createProject: async (project) => {
    try {
      const res = await apiClient.post('/v1/projects', project);
      return { success: true, data: res.data?.data || res.data };
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || err.message || 'Failed to create project';
      return { success: false, error: msg };
    }
  },

  // =========================================
  // SPRINTS
  // =========================================
  getSprints: async (projectId) => {
    try {
      const query = projectId ? `?projectId=${projectId}` : '';
      const res = await apiClient.get(`/v1/sprints${query}`);
      const items = res.data?.data?.content || res.data?.data || [];
      return Array.isArray(items) ? items : DEMO_SPRINTS;
    } catch {
      return DEMO_SPRINTS;
    }
  },

  createSprint: async (sprintData) => {
    try {
      const res = await apiClient.post('/v1/sprints', sprintData);
      return { success: true, data: res.data?.data || res.data };
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || err.message || 'Failed to create sprint';
      return { success: false, error: msg };
    }
  },

  startSprint: async (sprintId, payload = {}) => {
    try {
      const res = await apiClient.post(`/v1/sprints/${sprintId}/start`, payload);
      return { success: true, data: res.data?.data || res.data };
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || err.message || 'Failed to start sprint';
      return { success: false, error: msg };
    }
  },

  completeSprint: async (sprintId, payload = {}) => {
    try {
      const res = await apiClient.post(`/v1/sprints/${sprintId}/complete`, payload);
      return { success: true, data: res.data?.data || res.data };
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || err.message || 'Failed to complete sprint';
      return { success: false, error: msg };
    }
  },

  // =========================================
  // TASKS & KANBAN
  // =========================================
  getTasks: async (projectId, sprintId) => {
    try {
      const params = new URLSearchParams();
      if (projectId) params.append('projectId', projectId);
      if (sprintId) params.append('sprintId', sprintId);
      const queryString = params.toString() ? `?${params.toString()}` : '';

      const res = await apiClient.get(`/v1/tasks${queryString}`);
      const items = res.data?.data?.content || res.data?.data || [];
      return Array.isArray(items) ? items : DEMO_TASKS;
    } catch {
      return DEMO_TASKS;
    }
  },

  createTask: async (task) => {
    try {
      const res = await apiClient.post('/v1/tasks', task);
      return { success: true, data: res.data?.data || res.data };
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || err.message || 'Failed to create task';
      return { success: false, error: msg };
    }
  },

  updateTaskStatus: async (taskId, status) => {
    try {
      const res = await apiClient.patch(`/v1/tasks/${taskId}/status`, { status });
      return { success: true, data: res.data?.data || res.data };
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || err.message || 'Failed to update task status';
      return { success: false, error: msg };
    }
  },

  // =========================================
  // RESOURCES & CAPACITY
  // =========================================
  getResources: async () => {
    try {
      const res = await apiClient.get('/v1/resources');
      const items = res.data?.data?.content || res.data?.data || [];
      return Array.isArray(items) ? items : DEMO_RESOURCES;
    } catch {
      return DEMO_RESOURCES;
    }
  },

  allocateResource: async (allocationData) => {
    try {
      const res = await apiClient.post('/v1/resources/allocations', allocationData);
      return { success: true, data: res.data?.data || res.data };
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || err.message || 'Failed to allocate resource';
      return { success: false, error: msg };
    }
  },

  // =========================================
  // NOTIFICATIONS
  // =========================================
  getNotifications: async (recipientId) => {
    try {
      const res = await apiClient.get(`/v1/notifications?recipientId=${recipientId || ''}`);
      const items = res.data?.data?.content || res.data?.data || [];
      return Array.isArray(items) ? items : DEMO_NOTIFICATIONS;
    } catch {
      return DEMO_NOTIFICATIONS;
    }
  },

  getUnreadCount: async (recipientId) => {
    try {
      const res = await apiClient.get(`/v1/notifications/unread-count?recipientId=${recipientId || ''}`);
      return res.data?.data?.unreadCount ?? 0;
    } catch {
      return 0;
    }
  },

  markAsRead: async (notificationId) => {
    try {
      const res = await apiClient.patch(`/v1/notifications/${notificationId}/read`);
      return { success: true, data: res.data?.data };
    } catch {
      return { success: false };
    }
  },

  markAllAsRead: async () => {
    try {
      const res = await apiClient.post('/v1/notifications/mark-all-read');
      return { success: true, data: res.data?.data };
    } catch {
      return { success: false };
    }
  },

  // =========================================
  // AUDIT LOGS
  // =========================================
  getAuditLogs: async (criteria = {}) => {
    try {
      const params = new URLSearchParams(criteria).toString();
      const res = await apiClient.get(`/v1/audit?${params}`);
      const items = res.data?.data?.content || res.data?.data || [];
      return Array.isArray(items) ? items : DEMO_AUDIT_LOGS;
    } catch {
      return DEMO_AUDIT_LOGS;
    }
  },

  getAuditSummary: async () => {
    try {
      const res = await apiClient.get('/v1/audit/summary');
      return res.data?.data || null;
    } catch {
      return null;
    }
  },

  // =========================================
  // ANALYTICS DASHBOARD
  // =========================================
  getDashboardAnalytics: async () => {
    try {
      const res = await apiClient.get('/v1/analytics/dashboard');
      return res.data?.data || null;
    } catch {
      return null;
    }
  }
};

export default apiClient;
