import React from 'react';
import { Line } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js';
import { SystemMetrics } from '../types';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

interface MetricsPanelProps {
  metrics: SystemMetrics | null;
  history: {
    time: string[];
    memory: number[];
    threads: number[];
  };
}

const MetricsPanel: React.FC<MetricsPanelProps> = ({ metrics, history }) => {
  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    animation: { duration: 400 },
    scales: {
      y: { grid: { color: '#1e2535' }, ticks: { color: '#5a6480' } },
      x: { grid: { color: '#1e2535' }, ticks: { color: '#5a6480' } }
    },
    plugins: { legend: { display: false } }
  };

  const memoryData = {
    labels: history.time,
    datasets: [{
      label: 'Heap Used (MB)',
      data: history.memory,
      borderColor: '#00e5ff',
      backgroundColor: 'rgba(0,229,255,0.05)',
      borderWidth: 2,
      fill: true,
      tension: 0.4,
      pointRadius: 3,
      pointBackgroundColor: '#00e5ff'
    }]
  };

  const threadData = {
    labels: history.time,
    datasets: [{
      label: 'Active Threads',
      data: history.threads,
      borderColor: '#a855f7',
      backgroundColor: 'rgba(168,85,247,0.05)',
      borderWidth: 2,
      fill: true,
      tension: 0.4,
      pointRadius: 3,
      pointBackgroundColor: '#a855f7'
    }]
  };

  const uptime = metrics ? (
    metrics.system.uptimeSeconds < 60 ? `${metrics.system.uptimeSeconds}s` :
    metrics.system.uptimeSeconds < 3600 ? `${Math.floor(metrics.system.uptimeSeconds/60)}m ${metrics.system.uptimeSeconds%60}s` :
    `${Math.floor(metrics.system.uptimeSeconds/3600)}h`
  ) : '—';

  return (
    <div id="tab-metrics" className="tab-panel active">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 700 }}>Live System Metrics</h2>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div className="status-dot" />
          <span style={{ fontFamily: 'var(--mono)', fontSize: '11px', color: 'var(--muted)' }}>Auto-refresh every 3s</span>
        </div>
      </div>

      <div className="grid-2" style={{ marginBottom: '16px' }}>
        <div className="card">
          <div className="card-header"><span className="card-title">💾 JVM Memory</span></div>
          <div className="card-body">
            <div className="metric-bar">
              <div className="metric-bar-label">
                <span>Heap Used</span>
                <span>{metrics?.memory.heapUsedMB ?? '—'} / {metrics?.memory.heapMaxMB ?? '—'} MB</span>
              </div>
              <div className="metric-bar-track">
                <div 
                  className="metric-bar-fill" 
                  style={{ width: `${metrics ? (metrics.memory.heapUsedMB / metrics.memory.heapMaxMB * 100) : 0}%` }} 
                />
              </div>
            </div>
            <div className="metric-bar">
              <div className="metric-bar-label">
                <span>Total Memory</span>
                <span>{metrics?.memory.usedMB ?? '—'} / {metrics?.memory.maxMB ?? '—'} MB</span>
              </div>
              <div className="metric-bar-track">
                <div 
                  className="metric-bar-fill" 
                  style={{ 
                    width: `${metrics?.memory.usagePercent ?? 0}%`, 
                    background: 'linear-gradient(90deg,var(--purple),var(--accent))' 
                  }} 
                />
              </div>
            </div>
            <div style={{ fontFamily: 'var(--mono)', fontSize: '11px', color: 'var(--muted)', marginTop: '12px' }}>
              Max JVM Heap: <span style={{ color: 'var(--text)' }}>{metrics?.memory.maxMB ?? '—'} MB</span>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-header"><span className="card-title">🧵 Threads</span></div>
          <div className="card-body">
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
              <div>
                <div className="stat-label">Active Threads</div>
                <div style={{ fontSize: '32px', fontWeight: 800, color: 'var(--accent)' }}>{metrics?.threads.activeThreads ?? '—'}</div>
              </div>
              <div>
                <div className="stat-label">Peak Threads</div>
                <div style={{ fontSize: '32px', fontWeight: 800, color: 'var(--purple)' }}>{metrics?.threads.peakThreads ?? '—'}</div>
              </div>
              <div>
                <div className="stat-label">Daemon Threads</div>
                <div style={{ fontSize: '24px', fontWeight: 700, color: 'var(--muted)' }}>{metrics?.threads.daemonThreads ?? '—'}</div>
              </div>
              <div>
                <div className="stat-label">Uptime</div>
                <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--green)', fontFamily: 'var(--mono)' }}>{uptime}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="card" style={{ marginBottom: '16px' }}>
        <div className="card-header"><span className="card-title">📈 Memory Usage History</span></div>
        <div className="card-body">
          <div className="chart-wrap">
            <Line options={chartOptions} data={memoryData} />
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-header"><span className="card-title">📉 Thread Count History</span></div>
        <div className="card-body">
          <div className="chart-wrap">
            <Line options={chartOptions} data={threadData} />
          </div>
        </div>
      </div>
    </div>
  );
};

export default MetricsPanel;
