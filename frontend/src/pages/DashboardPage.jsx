import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { useTenant } from '../context/TenantContext';
import {
  FolderKanban,
  CheckCircle2,
  Zap,
  Users2,
  TrendingUp,
  AlertTriangle,
  ArrowUpRight,
  ShieldCheck,
  Plus
} from 'lucide-react';
import { Link } from 'react-router-dom';

export default function DashboardPage() {
  const { activeOrg } = useTenant();
  const [analytics, setAnalytics] = useState(null);
  const [projects, setProjects] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      const [analyticsData, projectsData, logsData] = await Promise.all([
        api.getDashboardAnalytics(),
        api.getProjects(),
        api.getAuditLogs({ limit: 4 })
      ]);
      setAnalytics(analyticsData);
      setProjects(projectsData);
      setAuditLogs(logsData.slice(0, 4));
      setLoading(false);
    };
    fetchData();
  }, [activeOrg]);

  return (
    <div className="fade-in">
      {/* Page Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.85rem', marginBottom: '0.25rem' }}>Executive Dashboard</h1>
          <p style={{ color: 'var(--text-secondary)' }}>
            Real-time portfolio metrics, sprint velocity, and resource capacity for <strong style={{ color: 'var(--text-primary)' }}>{activeOrg?.name}</strong>
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <Link to="/projects" className="btn btn-secondary">
            View All Projects
          </Link>
          <Link to="/boards" className="btn btn-primary">
            <Plus size={16} /> Open Sprint Board
          </Link>
        </div>
      </div>

      {/* KPI Stats Grid */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
        gap: '1.25rem',
        marginBottom: '2rem'
      }}>
        {/* Total Projects */}
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
            <span style={{ color: 'var(--text-secondary)', fontWeight: 600, fontSize: '0.85rem' }}>Active Projects</span>
            <div style={{
              width: '36px', height: '36px', borderRadius: '8px',
              background: 'rgba(99, 102, 241, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center'
            }}>
              <FolderKanban size={20} color="var(--brand-primary)" />
            </div>
          </div>
          <div style={{ fontSize: '2rem', fontWeight: 800, marginBottom: '0.35rem' }}>
            {analytics?.totalProjects || 3}
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.75rem', color: 'var(--status-success)' }}>
            <TrendingUp size={14} /> 100% On-Track across workspaces
          </div>
        </div>

        {/* Task Completion Rate */}
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
            <span style={{ color: 'var(--text-secondary)', fontWeight: 600, fontSize: '0.85rem' }}>Task Completion Rate</span>
            <div style={{
              width: '36px', height: '36px', borderRadius: '8px',
              background: 'rgba(16, 185, 129, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center'
            }}>
              <CheckCircle2 size={20} color="var(--status-success)" />
            </div>
          </div>
          <div style={{ fontSize: '2rem', fontWeight: 800, marginBottom: '0.35rem' }}>
            {analytics?.overallCompletionRate || 72.9}%
          </div>
          <div className="progress-container" style={{ marginTop: '0.5rem' }}>
            <div className="progress-fill" style={{ width: `${analytics?.overallCompletionRate || 72.9}%` }} />
          </div>
        </div>

        {/* Average Sprint Velocity */}
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
            <span style={{ color: 'var(--text-secondary)', fontWeight: 600, fontSize: '0.85rem' }}>Sprint Velocity</span>
            <div style={{
              width: '36px', height: '36px', borderRadius: '8px',
              background: 'rgba(6, 182, 212, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center'
            }}>
              <Zap size={20} color="var(--brand-secondary)" />
            </div>
          </div>
          <div style={{ fontSize: '2rem', fontWeight: 800, marginBottom: '0.35rem' }}>
            {analytics?.averageSprintVelocity || 42.5} <span style={{ fontSize: '1rem', color: 'var(--text-muted)' }}>pts/sprint</span>
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
            +14% higher than last quarter
          </div>
        </div>

        {/* Resource Allocation */}
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
            <span style={{ color: 'var(--text-secondary)', fontWeight: 600, fontSize: '0.85rem' }}>Team Capacity</span>
            <div style={{
              width: '36px', height: '36px', borderRadius: '8px',
              background: 'rgba(245, 158, 11, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center'
            }}>
              <Users2 size={20} color="var(--status-warning)" />
            </div>
          </div>
          <div style={{ fontSize: '2rem', fontWeight: 800, marginBottom: '0.35rem' }}>
            101.3%
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.75rem', color: 'var(--status-warning)' }}>
            <AlertTriangle size={14} /> 1 Resource over-allocated (&gt;100%)
          </div>
        </div>
      </div>

      {/* Main Content: Projects Grid & Audit Stream */}
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '1.5rem' }}>
        {/* Active Projects Showcase */}
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
            <h3 style={{ fontSize: '1.15rem' }}>Active Workspaces</h3>
            <Link to="/projects" style={{ fontSize: '0.8rem', color: 'var(--brand-primary)', textDecoration: 'none', fontWeight: 600 }}>
              View All &rarr;
            </Link>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {projects.map(proj => (
              <div key={proj.id} style={{
                padding: '1.15rem',
                borderRadius: 'var(--radius-md)',
                background: 'var(--bg-surface-elevated)',
                border: '1px solid var(--border-subtle)',
                display: 'flex',
                flexDirection: 'column',
                gap: '0.75rem'
              }}>
                <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <span className="badge badge-primary" style={{ fontSize: '0.7rem' }}>{proj.projectKey}</span>
                      <h4 style={{ fontSize: '1rem' }}>{proj.name}</h4>
                    </div>
                    <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '0.25rem' }}>
                      {proj.description}
                    </p>
                  </div>
                  <span className={`badge badge-${proj.status === 'ACTIVE' ? 'success' : 'warning'}`}>
                    {proj.status}
                  </span>
                </div>

                {/* Progress bar */}
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '0.25rem' }}>
                    <span>Progress</span>
                    <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{proj.progress}%</span>
                  </div>
                  <div className="progress-container">
                    <div className="progress-fill" style={{ width: `${proj.progress}%` }} />
                  </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: '0.75rem', color: 'var(--text-muted)', paddingTop: '0.25rem' }}>
                  <span>Tech Lead: <strong style={{ color: 'var(--text-primary)' }}>{proj.lead}</strong></span>
                  <span>Team Size: <strong style={{ color: 'var(--text-primary)' }}>{proj.teamSize} members</strong></span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Live Activity & Compliance Feed */}
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <ShieldCheck size={18} color="var(--brand-primary)" />
              <h3 style={{ fontSize: '1.15rem' }}>Audit Trail Feed</h3>
            </div>
            <Link to="/audit" style={{ fontSize: '0.8rem', color: 'var(--brand-primary)', textDecoration: 'none', fontWeight: 600 }}>
              Full Logs &rarr;
            </Link>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {auditLogs.map(log => (
              <div key={log.id} style={{
                padding: '0.85rem',
                borderRadius: 'var(--radius-md)',
                background: 'var(--bg-canvas)',
                border: '1px solid var(--border-subtle)',
                fontSize: '0.8rem'
              }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.35rem' }}>
                  <span className="badge badge-info" style={{ fontSize: '0.65rem' }}>{log.entityType}</span>
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.7rem' }}>{log.timestamp.split(' ')[1] || log.timestamp}</span>
                </div>
                <div style={{ fontWeight: 600, color: 'var(--text-primary)', marginBottom: '0.25rem' }}>
                  {log.action}
                </div>
                <div style={{ color: 'var(--text-secondary)', fontSize: '0.75rem', lineHeight: 1.3 }}>
                  {log.details}
                </div>
                <div style={{ color: 'var(--text-muted)', fontSize: '0.7rem', marginTop: '0.4rem' }}>
                  By: {log.performedBy}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
