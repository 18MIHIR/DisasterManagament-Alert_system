import React, { useCallback, useEffect, useRef, useState } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { disasterApi, reportApi, rescueTaskApi, rescueZoneApi, userApi } from '../api';
import { Disaster, Report, RescueTask, RescueZone, UserProfile } from '../types';
import ResponderTasks from './ResponderTasks';
import './RescueOperations.css';

interface RescueOperationsProps {
  userRole: string | null;
}

const CitizenRoutedList: React.FC = () => {
  const [rows, setRows] = useState<Report[]>([]);
  useEffect(() => {
    reportApi.getAssignedToMe().then(setRows).catch(() => setRows([]));
  }, []);
  if (rows.length === 0) {
    return <p className="muted">No citizen requests routed to you yet.</p>;
  }
  return (
    <ul className="routed-list">
      {rows.map(r => (
        <li key={r.id}>
          <span className="t">{new Date(r.submittedAt).toLocaleString()}</span>
          <span className="d">{r.details}</span>
          <span className="l">{r.location}</span>
        </li>
      ))}
    </ul>
  );
};

const RescueOperations: React.FC<RescueOperationsProps> = ({ userRole }) => {
  const isAdmin = userRole === 'ADMIN';
  const isResponder = userRole === 'RESPONDER';

  const [tab, setTab] = useState<'tasks' | 'incidents'>('tasks');
  const [zones, setZones] = useState<RescueZone[]>([]);
  const [responders, setResponders] = useState<UserProfile[]>([]);
  const [disasters, setDisasters] = useState<Disaster[]>([]);
  const [tasks, setTasks] = useState<RescueTask[]>([]);

  const [zoneId, setZoneId] = useState<number | ''>('');
  const [disasterId, setDisasterId] = useState<number | ''>('');
  const [responderId, setResponderId] = useState<number | ''>('');
  const [taskDescription, setTaskDescription] = useState('');
  const [assigning, setAssigning] = useState(false);

  const [incidentDetails, setIncidentDetails] = useState('');
  const [incidentLocation, setIncidentLocation] = useState('');
  const [incidentDisasterId, setIncidentDisasterId] = useState<number | ''>('');
  const [incidentTaskId, setIncidentTaskId] = useState<number | ''>('');
  const [incidentImage, setIncidentImage] = useState<string | null>(null);
  const [submittingIncident, setSubmittingIncident] = useState(false);

  const [audit, setAudit] = useState<Report[]>([]);
  const [selectedReport, setSelectedReport] = useState<Report | null>(null);

  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const mapRef = useRef<HTMLDivElement | null>(null);
  const leafletMap = useRef<L.Map | null>(null);
  const markersLayer = useRef<L.LayerGroup | null>(null);

  const loadAll = useCallback(async () => {
    setError(null);
    try {
      const [z, d] = await Promise.all([rescueZoneApi.list(), disasterApi.getActive()]);
      setZones(z);
      setDisasters(d);

      if (isAdmin) {
        const [t, resp, repAudit] = await Promise.all([
          rescueTaskApi.listActiveForMap().catch(() => [] as RescueTask[]),
          userApi.getResponders(),
          reportApi.audit().catch(() => [] as Report[]),
        ]);
        setTasks(t);
        setResponders(resp);
        setAudit(repAudit);
      } else {
        const mine = await rescueTaskApi.getMyTasks().catch(() => [] as RescueTask[]);
        setTasks(mine);
      }
    } catch (e: any) {
      setError(e.message || 'Failed to load rescue operations data');
    }
  }, [isAdmin]);

  useEffect(() => {
    if (isAdmin || isResponder) {
      loadAll();
      const id = setInterval(loadAll, 45000);
      return () => clearInterval(id);
    }
  }, [isAdmin, isResponder, loadAll]);

  const filteredDisasters = React.useMemo(() => {
    if (!zoneId) return disasters;
    const z = zones.find(x => x.id === zoneId);
    if (!z) return disasters;
    return disasters.filter(
      dis => !dis.region || dis.region.toLowerCase().includes(z.region.toLowerCase())
    );
  }, [disasters, zoneId, zones]);

  useEffect(() => {
    if (!mapRef.current || (!isAdmin && !isResponder)) return;

    if (!leafletMap.current) {
      leafletMap.current = L.map(mapRef.current).setView([20.5937, 78.9629], 5);
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap',
      }).addTo(leafletMap.current);
      markersLayer.current = L.layerGroup().addTo(leafletMap.current);
    }

    const map = leafletMap.current;
    const layer = markersLayer.current;
    if (!map || !layer) return;

    layer.clearLayers();

    const points: L.LatLngExpression[] = [];
    tasks.forEach(task => {
      const lat = task.rescueSiteLatitude ?? task.disasterLatitude;
      const lng = task.rescueSiteLongitude ?? task.disasterLongitude;
      if (lat == null || lng == null) return;
      const color =
        task.taskStatus === 'IN_PROGRESS'
          ? '#a855f7'
          : task.taskStatus === 'ACKNOWLEDGED'
          ? '#fbbf24'
          : '#22c55e';
      const circle = L.circleMarker([lat, lng], {
        radius: 9,
        color: '#0b0b0b',
        weight: 2,
        fillColor: color,
        fillOpacity: 0.9,
      });
      circle.bindPopup(
        `<strong>${task.disasterTitle}</strong><br/>` +
          `${task.taskStatus.replace('_', ' ')} · ${task.responderName}<br/>` +
          `<small>${task.description || ''}</small>`
      );
      circle.addTo(layer);
      points.push([lat, lng]);
    });

    if (isAdmin) {
      zones.forEach(z => {
        const zm = L.circleMarker([z.centerLatitude, z.centerLongitude], {
          radius: 6,
          color: '#94a3b8',
          weight: 1,
          fillColor: '#64748b',
          fillOpacity: 0.35,
        });
        zm.bindPopup(`<strong>Zone:</strong> ${z.name}<br/>${z.region}`);
        zm.addTo(layer);
        points.push([z.centerLatitude, z.centerLongitude]);
      });
    }

    if (points.length > 0) {
      map.fitBounds(L.latLngBounds(points), { padding: [36, 36], maxZoom: 12 });
    }
  }, [tasks, zones, isAdmin, isResponder]);

  useEffect(() => {
    return () => {
      leafletMap.current?.remove();
      leafletMap.current = null;
      markersLayer.current = null;
    };
  }, []);

  const handleAssign = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!disasterId || !responderId) return;
    setAssigning(true);
    setMessage(null);
    setError(null);
    try {
      let rLat: number | undefined;
      let rLng: number | undefined;
      if (zoneId) {
        const z = zones.find(x => x.id === zoneId);
        if (z) {
          rLat = z.centerLatitude;
          rLng = z.centerLongitude;
        }
      }
      await rescueTaskApi.create(
        Number(responderId),
        Number(disasterId),
        taskDescription || undefined,
        zoneId ? Number(zoneId) : undefined,
        rLat,
        rLng
      );
      setTaskDescription('');
      setMessage('Task assigned successfully.');
      await loadAll();
    } catch (err: any) {
      setError(err.message || 'Assignment failed');
    } finally {
      setAssigning(false);
    }
  };

  const onImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) {
      setIncidentImage(null);
      return;
    }
    if (file.size > 512 * 1024) {
      setError('Image must be under 512 KB');
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      const result = reader.result;
      if (typeof result === 'string') setIncidentImage(result);
    };
    reader.readAsDataURL(file);
  };

  const handleIncident = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isResponder) return;
    setSubmittingIncident(true);
    setError(null);
    setMessage(null);
    try {
      await reportApi.createIncident({
        details: incidentDetails,
        location: incidentLocation || undefined,
        disasterId: incidentDisasterId ? Number(incidentDisasterId) : undefined,
        rescueTaskId: incidentTaskId ? Number(incidentTaskId) : undefined,
        imageData: incidentImage || undefined,
      });
      setIncidentDetails('');
      setIncidentLocation('');
      setIncidentDisasterId('');
      setIncidentTaskId('');
      setIncidentImage(null);
      setMessage('Incident report submitted with timestamp for audit.');
      await loadAll();
    } catch (err: any) {
      setError(err.message || 'Failed to submit incident report');
    } finally {
      setSubmittingIncident(false);
    }
  };

  const openReportImage = async (r: Report) => {
    try {
      const full = await reportApi.getById(r.id);
      setSelectedReport(full);
    } catch {
      setError('Could not load report details');
    }
  };

  if (!isAdmin && !isResponder) {
    return (
      <div className="rescue-ops-unauthorized">
        <p>Rescue Operations is available to administrators and responders.</p>
      </div>
    );
  }

  return (
    <div className="rescue-ops">
      <div className="rescue-ops-header">
        <h1>Rescue Operations</h1>
        <p className="rescue-ops-sub">
          Module 4 — task assignment, live map, citizen routing, and incident reporting.
        </p>
      </div>

      <div className="rescue-ops-tabs">
        <button
          type="button"
          className={`ro-tab ${tab === 'tasks' ? 'active' : ''}`}
          onClick={() => setTab('tasks')}
        >
          {isAdmin ? 'Task Assignment' : 'My Tasks & Progress'}
        </button>
        <button
          type="button"
          className={`ro-tab ${tab === 'incidents' ? 'active' : ''}`}
          onClick={() => setTab('incidents')}
        >
          Incident Reporting
        </button>
      </div>

      {error && <div className="rescue-ops-banner error">{error}</div>}
      {message && <div className="rescue-ops-banner success">{message}</div>}

      {isResponder && tab === 'tasks' && (
        <div className="rescue-ops-panel">
          <ResponderTasks />
        </div>
      )}

      {isAdmin && tab === 'tasks' && (
        <section className="rescue-ops-section">
          <h2>Assign Rescue Tasks</h2>
          <form className="rescue-ops-form" onSubmit={handleAssign}>
            <label>
              Select zone
              <select
                value={zoneId}
                onChange={e => setZoneId(e.target.value ? Number(e.target.value) : '')}
              >
                <option value="">Optional — filter disasters &amp; default site coords</option>
                {zones.map(z => (
                  <option key={z.id} value={z.id}>
                    {z.name} ({z.region})
                  </option>
                ))}
              </select>
            </label>
            <label>
              Active disaster *
              <select
                value={disasterId}
                onChange={e => setDisasterId(e.target.value ? Number(e.target.value) : '')}
                required
              >
                <option value="">Select disaster</option>
                {filteredDisasters.map(d => (
                  <option key={d.id} value={d.id}>
                    {d.title} — {d.location} ({d.region})
                  </option>
                ))}
              </select>
            </label>
            <label>
              Responder *
              <select
                value={responderId}
                onChange={e => setResponderId(e.target.value ? Number(e.target.value) : '')}
                required
              >
                <option value="">Select responder</option>
                {responders.map(r => (
                  <option key={r.id} value={r.id}>
                    {r.name} ({r.region})
                  </option>
                ))}
              </select>
            </label>
            <label>
              Task description
              <textarea
                rows={4}
                value={taskDescription}
                onChange={e => setTaskDescription(e.target.value)}
                placeholder="Instructions for the responder team..."
              />
            </label>
            <button type="submit" className="ro-primary" disabled={assigning}>
              {assigning ? 'Assigning…' : 'Assign Task'}
            </button>
          </form>
        </section>
      )}

      {((isAdmin && tab === 'tasks') || (isResponder && tab === 'tasks') || tab === 'incidents') && (
        <section className="rescue-ops-section map-section">
          <h2>Active Rescue Operations</h2>
          <p className="map-legend">
            <span className="lg lg-green" /> Assigned
            <span className="lg lg-amber" /> Acknowledged
            <span className="lg lg-purple" /> In progress
            {isAdmin && <span className="lg lg-zone" />}
            {isAdmin && ' Zone center'}
          </p>
          <div className="rescue-map-wrap" ref={mapRef} />
        </section>
      )}

      {tab === 'incidents' && isResponder && (
        <section className="rescue-ops-section">
          <h2>Submit Incident Report</h2>
          <p className="hint">
            Status updates are timestamped. Optional image must be a small JPEG/PNG (under 512 KB).
          </p>
          <form className="rescue-ops-form" onSubmit={handleIncident}>
            <label>
              Linked task (optional)
              <select
                value={incidentTaskId}
                onChange={e => setIncidentTaskId(e.target.value ? Number(e.target.value) : '')}
              >
                <option value="">None</option>
                {tasks.map(task => (
                  <option key={task.id} value={task.id}>
                    #{task.id} — {task.disasterTitle} ({task.taskStatus})
                  </option>
                ))}
              </select>
            </label>
            <label>
              Related disaster (optional)
              <select
                value={incidentDisasterId}
                onChange={e => setIncidentDisasterId(e.target.value ? Number(e.target.value) : '')}
              >
                <option value="">None</option>
                {disasters.map(d => (
                  <option key={d.id} value={d.id}>
                    {d.title}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Location
              <input
                value={incidentLocation}
                onChange={e => setIncidentLocation(e.target.value)}
                placeholder="On-site description"
              />
            </label>
            <label>
              Details *
              <textarea
                required
                rows={5}
                value={incidentDetails}
                onChange={e => setIncidentDetails(e.target.value)}
                placeholder="Situation, casualties, resources needed..."
              />
            </label>
            <label className="file-label">
              Photo (optional)
              <input type="file" accept="image/*" onChange={onImageChange} />
            </label>
            {incidentImage && (
              <div className="img-preview">
                <img src={incidentImage} alt="Preview" />
              </div>
            )}
            <button type="submit" className="ro-primary" disabled={submittingIncident}>
              {submittingIncident ? 'Submitting…' : 'Submit Incident Report'}
            </button>
          </form>
        </section>
      )}

      {tab === 'incidents' && isAdmin && (
        <section className="rescue-ops-section">
          <h2>Report audit trail</h2>
          <p className="hint">All citizen emergency requests and responder incident reports with timestamps.</p>
          <div className="audit-table-wrap">
            <table className="audit-table">
              <thead>
                <tr>
                  <th>Time</th>
                  <th>Kind</th>
                  <th>From</th>
                  <th>Summary</th>
                  <th>Routed to</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {audit.map(r => (
                  <tr key={r.id}>
                    <td>{new Date(r.submittedAt).toLocaleString()}</td>
                    <td>{r.reportKind || 'EMERGENCY_REQUEST'}</td>
                    <td>{r.submittedByName || '—'}</td>
                    <td>
                      {r.details.slice(0, 80)}
                      {r.details.length > 80 ? '…' : ''}
                    </td>
                    <td>{r.responderName || '—'}</td>
                    <td>
                      {r.hasImage && (
                        <button type="button" className="linkish" onClick={() => openReportImage(r)}>
                          View image
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {audit.length === 0 && <p className="empty-audit">No reports yet.</p>}
          </div>
        </section>
      )}

      {tab === 'incidents' && isResponder && (
        <section className="rescue-ops-section narrow">
          <h3>My routed citizen requests</h3>
          <CitizenRoutedList />
        </section>
      )}

      {selectedReport && (
        <div
          className="report-modal-overlay"
          onClick={() => setSelectedReport(null)}
          role="presentation"
        >
          <div
            className="report-modal"
            onClick={e => e.stopPropagation()}
            role="dialog"
            aria-modal="true"
          >
            <button type="button" className="close-x" onClick={() => setSelectedReport(null)}>
              ×
            </button>
            {selectedReport.imageData && (
              <img src={selectedReport.imageData} alt="Report attachment" className="report-img" />
            )}
            <p className="report-meta">
              {new Date(selectedReport.submittedAt).toLocaleString()} · {selectedReport.reportKind}
            </p>
            <p>{selectedReport.details}</p>
          </div>
        </div>
      )}
    </div>
  );
};

export default RescueOperations;
