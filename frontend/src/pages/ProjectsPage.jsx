import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { Plus, Search, FolderKanban, Calendar, Users, ArrowUpRight, AlertCircle, Zap, Kanban } from 'lucide-react';
import { Link } from 'react-router-dom';
import Modal from '../components/Modal';

export default function ProjectsPage() {
  const { user } = useAuth();
  const [projects, setProjects] = useState([]);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  // New Project Form State
  const [newProject, setNewProject] = useState({
    name: '',
    projectKey: '',
    projectType: 'SOFTWARE',
    description: '',
    startDate: new Date().toISOString().split('T')[0],
    targetEndDate: new Date(Date.now() + 90 * 86400000).toISOString().split('T')[0]
  });

  const fetchProjects = async () => {
    const data = await api.getProjects();
    setProjects(data);
  };

  useEffect(() => {
    fetchProjects();
  }, []);

  const handleCreateProject = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrorMsg('');

    const res = await api.createProject({
      ...newProject,
      projectKey: newProject.projectKey.toUpperCase().trim(),
      leadUserId: user?.id || '00000000-0000-0000-0000-000000000001'
    });

    setLoading(false);
    if (res.success && res.data) {
      setProjects(prev => [res.data, ...prev]);
      setIsModalOpen(false);
      setNewProject({
        name: '',
        projectKey: '',
        projectType: 'SOFTWARE',
        description: '',
        startDate: new Date().toISOString().split('T')[0],
        targetEndDate: new Date(Date.now() + 90 * 86400000).toISOString().split('T')[0]
      });
    } else {
      setErrorMsg(res.error || 'Failed to create project');
    }
  };

  const filteredProjects = projects.filter(p => {
    const matchesSearch = (p.name || '').toLowerCase().includes(search.toLowerCase()) ||
                          (p.projectKey || '').toLowerCase().includes(search.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || p.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  return (
    <div className="fade-in">
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.85rem', marginBottom: '0.25rem' }}>Projects &amp; Workspaces</h1>
          <p style={{ color: 'var(--text-secondary)' }}>
            Manage enterprise project repositories, team assignments, and delivery status
          </p>
        </div>
        <button onClick={() => { setIsModalOpen(true); setErrorMsg(''); }} className="btn btn-primary">
          <Plus size={16} /> New Project
        </button>
      </div>

      {/* Filter & Search Bar */}
      <div className="card" style={{ padding: '1rem 1.25rem', marginBottom: '1.5rem', display: 'flex', gap: '1rem', alignItems: 'center' }}>
        <div style={{ display: 'flex', alignItems: 'center', flex: 1, position: 'relative' }}>
          <Search size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px' }} />
          <input
            type="text"
            placeholder="Filter projects by title or key..."
            className="input-field"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            style={{ paddingLeft: '36px' }}
          />
        </div>

        <div style={{ display: 'flex', gap: '0.5rem' }}>
          {['ALL', 'ACTIVE', 'PLANNING', 'COMPLETED'].map(status => (
            <button
              key={status}
              onClick={() => setStatusFilter(status)}
              className={`btn btn-sm ${statusFilter === status ? 'btn-primary' : 'btn-secondary'}`}
            >
              {status}
            </button>
          ))}
        </div>
      </div>

      {/* Empty State */}
      {filteredProjects.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '3.5rem 1rem' }}>
          <FolderKanban size={48} color="var(--brand-primary)" style={{ opacity: 0.6, marginBottom: '1rem' }} />
          <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>No Projects Found</h3>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', maxWidth: '400px', margin: '0 auto 1.5rem' }}>
            Start managing issues, sprints, and code deliverables by creating your first project workspace.
          </p>
          <button onClick={() => { setIsModalOpen(true); setErrorMsg(''); }} className="btn btn-primary">
            <Plus size={16} /> Create Your First Project
          </button>
        </div>
      ) : (
        /* Projects Grid */
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))',
          gap: '1.5rem'
        }}>
          {filteredProjects.map(proj => (
            <div key={proj.id} className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
                  <span className="badge badge-primary">{proj.projectKey}</span>
                  <span className={`badge badge-${proj.status === 'ACTIVE' ? 'success' : (proj.status === 'COMPLETED' ? 'info' : 'warning')}`}>
                    {proj.status || 'ACTIVE'}
                  </span>
                </div>

                <Link
                  to={`/projects/${proj.id}`}
                  style={{
                    fontSize: '1.15rem',
                    fontWeight: 700,
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '0.35rem',
                    color: 'var(--text-primary)',
                    textDecoration: 'none',
                    marginBottom: '0.5rem'
                  }}
                >
                  {proj.name}
                  <ArrowUpRight size={16} color="var(--brand-primary)" />
                </Link>

                <p style={{
                  fontSize: '0.825rem',
                  color: 'var(--text-secondary)',
                  lineHeight: 1.5,
                  marginBottom: '1.25rem'
                }}>
                  {proj.description || 'No description provided.'}
                </p>
              </div>

              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', marginBottom: '0.35rem' }}>
                  <span style={{ color: 'var(--text-muted)' }}>Type</span>
                  <span style={{ fontWeight: 600 }}>{proj.projectType || 'SOFTWARE'}</span>
                </div>

                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  borderTop: '1px solid var(--border-subtle)',
                  paddingTop: '0.85rem',
                  marginTop: '0.85rem',
                  fontSize: '0.75rem'
                }}>
                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <Link to={`/boards`} className="btn btn-secondary btn-sm" style={{ fontSize: '0.75rem', padding: '0.3rem 0.6rem' }}>
                      <Kanban size={13} /> Board
                    </Link>
                    <Link to={`/sprints`} className="btn btn-secondary btn-sm" style={{ fontSize: '0.75rem', padding: '0.3rem 0.6rem' }}>
                      <Zap size={13} /> Sprints
                    </Link>
                  </div>
                  <span style={{ color: 'var(--text-muted)' }}>ID: {(proj.id || '').substring(0, 8)}...</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* New Project Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Create New Project"
      >
        <form onSubmit={handleCreateProject}>
          {errorMsg && (
            <div style={{
              padding: '0.75rem',
              background: 'rgba(239, 68, 68, 0.12)',
              border: '1px solid rgba(239, 68, 68, 0.4)',
              color: 'var(--status-danger)',
              borderRadius: 'var(--radius-md)',
              marginBottom: '1.25rem',
              fontSize: '0.825rem',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem'
            }}>
              <AlertCircle size={16} />
              <span>{errorMsg}</span>
            </div>
          )}

          <div className="input-group">
            <label className="input-label">Project Name *</label>
            <input
              type="text"
              className="input-field"
              value={newProject.name}
              onChange={(e) => setNewProject({ ...newProject, name: e.target.value })}
              placeholder="e.g. Core Banking Platform"
              required
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="input-group">
              <label className="input-label">Project Key * (2-10 UPPERCASE)</label>
              <input
                type="text"
                className="input-field"
                value={newProject.projectKey}
                onChange={(e) => setNewProject({ ...newProject, projectKey: e.target.value.toUpperCase().replace(/[^A-Z0-9]/g, '') })}
                placeholder="e.g. CBP"
                maxLength={10}
                required
              />
            </div>

            <div className="input-group">
              <label className="input-label">Project Type *</label>
              <select
                className="input-field"
                value={newProject.projectType}
                onChange={(e) => setNewProject({ ...newProject, projectType: e.target.value })}
              >
                <option value="SOFTWARE">Software Development</option>
                <option value="BUSINESS">Business Initiative</option>
                <option value="OPERATIONS">Cloud Operations</option>
              </select>
            </div>
          </div>

          <div className="input-group">
            <label className="input-label">Description</label>
            <textarea
              className="input-field"
              rows={3}
              value={newProject.description}
              onChange={(e) => setNewProject({ ...newProject, description: e.target.value })}
              placeholder="Primary deliverables, stakeholders, and scope..."
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="input-group">
              <label className="input-label">Start Date</label>
              <input
                type="date"
                className="input-field"
                value={newProject.startDate}
                onChange={(e) => setNewProject({ ...newProject, startDate: e.target.value })}
              />
            </div>

            <div className="input-group">
              <label className="input-label">Target Completion</label>
              <input
                type="date"
                className="input-field"
                value={newProject.targetEndDate}
                onChange={(e) => setNewProject({ ...newProject, targetEndDate: e.target.value })}
              />
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
            <button type="button" onClick={() => setIsModalOpen(false)} className="btn btn-secondary">
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Creating Project...' : 'Create Project'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
