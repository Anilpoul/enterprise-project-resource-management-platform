import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api } from '../api/client';
import {
  ArrowLeft,
  Kanban,
  Zap,
  Users,
  Calendar,
  CheckCircle2,
  Clock,
  Plus
} from 'lucide-react';

export default function ProjectDetailPage() {
  const { id } = useParams();
  const [project, setProject] = useState(null);
  const [tasks, setTasks] = useState([]);
  const [sprints, setSprints] = useState([]);
  const [activeTab, setActiveTab] = useState('overview');

  useEffect(() => {
    const fetchData = async () => {
      const [allProjects, allTasks, allSprints] = await Promise.all([
        api.getProjects(),
        api.getTasks(id),
        api.getSprints(id)
      ]);
      const found = allProjects.find(p => p.id === id) || allProjects[0];
      setProject(found);
      setTasks(allTasks);
      setSprints(allSprints);
    };
    fetchData();
  }, [id]);

  if (!project) return <div className="fade-in" style={{ padding: '2rem' }}>Loading workspace...</div>;

  return (
    <div className="fade-in">
      {/* Back button & Title */}
      <div style={{ marginBottom: '1.5rem' }}>
        <Link to="/projects" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-secondary)', textDecoration: 'none', fontSize: '0.85rem', marginBottom: '0.75rem' }}>
          <ArrowLeft size={16} /> Back to Projects
        </Link>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
            <span className="badge badge-primary" style={{ fontSize: '0.85rem', padding: '0.3rem 0.75rem' }}>
              {project.projectKey}
            </span>
            <h1 style={{ fontSize: '1.85rem' }}>{project.name}</h1>
            <span className="badge badge-success">{project.status}</span>
          </div>

          <Link to="/boards" className="btn btn-primary">
            <Kanban size={16} /> Open Kanban Board
          </Link>
        </div>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '1rem', borderBottom: '1px solid var(--border-subtle)', marginBottom: '1.5rem' }}>
        {['overview', 'tasks', 'sprints', 'team'].map(tab => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            style={{
              padding: '0.75rem 1rem',
              background: 'transparent',
              border: 'none',
              borderBottom: activeTab === tab ? '2px solid var(--brand-primary)' : '2px solid transparent',
              color: activeTab === tab ? 'var(--text-primary)' : 'var(--text-secondary)',
              fontWeight: activeTab === tab ? 700 : 500,
              cursor: 'pointer',
              textTransform: 'capitalize'
            }}
          >
            {tab}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      {activeTab === 'overview' && (
        <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '1.5rem' }}>
          <div className="card">
            <h3 style={{ fontSize: '1.1rem', marginBottom: '1rem' }}>Project Mission &amp; Scope</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', lineHeight: 1.6, marginBottom: '1.5rem' }}>
              {project.description}
            </p>

            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))',
              gap: '1rem',
              padding: '1.25rem',
              background: 'var(--bg-surface-elevated)',
              borderRadius: 'var(--radius-md)'
            }}>
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Lead Architect</div>
                <div style={{ fontWeight: 600, marginTop: '0.2rem' }}>{project.lead}</div>
              </div>
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Target Completion</div>
                <div style={{ fontWeight: 600, marginTop: '0.2rem' }}>{project.endDate || '2026-12-31'}</div>
              </div>
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Priority Level</div>
                <div style={{ fontWeight: 600, marginTop: '0.2rem', color: 'var(--status-danger)' }}>{project.priority}</div>
              </div>
            </div>
          </div>

          <div className="card">
            <h3 style={{ fontSize: '1.1rem', marginBottom: '1rem' }}>Work Metrics</h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', marginBottom: '0.4rem' }}>
                  <span>Sprint Completion</span>
                  <span style={{ fontWeight: 600 }}>{project.progress}%</span>
                </div>
                <div className="progress-container">
                  <div className="progress-fill" style={{ width: `${project.progress}%` }} />
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem', background: 'var(--bg-canvas)', borderRadius: 'var(--radius-md)' }}>
                <span style={{ fontSize: '0.825rem', color: 'var(--text-secondary)' }}>Open Issues</span>
                <span style={{ fontWeight: 700, fontSize: '1.1rem' }}>{tasks.filter(t => t.status !== 'DONE').length}</span>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem', background: 'var(--bg-canvas)', borderRadius: 'var(--radius-md)' }}>
                <span style={{ fontSize: '0.825rem', color: 'var(--text-secondary)' }}>Completed Sprints</span>
                <span style={{ fontWeight: 700, fontSize: '1.1rem' }}>23</span>
              </div>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'tasks' && (
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
            <h3 style={{ fontSize: '1.1rem' }}>Task Backlog &amp; Issues</h3>
            <Link to="/boards" className="btn btn-primary btn-sm">
              <Plus size={14} /> Add Task
            </Link>
          </div>

          <div className="table-container">
            <table className="custom-table">
              <thead>
                <tr>
                  <th>Key</th>
                  <th>Title</th>
                  <th>Status</th>
                  <th>Priority</th>
                  <th>Assignee</th>
                  <th>Points</th>
                </tr>
              </thead>
              <tbody>
                {tasks.map(t => (
                  <tr key={t.id}>
                    <td><span className="badge badge-primary">{t.taskKey}</span></td>
                    <td style={{ fontWeight: 500 }}>{t.title}</td>
                    <td>
                      <span className={`badge badge-${t.status === 'DONE' ? 'success' : (t.status === 'IN_PROGRESS' ? 'warning' : 'info')}`}>
                        {t.status}
                      </span>
                    </td>
                    <td>
                      <span style={{ color: t.priority === 'CRITICAL' ? 'var(--status-danger)' : 'var(--text-secondary)', fontWeight: 600 }}>
                        {t.priority}
                      </span>
                    </td>
                    <td>{t.assigneeName}</td>
                    <td><strong>{t.storyPoints}</strong></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {activeTab === 'sprints' && (
        <div className="card">
          <h3 style={{ fontSize: '1.1rem', marginBottom: '1rem' }}>Project Sprints</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {sprints.map(s => (
              <div key={s.id} style={{
                padding: '1rem',
                borderRadius: 'var(--radius-md)',
                background: 'var(--bg-surface-elevated)',
                border: '1px solid var(--border-subtle)',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center'
              }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.35rem' }}>
                    <Zap size={16} color="var(--brand-primary)" />
                    <h4 style={{ fontSize: '0.95rem' }}>{s.name}</h4>
                    <span className="badge badge-success">{s.status}</span>
                  </div>
                  <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>{s.goal}</p>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: '0.95rem', fontWeight: 700 }}>{s.completedPoints} / {s.totalPoints} pts</div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{s.startDate} &rarr; {s.endDate}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {activeTab === 'team' && (
        <div className="card">
          <h3 style={{ fontSize: '1.1rem', marginBottom: '1rem' }}>Assigned Engineers</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '1rem' }}>
            {[
              { name: 'Elena Rostova', role: 'Principal Architect', allocation: '75%' },
              { name: 'Marcus Vance', role: 'Senior Fullstack Dev', allocation: '100%' },
              { name: 'Alex Chen', role: 'Lead PM', allocation: '50%' }
            ].map(m => (
              <div key={m.name} style={{
                padding: '1rem',
                borderRadius: 'var(--radius-md)',
                background: 'var(--bg-surface-elevated)',
                border: '1px solid var(--border-subtle)',
                display: 'flex',
                alignItems: 'center',
                gap: '0.75rem'
              }}>
                <div style={{
                  width: '38px', height: '38px', borderRadius: '50%',
                  background: 'var(--brand-gradient)', color: '#ffffff',
                  fontWeight: 700, display: 'flex', alignItems: 'center', justifyContent: 'center'
                }}>
                  {m.name.split(' ').map(n => n[0]).join('')}
                </div>
                <div>
                  <div style={{ fontWeight: 600 }}>{m.name}</div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>{m.role} • {m.allocation}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
