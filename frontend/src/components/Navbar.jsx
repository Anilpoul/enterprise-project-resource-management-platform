import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useTenant } from '../context/TenantContext';
import { useNotifications } from '../context/NotificationContext';
import {
  Bell,
  Search,
  Building2,
  ChevronDown,
  Sun,
  Moon,
  LogOut,
  UserCheck,
  Layers
} from 'lucide-react';

export default function Navbar() {
  const { user, logout, selectPersona } = useAuth();
  const { organizations, activeOrg, switchOrg } = useTenant();
  const { unreadCount, isDrawerOpen, setIsDrawerOpen } = useNotifications();

  const [theme, setTheme] = useState('dark');
  const [showOrgMenu, setShowOrgMenu] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);

  const toggleTheme = () => {
    const next = theme === 'dark' ? 'light' : 'dark';
    setTheme(next);
    document.documentElement.setAttribute('data-theme', next);
  };

  return (
    <header style={{
      height: '64px',
      borderBottom: '1px solid var(--border-subtle)',
      background: 'var(--bg-sidebar)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '0 1.75rem',
      position: 'sticky',
      top: 0,
      zIndex: 100,
      backdropFilter: 'blur(12px)'
    }}>
      {/* Brand & Org Switcher */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '1.75rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
          <div style={{
            width: '34px',
            height: '34px',
            borderRadius: '8px',
            background: 'var(--brand-gradient)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: 'var(--brand-glow)'
          }}>
            <Layers size={20} color="#ffffff" />
          </div>
          <span style={{
            fontFamily: 'var(--font-heading)',
            fontWeight: 800,
            fontSize: '1.15rem',
            letterSpacing: '-0.03em',
            background: 'linear-gradient(135deg, #ffffff 0%, #cbd5e1 100%)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent'
          }}>
            Apex DevOps
          </span>
        </div>

        {/* Organization Switcher Dropdown */}
        <div style={{ position: 'relative' }}>
          <button
            onClick={() => setShowOrgMenu(!showOrgMenu)}
            className="btn btn-secondary btn-sm"
            style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', borderRadius: 'var(--radius-full)' }}
          >
            <Building2 size={14} color="var(--brand-primary)" />
            <span style={{ fontWeight: 600 }}>{activeOrg?.name || 'Select Organization'}</span>
            <ChevronDown size={14} color="var(--text-muted)" />
          </button>

          {showOrgMenu && (
            <div className="card fade-in" style={{
              position: 'absolute',
              top: '110%',
              left: 0,
              width: '260px',
              padding: '0.5rem',
              zIndex: 200
            }}>
              <div style={{ padding: '0.5rem', fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 700 }}>
                ORGANIZATIONS
              </div>
              {organizations.map(org => (
                <div
                  key={org.id}
                  onClick={() => {
                    switchOrg(org.id);
                    setShowOrgMenu(false);
                  }}
                  style={{
                    padding: '0.6rem 0.75rem',
                    borderRadius: 'var(--radius-md)',
                    cursor: 'pointer',
                    background: org.id === activeOrg?.id ? 'var(--bg-surface-elevated)' : 'transparent',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '0.1rem',
                    transition: 'all 0.15s ease'
                  }}
                >
                  <div style={{ fontWeight: 600, fontSize: '0.85rem' }}>{org.name}</div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Role: {org.role}</div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Center Search Bar */}
      <div style={{ display: 'flex', alignItems: 'center', width: '320px', position: 'relative' }}>
        <Search size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px' }} />
        <input
          type="text"
          placeholder="Search tasks, sprints, members..."
          className="input-field"
          style={{ paddingLeft: '36px', height: '36px', fontSize: '0.8rem', borderRadius: 'var(--radius-full)' }}
        />
      </div>

      {/* Right Actions: Theme, Notifications, User */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
        {/* Theme Toggle */}
        <button onClick={toggleTheme} className="btn-icon" title="Toggle Theme">
          {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
        </button>

        {/* Notifications Bell */}
        <button
          onClick={() => setIsDrawerOpen(!isDrawerOpen)}
          className="btn-icon"
          style={{ position: 'relative' }}
          title="Notifications"
        >
          <Bell size={18} />
          {unreadCount > 0 && (
            <span style={{
              position: 'absolute',
              top: '4px',
              right: '4px',
              width: '18px',
              height: '18px',
              borderRadius: '50%',
              background: 'var(--status-danger)',
              color: '#ffffff',
              fontSize: '0.65rem',
              fontWeight: 800,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: '0 0 10px rgba(239, 68, 68, 0.6)'
            }}>
              {unreadCount}
            </span>
          )}
        </button>

        {/* User Menu */}
        <div style={{ position: 'relative' }}>
          <div
            onClick={() => setShowUserMenu(!showUserMenu)}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.6rem',
              cursor: 'pointer',
              padding: '0.25rem 0.5rem',
              borderRadius: 'var(--radius-full)',
              background: 'var(--bg-surface-elevated)'
            }}
          >
            <div style={{
              width: '28px',
              height: '28px',
              borderRadius: '50%',
              background: 'var(--brand-gradient)',
              color: '#ffffff',
              fontWeight: 700,
              fontSize: '0.75rem',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}>
              {user?.firstName?.[0] || 'U'}
            </div>
            <span style={{ fontWeight: 600, fontSize: '0.825rem' }}>{user?.firstName || 'User'}</span>
            <ChevronDown size={14} color="var(--text-muted)" />
          </div>

          {showUserMenu && (
            <div className="card fade-in" style={{
              position: 'absolute',
              top: '115%',
              right: 0,
              width: '240px',
              padding: '0.5rem',
              zIndex: 200
            }}>
              <div style={{ padding: '0.6rem', borderBottom: '1px solid var(--border-subtle)' }}>
                <div style={{ fontWeight: 700, fontSize: '0.85rem' }}>{user?.firstName} {user?.lastName}</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{user?.email}</div>
                <div style={{ fontSize: '0.7rem', color: 'var(--brand-primary)', fontWeight: 600, marginTop: '2px' }}>{user?.role}</div>
              </div>

              <div style={{ padding: '0.5rem 0.6rem 0.25rem', fontSize: '0.7rem', color: 'var(--text-muted)', fontWeight: 700 }}>
                QUICK SWITCH PERSONA
              </div>
              <button
                onClick={() => { selectPersona(0); setShowUserMenu(false); }}
                className="btn-icon"
                style={{ width: '100%', justifyContent: 'flex-start', gap: '0.5rem', fontSize: '0.8rem' }}
              >
                <UserCheck size={14} color="var(--brand-primary)" /> Sarah Connor (Admin)
              </button>
              <button
                onClick={() => { selectPersona(1); setShowUserMenu(false); }}
                className="btn-icon"
                style={{ width: '100%', justifyContent: 'flex-start', gap: '0.5rem', fontSize: '0.8rem' }}
              >
                <UserCheck size={14} color="var(--brand-secondary)" /> Alex Chen (Project Manager)
              </button>
              <button
                onClick={() => { selectPersona(2); setShowUserMenu(false); }}
                className="btn-icon"
                style={{ width: '100%', justifyContent: 'flex-start', gap: '0.5rem', fontSize: '0.8rem' }}
              >
                <UserCheck size={14} color="var(--status-success)" /> Elena Rostova (Architect)
              </button>

              <div style={{ borderTop: '1px solid var(--border-subtle)', marginTop: '0.5rem', paddingTop: '0.5rem' }}>
                <button
                  onClick={() => { logout(); setShowUserMenu(false); }}
                  className="btn-icon"
                  style={{ width: '100%', justifyContent: 'flex-start', gap: '0.5rem', color: 'var(--status-danger)', fontSize: '0.8rem' }}
                >
                  <LogOut size={14} /> Sign Out
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
