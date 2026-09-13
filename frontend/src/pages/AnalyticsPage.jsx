import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import {
  BarChart3,
  TrendingUp,
  Award,
  CheckCircle2,
  AlertTriangle,
  Zap,
  Star
} from 'lucide-react';

export default function AnalyticsPage() {
  const [analytics, setAnalytics] = useState(null);

  useEffect(() => {
    const fetchAnalytics = async () => {
      const data = await api.getDashboardAnalytics();
      setAnalytics(data);
    };
    fetchAnalytics();
  }, []);

  const SCORECARDS = [
    { name: 'Elena Rostova', role: 'Principal Architect', score: 96, rating: 'EXCELLENT', velocity: 26, tasksCompleted: 18 },
    { name: 'Marcus Vance', role: 'Senior Fullstack Dev', score: 92, rating: 'EXCELLENT', velocity: 22, tasksCompleted: 15 },
    { name: 'Sarah Connor', role: 'CTO / System Lead', score: 88, rating: 'COMMENDABLE', velocity: 16, tasksCompleted: 11 },
    { name: 'Alex Chen', role: 'Lead PM', score: 84, rating: 'COMMENDABLE', velocity: 14, tasksCompleted: 9 }
  ];

  return (
    <div className="fade-in">
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.85rem', marginBottom: '0.25rem' }}>Analytics &amp; Executive KPIs</h1>
        <p style={{ color: 'var(--text-secondary)' }}>
          Portfolio health diagnostics, sprint velocity distribution, and employee productivity scorecards
        </p>
      </div>

      {/* Analytics KPI Row */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '1.25rem', marginBottom: '2rem' }}>
        <div className="card">
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Overall Completion</div>
          <div style={{ fontSize: '2rem', fontWeight: 800, margin: '0.4rem 0', color: 'var(--status-success)' }}>
            {analytics?.overallCompletionRate || 72.9}%
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Based on 48 tracked tasks</div>
        </div>

        <div className="card">
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Average Velocity</div>
          <div style={{ fontSize: '2rem', fontWeight: 800, margin: '0.4rem 0', color: 'var(--brand-primary)' }}>
            {analytics?.averageSprintVelocity || 42.5}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Points per 2-week iteration</div>
        </div>

        <div className="card">
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Projects On-Track</div>
          <div style={{ fontSize: '2rem', fontWeight: 800, margin: '0.4rem 0', color: 'var(--brand-secondary)' }}>
            2 of 3
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>1 Workspace at Risk</div>
        </div>

        <div className="card">
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Active Contributors</div>
          <div style={{ fontSize: '2rem', fontWeight: 800, margin: '0.4rem 0', color: 'var(--text-primary)' }}>
            4 Engineers
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>101.3% Net Utilization</div>
        </div>
      </div>

      {/* Two Column Layout: Project Health & Velocity Graph */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem', marginBottom: '2rem' }}>
        {/* Project Health Indicators */}
        <div className="card">
          <h3 style={{ fontSize: '1.15rem', marginBottom: '1.25rem' }}>Project Health Matrix</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {[
              { name: 'NextGen Core Banking Engine', status: 'ON_TRACK', pct: 72, risk: 'Low Risk' },
              { name: 'Zero-Trust Identity Fabric', status: 'ON_TRACK', pct: 88, risk: 'Low Risk' },
              { name: 'AI Smart Telemetry Agent', status: 'AT_RISK', pct: 25, risk: 'Staffing Shortage' }
            ].map(p => (
              <div key={p.name} style={{
                padding: '1rem',
                background: 'var(--bg-surface-elevated)',
                borderRadius: 'var(--radius-md)',
                border: '1px solid var(--border-subtle)'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                  <span style={{ fontWeight: 600, fontSize: '0.9rem' }}>{p.name}</span>
                  <span className={`badge badge-${p.status === 'ON_TRACK' ? 'success' : 'warning'}`}>
                    {p.status}
                  </span>
                </div>
                <div className="progress-container" style={{ marginBottom: '0.5rem' }}>
                  <div className="progress-fill" style={{ width: `${p.pct}%` }} />
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                  <span>{p.pct}% milestones achieved</span>
                  <span>{p.risk}</span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Sprint Velocity Progression Chart */}
        <div className="card">
          <h3 style={{ fontSize: '1.15rem', marginBottom: '1.25rem' }}>Velocity Velocity Trends (Past 5 Sprints)</h3>
          <div style={{
            height: '240px',
            display: 'flex',
            alignItems: 'flex-end',
            justifyContent: 'space-around',
            padding: '1rem 0',
            borderBottom: '2px solid var(--border-subtle)'
          }}>
            {[
              { sprint: 'Sprint 20', points: 30, pct: 60 },
              { sprint: 'Sprint 21', points: 36, pct: 72 },
              { sprint: 'Sprint 22', points: 40, pct: 80 },
              { sprint: 'Sprint 23', points: 44, pct: 88 },
              { sprint: 'Sprint 24', points: 48, pct: 96 }
            ].map(col => (
              <div key={col.sprint} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.5rem' }}>
                <span style={{ fontWeight: 700, fontSize: '0.8rem' }}>{col.points}p</span>
                <div style={{
                  width: '42px',
                  height: `${col.pct * 1.6}px`,
                  background: 'var(--brand-gradient)',
                  borderRadius: '6px 6px 0 0',
                  boxShadow: 'var(--brand-glow)',
                  transition: 'height 0.4s ease'
                }} />
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.3rem' }}>{col.sprint}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Employee Productivity Scorecards */}
      <div className="card">
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
          <Award size={20} color="var(--brand-primary)" />
          <h3 style={{ fontSize: '1.15rem' }}>Employee Productivity &amp; Performance Scorecards</h3>
        </div>

        <div className="table-container">
          <table className="custom-table">
            <thead>
              <tr>
                <th>Engineer</th>
                <th>Role</th>
                <th>Overall Score</th>
                <th>Performance Rating</th>
                <th>Velocity Contribution</th>
                <th>Tasks Delivered</th>
              </tr>
            </thead>
            <tbody>
              {SCORECARDS.map(sc => (
                <tr key={sc.name}>
                  <td style={{ fontWeight: 600 }}>{sc.name}</td>
                  <td style={{ color: 'var(--text-secondary)' }}>{sc.role}</td>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <strong style={{ fontSize: '1rem', color: 'var(--brand-primary)' }}>{sc.score}</strong>
                      <div className="progress-container" style={{ width: '80px', height: '6px' }}>
                        <div className="progress-fill" style={{ width: `${sc.score}%` }} />
                      </div>
                    </div>
                  </td>
                  <td>
                    <span className="badge badge-success">
                      <Star size={10} /> {sc.rating}
                    </span>
                  </td>
                  <td><strong>{sc.velocity}</strong> pts</td>
                  <td><strong>{sc.tasksCompleted}</strong> completed</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
