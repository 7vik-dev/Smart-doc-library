import { useState, useEffect, useCallback, useRef } from 'react';
import { api } from '../utils/api';
import { Document, CacheStats, SystemMetrics, LogEntry } from '../types';

export const useDashboard = () => {
  const [documents, setDocuments] = useState<Document[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [stats, setStats] = useState<CacheStats | null>(null);
  const [metrics, setMetrics] = useState<SystemMetrics | null>(null);
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [isConnected, setIsConnected] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [cacheStatus, setCacheStatus] = useState('');
  
  const [history, setHistory] = useState<{ time: string[], memory: number[], threads: number[] }>({
    time: [], memory: [], threads: []
  });

  const appendLog = useCallback((level: LogEntry['level'], message: string) => {
    const now = new Date();
    const time = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`;
    setLogs(prev => [...prev, { time, level, message }]);
  }, []);

  const loadDocuments = useCallback(async () => {
    setIsLoading(true);
    appendLog('INFO', `📥 GET /api/documents?page=${page} — fetching documents...`);
    const start = Date.now();
    try {
      const res = await api.get<{ content: Document[], totalPages: number }>('/documents', { params: { page, size: 5 } });
      const duration = Date.now() - start;
      const docs = res.data.content;
      setTotalPages(res.data.totalPages);

      if (duration < 10) {
        appendLog('CACHE', `⚡ CACHE HIT! Response in ${duration}ms — no DB query needed!`);
        setCacheStatus(`⚡ Cache HIT (${duration}ms)`);
      } else {
        appendLog('INFO', `📦 CACHE MISS — MongoDB queried. Got ${docs.length} docs in ${duration}ms`);
        setCacheStatus(`🗄️ Cache MISS (${duration}ms)`);
      }
      setDocuments(docs);
    } catch (e: any) {
      appendLog('ERROR', `❌ Failed to load documents: ${e.message}`);
      // Centralized error handling
      console.error('API Error:', e);
    } finally {
      setIsLoading(false);
    }
  }, [appendLog, page]);

  const loadStats = useCallback(async () => {
    try {
      const res = await api.get<CacheStats>('/documents/stats');
      setStats(res.data);
    } catch (e) { /* ignore */ }
  }, []);

  const fetchMetrics = useCallback(async () => {
    try {
      const res = await api.get<SystemMetrics>('/monitor/system');
      const m = res.data;
      setMetrics(m);
      setIsConnected(true);

      const label = new Date().toLocaleTimeString('en', { hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit' });
      
      setHistory(prev => {
        const newTime = [...prev.time, label];
        const newMem = [...prev.memory, m.memory.heapUsedMB];
        const newThreads = [...prev.threads, m.threads.activeThreads];
        
        if (newTime.length > 20) {
          newTime.shift(); newMem.shift(); newThreads.shift();
        }
        return { time: newTime, memory: newMem, threads: newThreads };
      });
    } catch (e) {
      setIsConnected(false);
    }
  }, []);

  const createDocument = async (title: string, content: string) => {
    appendLog('INFO', `📤 POST /api/documents — creating "${title}"...`);
    try {
      const res = await api.post<Document>('/documents', { title, content });
      appendLog('INFO', `✅ Document created — id="${res.data.id}". Cache evicted.`);
      loadDocuments();
      loadStats();
    } catch (e: any) {
      appendLog('ERROR', `❌ Create failed: ${e.message}`);
    }
  };

  const searchDocuments = async (query: string) => {
    appendLog('INFO', `🔍 GET /api/documents/search?title=${query}`);
    const start = Date.now();
    try {
      const res = await api.get<Document[]>('/documents/search', { params: { title: query } });
      const duration = Date.now() - start;
      if (duration < 10) {
        appendLog('CACHE', `⚡ CACHE HIT for "${query}"! Returned ${res.data.length} results in ${duration}ms`);
        setCacheStatus(`⚡ Cache HIT (${duration}ms)`);
      } else {
        appendLog('INFO', `🗄️ CACHE MISS for "${query}" — MongoDB returned ${res.data.length} results in ${duration}ms`);
        setCacheStatus(`🗄️ Cache MISS (${duration}ms)`);
      }
      setDocuments(res.data);
    } catch (e: any) {
      appendLog('ERROR', `❌ Search failed: ${e.message}`);
    }
  };

  const clearCache = async () => {
    appendLog('CACHE', '🗑️ DELETE /api/monitor/cache — manually evicting all caches...');
    try {
      await api.delete('/monitor/cache');
      appendLog('CACHE', '✅ All caches cleared. Next GET /documents will hit MongoDB.');
      setCacheStatus('Caches cleared');
    } catch (e: any) {
      appendLog('ERROR', `❌ Cache clear failed: ${e.message}`);
    }
  };

  const requestSummary = async (docId: string) => {
    appendLog('ASYNC', `🚀 POST /api/documents/${docId}/summary — triggering async...`);
    try {
      const start = Date.now();
      const res = await api.post(`/documents/${docId}/summary`);
      const duration = Date.now() - start;
      appendLog('ASYNC', `⚡ HTTP response received in ${duration}ms! (processing runs in background)`);
      appendLog('ASYNC', `   Status: ${res.data.status} — background thread is working...`);
      
      // Initial refresh
      loadDocuments();
      
      // Polling for status updates
      const poll = setInterval(async () => {
        try {
          const pollRes = await api.get<Document>(`/documents/${docId}`);
          if (pollRes.data.status === 'COMPLETED') {
            appendLog('ASYNC', `✅ [ASYNC COMPLETE] Summary ready for "${pollRes.data.title}"!`);
            loadDocuments();
            clearInterval(poll);
          } else if (pollRes.data.status === 'ERROR') {
            appendLog('ERROR', `❌ Summary generation failed for "${pollRes.data.title}"`);
            clearInterval(poll);
          }
        } catch (e) {
          clearInterval(poll);
        }
      }, 2000);

      // Timeout after 60s
      setTimeout(() => clearInterval(poll), 60000);
      
    } catch (e: any) {
      appendLog('ERROR', `❌ Summary request failed: ${e.message}`);
    }
  };

  useEffect(() => {
    loadDocuments();
    loadStats();
    fetchMetrics();
    const metricsInterval = setInterval(fetchMetrics, 3000);
    const statsInterval = setInterval(loadStats, 5000);
    return () => {
      clearInterval(metricsInterval);
      clearInterval(statsInterval);
    };
  }, [loadDocuments, loadStats, fetchMetrics]);

  return {
    documents,
    page,
    totalPages,
    setPage,
    stats,
    metrics,
    logs,
    isConnected,
    isLoading,
    cacheStatus,
    history,
    createDocument,
    searchDocuments,
    clearCache,
    requestSummary,
    refreshDocuments: loadDocuments,
    clearLogs: () => setLogs([{ time: '--:--:--', level: 'INFO', message: 'Log cleared.' }])
  };
};
