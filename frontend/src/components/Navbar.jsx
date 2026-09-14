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
  Layers,
  Plus,
  ShieldCheck,
  Activity
} from 'lucide-react';
import Modal from './Modal';

export default function Navbar() {
  const { user, logout, selectPersona } = useAuth();
  const { organizations, activeOrg, switchOrg, createOrganization } = useTenant();
  const { unreadCount, isDrawerOpen, setIsDrawerOpen } = useNotifications();

  const [theme, setTheme] = useState('dark');
  const [showOrgMenu, setShowOrgMenu] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);

  // New Organization Modal State
  const [isOrgModalOpen, setIsOrgModalOpen] = useState(false);
  const [newOrgName, setNewOrgName] = useState('');
  const [newOrgDesc, setNewOrgDesc] = useState('');
  const [orgLoading, setOrgLoading] = useState(false);
  const [orgError, setOrgError] = useState('');

  const toggleTheme = () => {
    const next = theme === 'dark' ? 'light' : 'dark';
    setTheme(next);
    document.documentElement.setAttribute('data-theme', next);
  };

  const handleCreateOrg = async (e) => {
    e.preventDefault();
    if (!newOrgName.trim()) return;

    setOrgLoading(true);
    setOrgError('');

    const slug = newOrgName
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/(^-|-$)/g, '') || 'workspace';

    const res = await createOrganization({
      name: newOrgName.trim(),
      slug: `${slug}-${Math.floor(100 + Math.random() * 900)}`,
      description: newOrgDesc.trim(),
      adminUserId: user?.id || '00000000-0000-0000-0000-000000000001'
    });

    setOrgLoading(false);
    if (res.success) {
      setIsOrgModalOpen(false);
      setNewOrgName('');
      setNewOrgDesc('');
    } else {
      setOrgError(res.error || 'Failed to create organization');
    }
  };

  return (
    <>
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
                width: '280px',
                padding: '0.5rem',
                zIndex: 200,
                boxShadow: '0 15px 35px rgba(0,0,0,0.5)'
              }}>
                <div style={{
                  padding: '0.5rem',
                  fontSize: '0.725rem',
                  color: 'var(--text-muted)',
                  fontWeight: 700,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between'
                }}>
                  <span>ORGANIZATIONS</span>
                  <button
                    onClick={() => { setShowOrgMenu(false); setIsOrgModalOpen(true); }}
                    className="btn btn-secondary btn-sm"
                    style={{ padding: '0.2rem 0.5rem', fontSize: '0.7rem' }}
                  >
                    <Plus size={12} /> New Org
                  </button>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', maxHeight: '200px', overflowY: 'auto' }}>
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
                        border: org.id === activeOrg?.id ? '1px solid var(--border-highlight)' : '1px solid transparent'
                      }}
                    >
                      <div style={{ fontWeight: 600, fontSize: '0.85rem' }}>{org.name}</div>
                      <div style={{ fontSize: '0.725rem', color: 'var(--text-muted)' }}>Role: {org.role || 'Admin'}</div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Center Live Gateway Indicator */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <span style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.4rem',
            padding: '0.25rem 0.75rem',
            borderRadius: 'var(--radius-full)',
            background: 'rgba(16, 185, 129, 0.1)',
            border: '1px solid rgba(16, 185, 129, 0.3)',
            color: 'var(--status-success)',
            fontSize: '0.725rem',
            fontWeight: 600
          }}>
            <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: 'var(--status-success)' }} />
            API Gateway Active (:8080)
          </span>
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
                zIndex: 200,
                boxShadow: '0 15px 35px rgba(0,0,0,0.5)'
              }}>
                <div style={{ padding: '0.6rem', borderBottom: '1px solid var(--border-subtle)' }}>
                  <div style={{ fontWeight: 700, fontSize: '0.85rem' }}>{user?.firstName} {user?.lastName}</div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{user?.email}</div>
                  <div style={{ fontSize: '0.7rem', color: 'var(--brand-primary)', fontWeight: 600, marginTop: '2px' }}>{user?.role}</div>
                </div>

                <div style={{ padding: '0.5rem 0.6rem 0.25rem', fontSize: '0.7rem', color: 'var(--text-muted)', fontWeight: 700 }}>
                  DEMO PERSONAS
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
                  <UserCheck size={14} color="var(--brand-secondary)" /> Alex Chen (Lead PM)
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

      {/* Create Organization Modal */}
      <Modal
        isOpen={isOrgModalOpen}
        onClose={() => setIsOrgModalOpen(false)}
        title="Create New Organization Workspace"
      >
        <form onSubmit={handleCreateOrg}>
          {orgError && (
            <div style={{ padding: '0.75rem', background: 'rgba(239, 68, 68, 0.12)', color: 'var(--status-danger)', borderRadius: 'var(--radius-md)', marginBottom: '1rem', fontSize: '0.825rem' }}>
              {orgError}
            </div>
          )}

          <div className="input-group">
            <label className="input-label">Organization Name *</label>
            <input
              type="text"
              className="input-field"
              value={newOrgName}
              onChange={(e) => setNewOrgName(e.target.value)}
              placeholder="e.g. Acme Innovations Ltd"
              required
            />
          </div>

          <div className="input-group">
            <label className="input-label">Description</label>
            <textarea
              className="input-field"
              rows={3}
              value={newOrgDesc}
              onChange={(e) => setNewOrgDesc(e.target.value)}
              placeholder="Brief overview of the workspace..."
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
            <button type="button" onClick={() => setIsOrgModalOpen(false)} className="btn btn-secondary">
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={orgLoading}>
              {orgLoading ? 'Creating...' : 'Create Workspace'}
            </button>
          </div>
        </form>
      </Modal>
    </>
  );
}
