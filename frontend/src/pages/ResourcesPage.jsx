import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { Users2, AlertTriangle, CheckCircle2, UserX, Briefcase, Plus } from 'lucide-react';

export default function ResourcesPage() {
  const [resources, setResources] = useState([]);

  useEffect(() => {
    const fetchResources = async () => {
      const data = await api.getResources();
      setResources(data);
    };
    fetchResources();
  }, []);

  const overallocatedCount = resources.filter(r => r.totalAllocation > 100).length;
  const benchCount = resources.filter(r => r.totalAllocation === 0).length;
  const optimalCount = resources.filter(r => r.totalAllocation > 0 && r.totalAllocation <= 100).length;

  return (
    <div className="fade-in">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.85rem', marginBottom: '0.25rem' }}>Resource Management &amp; Capacity</h1>
          <p style={{ color: 'var(--text-secondary)' }}>
            Monitor engineering workload utilization, project allocations, and capacity bottlenecks
          </p>
        </div>
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
            <div key={res.id} className="card">
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  <div style={{
                    width: '40px', height: '40px', borderRadius: '50%',
                    background: 'var(--brand-gradient)', color: '#ffffff',
                    fontWeight: 700, display: 'flex', alignItems: 'center', justifyContent: 'center'
                  }}>
                    {res.name.split(' ').map(n => n[0]).join('')}
                  </div>
                  <div>
                    <h3 style={{ fontSize: '1rem' }}>{res.name}</h3>
                    <p style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>{res.role} • {res.department}</p>
                  </div>
                </div>

                <span className={`badge badge-${isOver ? 'danger' : (isBench ? 'info' : 'success')}`}>
                  {res.totalAllocation}%
                </span>
              </div>

              {/* Allocation Meter */}
              <div style={{ marginBottom: '1.25rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '0.3rem' }}>
                  <span>Workload Meter</span>
                  <span style={{ fontWeight: 700, color: isOver ? 'var(--status-danger)' : 'var(--text-primary)' }}>
                    {res.totalAllocation}%
                  </span>
                </div>
                <div className="progress-container">
                  <div
                    style={{
                      height: '100%',
                      width: `${Math.min(100, res.totalAllocation)}%`,
                      background: isOver ? 'var(--status-danger)' : 'var(--brand-gradient)',
                      borderRadius: 'var(--radius-full)'
                    }}
                  />
                </div>
              </div>

              {/* Project Assignments */}
              <div style={{ marginBottom: '1.25rem' }}>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 700, marginBottom: '0.5rem', textTransform: 'uppercase' }}>
                  Project Allocations
                </div>
                {res.projects && res.projects.length > 0 ? (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem' }}>
                    {res.projects.map(p => (
                      <div key={p.name} style={{
                        display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem',
                        padding: '0.4rem 0.6rem', background: 'var(--bg-surface-elevated)', borderRadius: 'var(--radius-sm)'
                      }}>
                        <span style={{ color: 'var(--text-secondary)' }}>{p.name}</span>
                        <strong style={{ color: 'var(--text-primary)' }}>{p.percentage}%</strong>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontStyle: 'italic' }}>
                    Available for upcoming project staffing
                  </div>
                )}
              </div>

              {/* Skills Tags */}
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 700, marginBottom: '0.5rem', textTransform: 'uppercase' }}>
                  Core Competencies
                </div>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.35rem' }}>
                  {res.skills.map(s => (
                    <span key={s} style={{
                      fontSize: '0.7rem', padding: '0.2rem 0.5rem', background: 'var(--bg-canvas)',
                      border: '1px solid var(--border-subtle)', borderRadius: 'var(--radius-full)', color: 'var(--text-secondary)'
                    }}>
                      {s}
                    </span>
                  ))}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
