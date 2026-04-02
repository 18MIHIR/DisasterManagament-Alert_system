import React, { useEffect, useState } from 'react';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, Legend, ResponsiveContainer,
  BarChart, Bar,
  PieChart, Pie, Cell
} from 'recharts';
import {
  DisasterTrendDTO, RegionPerformanceDTO,
  ResponderPerformanceDTO, AlertEngagementDTO
} from '../types';
import './Analytics.css';

interface AnalyticsProps {
  userRole: string | null;
}

const API_BASE_URL = 'http://localhost:8080/api/analytics';

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884d8'];

const Analytics: React.FC<AnalyticsProps> = ({ userRole }) => {
  const [trends, setTrends] = useState<DisasterTrendDTO[]>([]);
  const [regionPerformance, setRegionPerformance] = useState<RegionPerformanceDTO[]>([]);
  const [responderStats, setResponderStats] = useState<ResponderPerformanceDTO[]>([]);
  const [alertEngagement, setAlertEngagement] = useState<AlertEngagementDTO | null>(null);
  
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchAnalytics = async () => {
      const token = localStorage.getItem('dm_token');
      if (!token) return;

      const headers = { Authorization: `Bearer ${token}` };

      try {
        const [trendsRes, regionsRes, respondersRes, alertsRes] = await Promise.all([
          fetch(`${API_BASE_URL}/disaster-trends`, { headers }),
          fetch(`${API_BASE_URL}/region-performance`, { headers }),
          fetch(`${API_BASE_URL}/responder-performance`, { headers }),
          fetch(`${API_BASE_URL}/alert-insights`, { headers })
        ]);

        if (trendsRes.ok) setTrends(await trendsRes.json());
        if (regionsRes.ok) setRegionPerformance(await regionsRes.json());
        if (respondersRes.ok) setResponderStats(await respondersRes.json());
        if (alertsRes.ok) setAlertEngagement(await alertsRes.json());
      } catch (e) {
        console.error("Failed to load analytics", e);
      } finally {
        setLoading(false);
      }
    };

    fetchAnalytics();
  }, []);

  if (loading) {
    return <div className="analytics-loading">Loading Analytics Data...</div>;
  }

  const alertPieData = alertEngagement ? [
    { name: 'Acknowledged', value: alertEngagement.totalAcknowledged },
    { name: 'Ignored', value: alertEngagement.totalIgnored }
  ] : [];

  return (
    <div className="analytics-container fade-in">
      <header className="analytics-header">
        <h1>Analytics & Dashboard</h1>
        <p>Comprehensive overview of system metrics and performance.</p>
      </header>

      <div className="analytics-grid">
        {/* Disaster Trends (Line Chart) */}
        <div className="analytics-card span-2">
          <h2>Disaster Trends</h2>
          <p className="card-subtitle">Monthly handled disaster cases</p>
          <div className="chart-container">
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={trends} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" opacity={0.1} />
                <XAxis dataKey="monthYear" stroke="#888" />
                <YAxis stroke="#888" />
                <RechartsTooltip contentStyle={{ backgroundColor: '#1a1d24', border: 'none', borderRadius: '8px' }} />
                <Legend />
                <Line type="monotone" dataKey="count" stroke="#00C49F" strokeWidth={3} dot={{ r: 6 }} activeDot={{ r: 8 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Region Performance */}
        <div className="analytics-card">
          <h2>Region Efficiency</h2>
          <p className="card-subtitle">Resolution efficiency vs total disasters</p>
          <div className="chart-container">
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={regionPerformance} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" opacity={0.1} />
                <XAxis dataKey="region" stroke="#888" />
                <YAxis stroke="#888" />
                <RechartsTooltip contentStyle={{ backgroundColor: '#1a1d24', border: 'none', borderRadius: '8px' }} />
                <Legend />
                <Bar dataKey="resolutionEfficiency" fill="#8884d8" name="Efficiency (%)" radius={[4, 4, 0, 0]} />
                <Bar dataKey="totalDisasters" fill="#FFBB28" name="Disasters" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Alert Engagement */}
        <div className="analytics-card">
          <h2>Notification Insights</h2>
          <p className="card-subtitle">User engagement with emergency alerts</p>
          <div className="chart-container pie-container">
            <ResponsiveContainer width="100%" height={250}>
              <PieChart>
                <Pie
                  data={alertPieData}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={90}
                  paddingAngle={5}
                  dataKey="value"
                >
                  {alertPieData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <RechartsTooltip contentStyle={{ backgroundColor: '#1a1d24', border: 'none', borderRadius: '8px' }} />
                <Legend verticalAlign="bottom" height={36} />
              </PieChart>
            </ResponsiveContainer>
            {alertEngagement && (
              <div className="alert-stats">
                <div className="stat-pill">Broadcast: <strong>{alertEngagement.totalBroadcasted}</strong></div>
                <div className="stat-pill success">Ack: <strong>{alertEngagement.totalAcknowledged}</strong></div>
              </div>
            )}
          </div>
        </div>

        {/* Responder Metrics Table */}
        <div className="analytics-card span-2">
          <h2>Responder Activity</h2>
          <p className="card-subtitle">Completion rates by top responders</p>
          <div className="table-responsive">
            <table className="premium-table">
              <thead>
                <tr>
                  <th>Responder</th>
                  <th>Assigned</th>
                  <th>Completed</th>
                  <th>Success Rate</th>
                </tr>
              </thead>
              <tbody>
                {responderStats.map(stat => (
                  <tr key={stat.responderName}>
                    <td>{stat.responderName}</td>
                    <td>{stat.totalAssignedTasks}</td>
                    <td>{stat.completedTasks}</td>
                    <td>
                      <div className="progress-bar-container">
                        <div 
                           className="progress-fill" 
                           style={{ width: `${stat.completionRate}%`, backgroundColor: stat.completionRate > 80 ? '#00C49F' : '#FFBB28' }}
                        ></div>
                        <span className="progress-text">{stat.completionRate.toFixed(1)}%</span>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Analytics;
