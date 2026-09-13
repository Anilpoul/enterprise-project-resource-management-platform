import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import {
  Plus,
  Search,
  Filter,
  ArrowRight,
  ArrowLeft,
  CheckCircle2,
  AlertCircle,
  Clock,
  Sparkles
} from 'lucide-react';
import Modal from '../components/Modal';

const COLUMNS = [
  { id: 'TODO', label: 'To Do', color: 'var(--text-muted)' },
  { id: 'IN_PROGRESS', label: 'In Progress', color: 'var(--brand-primary)' },
  { id: 'REVIEW', label: 'Review', color: 'var(--brand-secondary)' },
  { id: 'DONE', label: 'Done', color: 'var(--status-success)' }
];

export default function BoardPage() {
  const [tasks, setTasks] = useState([]);
  const [search, setSearch] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [targetColumn, setTargetColumn] = useState('TODO');

  const [newTask, setNewTask] = useState({
    title: '',
    description: '',
    priority: 'HIGH',
    storyPoints: 5,
    assigneeName: 'Elena Rostova',
    taskType: 'TASK'
  });

  useEffect(() => {
    const fetchTasks = async () => {
      const data = await api.getTasks();
      setTasks(data);
    };
    fetchTasks();
  }, []);

  const moveTask = async (taskId, direction) => {
    const currentTask = tasks.find(t => t.id === taskId);
    if (!currentTask) return;

    const currentIndex = COLUMNS.findIndex(c => c.id === currentTask.status);
    const newIndex = currentIndex + direction;

    if (newIndex >= 0 && newIndex < COLUMNS.length) {
      const newStatus = COLUMNS[newIndex].id;
      setTasks(prev =>
        prev.map(t => t.id === taskId ? { ...t, status: newStatus } : t)
      );
      await api.updateTaskStatus(taskId, newStatus);
    }
  };

  const handleCreateTask = async (e) => {
    e.preventDefault();
    const created = await api.createTask({
      ...newTask,
      status: targetColumn,
      assigneeAvatar: newTask.assigneeName.split(' ').map(n => n[0]).join('')
    });
    setTasks(prev => [...prev, created]);
    setIsModalOpen(false);
    setNewTask({ title: '', description: '', priority: 'HIGH', storyPoints: 5, assigneeName: 'Elena Rostova', taskType: 'TASK' });
  };

  const filteredTasks = tasks.filter(t =>
    t.title.toLowerCase().includes(search.toLowerCase()) ||
    t.taskKey.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="fade-in" style={{ display: 'flex', flexDirection: 'column', height: 'calc(100vh - 130px)' }}>
      {/* Board Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', flexShrink: 0 }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <h1 style={{ fontSize: '1.85rem' }}>Sprint 24 Kanban Board</h1>
            <span className="badge badge-success" style={{ fontSize: '0.75rem' }}>ACTIVE SPRINT</span>
          </div>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>
            Goal: Achieve sub-50ms distributed transaction confirmation across ledgers
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <div style={{ position: 'relative', width: '240px' }}>
            <Search size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '10px', top: '10px' }} />
            <input
              type="text"
              placeholder="Search board cards..."
              className="input-field"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              style={{ paddingLeft: '34px', height: '36px', fontSize: '0.8rem' }}
            />
          </div>

          <button onClick={() => { setTargetColumn('TODO'); setIsModalOpen(true); }} className="btn btn-primary">
            <Plus size={16} /> New Issue
          </button>
        </div>
      </div>

      {/* Kanban Columns Container */}
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
                maxHeight: '100%'
              }}
            >
              {/* Column Header */}
              <div style={{
                padding: '0.85rem 1rem',
                borderBottom: '1px solid var(--border-subtle)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                flexShrink: 0
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <span style={{
                    width: '10px',
                    height: '10px',
                    borderRadius: '50%',
                    background: col.color
                  }} />
                  <span style={{ fontWeight: 700, fontSize: '0.9rem' }}>{col.label}</span>
                  <span className="badge badge-primary" style={{ fontSize: '0.65rem' }}>{colTasks.length}</span>
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>
                  {colPoints} pts
                </div>
              </div>

              {/* Column Cards */}
              <div style={{
                flex: 1,
                overflowY: 'auto',
                padding: '0.75rem',
                display: 'flex',
                flexDirection: 'column',
                gap: '0.75rem'
              }}>
                {colTasks.map(task => (
                  <div
                    key={task.id}
                    className="card"
                    style={{
                      padding: '1rem',
                      background: 'var(--bg-surface)',
                      cursor: 'grab'
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                      <span className="badge badge-primary" style={{ fontSize: '0.65rem' }}>{task.taskKey}</span>
                      <span className={`badge badge-${task.priority === 'CRITICAL' ? 'danger' : (task.priority === 'HIGH' ? 'warning' : 'info')}`} style={{ fontSize: '0.65rem' }}>
                        {task.priority}
                      </span>
                    </div>

                    <h4 style={{ fontSize: '0.875rem', lineHeight: 1.4, marginBottom: '0.6rem', fontWeight: 600 }}>
                      {task.title}
                    </h4>

                    {task.description && (
                      <p style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', lineHeight: 1.3, marginBottom: '0.75rem' }}>
                        {task.description}
                      </p>
                    )}

                    <div style={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      paddingTop: '0.6rem',
                      borderTop: '1px solid var(--border-subtle)',
                      fontSize: '0.75rem'
                    }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                        <div style={{
                          width: '24px', height: '24px', borderRadius: '50%',
                          background: 'var(--brand-gradient)', color: '#ffffff',
                          fontWeight: 700, fontSize: '0.65rem', display: 'flex', alignItems: 'center', justifyContent: 'center'
                        }}>
                          {task.assigneeAvatar || 'U'}
                        </div>
                        <span style={{ color: 'var(--text-secondary)' }}>{task.assigneeName}</span>
                      </div>

                      {/* Direction Move Buttons */}
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.2rem' }}>
                        {colIndex > 0 && (
                          <button
                            onClick={() => moveTask(task.id, -1)}
                            className="btn-icon"
                            style={{ padding: '3px' }}
                            title="Move back"
                          >
                            <ArrowLeft size={12} />
                          </button>
                        )}
                        <span style={{ fontWeight: 700, padding: '0 4px' }}>{task.storyPoints}p</span>
                        {colIndex < COLUMNS.length - 1 && (
                          <button
                            onClick={() => moveTask(task.id, 1)}
                            className="btn-icon"
                            style={{ padding: '3px' }}
                            title="Move next"
                          >
                            <ArrowRight size={12} />
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                ))}

                {/* Quick Add Button */}
                <button
                  onClick={() => { setTargetColumn(col.id); setIsModalOpen(true); }}
                  className="btn btn-secondary btn-sm"
                  style={{
                    width: '100%',
                    justifyContent: 'center',
                    borderStyle: 'dashed',
                    background: 'transparent',
                    color: 'var(--text-muted)'
                  }}
                >
                  <Plus size={14} /> Add Card
                </button>
              </div>
            </div>
          );
        })}
      </div>

      {/* New Task Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Add Sprint Task"
        footer={
          <>
            <button onClick={() => setIsModalOpen(false)} className="btn btn-secondary">Cancel</button>
            <button onClick={handleCreateTask} className="btn btn-primary">Create Task</button>
          </>
        }
      >
        <form onSubmit={handleCreateTask}>
          <div className="input-group">
            <label className="input-label">Task Summary</label>
            <input
              type="text"
              className="input-field"
              placeholder="e.g. Implement rate limiter on authentication endpoint"
              value={newTask.title}
              onChange={(e) => setNewTask({ ...newTask, title: e.target.value })}
              required
            />
          </div>

          <div className="input-group">
            <label className="input-label">Description</label>
            <textarea
              className="textarea-field"
              rows="3"
              placeholder="Detailed acceptance criteria..."
              value={newTask.description}
              onChange={(e) => setNewTask({ ...newTask, description: e.target.value })}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="input-group">
              <label className="input-label">Priority</label>
              <select
                className="select-field"
                value={newTask.priority}
                onChange={(e) => setNewTask({ ...newTask, priority: e.target.value })}
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="CRITICAL">Critical</option>
              </select>
            </div>

            <div className="input-group">
              <label className="input-label">Story Points</label>
              <input
                type="number"
                min="1"
                max="21"
                className="input-field"
                value={newTask.storyPoints}
                onChange={(e) => setNewTask({ ...newTask, storyPoints: parseInt(e.target.value) || 1 })}
              />
            </div>
          </div>

          <div className="input-group">
            <label className="input-label">Assignee</label>
            <select
              className="select-field"
              value={newTask.assigneeName}
              onChange={(e) => setNewTask({ ...newTask, assigneeName: e.target.value })}
            >
              <option value="Elena Rostova">Elena Rostova (Principal Architect)</option>
              <option value="Marcus Vance">Marcus Vance (Senior Fullstack)</option>
              <option value="Sarah Connor">Sarah Connor (CTO)</option>
              <option value="Alex Chen">Alex Chen (PM)</option>
            </select>
          </div>
        </form>
      </Modal>
    </div>
  );
}
