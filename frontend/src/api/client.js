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
  timeout: 8000,
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

// High-level API Service Object with Graceful Live / Fallback Support
export const api = {
  // Auth
  login: async (credentials) => {
    try {
      const res = await apiClient.post('/auth/login', credentials);
      return res.data;
    } catch {
      return {
        success: true,
        data: {
          accessToken: 'demo-jwt-token-xyz',
          userId: 'u1-admin',
          email: credentials.email || 'admin@apexcloud.io',
          firstName: 'Sarah',
          lastName: 'Connor',
          roles: ['ROLE_ADMIN']
        }
      };
    }
  },

  // Organizations
  getOrganizations: async () => {
    try {
      const res = await apiClient.get('/v1/organizations');
      return res.data?.data || DEMO_ORGS;
    } catch {
      return DEMO_ORGS;
    }
  },

  // Projects
  getProjects: async () => {
    try {
      const res = await apiClient.get('/v1/projects');
      return res.data?.data?.content || res.data?.data || DEMO_PROJECTS;
    } catch {
      return DEMO_PROJECTS;
    }
  },

  createProject: async (project) => {
    try {
      const res = await apiClient.post('/v1/projects', project);
      return res.data?.data;
    } catch {
      return {
        ...project,
        id: 'p-' + Date.now(),
        progress: 0,
        teamSize: 1
      };
    }
  },

  // Tasks
  getTasks: async (projectId) => {
    try {
      const res = await apiClient.get(`/v1/tasks?projectId=${projectId || ''}`);
      return res.data?.data?.content || res.data?.data || DEMO_TASKS;
    } catch {
      return DEMO_TASKS;
    }
  },

  createTask: async (task) => {
    try {
      const res = await apiClient.post('/v1/tasks', task);
      return res.data?.data;
    } catch {
      return {
        ...task,
        id: 't-' + Date.now(),
        taskKey: 'NCB-' + Math.floor(100 + Math.random() * 900)
      };
    }
  },

  updateTaskStatus: async (taskId, status) => {
    try {
      const res = await apiClient.patch(`/v1/tasks/${taskId}/status`, { status });
      return res.data?.data;
    } catch {
      return { id: taskId, status };
    }
  },

  // Sprints
  getSprints: async (projectId) => {
    try {
      const res = await apiClient.get(`/v1/sprints?projectId=${projectId || ''}`);
      return res.data?.data?.content || res.data?.data || DEMO_SPRINTS;
    } catch {
      return DEMO_SPRINTS;
    }
  },

  // Resources
  getResources: async () => {
    try {
      const res = await apiClient.get('/v1/resources');
      return res.data?.data?.content || res.data?.data || DEMO_RESOURCES;
    } catch {
      return DEMO_RESOURCES;
    }
  },

  // Notifications
  getNotifications: async (recipientId) => {
    try {
      const res = await apiClient.get(`/v1/notifications?recipientId=${recipientId || ''}`);
      return res.data?.data?.content || res.data?.data || DEMO_NOTIFICATIONS;
    } catch {
      return DEMO_NOTIFICATIONS;
    }
  },

  getUnreadCount: async (recipientId) => {
    try {
      const res = await apiClient.get(`/v1/notifications/unread-count?recipientId=${recipientId || ''}`);
      return res.data?.data?.unreadCount ?? 2;
    } catch {
      return 2;
    }
  },

  // Audit Logs
  getAuditLogs: async (criteria = {}) => {
    try {
      const params = new URLSearchParams(criteria).toString();
      const res = await apiClient.get(`/v1/audit?${params}`);
      return res.data?.data?.content || res.data?.data || DEMO_AUDIT_LOGS;
    } catch {
      return DEMO_AUDIT_LOGS;
    }
  },

  getAuditSummary: async () => {
    try {
      const res = await apiClient.get('/v1/audit/summary');
      return res.data?.data;
    } catch {
      return {
        totalAuditLogs: 148,
        countByAction: { TASK_STATUS_CHANGED: 45, RESOURCE_ALLOCATED: 32, SPRINT_STARTED: 18, USER_LOGIN: 53 },
        countByEntityType: { TASK: 45, RESOURCE: 32, SPRINT: 18, AUTH: 53 },
        countByStatus: { SUCCESS: 148 }
      };
    }
  },

  // Analytics Dashboard
  getDashboardAnalytics: async () => {
    try {
      const res = await apiClient.get('/v1/analytics/dashboard');
      return res.data?.data;
    } catch {
      return {
        totalProjects: 3,
        totalTasks: 48,
        completedTasks: 35,
        overallCompletionRate: 72.9,
        averageSprintVelocity: 42.5,
        healthBreakdown: { ON_TRACK: 2, AT_RISK: 1, DELAYED: 0 }
      };
    }
  }
};

export default apiClient;
