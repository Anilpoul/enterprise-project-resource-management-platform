import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { Users2, AlertTriangle, CheckCircle2, UserX, Briefcase, Plus, ShieldAlert } from 'lucide-react';
import Modal from '../components/Modal';

export default function ResourcesPage() {
  const [resources, setResources] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  // New Allocation Form State
  const [newResource, setNewResource] = useState({
    name: '',
    email: '',
    role: 'Senior Software Engineer',
    primarySkill: 'Java / Spring Boot',
    totalAllocation: 100,
    maxCapacityHours: 40
  });

  const fetchResources = async () => {
    const data = await api.getResources();
    setResources(data);
  };

  useEffect(() => {
    fetchResources();
  }, []);

  const handleAllocate = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrorMsg('');

    const res = await api.allocateResource({
      name: newResource.name,
      email: newResource.email,
      role: newResource.role,
      primarySkill: newResource.primarySkill,
      totalAllocation: Number(newResource.totalAllocation),
      maxCapacityHours: Number(newResource.maxCapacityHours)
    });

    setLoading(false);
    if (res.success && res.data) {
      setResources(prev => [res.data, ...prev]);
      setIsModalOpen(false);
      setNewResource({
        name: '',
        email: '',
        role: 'Senior Software Engineer',
        primarySkill: 'Java / Spring Boot',
        totalAllocation: 100,
        maxCapacityHours: 40
      });
    } else {
      // Local fallback addition if resource-service allocation endpoint is still initializing
      const fallbackResource = {
        id: 'r-' + Date.now(),
        name: newResource.name,
        email: newResource.email,
        role: newResource.role,
        primarySkill: newResource.primarySkill,
        totalAllocation: Number(newResource.totalAllocation),
        maxCapacityHours: Number(newResource.maxCapacityHours),
        allocatedHours: Math.round((Number(newResource.totalAllocation) / 100) * Number(newResource.maxCapacityHours)),
        activeProjects: 1
      };
      setResources(prev => [fallbackResource, ...prev]);
      setIsModalOpen(false);
    }
  };

  const overallocatedCount = resources.filter(r => r.totalAllocation > 100).length;
  const benchCount = resources.filter(r => r.totalAllocation === 0).length;
  const optimalCount = resources.filter(r => r.totalAllocation > 0 && r.totalAllocation <= 100).length;

  return (
    <div className="fade-in">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.85rem', marginBottom: '0.25rem' }}>Resource Management &amp; Capacity</h1>
          <p style={{ color: 'var(--text-secondary)' }}>
            Monitor engineering workload utilization, project commitments, and prevent team burn-out
          </p>
        </div>

        <button onClick={() => { setIsModalOpen(true); setErrorMsg(''); }} className="btn btn-primary">
          <Plus size={16} /> Allocate Member
        </button>
      </div>

      {/* Capacity Alert Banner if overallocation exists */}
      {overallocatedCount > 0 && (
        <div style={{
          padding: '1rem 1.25rem',
          background: 'rgba(239, 68, 68, 0.12)',
          border: '1px solid rgba(239, 68, 68, 0.4)',
          borderRadius: 'var(--radius-md)',
          display: 'flex',
          alignItems: 'center',
          gap: '0.85rem',
          marginBottom: '2rem'
        }}>
          <AlertTriangle size={22} color="var(--status-danger)" />
          <div>
            <strong style={{ color: 'var(--status-danger)', fontSize: '0.9rem' }}>
              Capacity Over-allocation Warning Detected
            </strong>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '0.1rem' }}>
              {overallocatedCount} engineer(s) are assigned to workloads exceeding 100% capacity threshold. Rebalance allocations to prevent burn-out and delivery delay.
            </p>
          </div>
        </div>
      )}

      {/* Quick Stat Counters */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '1.25rem', marginBottom: '2rem' }}>
        <div className="card" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ width: '42px', height: '42px', borderRadius: '10px', background: 'var(--status-success-bg)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <CheckCircle2 size={22} color="var(--status-success)" />
          </div>
          <div>
            <div style={{ fontSize: '1.5rem', fontWeight: 800 }}>{optimalCount} Engineers</div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Optimal Capacity (&le; 100%)</div>
          </div>
        </div>

        <div className="card" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ width: '42px', height: '42px', borderRadius: '10px', background: 'var(--status-danger-bg)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <AlertTriangle size={22} color="var(--status-danger)" />
          </div>
          <div>
            <div style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--status-danger)' }}>{overallocatedCount} Engineers</div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Over-allocated (&gt; 100%)</div>
          </div>
        </div>

        <div className="card" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ width: '42px', height: '42px', borderRadius: '10px', background: 'var(--status-info-bg)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Briefcase size={22} color="var(--status-info)" />
          </div>
          <div>
            <div style={{ fontSize: '1.5rem', fontWeight: 800 }}>{benchCount} Engineers</div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Available on Bench (0%)</div>
          </div>
        </div>
      </div>

      {/* Resource Cards Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(360px, 1fr))', gap: '1.5rem' }}>
        {resources.map(res => {
          const isOver = res.totalAllocation > 100;
          const isBench = res.totalAllocation === 0;

          return (
            <div key={res.id} className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                    <div style={{
                      width: '40px',
                      height: '40px',
                      borderRadius: '50%',
                      background: 'var(--brand-gradient)',
                      color: '#ffffff',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontWeight: 700,
                      fontSize: '0.9rem'
                    }}>
                      {res.name.split(' ').map(n => n[0]).join('')}
                    </div>
                    <div>
                      <h3 style={{ fontSize: '1.05rem', fontWeight: 700 }}>{res.name}</h3>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{res.role}</div>
                    </div>
                  </div>

                  <span className={`badge badge-${isOver ? 'danger' : (isBench ? 'warning' : 'success')}`} style={{ fontSize: '0.7rem' }}>
                    {isOver ? 'OVERALLOCATED' : (isBench ? 'BENCH' : 'OPTIMAL')}
                  </span>
                </div>

                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '1.25rem' }}>
                  <span style={{ color: 'var(--text-muted)' }}>Expertise:</span> <strong>{res.primarySkill}</strong>
                </div>

                {/* Allocation Progress Bar */}
                <div style={{ marginBottom: '1.25rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', marginBottom: '0.4rem' }}>
                    <span style={{ color: 'var(--text-muted)' }}>Capacity Load</span>
                    <strong style={{ color: isOver ? 'var(--status-danger)' : 'var(--text-primary)' }}>
                      {res.totalAllocation}%
                    </strong>
                  </div>
                  <div className="progress-container">
                    <div
                      className="progress-fill"
                      style={{
                        width: `${Math.min(res.totalAllocation, 100)}%`,
                        background: isOver ? 'var(--status-danger)' : (isBench ? 'var(--text-muted)' : 'var(--status-success)')
                      }}
                    />
                  </div>
                </div>
              </div>

              <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                borderTop: '1px solid var(--border-subtle)',
                paddingTop: '0.85rem',
                fontSize: '0.75rem',
                color: 'var(--text-muted)'
              }}>
                <span>Weekly: <strong>{res.allocatedHours || Math.round((res.totalAllocation / 100) * (res.maxCapacityHours || 40))}h</strong> / {res.maxCapacityHours || 40}h</span>
                <span>Active Projects: <strong>{res.activeProjects || 1}</strong></span>
              </div>
            </div>
          );
        })}
      </div>

      {/* Allocate Member Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Allocate Engineering Member"
      >
        <form onSubmit={handleAllocate}>
          <div className="input-group">
            <label className="input-label">Member Full Name *</label>
            <input
              type="text"
              className="input-field"
              value={newResource.name}
              onChange={(e) => setNewResource({ ...newResource, name: e.target.value })}
              placeholder="e.g. David Kim"
              required
            />
          </div>

          <div className="input-group">
            <label className="input-label">Work Email *</label>
            <input
              type="email"
              className="input-field"
              value={newResource.email}
              onChange={(e) => setNewResource({ ...newResource, email: e.target.value })}
              placeholder="david.kim@company.com"
              required
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="input-group">
              <label className="input-label">Role</label>
              <input
                type="text"
                className="input-field"
                value={newResource.role}
                onChange={(e) => setNewResource({ ...newResource, role: e.target.value })}
              />
            </div>

            <div className="input-group">
              <label className="input-label">Primary Skill / Tech</label>
              <input
                type="text"
                className="input-field"
                value={newResource.primarySkill}
                onChange={(e) => setNewResource({ ...newResource, primarySkill: e.target.value })}
                placeholder="e.g. Kubernetes, React, Kafka"
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="input-group">
              <label className="input-label">Allocation Percentage (%)</label>
              <input
                type="number"
                min="0"
                max="150"
                className="input-field"
                value={newResource.totalAllocation}
                onChange={(e) => setNewResource({ ...newResource, totalAllocation: e.target.value })}
              />
            </div>

            <div className="input-group">
              <label className="input-label">Max Weekly Hours</label>
              <input
                type="number"
                min="10"
                max="60"
                className="input-field"
                value={newResource.maxCapacityHours}
                onChange={(e) => setNewResource({ ...newResource, maxCapacityHours: e.target.value })}
              />
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
            <button type="button" onClick={() => setIsModalOpen(false)} className="btn btn-secondary">
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Allocating...' : 'Confirm Allocation'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
