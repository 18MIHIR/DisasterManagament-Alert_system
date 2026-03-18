import React from 'react';
import { DisasterFilters, DisasterType, DisasterSeverity, DisasterStatus } from '../types';

interface DisasterFiltersPanelProps {
  filters: DisasterFilters;
  onChange: (key: keyof DisasterFilters, value: string) => void;
  onClear: () => void;
}

const disasterTypes: DisasterType[] = [
  'FLOOD', 'CYCLONE', 'EARTHQUAKE', 'TSUNAMI', 'WILDFIRE', 
  'LANDSLIDE', 'DROUGHT', 'TORNADO', 'VOLCANIC_ERUPTION', 
  'SEVERE_STORM', 'HEATWAVE', 'COLDWAVE', 'OTHER'
];

const severityLevels: DisasterSeverity[] = ['LOW', 'MODERATE', 'HIGH', 'CRITICAL', 'EXTREME'];

const statusOptions: DisasterStatus[] = ['PENDING', 'ACTIVE', 'RESOLVED', 'CANCELLED'];

const DisasterFiltersPanel: React.FC<DisasterFiltersPanelProps> = ({ filters, onChange, onClear }) => {
  const hasFilters = filters.type || filters.severity || filters.status || filters.region;

  return (
    <div className="filters-panel">
      <div className="filters-row">
        <div className="filter-group">
          <label>Disaster Type</label>
          <select 
            value={filters.type} 
            onChange={e => onChange('type', e.target.value)}
          >
            <option value="">All Types</option>
            {disasterTypes.map(type => (
              <option key={type} value={type}>
                {type.replace('_', ' ')}
              </option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <label>Severity</label>
          <select 
            value={filters.severity} 
            onChange={e => onChange('severity', e.target.value)}
          >
            <option value="">All Severities</option>
            {severityLevels.map(level => (
              <option key={level} value={level}>{level}</option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <label>Status</label>
          <select 
            value={filters.status} 
            onChange={e => onChange('status', e.target.value)}
          >
            <option value="">All Statuses</option>
            {statusOptions.map(status => (
              <option key={status} value={status}>{status}</option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <label>Region</label>
          <input
            type="text"
            placeholder="Search region..."
            value={filters.region}
            onChange={e => onChange('region', e.target.value)}
          />
        </div>

        {hasFilters && (
          <button className="clear-filters-btn" onClick={onClear}>
            Clear Filters
          </button>
        )}
      </div>
    </div>
  );
};

export default DisasterFiltersPanel;
