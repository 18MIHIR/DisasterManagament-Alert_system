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

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    onError('');
    try {
      await reportApi.create(details, undefined, location || undefined);
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
