export interface Document {
  id: string;
  title: string;
  content?: string;
  fileName?: string;
  createdAt?: string;
  summary?: string;
  status?: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'ERROR';
}

export interface CacheStats {
  totalDocuments: number;
  cacheHits: number;
  cacheMisses: number;
  cacheHitRatio: string;
}

export interface SystemMetrics {
  memory: {
    usedMB: number;
    maxMB: number;
    totalMB: number;
    heapUsedMB: number;
    heapMaxMB: number;
    usagePercent: number;
  };
  threads: {
    activeThreads: number;
    daemonThreads: number;
    peakThreads: number;
  };
  system: {
    availableProcessors: number;
    osName: string;
    javaVersion: string;
    uptimeSeconds: number;
  };
  timestamp: number;
}

export interface LogEntry {
  time: string;
  level: 'INFO' | 'WARN' | 'ERROR' | 'CACHE' | 'ASYNC';
  message: string;
}
