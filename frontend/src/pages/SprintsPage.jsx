import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { Zap, Calendar, Target, CheckCircle2, Play, Plus, Clock } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function SprintsPage() {
  const [sprints, setSprints] = useState([]);

  useEffect(() => {
    const fetchSprints = async () => {
      const data = await api.getSprints();
      setSprints(data);
    };
    fetchSprints();
  }, []);

  const activeSprint = sprints.find(s => s.status === 'ACTIVE') || sprints[0];
  const otherSprints = sprints.filter(s => s.id !== activeSprint?.id);

  return (
    <div className="fade-in">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.85rem', marginBottom: '0.25rem' }}>Sprint Planning &amp; Velocity</h1>
          <p style={{ color: 'var(--text-secondary)' }}>
            Manage iteration lifecycles, team commitments, and burndown velocity
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <Link to="/boards" className="btn btn-primary">
            <Zap size={16} /> Open Active Board
          </Link>
        </div>
      </div>

      {/* Active Sprint Spotlight Card */}
      {activeSprint && (
        <div className="card" style={{
          background: 'linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(6, 182, 212, 0.05) 100%)',
          border: '1px solid var(--border-highlight)',
          marginBottom: '2rem',
          padding: '1.75rem'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <span className="badge badge-success" style={{ fontSize: '0.75rem' }}>IN PROGRESS</span>
              <h2 style={{ fontSize: '1.4rem' }}>{activeSprint.name}</h2>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-muted)', fontSize: '0.825rem' }}>
              <Calendar size={15} /> {activeSprint.startDate} &rarr; {activeSprint.endDate}
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
            marginBottom: '1.5rem',
            fontSize: '0.85rem'
          }}>
            <Target size={18} color="var(--brand-primary)" />
            <span>Sprint Goal: <strong style={{ color: 'var(--text-primary)' }}>{activeSprint.goal}</strong></span>
          </div>

          {/* Points Progress */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1.5rem', alignItems: 'center' }}>
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', marginBottom: '0.4rem' }}>
                <span>Burned Velocity</span>
                <span style={{ fontWeight: 700 }}>{activeSprint.completedPoints} of {activeSprint.totalPoints} pts ({Math.round((activeSprint.completedPoints / activeSprint.totalPoints) * 100)}%)</span>
              </div>
              <div className="progress-container" style={{ height: '10px' }}>
                <div className="progress-fill" style={{ width: `${(activeSprint.completedPoints / activeSprint.totalPoints) * 100}%` }} />
              </div>
            </div>

            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
              <div style={{ textAlign: 'center', padding: '0.5rem 1.25rem', background: 'var(--bg-surface)', borderRadius: 'var(--radius-md)' }}>
                <div style={{ fontSize: '1.3rem', fontWeight: 800, color: 'var(--brand-primary)' }}>14</div>
                <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>Points Remaining</div>
              </div>
              <div style={{ textAlign: 'center', padding: '0.5rem 1.25rem', background: 'var(--bg-surface)', borderRadius: 'var(--radius-md)' }}>
                <div style={{ fontSize: '1.3rem', fontWeight: 800, color: 'var(--status-success)' }}>2</div>
                <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>Days Left</div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Upcoming / Planned Sprints List */}
      <h3 style={{ fontSize: '1.2rem', marginBottom: '1rem' }}>Planned &amp; Past Iterations</h3>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        {otherSprints.map(s => (
          <div key={s.id} className="card" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.35rem' }}>
                <h4 style={{ fontSize: '1rem' }}>{s.name}</h4>
                <span className="badge badge-primary">{s.status}</span>
              </div>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>{s.goal}</p>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '2rem' }}>
              <div style={{ textAlign: 'right' }}>
                <div style={{ fontWeight: 700, fontSize: '0.9rem' }}>{s.totalPoints} story points</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{s.startDate} &rarr; {s.endDate}</div>
              </div>
              <button className="btn btn-secondary btn-sm">
                <Play size={14} /> Start Sprint
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
