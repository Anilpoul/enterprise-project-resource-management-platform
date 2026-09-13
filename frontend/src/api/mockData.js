// Realistic demo dataset representing live enterprise state
export const DEMO_ORGS = [
  { id: '11111111-1111-1111-1111-111111111111', name: 'Apex Cloud Technologies', slug: 'apex-cloud', role: 'ORG_ADMIN' },
  { id: '22222222-2222-2222-2222-222222222222', name: 'Nexus Financial Solutions', slug: 'nexus-fin', role: 'PROJECT_MANAGER' },
  { id: '33333333-3333-3333-3333-333333333333', name: 'CyberScale Labs', slug: 'cyberscale', role: 'TEAM_MEMBER' }
];

export const DEMO_USERS = [
  { id: 'u1-admin', email: 'admin@apexcloud.io', firstName: 'Sarah', lastName: 'Connor', role: 'ROLE_ADMIN', designation: 'Chief Technology Officer' },
  { id: 'u2-pm', email: 'alex.chen@apexcloud.io', firstName: 'Alex', lastName: 'Chen', role: 'ROLE_MANAGER', designation: 'Lead Technical PM' },
  { id: 'u3-dev', email: 'elena.rostova@apexcloud.io', firstName: 'Elena', lastName: 'Rostova', role: 'ROLE_USER', designation: 'Principal Architect' },
  { id: 'u4-lead', email: 'marcus.vance@apexcloud.io', firstName: 'Marcus', lastName: 'Vance', role: 'ROLE_USER', designation: 'Senior Fullstack Dev' }
];

export const DEMO_PROJECTS = [
  {
    id: 'p101',
    name: 'NextGen Core Banking Engine',
    projectKey: 'NCB',
    description: 'High-throughput event-driven ledger and settlement platform.',
    status: 'ACTIVE',
    priority: 'CRITICAL',
    startDate: '2026-08-01',
    endDate: '2026-12-31',
    progress: 72,
    lead: 'Alex Chen',
    teamSize: 12
  },
  {
    id: 'p102',
    name: 'Zero-Trust Identity Fabric',
    projectKey: 'ZTI',
    description: 'OAuth2/OIDC centralized identity layer with continuous authorization.',
    status: 'ACTIVE',
    priority: 'HIGH',
    startDate: '2026-08-15',
    endDate: '2026-11-30',
    progress: 88,
    lead: 'Sarah Connor',
    teamSize: 8
  },
  {
    id: 'p103',
    name: 'AI Smart Telemetry Agent',
    projectKey: 'STA',
    description: 'Edge metrics collection agent using eBPF and LLM anomaly detection.',
    status: 'PLANNING',
    priority: 'MEDIUM',
    startDate: '2026-09-20',
    endDate: '2027-02-28',
    progress: 25,
    lead: 'Elena Rostova',
    teamSize: 5
  }
];

export const DEMO_SPRINTS = [
  {
    id: 's201',
    projectId: 'p101',
    name: 'Sprint 24: Settlement Acceleration',
    status: 'ACTIVE',
    goal: 'Achieve sub-50ms distributed transaction confirmation across ledgers',
    startDate: '2026-09-01',
    endDate: '2026-09-15',
    totalPoints: 48,
    completedPoints: 34
  },
  {
    id: 's202',
    projectId: 'p101',
    name: 'Sprint 25: Observability & Resilience',
    status: 'PLANNED',
    goal: 'Complete OpenTelemetry auto-instrumentation and chaos testing suite',
    startDate: '2026-09-16',
    endDate: '2026-09-30',
    totalPoints: 52,
    completedPoints: 0
  }
];

export const DEMO_TASKS = [
  {
    id: 't1',
    taskKey: 'NCB-101',
    projectId: 'p101',
    sprintId: 's201',
    title: 'Migrate high-volume reconciliation query to PostgreSQL partitioned indexes',
    description: 'Optimize transaction scanning query across 10M+ rows daily.',
    status: 'IN_PROGRESS',
    priority: 'HIGH',
    taskType: 'TASK',
    assigneeName: 'Elena Rostova',
    assigneeAvatar: 'ER',
    storyPoints: 8
  },
  {
    id: 't2',
    taskKey: 'NCB-102',
    projectId: 'p101',
    sprintId: 's201',
    title: 'Configure Kafka idempotent producer with acks=all and retries',
    description: 'Prevent message duplication during leader broker failovers.',
    status: 'DONE',
    priority: 'CRITICAL',
    taskType: 'TASK',
    assigneeName: 'Marcus Vance',
    assigneeAvatar: 'MV',
    storyPoints: 5
  },
  {
    id: 't3',
    taskKey: 'NCB-103',
    projectId: 'p101',
    sprintId: 's201',
    title: 'Security vulnerability patch in Netty NIO buffer handling',
    description: 'Upgrade netty dependencies across gateway and internal consumers.',
    status: 'REVIEW',
    priority: 'CRITICAL',
    taskType: 'BUG',
    assigneeName: 'Sarah Connor',
    assigneeAvatar: 'SC',
    storyPoints: 3
  },
  {
    id: 't4',
    taskKey: 'NCB-104',
    projectId: 'p101',
    sprintId: 's201',
    title: 'Implement multi-region database failover health checker probe',
    description: 'Automated liveness probe verifying replication lag under 200ms.',
    status: 'TODO',
    priority: 'MEDIUM',
    taskType: 'STORY',
    assigneeName: 'Alex Chen',
    assigneeAvatar: 'AC',
    storyPoints: 13
  },
  {
    id: 't5',
    taskKey: 'NCB-105',
    projectId: 'p101',
    sprintId: 's201',
    title: 'Add Prometheus latency histogram metrics to API Gateway routes',
    description: 'Track p95, p99 gateway forwarding response times.',
    status: 'TODO',
    priority: 'LOW',
    taskType: 'TASK',
    assigneeName: 'Elena Rostova',
    assigneeAvatar: 'ER',
    storyPoints: 5
  }
];

