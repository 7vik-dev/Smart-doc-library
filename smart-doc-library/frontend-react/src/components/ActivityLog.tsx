import React, { useRef, useEffect } from 'react';
import { LogEntry } from '../types';
import { Terminal, Trash2 } from 'lucide-react';

interface ActivityLogProps {
  logs: LogEntry[];
  onClear: () => void;
}

const ActivityLog: React.FC<ActivityLogProps> = ({ logs, onClear }) => {
  const consoleRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (consoleRef.current) {
      consoleRef.current.scrollTop = consoleRef.current.scrollHeight;
    }
  }, [logs]);

  const levelClasses: Record<string, string> = {
    'INFO': 'log-level-info', 'WARN': 'log-level-warn',
    'ERROR': 'log-level-error', 'CACHE': 'log-level-cache', 'ASYNC': 'log-level-async'
  };

  return (
    <div className="card" style={{ marginTop: '16px' }}>
      <div className="card-header">
        <span className="card-title"><Terminal className="w-4 h-4" /> Activity Log</span>
        <button className="btn btn-ghost btn-sm" onClick={onClear}><Trash2 className="w-3 h-3" /> Clear</button>
      </div>
      <div className="log-console" ref={consoleRef}>
        {logs.map((log, i) => (
          <div key={i} className="log-entry">
            <span className="log-time">{log.time}</span>
            <span className={levelClasses[log.level] || 'log-level-info'}>{log.level.padEnd(5)}</span>
            <span className="log-msg">{log.message}</span>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ActivityLog;
