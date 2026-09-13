import React from 'react';
import { useNotifications } from '../context/NotificationContext';
import { X, CheckCheck, Bell, AlertTriangle, ArrowRight } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function NotificationDrawer() {
  const { notifications, isDrawerOpen, setIsDrawerOpen, markAsRead, markAllAsRead } = useNotifications();

  if (!isDrawerOpen) return null;

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        zIndex: 300,
        display: 'flex',
        justifyContent: 'flex-end',
        background: 'rgba(0, 0, 0, 0.4)'
      }}
      onClick={() => setIsDrawerOpen(false)}
    >
      <div
        className="fade-in"
        style={{
          width: '380px',
          height: '100%',
          background: 'var(--bg-surface)',
          borderLeft: '1px solid var(--border-subtle)',
          display: 'flex',
          flexDirection: 'column',
          boxShadow: 'var(--shadow-lg)'
        }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Drawer Header */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          padding: '1.25rem 1.5rem',
          borderBottom: '1px solid var(--border-subtle)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Bell size={18} color="var(--brand-primary)" />
            <h3 style={{ fontSize: '1rem' }}>Notifications</h3>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <button
              onClick={markAllAsRead}
              className="btn btn-secondary btn-sm"
              title="Mark All Read"
              style={{ fontSize: '0.75rem', padding: '0.3rem 0.5rem' }}
            >
              <CheckCheck size={14} />
            </button>
            <button onClick={() => setIsDrawerOpen(false)} className="btn-icon">
              <X size={18} />
            </button>
          </div>
        </div>

        {/* Notifications List */}
        <div style={{ flex: 1, overflowY: 'auto', padding: '1rem' }}>
          {notifications.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '3rem 1rem', color: 'var(--text-muted)' }}>
              No notifications yet.
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              {notifications.map(item => (
                <div
                  key={item.id}
                  onClick={() => markAsRead(item.id)}
                  style={{
                    padding: '0.85rem',
                    borderRadius: 'var(--radius-md)',
                    background: item.isRead ? 'var(--bg-canvas)' : 'var(--bg-surface-elevated)',
                    border: '1px solid',
                    borderColor: item.isRead ? 'var(--border-subtle)' : 'var(--border-highlight)',
                    cursor: 'pointer',
                    transition: 'all 0.15s ease'
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.3rem' }}>
                    <span style={{
                      fontWeight: item.isRead ? 500 : 700,
                      fontSize: '0.85rem',
                      color: item.isRead ? 'var(--text-primary)' : 'var(--brand-primary)'
                    }}>
                      {item.title}
                    </span>
                    {!item.isRead && (
                      <span style={{
                        width: '8px',
                        height: '8px',
                        borderRadius: '50%',
                        background: 'var(--status-danger)'
                      }} />
                    )}
                  </div>
                  <p style={{ fontSize: '0.775rem', color: 'var(--text-secondary)', lineHeight: 1.4 }}>
                    {item.message}
                  </p>
                  <div style={{ marginTop: '0.5rem', fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                    {item.createdAt}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Drawer Footer */}
        <div style={{
          padding: '1rem',
          borderTop: '1px solid var(--border-subtle)',
          background: 'var(--bg-surface-elevated)'
        }}>
          <Link
            to="/notifications"
            onClick={() => setIsDrawerOpen(false)}
            className="btn btn-secondary"
            style={{ width: '100%', justifyContent: 'center', fontSize: '0.8rem' }}
          >
            View All & Preferences <ArrowRight size={14} />
          </Link>
        </div>
      </div>
    </div>
  );
}
