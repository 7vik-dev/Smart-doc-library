import React, { useState } from 'react';
import Header from './components/Header';
import StatsStrip from './components/StatsStrip';
import AddDocumentForm from './components/AddDocumentForm';
import SearchPanel from './components/SearchPanel';
import DocumentList from './components/DocumentList';
import ActivityLog from './components/ActivityLog';
import MetricsPanel from './components/MetricsPanel';
import ActuatorPanel from './components/ActuatorPanel';
import ConceptsPanel from './components/ConceptsPanel';
import { useDashboard } from './hooks/useDashboard';

const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'documents' | 'metrics' | 'actuator' | 'concepts'>('documents');
  const dashboard = useDashboard();
  const isRender = window.location.hostname !== 'localhost';

  return (
    <div className="app">
      <Header isConnected={dashboard.isConnected} isRender={isRender} />

      {/* TABS */}
      <div className="tabs">
        <button 
          className={`tab ${activeTab === 'documents' ? 'active' : ''}`} 
          onClick={() => setActiveTab('documents')}
        >
          📄 Documents
        </button>
        <button 
          className={`tab ${activeTab === 'metrics' ? 'active' : ''}`} 
          onClick={() => setActiveTab('metrics')}
        >
          📊 Live Metrics
        </button>
        <button 
          className={`tab ${activeTab === 'actuator' ? 'active' : ''}`} 
          onClick={() => setActiveTab('actuator')}
        >
          🔩 Actuator
        </button>
        <button 
          className={`tab ${activeTab === 'concepts' ? 'active' : ''}`} 
          onClick={() => setActiveTab('concepts')}
        >
          🎓 How It Works
        </button>
      </div>

      <main>
        {activeTab === 'documents' && (
          <div id="tab-documents" className="tab-panel active">
            <StatsStrip stats={dashboard.stats} />
            
            <div className="grid-2" style={{ marginBottom: '16px' }}>
              <AddDocumentForm onCreate={dashboard.createDocument} isLoading={dashboard.isLoading} />
              <SearchPanel 
                onSearch={dashboard.searchDocuments} 
                onRefresh={dashboard.refreshDocuments} 
                onClearCache={dashboard.clearCache} 
                cacheStatus={dashboard.cacheStatus} 
              />
            </div>

            <DocumentList 
              documents={dashboard.documents} 
              isLoading={dashboard.isLoading} 
              onRequestSummary={dashboard.requestSummary} 
              page={dashboard.page}
              totalPages={dashboard.totalPages}
              onPageChange={dashboard.setPage}
            />

            <ActivityLog logs={dashboard.logs} onClear={dashboard.clearLogs} />
          </div>
        )}

        {activeTab === 'metrics' && (
          <MetricsPanel metrics={dashboard.metrics} history={dashboard.history} />
        )}

        {activeTab === 'actuator' && <ActuatorPanel />}

        {activeTab === 'concepts' && <ConceptsPanel />}
      </main>
    </div>
  );
};

export default App;
