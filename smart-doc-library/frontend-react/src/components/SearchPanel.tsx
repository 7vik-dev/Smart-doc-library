import React, { useState, useEffect } from 'react';
import { Search, RefreshCw, Trash2 } from 'lucide-react';

interface SearchPanelProps {
  onSearch: (query: string) => void;
  onRefresh: () => void;
  onClearCache: () => void;
  cacheStatus: string;
}

const SearchPanel: React.FC<SearchPanelProps> = ({ onSearch, onRefresh, onClearCache, cacheStatus }) => {
  const [query, setQuery] = useState('');

  // Debounced search
  useEffect(() => {
    const timer = setTimeout(() => {
      if (query.trim()) {
        onSearch(query);
      }
    }, 500); // 500ms debounce

    return () => clearTimeout(timer);
  }, [query, onSearch]);

  const handleSearch = () => {
    if (!query.trim()) return;
    onSearch(query);
  };

  const badgeClass = cacheStatus.includes('HIT') ? 'badge-cache-hit' : cacheStatus.includes('MISS') ? 'badge-cache-miss' : 'badge-none';

  return (
    <div className="card">
      <div className="card-header">
        <span className="card-title"><Search className="w-4 h-4" /> Search + Cache Demo</span>
      </div>
      <div className="card-body">
        <div className="field">
          <label>Search by title (auto-debounced)</label>
          <div className="input-group">
            <input 
              type="text" 
              value={query} 
              onChange={(e) => setQuery(e.target.value)} 
              placeholder="e.g. Spring, MongoDB..." 
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            />
            <button className="btn btn-primary" onClick={handleSearch}><Search className="w-4 h-4" /></button>
          </div>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '12px' }}>
          <span style={{ fontFamily: 'var(--mono)', fontSize: '11px', color: 'var(--muted)' }}>Last request:</span>
          <span className={`badge ${badgeClass}`}>{cacheStatus || 'No request yet'}</span>
        </div>
        <div style={{ fontFamily: 'var(--mono)', fontSize: '11px', color: 'var(--muted)', lineHeight: '1.6' }}>
          💡 Search the same term twice.<br />
          1st: CACHE MISS → hits MongoDB<br />
          2nd: CACHE HIT → returns from memory
        </div>
        <div className="sep" style={{ margin: '12px 0' }} />
        <div style={{ display: 'flex', gap: '8px' }}>
          <button className="btn btn-ghost btn-sm" onClick={onRefresh}><RefreshCw className="w-3 h-3" /> Refresh List</button>
          <button className="btn btn-danger btn-sm" onClick={onClearCache}><Trash2 className="w-3 h-3" /> Clear Cache</button>
        </div>
      </div>
    </div>
  );
};

export default SearchPanel;
