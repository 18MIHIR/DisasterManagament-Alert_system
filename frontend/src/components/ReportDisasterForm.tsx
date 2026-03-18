import React, { useState } from 'react';
import { disasterApi } from '../api';
import { DisasterType, DisasterSeverity } from '../types';
import './ReportDisasterForm.css';

interface ReportDisasterFormProps {
  onClose: () => void;
  onSuccess: () => void;
  onError: (msg: string) => void;
}

const disasterTypes: DisasterType[] = [
  'FLOOD', 'CYCLONE', 'EARTHQUAKE', 'TSUNAMI', 'WILDFIRE',
  'LANDSLIDE', 'DROUGHT', 'TORNADO', 'SEVERE_STORM',
  'HEATWAVE', 'COLDWAVE', 'VOLCANIC_ERUPTION', 'OTHER'
];

const severities: DisasterSeverity[] = ['LOW', 'MODERATE', 'HIGH', 'CRITICAL', 'EXTREME'];

const ReportDisasterForm: React.FC<ReportDisasterFormProps> = ({ onClose, onSuccess, onError }) => {
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    title: '',
    description: '',
    type: 'FLOOD' as DisasterType,
    severity: 'MODERATE' as DisasterSeverity,
    location: '',
    region: '',
    country: 'India',
    latitude: '',
    longitude: '',
    advisoryMessage: '',
    affectedAreas: '',
    estimatedAffectedPopulation: '',
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    onError('');
    try {
      await disasterApi.create({
        title: form.title,
        description: form.description || undefined,
        type: form.type,
        severity: form.severity,
        location: form.location,
        region: form.region || undefined,
        country: form.country || 'India',
        latitude: form.latitude ? parseFloat(form.latitude) : undefined,
        longitude: form.longitude ? parseFloat(form.longitude) : undefined,
        advisoryMessage: form.advisoryMessage || undefined,
        affectedAreas: form.affectedAreas || undefined,
        estimatedAffectedPopulation: form.estimatedAffectedPopulation ? parseInt(form.estimatedAffectedPopulation, 10) : undefined,
      });
      onSuccess();
    } catch (err: any) {
      onError(err.message || 'Failed to submit disaster report');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="report-modal-overlay" onClick={onClose}>
      <div className="report-modal" onClick={e => e.stopPropagation()}>
        <div className="report-modal-header">
          <h2>Report Disaster</h2>
          <button className="report-modal-close" onClick={onClose}>×</button>
        </div>
        <p className="report-modal-hint">Report will be reviewed by Admin. If verified, the alert will be pushed to the dashboard.</p>

        <form onSubmit={handleSubmit} className="report-form">
          <div className="report-form-row">
            <div className="report-form-group full">
              <label>Title *</label>
              <input
                name="title"
                value={form.title}
                onChange={handleChange}
                placeholder="e.g. Flood in Mumbai suburbs"
                required
              />
            </div>
          </div>

          <div className="report-form-row">
            <div className="report-form-group">
              <label>Disaster Type *</label>
              <select name="type" value={form.type} onChange={handleChange} required>
                {disasterTypes.map(t => (
                  <option key={t} value={t}>{t.replace('_', ' ')}</option>
                ))}
              </select>
            </div>
            <div className="report-form-group">
              <label>Severity *</label>
              <select name="severity" value={form.severity} onChange={handleChange} required>
                {severities.map(s => (
                  <option key={s} value={s}>{s}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="report-form-row">
            <div className="report-form-group">
              <label>Location *</label>
              <input
                name="location"
                value={form.location}
                onChange={handleChange}
                placeholder="e.g. Mumbai, Maharashtra"
                required
              />
            </div>
            <div className="report-form-group">
              <label>Region / State</label>
              <input
                name="region"
                value={form.region}
                onChange={handleChange}
                placeholder="e.g. Maharashtra"
              />
            </div>
          </div>

          <div className="report-form-row">
            <div className="report-form-group">
              <label>Latitude</label>
              <input
                name="latitude"
                type="number"
                step="any"
                value={form.latitude}
                onChange={handleChange}
                placeholder="19.0760"
              />
            </div>
            <div className="report-form-group">
              <label>Longitude</label>
              <input
                name="longitude"
                type="number"
                step="any"
                value={form.longitude}
                onChange={handleChange}
                placeholder="72.8777"
              />
            </div>
          </div>

          <div className="report-form-group">
            <label>Description</label>
            <textarea
              name="description"
              value={form.description}
              onChange={handleChange}
              placeholder="Provide details about the disaster..."
              rows={3}
            />
          </div>

          <div className="report-form-group">
            <label>Advisory / Instructions</label>
            <input
              name="advisoryMessage"
              value={form.advisoryMessage}
              onChange={handleChange}
              placeholder="e.g. Evacuate affected areas. Call helpline: 108"
            />
          </div>

          <div className="report-form-row">
            <div className="report-form-group">
              <label>Affected Areas</label>
              <input
                name="affectedAreas"
                value={form.affectedAreas}
                onChange={handleChange}
                placeholder="e.g. Andheri, Borivali"
              />
            </div>
            <div className="report-form-group">
              <label>Est. Affected Population</label>
              <input
                name="estimatedAffectedPopulation"
                type="number"
                min="0"
                value={form.estimatedAffectedPopulation}
                onChange={handleChange}
                placeholder="e.g. 5000"
              />
            </div>
          </div>

          <div className="report-form-actions">
            <button type="button" className="report-btn cancel" onClick={onClose}>
              Cancel
            </button>
            <button type="submit" className="report-btn submit" disabled={loading}>
              {loading ? 'Submitting...' : 'Submit Report'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ReportDisasterForm;
