import React from 'react';
import { CacheStats } from '../types';

interface StatsStripProps {
  stats: CacheStats | null;
}

const StatsStrip: React.FC<StatsStripProps> = ({ stats }) => {
  return (
    <div className="stats-strip">
      <div className="stat-card" style={{ '--accent-color': 'var(--accent)' } as React.CSSProperties}>
        <div className="stat-label">Total Documents</div>
        <div className="stat-value">{stats?.totalDocuments ?? '—'}</div>
        <div className="stat-sub">in MongoDB</div>
      </div>
      <div className="stat-card" style={{ '--accent-color': 'var(--green)' } as React.CSSProperties}>
        <div className="stat-label">Cache Hits</div>
        <div className="stat-value">{stats?.cacheHits ?? '—'}</div>
        <div className="stat-sub">no DB query</div>
      </div>
      <div className="stat-card" style={{ '--accent-color': 'var(--orange)' } as React.CSSProperties}>
        <div className="stat-label">Cache Misses</div>
        <div className="stat-value">{stats?.cacheMisses ?? '—'}</div>
        <div className="stat-sub">hit MongoDB</div>
      </div>
      <div className="stat-card" style={{ '--accent-color': 'var(--purple)' } as React.CSSProperties}>
        <div className="stat-label">Hit Ratio</div>
        <div className="stat-value">{stats?.cacheHitRatio ?? '—'}</div>
        <div className="stat-sub">efficiency</div>
      </div>
    </div>
  );
};

export default StatsStrip;
