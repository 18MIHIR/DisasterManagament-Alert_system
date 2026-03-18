import React from 'react';
import { DashboardStats } from '../types';

interface StatsPanelProps {
  stats: DashboardStats;
}

const StatsPanel: React.FC<StatsPanelProps> = ({ stats }) => {
  return (
    <div className="stats-panel">
      <div className="stats-grid">
        <div className="stat-card active">
          <div className="stat-icon">⚡</div>
          <div className="stat-info">
            <span className="stat-value">{stats.totalActive}</span>
            <span className="stat-label">Active Alerts</span>
          </div>
        </div>

        <div className="stat-card pending">
          <div className="stat-icon">⏳</div>
          <div className="stat-info">
            <span className="stat-value">{stats.totalPending}</span>
            <span className="stat-label">Pending</span>
          </div>
        </div>

        <div className="stat-card resolved">
          <div className="stat-icon">✓</div>
          <div className="stat-info">
            <span className="stat-value">{stats.totalResolved}</span>
            <span className="stat-label">Resolved</span>
          </div>
        </div>

        <div className="stat-card type-stat">
          <div className="stat-icon">🌍</div>
          <div className="stat-info">
            <span className="stat-value">{stats.activeEarthquakes}</span>
            <span className="stat-label">Earthquakes</span>
          </div>
        </div>

        <div className="stat-card type-stat">
          <div className="stat-icon">🌊</div>
          <div className="stat-info">
            <span className="stat-value">{stats.activeFloods}</span>
            <span className="stat-label">Floods</span>
          </div>
        </div>

        <div className="stat-card type-stat">
          <div className="stat-icon">🌀</div>
          <div className="stat-info">
            <span className="stat-value">{stats.activeCyclones}</span>
            <span className="stat-label">Cyclones</span>
          </div>
        </div>

        <div className="stat-card type-stat">
          <div className="stat-icon">🔥</div>
          <div className="stat-info">
            <span className="stat-value">{stats.activeWildfires}</span>
            <span className="stat-label">Wildfires</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default StatsPanel;
