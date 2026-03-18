import React from 'react';
import { Disaster } from '../types';

interface DisasterCardProps {
  disaster: Disaster;
  onClick: () => void;
  isAdmin: boolean;
}

const getTypeIcon = (type: string): string => {
  const icons: Record<string, string> = {
    EARTHQUAKE: '🌍',
    FLOOD: '🌊',
    CYCLONE: '🌀',
    TSUNAMI: '🌊',
    WILDFIRE: '🔥',
    LANDSLIDE: '⛰️',
    DROUGHT: '☀️',
    TORNADO: '🌪️',
    VOLCANIC_ERUPTION: '🌋',
    SEVERE_STORM: '⛈️',
    HEATWAVE: '🌡️',
    COLDWAVE: '❄️',
    OTHER: '⚠️',
  };
  return icons[type] || '⚠️';
};

const formatTime = (dateString: string): string => {
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
  const diffDays = Math.floor(diffHours / 24);

  if (diffHours < 1) return 'Just now';
  if (diffHours < 24) return `${diffHours}h ago`;
  if (diffDays < 7) return `${diffDays}d ago`;
  return date.toLocaleDateString();
};

const DisasterCard: React.FC<DisasterCardProps> = ({ disaster, onClick, isAdmin }) => {
  return (
    <div className={`disaster-card ${disaster.severity.toLowerCase()}`} onClick={onClick}>
      <div className="card-header">
        <span className="type-icon">{getTypeIcon(disaster.type)}</span>
        <div className="card-badges">
          <span className={`severity-badge ${disaster.severity.toLowerCase()}`}>
            {disaster.severity}
          </span>
          <span className={`status-badge ${disaster.status.toLowerCase()}`}>
            {disaster.status}
          </span>
        </div>
      </div>
      
      <h3 className="card-title">{disaster.title}</h3>
      
      <div className="card-meta">
        <span className="location">📍 {disaster.location}</span>
        <span className="time">🕐 {formatTime(disaster.eventTime)}</span>
      </div>
      
      {disaster.description && (
        <p className="card-description">
          {disaster.description.length > 120 
            ? `${disaster.description.substring(0, 120)}...` 
            : disaster.description}
        </p>
      )}

      <div className="card-footer">
        <span className="source">{disaster.source || 'Manual'}</span>
        {isAdmin && disaster.status === 'PENDING' && (
          <span className="pending-indicator">Needs Verification</span>
        )}
      </div>
    </div>
  );
};

export default DisasterCard;
