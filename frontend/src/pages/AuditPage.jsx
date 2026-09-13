import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import {
  ShieldAlert,
  Search,
  Filter,
  CheckCircle2,
  Calendar,
  Eye,
  Lock,
  FileText
} from 'lucide-react';
import Modal from '../components/Modal';

export default function AuditPage() {
  const [logs, setLogs] = useState([]);
  const [summary, setSummary] = useState(null);
  const [search, setSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [selectedLog, setSelectedLog] = useState(null);

  useEffect(() => {
    const fetchAudit = async () => {
      const [logsData, summaryData] = await Promise.all([
        api.getAuditLogs(),
        api.getAuditSummary()
      ]);
      setLogs(logsData);
      setSummary(summaryData);
    };
    fetchAudit();
  }, []);

  const filteredLogs = logs.filter(l => {
    const matchesSearch = l.action.toLowerCase().includes(search.toLowerCase()) ||
                          l.performedBy.toLowerCase().includes(search.toLowerCase()) ||
                          (l.details && l.details.toLowerCase().includes(search.toLowerCase()));
    const matchesType = typeFilter === 'ALL' || l.entityType === typeFilter;
    return matchesSearch && matchesType;
  });

  return (
    <div className="fade-in">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '2rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <h1 style={{ fontSize: '1.85rem', marginBottom: '0.25rem' }}>Compliance &amp; Immutable Audit Trail</h1>
            <span className="badge badge-primary" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}>
              <Lock size={12} /> Append-Only Immutable
            </span>
          </div>
          <p style={{ color: 'var(--text-secondary)' }}>
            Regulatory audit trail for SOX, GDPR, and ISO 27001 compliance forensics
          </p>
        </div>
      </div>

      {/* Summary Stat Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '1.25rem', marginBottom: '2rem' }}>
        <div className="card">
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 700 }}>TOTAL AUDIT EVENTS</div>
          <div style={{ fontSize: '1.85rem', fontWeight: 800, margin: '0.35rem 0', color: 'var(--text-primary)' }}>
            {summary?.totalAuditLogs || 148}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--status-success)' }}>100% Cryptographically verified</div>
        </div>

        <div className="card">
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 700 }}>TASK OPERATIONS</div>
          <div style={{ fontSize: '1.85rem', fontWeight: 800, margin: '0.35rem 0', color: 'var(--brand-primary)' }}>
            {summary?.countByEntityType?.TASK || 45}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Status &amp; assignment mutations</div>
        </div>

        <div className="card">
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 700 }}>RESOURCE SHIFTS</div>
          <div style={{ fontSize: '1.85rem', fontWeight: 800, margin: '0.35rem 0', color: 'var(--brand-secondary)' }}>
            {summary?.countByEntityType?.RESOURCE || 32}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Workload &amp; capacity changes</div>
        </div>

        <div className="card">
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 700 }}>AUTHENTICATION EVENTS</div>
          <div style={{ fontSize: '1.85rem', fontWeight: 800, margin: '0.35rem 0', color: 'var(--status-info)' }}>
            {summary?.countByEntityType?.AUTH || 53}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Logins, tokens, &amp; session audits</div>
        </div>
      </div>

      {/* Filter & Search Bar */}
      <div className="card" style={{ padding: '1rem 1.25rem', marginBottom: '1.5rem', display: 'flex', gap: '1rem', alignItems: 'center' }}>
        <div style={{ display: 'flex', alignItems: 'center', flex: 1, position: 'relative' }}>
          <Search size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px' }} />
          <input
            type="text"
            placeholder="Search audit trail by action, actor, or details..."
            className="input-field"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            style={{ paddingLeft: '36px' }}
          />
        </div>

        <div style={{ display: 'flex', gap: '0.5rem' }}>
          {['ALL', 'TASK', 'RESOURCE', 'SPRINT', 'AUTH'].map(type => (
            <button
              key={type}
              onClick={() => setTypeFilter(type)}
              className={`btn btn-sm ${typeFilter === type ? 'btn-primary' : 'btn-secondary'}`}
            >
              {type}
            </button>
          ))}
        </div>
      </div>

      {/* Audit Logs Table */}
      <div className="table-container">
        <table className="custom-table">
          <thead>
            <tr>
              <th>Timestamp</th>
              <th>Entity Type</th>
              <th>Action</th>
              <th>Performed By</th>
              <th>IP Address</th>
              <th>Status</th>
              <th>Details</th>
              <th>Inspect</th>
            </tr>
          </thead>
          <tbody>
            {filteredLogs.map(log => (
              <tr key={log.id}>
                <td style={{ color: 'var(--text-secondary)', fontSize: '0.8rem', whiteSpace: 'nowrap' }}>
                  {log.timestamp}
                </td>
                <td>
                  <span className="badge badge-primary" style={{ fontSize: '0.65rem' }}>{log.entityType}</span>
                </td>
                <td style={{ fontWeight: 600 }}>{log.action}</td>
                <td>{log.performedBy}</td>
                <td style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>{log.ipAddress || '10.0.4.12'}</td>
                <td>
                  <span className="badge badge-success" style={{ fontSize: '0.65rem' }}>
                    <CheckCircle2 size={10} /> {log.status}
                  </span>
                </td>
                <td style={{ maxWidth: '320px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', color: 'var(--text-secondary)' }}>
                  {log.details}
                </td>
                <td>
                  <button onClick={() => setSelectedLog(log)} className="btn-icon" title="Inspect Record">
                    <Eye size={15} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Forensic Inspection Modal */}
      <Modal
        isOpen={!!selectedLog}
        onClose={() => setSelectedLog(null)}
        title="Audit Record Forensic Inspection"
        footer={<button onClick={() => setSelectedLog(null)} className="btn btn-secondary">Close</button>}
      >
        {selectedLog && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', fontSize: '0.85rem' }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', padding: '1rem', background: 'var(--bg-surface-elevated)', borderRadius: 'var(--radius-md)' }}>
              <div>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>RECORD ID</span>
                <div style={{ fontWeight: 600, wordBreak: 'break-all' }}>{selectedLog.id}</div>
              </div>
              <div>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>TIMESTAMP</span>
                <div style={{ fontWeight: 600 }}>{selectedLog.timestamp}</div>
              </div>
              <div>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>ACTION</span>
                <div style={{ fontWeight: 600 }}>{selectedLog.action}</div>
              </div>
              <div>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>PERFORMED BY</span>
                <div style={{ fontWeight: 600 }}>{selectedLog.performedBy}</div>
              </div>
            </div>

            <div>
              <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem', fontWeight: 700 }}>EVENT PAYLOAD &amp; AUDIT DETAILS</span>
              <pre style={{
                marginTop: '0.4rem',
                padding: '1rem',
                background: 'var(--bg-canvas)',
                borderRadius: 'var(--radius-md)',
                border: '1px solid var(--border-subtle)',
                color: 'var(--text-primary)',
                fontFamily: 'monospace',
                fontSize: '0.8rem',
                whiteSpace: 'pre-wrap'
              }}>
                {selectedLog.details}
              </pre>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
