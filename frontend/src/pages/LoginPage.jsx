import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useTenant } from '../context/TenantContext';
import { useNavigate } from 'react-router-dom';
import {
  Layers,
  ShieldCheck,
  ArrowRight,
  UserCheck,
  UserPlus,
  LogIn,
  AlertCircle,
  CheckCircle2,
  Building2
} from 'lucide-react';

export default function LoginPage() {
  const { login, register, selectPersona } = useAuth();
  const { createOrganization } = useTenant();
  const navigate = useNavigate();

  const [mode, setMode] = useState('login'); // 'login' or 'register'
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // Login Form State
  const [loginEmail, setLoginEmail] = useState('admin@apexcloud.io');
  const [loginPassword, setLoginPassword] = useState('Password123!');

  // Register Form State
  const [regData, setRegData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    orgName: ''
  });

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrorMessage('');

    const res = await login({ email: loginEmail, password: loginPassword });
    setLoading(false);

    if (res.success) {
      navigate('/');
    } else {
      setErrorMessage(res.error || 'Authentication failed. Please verify credentials.');
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrorMessage('');
    setSuccessMessage('');

    // Client-side password validation
    const passwordRegex = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@#$%^&+=!]).{8,20}$/;
    if (!passwordRegex.test(regData.password)) {
      setLoading(false);
      setErrorMessage(
        'Password must be 8-20 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character (@#$%^&+=!).'
      );
      return;
    }

    const regRes = await register({
      firstName: regData.firstName,
      lastName: regData.lastName,
      email: regData.email,
      password: regData.password
    });

    if (!regRes.success) {
      setLoading(false);
      setErrorMessage(regRes.error || 'Registration failed.');
      return;
    }

    const userId = regRes.profile.id;

    // If an organization name was provided, create the initial tenant workspace
    if (regData.orgName.trim()) {
      const slug = regData.orgName
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, '-')
        .replace(/(^-|-$)/g, '') || 'workspace';

      await createOrganization({
        name: regData.orgName.trim(),
        slug: `${slug}-${Math.floor(100 + Math.random() * 900)}`,
        description: 'Primary workspace created during registration',
        adminUserId: userId
      });
    }

    setLoading(false);
    setSuccessMessage('Account registered successfully! Launching workspace...');
    setTimeout(() => {
      navigate('/');
    }, 600);
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
        maxWidth: '480px',
        padding: '2.5rem',
        border: '1px solid var(--border-highlight)',
        boxShadow: '0 20px 50px rgba(0, 0, 0, 0.6)'
      }}>
        {/* Brand Header */}
        <div style={{ textAlign: 'center', marginBottom: '1.75rem' }}>
          <div style={{
            width: '50px',
            height: '50px',
            borderRadius: '12px',
            background: 'var(--brand-gradient)',
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: 'var(--brand-glow)',
            marginBottom: '0.85rem'
          }}>
            <Layers size={28} color="#ffffff" />
          </div>
          <h2 style={{ fontSize: '1.65rem', marginBottom: '0.25rem' }}>Apex DevOps Platform</h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>
            Enterprise Project, Sprint &amp; Compliance Management
          </p>
        </div>

        {/* Mode Switcher Tabs */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: '1fr 1fr',
          background: 'var(--bg-surface-elevated)',
          padding: '4px',
          borderRadius: 'var(--radius-md)',
          marginBottom: '1.5rem',
          border: '1px solid var(--border-subtle)'
        }}>
          <button
            type="button"
            onClick={() => { setMode('login'); setErrorMessage(''); }}
            className={`btn btn-sm ${mode === 'login' ? 'btn-primary' : 'btn-secondary'}`}
            style={{ borderRadius: 'var(--radius-sm)', border: 'none' }}
          >
            <LogIn size={15} /> Sign In
          </button>
          <button
            type="button"
            onClick={() => { setMode('register'); setErrorMessage(''); }}
            className={`btn btn-sm ${mode === 'register' ? 'btn-primary' : 'btn-secondary'}`}
            style={{ borderRadius: 'var(--radius-sm)', border: 'none' }}
          >
            <UserPlus size={15} /> Create Account
          </button>
        </div>

        {/* Error / Success Alerts */}
        {errorMessage && (
          <div style={{
            padding: '0.75rem 1rem',
            background: 'rgba(239, 68, 68, 0.12)',
            border: '1px solid rgba(239, 68, 68, 0.4)',
            borderRadius: 'var(--radius-md)',
            color: 'var(--status-danger)',
            fontSize: '0.825rem',
            marginBottom: '1.25rem',
            display: 'flex',
            alignItems: 'flex-start',
            gap: '0.5rem',
            lineHeight: 1.4
          }}>
            <AlertCircle size={16} style={{ flexShrink: 0, marginTop: '2px' }} />
            <span>{errorMessage}</span>
          </div>
        )}

        {successMessage && (
          <div style={{
            padding: '0.75rem 1rem',
            background: 'rgba(16, 185, 129, 0.12)',
            border: '1px solid rgba(16, 185, 129, 0.4)',
            borderRadius: 'var(--radius-md)',
            color: 'var(--status-success)',
            fontSize: '0.825rem',
            marginBottom: '1.25rem',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem'
          }}>
            <CheckCircle2 size={16} />
            <span>{successMessage}</span>
          </div>
        )}

        {/* Mode: Login */}
        {mode === 'login' ? (
          <form onSubmit={handleLogin}>
            <div className="input-group">
              <label className="input-label">Work Email</label>
              <input
                type="email"
                className="input-field"
                value={loginEmail}
                onChange={(e) => setLoginEmail(e.target.value)}
                placeholder="name@company.com"
                required
              />
            </div>

            <div className="input-group">
              <label className="input-label">Password</label>
              <input
                type="password"
                className="input-field"
                value={loginPassword}
                onChange={(e) => setLoginPassword(e.target.value)}
                placeholder="••••••••"
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
        ) : (
          /* Mode: Register */
          <form onSubmit={handleRegister}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
              <div className="input-group">
                <label className="input-label">First Name *</label>
                <input
                  type="text"
                  className="input-field"
                  value={regData.firstName}
                  onChange={(e) => setRegData({ ...regData, firstName: e.target.value })}
                  placeholder="John"
                  required
                />
              </div>

              <div className="input-group">
                <label className="input-label">Last Name *</label>
                <input
                  type="text"
                  className="input-field"
                  value={regData.lastName}
                  onChange={(e) => setRegData({ ...regData, lastName: e.target.value })}
                  placeholder="Doe"
                  required
                />
              </div>
            </div>

            <div className="input-group">
              <label className="input-label">Work Email *</label>
              <input
                type="email"
                className="input-field"
                value={regData.email}
                onChange={(e) => setRegData({ ...regData, email: e.target.value })}
                placeholder="john.doe@company.com"
                required
              />
            </div>

            <div className="input-group">
              <label className="input-label">
                Password * <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>(8-20 chars, Upper, Lower, Number, Special)</span>
              </label>
              <input
                type="password"
                className="input-field"
                value={regData.password}
                onChange={(e) => setRegData({ ...regData, password: e.target.value })}
                placeholder="Password123!"
                required
              />
            </div>

            <div className="input-group">
              <label className="input-label">
                <Building2 size={13} style={{ display: 'inline', marginRight: '4px' }} />
                Organization Workspace Name *
              </label>
              <input
                type="text"
                className="input-field"
                value={regData.orgName}
                onChange={(e) => setRegData({ ...regData, orgName: e.target.value })}
                placeholder="e.g. Acme Corp or Fintech Global"
                required
              />
            </div>

            <button
              type="submit"
              className="btn btn-primary"
              style={{ width: '100%', marginTop: '0.5rem', padding: '0.75rem' }}
              disabled={loading}
            >
              {loading ? 'Creating Account & Workspace...' : 'Register & Launch Workspace'} <ArrowRight size={16} />
            </button>
          </form>
        )}

        {/* Quick Demo Persona Section */}
        <div style={{
          marginTop: '1.75rem',
          paddingTop: '1.25rem',
          borderTop: '1px solid var(--border-subtle)'
        }}>
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.4rem',
            fontSize: '0.725rem',
            fontWeight: 700,
            color: 'var(--text-muted)',
            marginBottom: '0.75rem',
            textTransform: 'uppercase'
          }}>
            <ShieldCheck size={14} color="var(--brand-primary)" />
            Instant Demo Shortcuts
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            <button
              type="button"
              onClick={() => handleQuickPersona(0)}
              className="btn btn-secondary btn-sm"
              style={{ justifyContent: 'space-between', padding: '0.5rem 0.85rem' }}
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
              style={{ justifyContent: 'space-between', padding: '0.5rem 0.85rem' }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <UserCheck size={14} color="var(--brand-secondary)" />
                <span style={{ fontWeight: 600 }}>Alex Chen</span>
              </div>
              <span className="badge badge-info" style={{ fontSize: '0.65rem' }}>Lead PM</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
