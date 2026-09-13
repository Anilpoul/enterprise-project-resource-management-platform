import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { Plus, Search, FolderKanban, Calendar, Users, ArrowUpRight } from 'lucide-react';
import { Link } from 'react-router-dom';
import Modal from '../components/Modal';

export default function ProjectsPage() {
  const [projects, setProjects] = useState([]);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [isModalOpen, setIsModalOpen] = useState(false);

  // New Project Form State
  const [newProject, setNewProject] = useState({
    name: '',
    projectKey: '',
    description: '',
    status: 'ACTIVE',
    priority: 'HIGH'
  });

  useEffect(() => {
    const fetchProjects = async () => {
      const data = await api.getProjects();
      setProjects(data);
    };
    fetchProjects();
  }, []);

  const handleCreateProject = async (e) => {
    e.preventDefault();
    const created = await api.createProject(newProject);
    setProjects(prev => [created, ...prev]);
    setIsModalOpen(false);
    setNewProject({ name: '', projectKey: '', description: '', status: 'ACTIVE', priority: 'HIGH' });
  };

  const filteredProjects = projects.filter(p => {
    const matchesSearch = p.name.toLowerCase().includes(search.toLowerCase()) ||
                          p.projectKey.toLowerCase().includes(search.toLowerCase());
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
        <button onClick={() => setIsModalOpen(true)} className="btn btn-primary">
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

      {/* Projects Grid */}
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
                  {proj.status}
                </span>
              </div>

              <h3 style={{ fontSize: '1.15rem', marginBottom: '0.5rem' }}>{proj.name}</h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.825rem', lineHeight: 1.4, marginBottom: '1.25rem' }}>
                {proj.description}
              </p>
            </div>

            <div>
              {/* Progress */}
              <div style={{ marginBottom: '1rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '0.25rem' }}>
                  <span>Completion</span>
                  <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{proj.progress}%</span>
                </div>
                <div className="progress-container">
                  <div className="progress-fill" style={{ width: `${proj.progress}%` }} />
                </div>
              </div>

              <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                paddingTop: '0.75rem',
                borderTop: '1px solid var(--border-subtle)',
                fontSize: '0.775rem',
                color: 'var(--text-secondary)'
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                  <Users size={14} /> {proj.teamSize} members
                </div>
                <Link
                  to={`/projects/${proj.id}`}
                  style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', color: 'var(--brand-primary)', textDecoration: 'none', fontWeight: 600 }}
                >
                  Workspace <ArrowUpRight size={14} />
                </Link>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* New Project Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Create New Project"
        footer={
          <>
            <button onClick={() => setIsModalOpen(false)} className="btn btn-secondary">Cancel</button>
            <button onClick={handleCreateProject} className="btn btn-primary">Create Workspace</button>
          </>
        }
      >
        <form onSubmit={handleCreateProject}>
          <div className="input-group">
            <label className="input-label">Project Name</label>
            <input
              type="text"
              className="input-field"
              placeholder="e.g. Distributed Payment Gateway"
              value={newProject.name}
              onChange={(e) => setNewProject({ ...newProject, name: e.target.value })}
              required
            />
          </div>

          <div className="input-group">
            <label className="input-label">Project Key (3-6 chars)</label>
            <input
              type="text"
              className="input-field"
              placeholder="e.g. DPG"
              value={newProject.projectKey}
              onChange={(e) => setNewProject({ ...newProject, projectKey: e.target.value.toUpperCase() })}
              required
            />
          </div>

          <div className="input-group">
            <label className="input-label">Description</label>
            <textarea
              className="textarea-field"
              rows="3"
              placeholder="Goals and scope of this project..."
              value={newProject.description}
              onChange={(e) => setNewProject({ ...newProject, description: e.target.value })}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="input-group">
              <label className="input-label">Initial Status</label>
              <select
                className="select-field"
                value={newProject.status}
                onChange={(e) => setNewProject({ ...newProject, status: e.target.value })}
              >
                <option value="PLANNING">Planning</option>
                <option value="ACTIVE">Active</option>
                <option value="ON_HOLD">On Hold</option>
              </select>
            </div>

            <div className="input-group">
              <label className="input-label">Priority</label>
              <select
                className="select-field"
                value={newProject.priority}
                onChange={(e) => setNewProject({ ...newProject, priority: e.target.value })}
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="CRITICAL">Critical</option>
              </select>
            </div>
          </div>
        </form>
      </Modal>
    </div>
  );
}
