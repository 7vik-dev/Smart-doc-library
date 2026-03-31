import React from 'react';
import { BookOpen, ShieldCheck, Server } from 'lucide-react';

interface HeaderProps {
  isConnected: boolean;
  isRender: boolean;
}

const Header: React.FC<HeaderProps> = ({ isConnected, isRender }) => {
  return (
    <header>
      <div className="logo">
        <div className="logo-icon"><BookOpen className="w-5 h-5" /></div>
        <div className="logo-text">
          <h1>Smart Document Library</h1>
          <span>Performance Monitoring Demo</span>
        </div>
      </div>
      <div className="header-status">
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <div 
            className="status-dot" 
            style={{ 
              background: isConnected ? '#00ff88' : '#ff4444',
              boxShadow: isConnected ? '0 0 8px #00ff88' : '0 0 8px #ff4444'
            }} 
          />
          <span className="status-label">
            {isConnected ? 'Backend Connected' : 'Backend Offline'}
          </span>
        </div>
        <span className="api-badge">
          {isRender ? <ShieldCheck className="w-3 h-3 inline mr-1" /> : <Server className="w-3 h-3 inline mr-1" />}
          {isRender ? 'Render Deployment' : 'localhost:8080'}
        </span>
      </div>
    </header>
  );
};

export default Header;
