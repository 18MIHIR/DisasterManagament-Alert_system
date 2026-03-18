import React, { useEffect, useState, useCallback } from 'react';
import { disasterApi, rescueTaskApi, userApi } from '../api';
import { Disaster, DashboardStats, DisasterFilters, UserProfile } from '../types';
import DisasterCard from './DisasterCard';
import DisasterFiltersPanel from './DisasterFilters';
import StatsPanel from './StatsPanel';
import ReportDisasterForm from './ReportDisasterForm';
import ReportEmergencyForm from './ReportEmergencyForm';
import ResponderTasks from './ResponderTasks';
import AlertsPanel from './AlertsPanel';
import './Dashboard.css';

interface DashboardProps {
  userRole: string | null;
}

const Dashboard: React.FC<DashboardProps> = ({ userRole }) => {
  const [disasters, setDisasters] = useState<Disaster[]>([]);
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFilters] = useState<DisasterFilters>({
    type: '',
    severity: '',
    status: '',
    region: '',
  });
  const [view, setView] = useState<'all' | 'active' | 'pending'>('active');
  const [selectedDisaster, setSelectedDisaster] = useState<Disaster | null>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [showReportForm, setShowReportForm] = useState(false);
  const [showEmergencyForm, setShowEmergencyForm] = useState(false);
  const [responders, setResponders] = useState<UserProfile[]>([]);
  const [assignResponderId, setAssignResponderId] = useState<number | ''>('');
  const [assigning, setAssigning] = useState(false);

  const loadData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [statsData, disastersData] = await Promise.all([
        disasterApi.getStats(),
        view === 'active' 
          ? disasterApi.getActive()
          : view === 'pending'
          ? disasterApi.getPending()
          : disasterApi.getAll(filters),
      ]);
      setStats(statsData);
      setDisasters(disastersData);
    } catch (err: any) {
      setError(err.message || 'Failed to load data');
    } finally {
      setLoading(false);
    }
  }, [view, filters]);

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 60000);
    return () => clearInterval(interval);
  }, [loadData]);

  useEffect(() => {
    if (userRole === 'ADMIN' && selectedDisaster?.status === 'ACTIVE') {
      userApi.getResponders().then(setResponders).catch(() => setResponders([]));
    } else {
      setResponders([]);
    }
    setAssignResponderId('');
  }, [userRole, selectedDisaster?.id, selectedDisaster?.status]);

  const handleFilterChange = (key: keyof DisasterFilters, value: string) => {
    setFilters(prev => ({ ...prev, [key]: value }));
    if (view !== 'all') setView('all');
  };

  const clearFilters = () => {
    setFilters({ type: '', severity: '', status: '', region: '' });
  };

  const handleVerify = async (id: number) => {
    try {
      await disasterApi.verify(id);
      loadData();
      setSelectedDisaster(null);
    } catch (err: any) {
      setError(err.message || 'Failed to verify');
    }
  };

  const handleResolve = async (id: number) => {
    try {
      await disasterApi.resolve(id);
      loadData();
      setSelectedDisaster(null);
    } catch (err: any) {
      setError(err.message || 'Failed to resolve');
    }
  };

  const handleCancel = async (id: number) => {
    try {
      await disasterApi.cancel(id);
      loadData();
      setSelectedDisaster(null);
    } catch (err: any) {
      setError(err.message || 'Failed to cancel');
    }
  };

  const handleRemoveAllPending = async () => {
    if (!window.confirm(`Remove all ${stats?.totalPending || 0} pending alerts? This cannot be undone.`)) return;
    try {
      const result = await disasterApi.deleteAllPending();
      loadData();
      setSelectedDisaster(null);
      setError(null);
    } catch (err: any) {
      setError(err.message || 'Failed to remove pending alerts');
    }
  };

  const handleAssignTask = async () => {
    if (!selectedDisaster || !assignResponderId) return;
    setAssigning(true);
    try {
      await rescueTaskApi.create(Number(assignResponderId), selectedDisaster.id);
      setAssignResponderId('');
      setError(null);
    } catch (err: any) {
      setError(err.message || 'Failed to assign task');
    } finally {
      setAssigning(false);
    }
  };

  const handleRefreshExternal = async () => {
    setRefreshing(true);
    try {
      await disasterApi.triggerFetch();
      setTimeout(loadData, 2000);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch external data');
    } finally {
      setRefreshing(false);
    }
  };

  const isAdmin = userRole === 'ADMIN';
  const isResponder = userRole === 'RESPONDER';
  const isCitizen = userRole === 'CITIZEN';
  const canReport = isAdmin || isResponder;

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h1>Disaster Monitoring Dashboard</h1>
        <div className="header-actions">
          {isCitizen && (
            <button 
              className="refresh-btn report-btn emergency"
              onClick={() => { setError(null); setShowEmergencyForm(true); }}
            >
              Request Emergency Help
            </button>
          )}
          {canReport && (
            <button 
              className="refresh-btn report-btn"
              onClick={() => { setError(null); setShowReportForm(true); }}
            >
              Report Disaster
            </button>
          )}
          {isAdmin && (
            <button 
              className="refresh-btn"
              onClick={handleRefreshExternal}
              disabled={refreshing}
            >
              {refreshing ? 'Fetching...' : 'Fetch External Data'}
            </button>
          )}
          <button className="refresh-btn" onClick={loadData} disabled={loading}>
            {loading ? 'Loading...' : 'Refresh'}
          </button>
        </div>
      </div>

      {error && <div className="dashboard-error">{error}</div>}

      {isCitizen && <AlertsPanel />}
      {isResponder && <ResponderTasks />}

      {stats && <StatsPanel stats={stats} />}

      <div className="dashboard-controls">
        {isAdmin && view === 'pending' && (stats?.totalPending || 0) > 0 && (
          <button
            className="remove-pending-btn"
            onClick={handleRemoveAllPending}
          >
            Remove All Pending ({stats?.totalPending || 0})
          </button>
        )}
        <div className="view-tabs">
          <button 
            className={`tab ${view === 'active' ? 'active' : ''}`}
            onClick={() => setView('active')}
          >
            Active Alerts ({stats?.totalActive || 0})
          </button>
          {(isAdmin || isResponder) && (
            <button 
              className={`tab ${view === 'pending' ? 'active' : ''}`}
              onClick={() => setView('pending')}
            >
              Pending Verification ({stats?.totalPending || 0})
            </button>
          )}
          <button 
            className={`tab ${view === 'all' ? 'active' : ''}`}
            onClick={() => setView('all')}
          >
            All Disasters
          </button>
        </div>
      </div>

      {view === 'all' && (
        <DisasterFiltersPanel 
          filters={filters} 
          onChange={handleFilterChange}
          onClear={clearFilters}
        />
      )}

      <div className="disasters-grid">
        {loading ? (
          <div className="loading-state">Loading disasters...</div>
        ) : disasters.length === 0 ? (
          <div className="empty-state">No disasters found</div>
        ) : (
          disasters.map(disaster => (
            <DisasterCard 
              key={disaster.id}
              disaster={disaster}
              onClick={() => setSelectedDisaster(disaster)}
              isAdmin={isAdmin}
            />
          ))
        )}
      </div>

      {selectedDisaster && (
        <div className="modal-overlay" onClick={() => setSelectedDisaster(null)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <button className="modal-close" onClick={() => setSelectedDisaster(null)}>×</button>
            <div className="disaster-detail">
              <div className="detail-header">
                <span className={`severity-badge ${selectedDisaster.severity.toLowerCase()}`}>
                  {selectedDisaster.severity}
                </span>
                <span className={`status-badge ${selectedDisaster.status.toLowerCase()}`}>
                  {selectedDisaster.status}
                </span>
                <span className="type-badge">{selectedDisaster.type.replace('_', ' ')}</span>
              </div>
              
              <h2>{selectedDisaster.title}</h2>
              
              <div className="detail-section">
                <h4>Description</h4>
                <p>{selectedDisaster.description || 'No description available'}</p>
              </div>

              <div className="detail-grid">
                <div className="detail-item">
                  <label>Location</label>
                  <span>{selectedDisaster.location}</span>
                </div>
                <div className="detail-item">
                  <label>Region</label>
                  <span>{selectedDisaster.region || 'N/A'}</span>
                </div>
                <div className="detail-item">
                  <label>Event Time</label>
                  <span>{new Date(selectedDisaster.eventTime).toLocaleString()}</span>
                </div>
                <div className="detail-item">
                  <label>Source</label>
                  <span>
                    {selectedDisaster.source === 'RESPONDER_REPORT'
                      ? 'Reported by Responder – Awaiting Admin review'
                      : selectedDisaster.source || 'Manual Entry'}
                  </span>
                </div>
                {selectedDisaster.latitude && selectedDisaster.longitude && (
                  <div className="detail-item">
                    <label>Coordinates</label>
                    <span>{selectedDisaster.latitude.toFixed(4)}, {selectedDisaster.longitude.toFixed(4)}</span>
                  </div>
                )}
                {selectedDisaster.estimatedAffectedPopulation && (
                  <div className="detail-item">
                    <label>Affected Population</label>
                    <span>{selectedDisaster.estimatedAffectedPopulation.toLocaleString()}</span>
                  </div>
                )}
              </div>

              {selectedDisaster.advisoryMessage && (
                <div className="detail-section">
                  <h4>Advisory</h4>
                  <p className="advisory-text">{selectedDisaster.advisoryMessage}</p>
                </div>
              )}

              {selectedDisaster.alertBroadcastAt && (
                <div className="detail-item">
                  <label>Alert Broadcast</label>
                  <span>{new Date(selectedDisaster.alertBroadcastAt).toLocaleString()}</span>
                </div>
              )}

              {isAdmin && selectedDisaster.status === 'ACTIVE' && responders.length > 0 && (
                <div className="admin-actions assign-task">
                  <h4>Assign Rescue Task</h4>
                  <div className="assign-task-row">
                    <select
                      value={assignResponderId}
                      onChange={e => setAssignResponderId(e.target.value ? Number(e.target.value) : '')}
                      className="assign-select"
                    >
                      <option value="">Select responder</option>
                      {responders.map(r => (
                        <option key={r.id} value={r.id}>{r.name} ({r.region})</option>
                      ))}
                    </select>
                    <button
                      className="action-btn verify"
                      onClick={handleAssignTask}
                      disabled={!assignResponderId || assigning}
                    >
                      {assigning ? 'Assigning...' : 'Assign Task'}
                    </button>
                  </div>
                </div>
              )}

              {selectedDisaster.source === 'RESPONDER_REPORT' && (
                <div className="detail-item">
                  <label>Source</label>
                  <span>Reported by Responder – awaiting Admin review</span>
                </div>
              )}
              {isAdmin && (
                <div className="admin-actions">
                  {selectedDisaster.status === 'PENDING' && (
                    <>
                      <button 
                        className="action-btn verify"
                        onClick={() => handleVerify(selectedDisaster.id)}
                      >
                        Verify & Push Alert
                      </button>
                      <button 
                        className="action-btn cancel"
                        onClick={() => handleCancel(selectedDisaster.id)}
                      >
                        Cancel Alert
                      </button>
                    </>
                  )}
                  {selectedDisaster.status === 'ACTIVE' && (
                    <button 
                      className="action-btn resolve"
                      onClick={() => handleResolve(selectedDisaster.id)}
                    >
                      Mark as Resolved
                    </button>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {showReportForm && (
        <ReportDisasterForm
          onClose={() => setShowReportForm(false)}
          onSuccess={() => {
            setShowReportForm(false);
            loadData();
          }}
          onError={(msg) => setError(msg)}
        />
      )}
      {showEmergencyForm && (
        <ReportEmergencyForm
          onClose={() => setShowEmergencyForm(false)}
          onSuccess={() => {
            setShowEmergencyForm(false);
            setError(null);
          }}
          onError={(msg) => setError(msg)}
        />
      )}
    </div>
  );
};

export default Dashboard;
