import React, { useState } from 'react';
import { reportApi } from '../api';
import './ReportDisasterForm.css';

interface ReportEmergencyFormProps {
  onClose: () => void;
  onSuccess: () => void;
  onError: (msg: string) => void;
}

const ReportEmergencyForm: React.FC<ReportEmergencyFormProps> = ({ onClose, onSuccess, onError }) => {
  const [loading, setLoading] = useState(false);
  const [details, setDetails] = useState('');
  const [location, setLocation] = useState('');
  const [latitude, setLatitude] = useState<number | undefined>(undefined);
  const [longitude, setLongitude] = useState<number | undefined>(undefined);
  const [locating, setLocating] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    onError('');
    try {
      await reportApi.create(
        details,
        undefined,
        location || undefined,
        latitude,
        longitude
      );
      onSuccess();
    } catch (err: any) {
      onError(err.message || 'Failed to submit emergency request');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="report-modal-overlay" onClick={onClose}>
      <div className="report-modal" onClick={e => e.stopPropagation()}>
        <div className="report-modal-header">
          <h2>Request Emergency Help</h2>
          <button className="report-modal-close" onClick={onClose}>×</button>
        </div>
        <p className="report-modal-hint">Submit your emergency help request. It will be routed to the nearest available responder in your region.</p>
        <form onSubmit={handleSubmit} className="report-form">
          <div className="report-form-group">
            <label>Location *</label>
            <input
              value={location}
              onChange={e => setLocation(e.target.value)}
              placeholder="e.g. Near Central Park, Mumbai"
              required
            />
            <button
              type="button"
              className="report-geo-btn"
              disabled={locating}
              onClick={() => {
                if (!navigator.geolocation) {
                  onError('Geolocation is not supported by this browser');
                  return;
                }
                setLocating(true);
                navigator.geolocation.getCurrentPosition(
                  pos => {
                    setLatitude(pos.coords.latitude);
                    setLongitude(pos.coords.longitude);
                    setLocating(false);
                  },
                  () => {
                    setLocating(false);
                    onError('Could not read your location; you can still submit with text address.');
                  },
                  { enableHighAccuracy: true, timeout: 12000 }
                );
              }}
            >
              {locating ? 'Getting location…' : 'Use my location (improves routing)'}
            </button>
            {latitude != null && longitude != null && (
              <p className="report-coords-hint">
                Coordinates captured for routing: {latitude.toFixed(5)}, {longitude.toFixed(5)}
              </p>
            )}
          </div>
          <div className="report-form-group">
            <label>Description of emergency *</label>
            <textarea
              value={details}
              onChange={e => setDetails(e.target.value)}
              placeholder="Describe your situation, number of people affected, specific needs..."
              rows={4}
              required
            />
          </div>
          <div className="report-form-actions">
            <button type="button" className="report-btn cancel" onClick={onClose}>Cancel</button>
            <button type="submit" className="report-btn submit" disabled={loading}>
              {loading ? 'Submitting...' : 'Submit Request'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ReportEmergencyForm;
