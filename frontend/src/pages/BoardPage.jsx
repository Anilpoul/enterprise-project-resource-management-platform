import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';
import {
  Plus,
  Search,
  ArrowRight,
  ArrowLeft,
  CheckCircle2,
  AlertCircle,
  Clock,
  Sparkles,
  Layers,
  FolderKanban,
  Zap
} from 'lucide-react';
import Modal from '../components/Modal';

const COLUMNS = [
  { id: 'TODO', label: 'To Do', color: 'var(--text-muted)' },
  { id: 'IN_PROGRESS', label: 'In Progress', color: 'var(--brand-primary)' },
  { id: 'REVIEW', label: 'Review', color: 'var(--brand-secondary)' },
  { id: 'DONE', label: 'Done', color: 'var(--status-success)' }
];

export default function BoardPage() {
  const { user } = useAuth();
  const [projects, setProjects] = useState([]);
  const [selectedProjectId, setSelectedProjectId] = useState('');
  const [sprints, setSprints] = useState([]);
  const [selectedSprintId, setSelectedSprintId] = useState('');
  const [tasks, setTasks] = useState([]);
  const [search, setSearch] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [targetColumn, setTargetColumn] = useState('TODO');
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  // New Task Form State
  const [newTask, setNewTask] = useState({
    title: '',
    description: '',
    taskType: 'STORY',
    priority: 'HIGH',
    storyPoints: 5,
    estimatedHours: 8
  });

  // Load Projects on mount
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

  // Load Sprints when project changes
  useEffect(() => {
    const loadSprints = async () => {
      if (!selectedProjectId) return;
      const sps = await api.getSprints(selectedProjectId);
      setSprints(sps);
      const active = sps.find(s => s.status === 'ACTIVE');
      if (active) {
        setSelectedSprintId(active.id);
      } else if (sps.length > 0) {
        setSelectedSprintId(sps[0].id);
      } else {
        setSelectedSprintId('');
      }
    };
    loadSprints();
  }, [selectedProjectId]);

  // Load Tasks when project or sprint changes
  const fetchTasks = async () => {
    if (!selectedProjectId) return;
    const data = await api.getTasks(selectedProjectId, selectedSprintId);
    setTasks(data);
  };

  useEffect(() => {
    fetchTasks();
  }, [selectedProjectId, selectedSprintId]);

  const moveTask = async (taskId, direction) => {
    const currentTask = tasks.find(t => t.id === taskId);
    if (!currentTask) return;

    const currentIndex = COLUMNS.findIndex(c => c.id === currentTask.status);
    const newIndex = currentIndex + direction;

    if (newIndex >= 0 && newIndex < COLUMNS.length) {
      const newStatus = COLUMNS[newIndex].id;

      // Optimistic update
      setTasks(prev =>
        prev.map(t => t.id === taskId ? { ...t, status: newStatus } : t)
      );

      const res = await api.updateTaskStatus(taskId, newStatus);
      if (!res.success) {
        // Revert on failure
        setTasks(prev =>
          prev.map(t => t.id === taskId ? { ...t, status: currentTask.status } : t)
        );
        setErrorMsg(res.error || 'Failed to update task status');
        setTimeout(() => setErrorMsg(''), 4000);
      }
    }
  };

  const handleCreateTask = async (e) => {
    e.preventDefault();
    if (!selectedProjectId) {
      setErrorMsg('Please select or create a project first.');
      return;
    }

    const currentProject = projects.find(p => p.id === selectedProjectId);
    const projectKey = currentProject?.projectKey || 'TASK';

    setLoading(true);
    setErrorMsg('');

    const res = await api.createTask({
      projectId: selectedProjectId,
      projectKey,
      title: newTask.title,
      description: newTask.description,
      taskType: newTask.taskType,
      priority: newTask.priority,
      storyPoints: Number(newTask.storyPoints),
      estimatedHours: Number(newTask.estimatedHours),
      sprintId: selectedSprintId || null,
      assigneeId: user?.id || null
    });

    setLoading(false);
    if (res.success && res.data) {
      const created = res.data;
      setTasks(prev => [
        ...prev,
        {
          ...created,
          status: targetColumn,
          assigneeName: `${user?.firstName || 'User'} ${user?.lastName || ''}`.trim()
        }
      ]);
      setIsModalOpen(false);
      setNewTask({
        title: '',
        description: '',
        taskType: 'STORY',
        priority: 'HIGH',
        storyPoints: 5,
        estimatedHours: 8
      });
    } else {
      setErrorMsg(res.error || 'Failed to create task');
    }
  };

  const currentProject = projects.find(p => p.id === selectedProjectId);
  const currentSprint = sprints.find(s => s.id === selectedSprintId);

  const filteredTasks = tasks.filter(t =>
    (t.title || '').toLowerCase().includes(search.toLowerCase()) ||
    (t.taskKey || '').toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="fade-in" style={{ display: 'flex', flexDirection: 'column', height: 'calc(100vh - 130px)' }}>
      {/* Board Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.25rem', flexShrink: 0 }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.2rem' }}>
            <h1 style={{ fontSize: '1.75rem' }}>
              {currentSprint ? currentSprint.name : (currentProject ? `${currentProject.name} Board` : 'Kanban Board')}
            </h1>
            {currentSprint?.status === 'ACTIVE' && (
              <span className="badge badge-success" style={{ fontSize: '0.7rem' }}>ACTIVE SPRINT</span>
            )}
          </div>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.825rem' }}>
            {currentSprint?.goal ? `Goal: ${currentSprint.goal}` : 'Drag or move cards through workflows across columns'}
          </p>
        </div>

        {/* Filters & Actions */}
        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          {/* Project Switcher */}
          {projects.length > 0 && (
            <select
              className="input-field"
              value={selectedProjectId}
              onChange={(e) => setSelectedProjectId(e.target.value)}
              style={{ width: '190px', height: '36px', fontSize: '0.8rem' }}
            >
              {projects.map(p => (
                <option key={p.id} value={p.id}>
                  {p.projectKey ? `[${p.projectKey}] ` : ''}{p.name}
                </option>
              ))}
            </select>
          )}

          {/* Sprint Switcher */}
          <select
            className="input-field"
            value={selectedSprintId}
            onChange={(e) => setSelectedSprintId(e.target.value)}
            style={{ width: '180px', height: '36px', fontSize: '0.8rem' }}
          >
            <option value="">All Tasks / Backlog</option>
            {sprints.map(s => (
              <option key={s.id} value={s.id}>
                {s.status === 'ACTIVE' ? '★ ' : ''}{s.name}
              </option>
            ))}
          </select>

          {/* Search Bar */}
          <div style={{ position: 'relative', width: '200px' }}>
            <Search size={15} color="var(--text-muted)" style={{ position: 'absolute', left: '10px', top: '10px' }} />
            <input
              type="text"
              placeholder="Search cards..."
              className="input-field"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              style={{ paddingLeft: '32px', height: '36px', fontSize: '0.8rem' }}
            />
          </div>

          <button onClick={() => { setTargetColumn('TODO'); setIsModalOpen(true); setErrorMsg(''); }} className="btn btn-primary btn-sm">
            <Plus size={15} /> New Issue
          </button>
        </div>
      </div>

      {/* Error Alert */}
      {errorMsg && (
        <div style={{
          padding: '0.6rem 1rem',
          background: 'rgba(239, 68, 68, 0.12)',
          border: '1px solid rgba(239, 68, 68, 0.4)',
          borderRadius: 'var(--radius-md)',
          color: 'var(--status-danger)',
          fontSize: '0.8rem',
          marginBottom: '1rem',
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem',
          flexShrink: 0
        }}>
          <AlertCircle size={15} />
          <span>{errorMsg}</span>
        </div>
      )}

      {/* Kanban Columns */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(4, 1fr)',
        gap: '1.25rem',
        flex: 1,
        minHeight: 0,
        overflowX: 'auto'
      }}>
        {COLUMNS.map((col, colIndex) => {
          const colTasks = filteredTasks.filter(t => t.status === col.id);
          const colPoints = colTasks.reduce((sum, t) => sum + (t.storyPoints || 0), 0);

          return (
            <div
              key={col.id}
              style={{
                background: 'var(--bg-sidebar)',
                borderRadius: 'var(--radius-lg)',
                border: '1px solid var(--border-subtle)',
                display: 'flex',
                flexDirection: 'column',
                height: '100%',
                minWidth: '260px',
                overflow: 'hidden'
              }}
            >
              {/* Column Header */}
              <div style={{
                padding: '0.85rem 1rem',
                borderBottom: '1px solid var(--border-subtle)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                background: 'var(--bg-surface)'
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                  <span style={{
                    width: '9px',
                    height: '9px',
                    borderRadius: '50%',
                    background: col.color
                  }} />
                  <span style={{ fontWeight: 700, fontSize: '0.85rem' }}>{col.label}</span>
                  <span className="badge badge-info" style={{ fontSize: '0.65rem' }}>
                    {colTasks.length}
                  </span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>{colPoints} pts</span>
                  <button
                    onClick={() => { setTargetColumn(col.id); setIsModalOpen(true); setErrorMsg(''); }}
                    className="btn-icon"
                    title={`Add card to ${col.label}`}
                    style={{ width: '22px', height: '22px' }}
                  >
                    <Plus size={14} />
                  </button>
                </div>
              </div>

              {/* Tasks Scrollable Body */}
              <div style={{
                padding: '0.85rem',
                overflowY: 'auto',
                flex: 1,
                display: 'flex',
                flexDirection: 'column',
                gap: '0.75rem'
              }}>
                {colTasks.length === 0 ? (
                  <div style={{
                    border: '1px dashed var(--border-subtle)',
                    borderRadius: 'var(--radius-md)',
                    padding: '2rem 1rem',
                    textAlign: 'center',
                    color: 'var(--text-muted)',
                    fontSize: '0.75rem'
                  }}>
                    No tasks in {col.label}
                  </div>
                ) : (
                  colTasks.map(task => (
                    <div
                      key={task.id}
                      className="card"
                      style={{
                        padding: '1rem',
                        cursor: 'grab',
                        background: 'var(--bg-surface-elevated)',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: '0.6rem',
                        boxShadow: '0 2px 8px rgba(0,0,0,0.2)'
                      }}
                    >
                      {/* Top: Key, Type, Priority */}
                      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                          <span style={{ fontSize: '0.725rem', fontWeight: 700, color: 'var(--brand-primary)' }}>
                            {task.taskKey || 'TASK'}
                          </span>
                          <span className="badge badge-primary" style={{ fontSize: '0.65rem', padding: '0.1rem 0.35rem' }}>
                            {task.taskType || 'TASK'}
                          </span>
                        </div>
                        <span className={`badge badge-${task.priority === 'CRITICAL' || task.priority === 'HIGH' ? 'danger' : 'warning'}`} style={{ fontSize: '0.65rem' }}>
                          {task.priority || 'MEDIUM'}
                        </span>
                      </div>

                      {/* Title */}
                      <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-primary)', lineHeight: 1.4 }}>
                        {task.title}
                      </div>

                      {/* Footer: Story Points & Move Arrows */}
                      <div style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        borderTop: '1px solid var(--border-subtle)',
                        paddingTop: '0.5rem',
                        marginTop: '0.25rem',
                        fontSize: '0.725rem',
                        color: 'var(--text-muted)'
                      }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                          <span className="badge badge-info" style={{ fontSize: '0.65rem' }}>
                            {task.storyPoints || 0} pts
                          </span>
                          {task.assigneeName && (
                            <span style={{ fontSize: '0.7rem' }}>{task.assigneeName}</span>
                          )}
                        </div>

                        {/* Move Controls */}
                        <div style={{ display: 'flex', gap: '0.2rem' }}>
                          {colIndex > 0 && (
                            <button
                              onClick={() => moveTask(task.id, -1)}
                              className="btn-icon"
                              title={`Move to ${COLUMNS[colIndex - 1].label}`}
                              style={{ width: '22px', height: '22px' }}
                            >
                              <ArrowLeft size={12} />
                            </button>
                          )}
                          {colIndex < COLUMNS.length - 1 && (
                            <button
                              onClick={() => moveTask(task.id, 1)}
                              className="btn-icon"
                              title={`Move to ${COLUMNS[colIndex + 1].label}`}
                              style={{ width: '22px', height: '22px' }}
                            >
                              <ArrowRight size={12} />
                            </button>
                          )}
                        </div>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Create Task Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={`Create New Issue in ${COLUMNS.find(c => c.id === targetColumn)?.label || 'To Do'}`}
      >
        <form onSubmit={handleCreateTask}>
          {errorMsg && (
            <div style={{ padding: '0.75rem', background: 'rgba(239, 68, 68, 0.12)', color: 'var(--status-danger)', borderRadius: 'var(--radius-md)', marginBottom: '1rem', fontSize: '0.825rem' }}>
              {errorMsg}
            </div>
          )}

          <div className="input-group">
            <label className="input-label">Title *</label>
            <input
              type="text"
              className="input-field"
              value={newTask.title}
              onChange={(e) => setNewTask({ ...newTask, title: e.target.value })}
              placeholder="e.g. Implement resilient Kafka consumer retry topic"
              required
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="input-group">
              <label className="input-label">Type</label>
              <select
                className="input-field"
                value={newTask.taskType}
                onChange={(e) => setNewTask({ ...newTask, taskType: e.target.value })}
              >
                <option value="STORY">User Story</option>
                <option value="BUG">Bug Fix</option>
                <option value="TASK">Engineering Task</option>
                <option value="EPIC">Epic</option>
              </select>
            </div>

            <div className="input-group">
              <label className="input-label">Priority</label>
              <select
                className="input-field"
                value={newTask.priority}
                onChange={(e) => setNewTask({ ...newTask, priority: e.target.value })}
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="CRITICAL">Critical</option>
              </select>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="input-group">
              <label className="input-label">Story Points</label>
              <input
                type="number"
                min="1"
                max="21"
                className="input-field"
                value={newTask.storyPoints}
                onChange={(e) => setNewTask({ ...newTask, storyPoints: e.target.value })}
              />
            </div>

            <div className="input-group">
              <label className="input-label">Estimated Hours</label>
              <input
                type="number"
                min="0.5"
                step="0.5"
                className="input-field"
                value={newTask.estimatedHours}
                onChange={(e) => setNewTask({ ...newTask, estimatedHours: e.target.value })}
              />
            </div>
          </div>

          <div className="input-group">
            <label className="input-label">Description</label>
            <textarea
              className="input-field"
              rows={3}
              value={newTask.description}
              onChange={(e) => setNewTask({ ...newTask, description: e.target.value })}
              placeholder="Acceptance criteria, technical constraints..."
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
            <button type="button" onClick={() => setIsModalOpen(false)} className="btn btn-secondary">
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Creating Issue...' : 'Create Issue'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
