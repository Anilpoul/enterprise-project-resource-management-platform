import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  FolderKanban,
  Kanban,
  Zap,
  Users2,
  BarChart3,
  ShieldAlert,
  Bell,
  Settings
} from 'lucide-react';
import { useNotifications } from '../context/NotificationContext';

const NAV_ITEMS = [
  { path: '/', label: 'Executive Dashboard', icon: LayoutDashboard },
  { path: '/projects', label: 'Projects & Teams', icon: FolderKanban },
  { path: '/boards', label: 'Kanban & Scrum', icon: Kanban },
  { path: '/sprints', label: 'Sprint Planning', icon: Zap },
  { path: '/resources', label: 'Resource Capacity', icon: Users2 },
  { path: '/analytics', label: 'Analytics & KPIs', icon: BarChart3 },
  { path: '/audit', label: 'Compliance & Audit', icon: ShieldAlert },
  { path: '/notifications', label: 'Notifications', icon: Bell, badge: true }
];

export default function Sidebar() {
  const { unreadCount } = useNotifications();

  return (
    <aside style={{
      width: '250px',
      background: 'var(--bg-sidebar)',
      borderRight: '1px solid var(--border-subtle)',
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'space-between',
      padding: '1.25rem 0.85rem'
    }}>
      {/* Navigation List */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
        <div style={{
          padding: '0.4rem 0.75rem',
          fontSize: '0.7rem',
          fontWeight: 800,
          color: 'var(--text-muted)',
          letterSpacing: '0.08em'
        }}>
          PLATFORM CORE
        </div>

        {NAV_ITEMS.map(item => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.path}
              to={item.path}
              style={({ isActive }) => ({
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '0.65rem 0.85rem',
                borderRadius: 'var(--radius-md)',
                color: isActive ? '#ffffff' : 'var(--text-secondary)',
                background: isActive ? 'var(--brand-gradient)' : 'transparent',
                fontWeight: isActive ? 600 : 500,
                fontSize: '0.85rem',
                textDecoration: 'none',
                boxShadow: isActive ? 'var(--brand-glow)' : 'none',
                transition: 'all 0.15s ease'
              })}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <Icon size={18} />
                <span>{item.label}</span>
              </div>
              {item.badge && unreadCount > 0 && (
                <span className="badge badge-danger" style={{ fontSize: '0.65rem', padding: '0.1rem 0.45rem' }}>
                  {unreadCount}
                </span>
              )}
            </NavLink>
          );
        })}
      </div>

      {/* Footer Info */}
      <div style={{
        padding: '1rem 0.85rem',
        borderTop: '1px solid var(--border-subtle)',
        display: 'flex',
        flexDirection: 'column',
        gap: '0.5rem'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Gateway Status</span>
          <span className="badge badge-success" style={{ fontSize: '0.65rem' }}>Online (8080)</span>
        </div>
        <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
          15 Microservices • 311 Tests Passed
        </div>
      </div>
    </aside>
  );
}
