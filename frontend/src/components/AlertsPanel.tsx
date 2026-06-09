import React, { useEffect, useState } from 'react';
import { alertApi } from '../api';
import { Alert } from '../types';
import './AlertsPanel.css';

const AlertsPanel: React.FC = () => {
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const data = await alertApi.getForCitizen();
        setAlerts(data);
      } catch {
        setAlerts([]);
      } finally {
        setLoading(false);
      }
    };
    load();
    const interval = setInterval(load, 30000);
    return () => clearInterval(interval);
  }, []);

  if (loading) return null;

  return (
    <div className="alerts-panel">
      <h3 className="alerts-panel-title">Alerts for Your Region</h3>
      {alerts.length === 0 ? (
        <div className="alerts-empty">No active alerts for your region right now.</div>
      ) : (
        <ul className="alerts-list">
          {alerts.map(alert => (
            <li key={alert.id} className="alert-item">
              <div className="alert-header">
                <span className="alert-disaster">{alert.disasterTitle}</span>
                <span className="alert-time">{new Date(alert.broadcastTime).toLocaleString()}</span>
              </div>
              <p className="alert-message">{alert.message}</p>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default AlertsPanel;
