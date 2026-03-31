import React from 'react';
import { Document } from '../types';
import { FileText, Clock, CheckCircle, AlertCircle, Zap } from 'lucide-react';

interface DocumentListProps {
  documents: Document[];
  isLoading: boolean;
  onRequestSummary: (docId: string) => void;
  page?: number;
  totalPages?: number;
  onPageChange?: (page: number) => void;
}

const DocumentList: React.FC<DocumentListProps> = ({ 
  documents, isLoading, onRequestSummary, 
  page = 0, totalPages = 1, onPageChange 
}) => {
  const summaryIcons: Record<string, React.ReactNode> = { 
    PENDING: <Clock className="w-4 h-4 text-orange-400" />, 
    PROCESSING: <Zap className="w-4 h-4 text-blue-400 animate-pulse" />, 
    COMPLETED: <CheckCircle className="w-4 h-4 text-green-400" />, 
    ERROR: <AlertCircle className="w-4 h-4 text-red-400" /> 
  };
  
  const summaryBadgeClass: Record<string, string> = { 
    PENDING: 'badge-pending', 
    PROCESSING: 'badge-processing', 
    COMPLETED: 'badge-completed', 
    ERROR: 'badge-error' 
  };

  if (isLoading) {
    return (
      <div className="card">
        <div className="card-header">
          <span className="card-title"><FileText className="w-4 h-4" /> Document Library</span>
        </div>
        <div className="card-body">
          <div className="loading-state">
            <div className="spinner" /> Loading documents...
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="card">
      <div className="card-header">
        <span className="card-title"><FileText className="w-4 h-4" /> Document Library</span>
      </div>
      <div className="card-body" style={{ padding: '12px' }}>
        <div id="docList">
          {documents.length === 0 ? (
            <div className="empty-state">
              <div className="icon">📭</div>No documents yet. Create one above!
            </div>
          ) : (
            <>
              {documents.map(doc => {
                const status = doc.status || 'PENDING';
                const date = doc.createdAt ? new Date(doc.createdAt).toLocaleDateString() : '—';
                const words = doc.content ? doc.content.split(' ').length : 0;
                return (
                  <div key={doc.id} className="doc-item">
                    <div className="doc-icon">
                      {summaryIcons[status] || <FileText className="w-4 h-4" />}
                    </div>
                    <div className="doc-info">
                      <div className="doc-title">{doc.title}</div>
                      <div className="doc-meta">
                        <span>📅 {date}</span>
                        <span>📝 {words} words</span>
                        <span className={`badge ${summaryBadgeClass[status] || 'badge-none'}`}>{status}</span>
                      </div>
                      {doc.summary && (
                        <div style={{ fontSize: '12px', color: '#7a8aaa', marginTop: '6px', fontFamily: 'var(--mono)' }}>
                          {doc.summary.substring(0, 150)}...
                        </div>
                      )}
                    </div>
                    <div className="doc-actions">
                      <button 
                        className="btn btn-ghost btn-sm" 
                        onClick={() => onRequestSummary(doc.id)} 
                        disabled={status === 'PROCESSING' || status === 'COMPLETED'}
                        title="Generate Summary"
                      >
                        {status === 'PROCESSING' ? <span className="spinner" /> : '🤖 Summary'}
                      </button>
                    </div>
                  </div>
                );
              })}
              
              {onPageChange && totalPages > 1 && (
                <div style={{ display: 'flex', justifyContent: 'center', gap: '8px', marginTop: '16px', padding: '8px' }}>
                  <button 
                    className="btn btn-ghost btn-sm" 
                    disabled={page === 0} 
                    onClick={() => onPageChange(page - 1)}
                  >
                    Previous
                  </button>
                  <span style={{ fontFamily: 'var(--mono)', fontSize: '12px', display: 'flex', alignItems: 'center' }}>
                    Page {page + 1} of {totalPages}
                  </span>
                  <button 
                    className="btn btn-ghost btn-sm" 
                    disabled={page === totalPages - 1} 
                    onClick={() => onPageChange(page + 1)}
                  >
                    Next
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default DocumentList;
