import React, { useState } from 'react';
import { actuator } from '../utils/api';

const ActuatorPanel: React.FC = () => {
  const [healthData, setHealthData] = useState<string>('Click Refresh to fetch /actuator/health...');

  const fetchHealth = async () => {
    try {
      const res = await actuator.get('/health');
      setHealthData(JSON.stringify(res.data, null, 2));
    } catch (e) {
      setHealthData('Error: Cannot reach /actuator/health');
    }
  };

  const actuatorLinks = [
    { emoji: '💚', path: '/health', desc: 'App + MongoDB status' },
    { emoji: '📊', path: '/metrics', desc: 'JVM, HTTP, cache stats' },
    { emoji: '📝', path: '/loggers', desc: 'View & change log levels' },
    { emoji: '⚡', path: '/caches', desc: 'Registered caches' },
    { emoji: '⚙️', path: '/env', desc: 'Config & environment vars' },
    { emoji: 'ℹ️', path: '/info', desc: 'App version & build info' },
  ];

  return (
    <div id="tab-actuator" className="tab-panel active">
      <div style={{ marginBottom: '20px' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 700, marginBottom: '6px' }}>Spring Boot Actuator</h2>
        <p style={{ fontSize: '13px', color: 'var(--muted)', lineHeight: '1.6' }}>
          Actuator exposes production-ready monitoring endpoints. Click any endpoint to open it in a new tab.
          In production, these should be secured and only accessible internally.
        </p>
      </div>

      <div className="actuator-grid" style={{ marginBottom: '24px' }}>
        {actuatorLinks.map((link) => (
          <a 
            key={link.path} 
            href={actuator.defaults.baseURL + link.path} 
            target="_blank" 
            rel="noreferrer"
            className="actuator-link"
          >
            <span className="actuator-emoji">{link.emoji}</span>
            <div className="actuator-info">
              <div className="actuator-path">{actuator.defaults.baseURL + link.path}</div>
              <div className="actuator-desc">{link.desc}</div>
            </div>
          </a>
        ))}
      </div>

      <div className="card">
        <div className="card-header">
          <span className="card-title">🔴 Live Health Check</span>
          <button className="btn btn-ghost btn-sm" onClick={fetchHealth}>Refresh</button>
        </div>
        <div className="card-body">
          <pre style={{ fontFamily: 'var(--mono)', fontSize: '12px', color: 'var(--accent)', whiteSpace: 'pre-wrap', lineHeight: '1.6' }}>
            {healthData}
          </pre>
        </div>
      </div>
    </div>
  );
};

export default ActuatorPanel;