export const DEMO_RESOURCES = [
  {
    id: 'r1',
    userId: 'u3-dev',
    name: 'Elena Rostova',
    role: 'Principal Architect',
    department: 'Core Engineering',
    totalAllocation: 125, // Overallocated!
    projects: [
      { name: 'NextGen Core Banking Engine', percentage: 75 },
      { name: 'AI Smart Telemetry Agent', percentage: 50 }
    ],
    skills: ['Java 21', 'Spring Boot', 'Kafka', 'PostgreSQL', 'Distributed Systems'],
    status: 'OVERALLOCATED'
  },
  {
    id: 'r2',
    userId: 'u4-lead',
    name: 'Marcus Vance',
    role: 'Senior Fullstack Dev',
    department: 'Product Systems',
    totalAllocation: 100,
    projects: [
      { name: 'NextGen Core Banking Engine', percentage: 100 }
    ],
    skills: ['React', 'TypeScript', 'Node.js', 'Redis', 'Docker'],
    status: 'OPTIMAL'
  },
  {
    id: 'r3',
    userId: 'u2-pm',
    name: 'Alex Chen',
    role: 'Lead Technical PM',
    department: 'Project Office',
    totalAllocation: 80,
    projects: [
      { name: 'NextGen Core Banking Engine', percentage: 50 },
      { name: 'Zero-Trust Identity Fabric', percentage: 30 }
    ],
    skills: ['Agile / Scrum', 'Jira', 'Architecture', 'Risk Analysis'],
    status: 'OPTIMAL'
  },
  {
    id: 'r4',
    userId: 'u5-bench',
    name: 'David Kim',
    role: 'DevOps Engineer',
    department: 'Infrastructure',
    totalAllocation: 0,
    projects: [],
    skills: ['Kubernetes', 'Terraform', 'Helm', 'CI/CD', 'AWS'],
    status: 'ON_BENCH'
  }
];

export const DEMO_NOTIFICATIONS = [
  {
    id: 'n1',
    title: 'Resource Over-allocated Warning',
    message: 'Elena Rostova is allocated at 125% across 2 projects, exceeding 100% threshold.',
    notificationType: 'RESOURCE_OVERALLOCATED',
    channel: 'IN_APP',
    isRead: false,
    createdAt: '10 mins ago'
  },
  {
    id: 'n2',
    title: 'Task Assigned: NCB-101',
    message: 'You have been assigned to task: Migrate high-volume reconciliation query.',
    notificationType: 'TASK_ASSIGNED',
    channel: 'IN_APP',
    isRead: false,
    createdAt: '1 hour ago'
  },
  {
    id: 'n3',
    title: 'Sprint 24 Started',
    message: 'Sprint 24: Settlement Acceleration has been officially started.',
    notificationType: 'SPRINT_STARTED',
    channel: 'IN_APP',
    isRead: true,
    createdAt: 'Yesterday'
  }
];

export const DEMO_AUDIT_LOGS = [
  {
    id: 'a1',
    entityType: 'RESOURCE',
    entityId: 'r1',
    action: 'RESOURCE_ALLOCATED',
    performedBy: 'sarah.connor@apexcloud.io',
    status: 'SUCCESS',
    ipAddress: '192.168.1.45',
    details: 'Allocated 50% on project p103 resulting in 125% total workload.',
    timestamp: '2026-09-13 18:30:12'
  },
  {
    id: 'a2',
    entityType: 'TASK',
    entityId: 't2',
    action: 'TASK_STATUS_CHANGED',
    performedBy: 'marcus.vance@apexcloud.io',
    status: 'SUCCESS',
    ipAddress: '10.0.4.12',
    details: 'Changed status from IN_PROGRESS to DONE.',
    timestamp: '2026-09-13 17:45:00'
  },
  {
    id: 'a3',
    entityType: 'SPRINT',
    entityId: 's201',
    action: 'SPRINT_STARTED',
    performedBy: 'alex.chen@apexcloud.io',
    status: 'SUCCESS',
    ipAddress: '10.0.2.89',
    details: 'Sprint 24: Settlement Acceleration started with 48 story points.',
    timestamp: '2026-09-13 14:10:00'
  },
  {
    id: 'a4',
    entityType: 'AUTH',
    entityId: 'u1-admin',
    action: 'USER_LOGIN',
    performedBy: 'admin@apexcloud.io',
    status: 'SUCCESS',
    ipAddress: '172.16.0.5',
    details: 'JWT access token generated with role ORG_ADMIN.',
    timestamp: '2026-09-13 09:02:14'
  }
];
