import React, { useState } from 'react';
import { useNotifications } from '../context/NotificationContext';
import {
  Bell,
  CheckCheck,
  Mail,
  Smartphone,
  Sliders,
  AlertTriangle,
  Zap,
  CheckCircle2,
  Trash2
} from 'lucide-react';

export default function NotificationsPage() {
  const { notifications, markAsRead, markAllAsRead } = useNotifications();
  const [filter, setFilter] = useState('ALL');

  // Preferences State
  const [prefs, setPrefs] = useState({
    emailEnabled: true,
    inAppEnabled: true,
    taskNotifications: true,
    sprintNotifications: true,
    resourceNotifications: true
  });
  const [prefSaved, setPrefSaved] = useState(false);

  const handleTogglePref = (key) => {
    setPrefs(prev => ({ ...prev, [key]: !prev[key] }));
    setPrefSaved(true);
    setTimeout(() => setPrefSaved(false), 2000);
  };

  const filtered = notifications.filter(n => filter === 'ALL' || (filter === 'UNREAD' && !n.isRead));

  return (
    <div className="fade-in">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.85rem', marginBottom: '0.25rem' }}>Notifications &amp; Alert Preferences</h1>
          <p style={{ color: 'var(--text-secondary)' }}>
            Review real-time task triggers, sprint announcements, and configure delivery channels
          </p>
        </div>

        <button onClick={markAllAsRead} className="btn btn-secondary">
          <CheckCheck size={16} /> Mark All as Read
        </button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '1.5rem' }}>
        {/* Notifications Inbox */}
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.25rem' }}>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <button
                onClick={() => setFilter('ALL')}
                className={`btn btn-sm ${filter === 'ALL' ? 'btn-primary' : 'btn-secondary'}`}
              >
                All ({notifications.length})
              </button>
              <button
                onClick={() => setFilter('UNREAD')}
                className={`btn btn-sm ${filter === 'UNREAD' ? 'btn-primary' : 'btn-secondary'}`}
              >
                Unread ({notifications.filter(n => !n.isRead).length})
              </button>
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            {filtered.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '3rem 1rem', color: 'var(--text-muted)' }}>
                No notifications in this view.
              </div>
            ) : (
              filtered.map(item => (
                <div
                  key={item.id}
                  style={{
                    padding: '1rem',
                    borderRadius: 'var(--radius-md)',
                    background: item.isRead ? 'var(--bg-canvas)' : 'var(--bg-surface-elevated)',
                    border: '1px solid',
                    borderColor: item.isRead ? 'var(--border-subtle)' : 'var(--border-highlight)',
                    display: 'flex',
                    alignItems: 'flex-start',
                    justifyContent: 'space-between',
                    gap: '1rem'
                  }}
                >
                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.35rem' }}>
                      <span className={`badge badge-${item.notificationType.includes('OVERALLOCATED') ? 'danger' : 'primary'}`} style={{ fontSize: '0.65rem' }}>
                        {item.notificationType}
                      </span>
                      <h4 style={{ fontSize: '0.95rem', fontWeight: item.isRead ? 500 : 700 }}>
                        {item.title}
                      </h4>
                      {!item.isRead && (
                        <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: 'var(--status-danger)' }} />
                      )}
                    </div>
                    <p style={{ fontSize: '0.825rem', color: 'var(--text-secondary)', lineHeight: 1.4 }}>
                      {item.message}
                    </p>
                    <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', marginTop: '0.5rem' }}>
                      {item.createdAt}
                    </div>
                  </div>

                  {!item.isRead && (
                    <button
                      onClick={() => markAsRead(item.id)}
                      className="btn btn-secondary btn-sm"
                      title="Mark Read"
                    >
                      <CheckCheck size={14} />
                    </button>
                  )}
                </div>
              ))
            )}
          </div>
        </div>

        {/* User Delivery Preferences Panel */}
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
            <Sliders size={18} color="var(--brand-primary)" />
            <h3 style={{ fontSize: '1.15rem' }}>Delivery Channels</h3>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            {/* Email Channel */}
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                <Mail size={16} color="var(--brand-secondary)" />
                <div>
                  <div style={{ fontWeight: 600, fontSize: '0.85rem' }}>Email Notifications</div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Send critical alerts to inbox</div>
                </div>
              </div>
              <input
                type="checkbox"
                checked={prefs.emailEnabled}
                onChange={() => handleTogglePref('emailEnabled')}
                style={{ width: '18px', height: '18px', cursor: 'pointer', accentColor: 'var(--brand-primary)' }}
              />
            </div>

            {/* In-App Channel */}
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                <Smartphone size={16} color="var(--brand-primary)" />
                <div>
                  <div style={{ fontWeight: 600, fontSize: '0.85rem' }}>In-App Toast Alerts</div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Real-time top banner alerts</div>
                </div>
              </div>
              <input
                type="checkbox"
                checked={prefs.inAppEnabled}
                onChange={() => handleTogglePref('inAppEnabled')}
                style={{ width: '18px', height: '18px', cursor: 'pointer', accentColor: 'var(--brand-primary)' }}
              />
            </div>

            <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: '1rem', marginTop: '0.5rem' }}>
              <div style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', marginBottom: '0.85rem', textTransform: 'uppercase' }}>
                Event Category Toggles
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <span style={{ fontSize: '0.825rem' }}>Task Assignments &amp; Status</span>
                  <input
                    type="checkbox"
                    checked={prefs.taskNotifications}
                    onChange={() => handleTogglePref('taskNotifications')}
                    style={{ width: '16px', height: '16px', cursor: 'pointer', accentColor: 'var(--brand-primary)' }}
                  />
                </div>

                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <span style={{ fontSize: '0.825rem' }}>Sprint Transitions &amp; Goals</span>
                  <input
                    type="checkbox"
                    checked={prefs.sprintNotifications}
                    onChange={() => handleTogglePref('sprintNotifications')}
                    style={{ width: '16px', height: '16px', cursor: 'pointer', accentColor: 'var(--brand-primary)' }}
                  />
                </div>

                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <span style={{ fontSize: '0.825rem' }}>Resource Over-allocation Alerts</span>
                  <input
                    type="checkbox"
                    checked={prefs.resourceNotifications}
                    onChange={() => handleTogglePref('resourceNotifications')}
                    style={{ width: '16px', height: '16px', cursor: 'pointer', accentColor: 'var(--brand-primary)' }}
                  />
                </div>
              </div>
            </div>

            {prefSaved && (
              <div style={{
                padding: '0.5rem',
                background: 'var(--status-success-bg)',
                color: 'var(--status-success)',
                fontSize: '0.75rem',
                borderRadius: 'var(--radius-sm)',
                textAlign: 'center',
                fontWeight: 600
              }}>
                Preferences saved successfully!
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
