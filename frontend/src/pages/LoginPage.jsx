import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { Layers, ShieldCheck, ArrowRight, UserCheck } from 'lucide-react';
import { DEMO_USERS } from '../api/mockData';

export default function LoginPage() {
  const { login, selectPersona } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState('admin@apexcloud.io');
  const [password, setPassword] = useState('Password123!');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    await login({ email, password });
    setLoading(false);
    navigate('/');
  };

  const handleQuickPersona = (index) => {
    selectPersona(index);
    navigate('/');
  };

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'radial-gradient(ellipse at top, #111827 0%, #090d16 100%)',
      padding: '2rem'
    }}>
      <div className="card fade-in" style={{
        width: '100%',
        maxWidth: '460px',
        padding: '2.5rem',
        border: '1px solid var(--border-highlight)',
        boxShadow: '0 20px 50px rgba(0, 0, 0, 0.6)'
      }}>
        {/* Header */}
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <div style={{
            width: '48px',
            height: '48px',
            borderRadius: '12px',
            background: 'var(--brand-gradient)',
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: 'var(--brand-glow)',
            marginBottom: '1rem'
          }}>
            <Layers size={26} color="#ffffff" />
          </div>
          <h2 style={{ fontSize: '1.6rem', marginBottom: '0.35rem' }}>Apex DevOps Platform</h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>
            Enterprise Project, Sprint &amp; Compliance Management
          </p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit}>
          <div className="input-group">
            <label className="input-label">Work Email</label>
            <input
              type="email"
              className="input-field"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="input-group">
            <label className="input-label">Password</label>
            <input
              type="password"
              className="input-field"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            style={{ width: '100%', marginTop: '0.5rem', padding: '0.75rem' }}
            disabled={loading}
          >
            {loading ? 'Authenticating...' : 'Sign In to Workspace'} <ArrowRight size={16} />
          </button>
        </form>

        {/* Quick Demo Persona Section */}
        <div style={{
          marginTop: '2rem',
          paddingTop: '1.5rem',
          borderTop: '1px solid var(--border-subtle)'
        }}>
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.4rem',
            fontSize: '0.75rem',
            fontWeight: 700,
            color: 'var(--text-muted)',
            marginBottom: '0.85rem',
            textTransform: 'uppercase'
          }}>
            <ShieldCheck size={14} color="var(--brand-primary)" />
            Instant Demo Sign-In
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            <button
              type="button"
              onClick={() => handleQuickPersona(0)}
              className="btn btn-secondary btn-sm"
              style={{ justifyContent: 'space-between', padding: '0.55rem 0.85rem' }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <UserCheck size={14} color="var(--brand-primary)" />
                <span style={{ fontWeight: 600 }}>Sarah Connor</span>
              </div>
              <span className="badge badge-primary" style={{ fontSize: '0.65rem' }}>Admin / CTO</span>
            </button>

            <button
              type="button"
              onClick={() => handleQuickPersona(1)}
              className="btn btn-secondary btn-sm"
              style={{ justifyContent: 'space-between', padding: '0.55rem 0.85rem' }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <UserCheck size={14} color="var(--brand-secondary)" />
                <span style={{ fontWeight: 600 }}>Alex Chen</span>
              </div>
              <span className="badge badge-info" style={{ fontSize: '0.65rem' }}>Lead PM</span>
            </button>

            <button
              type="button"
              onClick={() => handleQuickPersona(2)}
              className="btn btn-secondary btn-sm"
              style={{ justifyContent: 'space-between', padding: '0.55rem 0.85rem' }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <UserCheck size={14} color="var(--status-success)" />
                <span style={{ fontWeight: 600 }}>Elena Rostova</span>
              </div>
              <span className="badge badge-success" style={{ fontSize: '0.65rem' }}>Principal Architect</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
