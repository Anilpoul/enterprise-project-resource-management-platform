import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { Zap, Calendar, Target, CheckCircle2, Play, Plus, Clock, AlertCircle, CheckCheck } from 'lucide-react';
import { Link } from 'react-router-dom';
import Modal from '../components/Modal';

export default function SprintsPage() {
  const [projects, setProjects] = useState([]);
  const [selectedProjectId, setSelectedProjectId] = useState('');
  const [sprints, setSprints] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  // New Sprint Form State
  const [newSprint, setNewSprint] = useState({
    name: '',
    goal: '',
    startDate: new Date().toISOString().split('T')[0],
    endDate: new Date(Date.now() + 14 * 86400000).toISOString().split('T')[0]
  });

  // Load projects first
  useEffect(() => {
    const loadProjects = async () => {
      const projs = await api.getProjects();
      setProjects(projs);
      if (projs.length > 0) {
        setSelectedProjectId(projs[0].id);
      }
    };
    loadProjects();
  }, []);

  // Fetch sprints whenever selected project changes
  const fetchSprints = async () => {
    const data = await api.getSprints(selectedProjectId);
    setSprints(data);
  };

  useEffect(() => {
    if (selectedProjectId) {
      fetchSprints();
    }
  }, [selectedProjectId]);

  const handleCreateSprint = async (e) => {
    e.preventDefault();
    if (!selectedProjectId) {
      setErrorMsg('Please select or create a project first.');
      return;
    }

    setLoading(true);
    setErrorMsg('');

    const res = await api.createSprint({
      projectId: selectedProjectId,
      name: newSprint.name,
      goal: newSprint.goal,
      startDate: newSprint.startDate,
      endDate: newSprint.endDate
    });

    setLoading(false);
    if (res.success && res.data) {
      setSprints(prev => [res.data, ...prev]);
      setIsModalOpen(false);
      setSuccessMsg(`Sprint "${newSprint.name}" created successfully!`);
      setTimeout(() => setSuccessMsg(''), 3000);
      setNewSprint({
        name: '',
        goal: '',
        startDate: new Date().toISOString().split('T')[0],
        endDate: new Date(Date.now() + 14 * 86400000).toISOString().split('T')[0]
      });
    } else {
      setErrorMsg(res.error || 'Failed to create sprint');
    }
  };

  const handleStartSprint = async (sprintId, goal) => {
    setLoading(true);
    const res = await api.startSprint(sprintId, { goal });
    setLoading(false);
    if (res.success) {
      setSuccessMsg('Sprint started and moved to active!');
      setTimeout(() => setSuccessMsg(''), 3000);
      fetchSprints();
    } else {
      setErrorMsg(res.error || 'Failed to start sprint');
    }
  };

  const handleCompleteSprint = async (sprintId) => {
    if (!window.confirm('Are you sure you want to complete this sprint? Remaining open tasks will move to backlog.')) return;

    setLoading(true);
    const res = await api.completeSprint(sprintId);
    setLoading(false);
    if (res.success) {
      setSuccessMsg('Sprint completed successfully!');
      setTimeout(() => setSuccessMsg(''), 3000);
      fetchSprints();
    } else {
      setErrorMsg(res.error || 'Failed to complete sprint');
    }
  };

  const activeSprint = sprints.find(s => s.status === 'ACTIVE');
  const otherSprints = sprints.filter(s => s.id !== activeSprint?.id);

  return (
    <div className="fade-in">
      {/* Header & Controls */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.75rem' }}>
        <div>
          <h1 style={{ fontSize: '1.85rem', marginBottom: '0.25rem' }}>Sprint Planning &amp; Iterations</h1>
          <p style={{ color: 'var(--text-secondary)' }}>
            Manage iteration cycles, define sprint goals, and drive commitments
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          {/* Project Selector */}
          {projects.length > 0 && (
            <select
              className="input-field"
              value={selectedProjectId}
              onChange={(e) => setSelectedProjectId(e.target.value)}
              style={{ width: '220px', height: '38px', fontSize: '0.85rem' }}
            >
              {projects.map(p => (
                <option key={p.id} value={p.id}>
                  {p.projectKey ? `[${p.projectKey}] ` : ''}{p.name}
                </option>
              ))}
            </select>
          )}

          <button onClick={() => { setIsModalOpen(true); setErrorMsg(''); }} className="btn btn-primary">
            <Plus size={16} /> New Sprint
          </button>
          <Link to="/boards" className="btn btn-secondary">
            <Zap size={16} /> Open Board
          </Link>
        </div>
      </div>

      {/* Success / Error Alerts */}
      {successMsg && (
        <div style={{
          padding: '0.75rem 1rem',
          background: 'rgba(16, 185, 129, 0.12)',
          border: '1px solid rgba(16, 185, 129, 0.4)',
          borderRadius: 'var(--radius-md)',
          color: 'var(--status-success)',
          fontSize: '0.85rem',
          marginBottom: '1.5rem',
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem'
        }}>
          <CheckCircle2 size={16} />
          <span>{successMsg}</span>
        </div>
      )}

      {errorMsg && (
        <div style={{
          padding: '0.75rem 1rem',
          background: 'rgba(239, 68, 68, 0.12)',
          border: '1px solid rgba(239, 68, 68, 0.4)',
          borderRadius: 'var(--radius-md)',
          color: 'var(--status-danger)',
          fontSize: '0.85rem',
          marginBottom: '1.5rem',
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem'
        }}>
          <AlertCircle size={16} />
          <span>{errorMsg}</span>
        </div>
      )}

      {/* Active Sprint Spotlight Card */}
      {activeSprint ? (
        <div className="card" style={{
          background: 'linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(6, 182, 212, 0.05) 100%)',
          border: '1px solid var(--border-highlight)',
          marginBottom: '2rem',
          padding: '1.75rem'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <span className="badge badge-success" style={{ fontSize: '0.75rem' }}>ACTIVE SPRINT</span>
              <h2 style={{ fontSize: '1.4rem' }}>{activeSprint.name}</h2>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-muted)', fontSize: '0.825rem' }}>
                <Calendar size={15} /> {activeSprint.startDate} &rarr; {activeSprint.endDate}
              </div>
              <button
                onClick={() => handleCompleteSprint(activeSprint.id)}
                className="btn btn-secondary btn-sm"
                disabled={loading}
              >
                <CheckCheck size={14} /> Complete Sprint
              </button>
            </div>
          </div>

          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.6rem',
            padding: '0.75rem 1rem',
            background: 'var(--bg-surface)',
            borderRadius: 'var(--radius-md)',
            border: '1px solid var(--border-subtle)',
            marginBottom: '1.25rem',
            fontSize: '0.85rem'
          }}>
            <Target size={18} color="var(--brand-primary)" />
            <span>Sprint Goal: <strong style={{ color: 'var(--text-primary)' }}>{activeSprint.goal || 'No goal set'}</strong></span>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
              Status: <span style={{ color: 'var(--status-success)', fontWeight: 600 }}>Active Iteration</span>
            </span>
            <Link to="/boards" className="btn btn-primary btn-sm">
              <Zap size={14} /> View Kanban Board
            </Link>
          </div>
        </div>
      ) : (
        <div className="card" style={{
          padding: '1.5rem',
          marginBottom: '2rem',
          background: 'var(--bg-surface-elevated)',
          border: '1px dashed var(--border-highlight)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between'
        }}>
          <div>
            <h3 style={{ fontSize: '1.1rem', marginBottom: '0.25rem' }}>No Active Sprint</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.825rem' }}>
              Start an upcoming sprint from the list below or create a new iteration to begin tracking team velocity.
            </p>
          </div>
          <button onClick={() => { setIsModalOpen(true); setErrorMsg(''); }} className="btn btn-primary btn-sm">
            <Plus size={14} /> Create Sprint
          </button>
        </div>
      )}

      {/* Planned & Past Sprints List */}
      <h3 style={{ fontSize: '1.2rem', marginBottom: '1rem' }}>Planned &amp; Past Iterations</h3>
      {otherSprints.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '2.5rem 1rem', color: 'var(--text-muted)' }}>
          No planned or completed iterations found for this project.
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
          {otherSprints.map(s => (
            <div key={s.id} className="card" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '1rem 1.25rem' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.35rem' }}>
                  <span className={`badge badge-${s.status === 'COMPLETED' ? 'info' : 'warning'}`}>
                    {s.status || 'PLANNING'}
                  </span>
                  <h4 style={{ fontSize: '1rem', fontWeight: 600 }}>{s.name}</h4>
                </div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', display: 'flex', alignItems: 'center', gap: '1rem' }}>
                  <span>Goal: {s.goal || 'General development'}</span>
                  <span>Dates: {s.startDate} &rarr; {s.endDate}</span>
                </div>
              </div>

              <div>
                {s.status === 'PLANNING' && (
                  <button
                    onClick={() => handleStartSprint(s.id, s.goal)}
                    className="btn btn-primary btn-sm"
                    disabled={loading}
                  >
                    <Play size={14} /> Start Sprint
                  </button>
                )}
                {s.status === 'COMPLETED' && (
                  <span className="badge badge-success" style={{ fontSize: '0.75rem' }}>
                    <CheckCircle2 size={12} style={{ display: 'inline', marginRight: '3px' }} /> Concluded
                  </span>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Create Sprint Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Create New Sprint"
      >
        <form onSubmit={handleCreateSprint}>
          {errorMsg && (
            <div style={{ padding: '0.75rem', background: 'rgba(239, 68, 68, 0.12)', color: 'var(--status-danger)', borderRadius: 'var(--radius-md)', marginBottom: '1rem', fontSize: '0.825rem' }}>
              {errorMsg}
            </div>
          )}

          <div className="input-group">
            <label className="input-label">Project *</label>
            <select
              className="input-field"
              value={selectedProjectId}
              onChange={(e) => setSelectedProjectId(e.target.value)}
              required
            >
              {projects.map(p => (
                <option key={p.id} value={p.id}>
                  {p.projectKey ? `[${p.projectKey}] ` : ''}{p.name}
                </option>
              ))}
            </select>
          </div>

          <div className="input-group">
            <label className="input-label">Sprint Name *</label>
            <input
              type="text"
              className="input-field"
              value={newSprint.name}
              onChange={(e) => setNewSprint({ ...newSprint, name: e.target.value })}
              placeholder="e.g. Sprint 1 - Authentication & Core API"
              required
            />
          </div>

          <div className="input-group">
            <label className="input-label">Sprint Goal</label>
            <textarea
              className="input-field"
              rows={2}
              value={newSprint.goal}
              onChange={(e) => setNewSprint({ ...newSprint, goal: e.target.value })}
              placeholder="What is the key target outcome for this iteration?"
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="input-group">
              <label className="input-label">Start Date *</label>
              <input
                type="date"
                className="input-field"
                value={newSprint.startDate}
                onChange={(e) => setNewSprint({ ...newSprint, startDate: e.target.value })}
                required
              />
            </div>

            <div className="input-group">
              <label className="input-label">End Date *</label>
              <input
                type="date"
                className="input-field"
                value={newSprint.endDate}
                onChange={(e) => setNewSprint({ ...newSprint, endDate: e.target.value })}
                required
              />
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
            <button type="button" onClick={() => setIsModalOpen(false)} className="btn btn-secondary">
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Creating...' : 'Create Sprint'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
