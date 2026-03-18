export type Role = 'ADMIN' | 'RESPONDER' | 'CITIZEN';

export type DisasterType = 
  | 'FLOOD'
  | 'CYCLONE'
  | 'EARTHQUAKE'
  | 'TSUNAMI'
  | 'WILDFIRE'
  | 'LANDSLIDE'
  | 'DROUGHT'
  | 'TORNADO'
  | 'VOLCANIC_ERUPTION'
  | 'SEVERE_STORM'
  | 'HEATWAVE'
  | 'COLDWAVE'
  | 'OTHER';

export type DisasterSeverity = 'LOW' | 'MODERATE' | 'HIGH' | 'CRITICAL' | 'EXTREME';

export type DisasterStatus = 'PENDING' | 'ACTIVE' | 'RESOLVED' | 'CANCELLED';

export interface AuthResponse {
  token: string;
  role: Role;
  name: string;
  region: string;
}

export interface UserProfile {
  id: number;
  name: string;
  email: string;
  role: Role;
  phone: string;
  region: string;
}

export type TaskStatus = 'ASSIGNED' | 'ACKNOWLEDGED' | 'IN_PROGRESS' | 'COMPLETED';

export interface Alert {
  id: number;
  disasterId: number;
  disasterTitle: string;
  message: string;
  createdByName: string;
  broadcastTime: string;
  region: string;
}

export interface RescueTask {
  id: number;
  responderId: number;
  responderName: string;
  disasterId: number;
  disasterTitle: string;
  disasterLocation: string;
  taskStatus: TaskStatus;
  description: string;
  updatedAt: string;
  acknowledgedAt: string | null;
}

export interface Report {
  id: number;
  disasterId: number | null;
  disasterTitle: string | null;
  responderId: number | null;
  responderName: string | null;
  details: string;
  location: string | null;
  submittedAt: string;
}

export interface Disaster {
  id: number;
  title: string;
  description: string;
  type: DisasterType;
  severity: DisasterSeverity;
  status: DisasterStatus;
  location: string;
  region: string;
  country: string;
  latitude: number | null;
  longitude: number | null;
  source: string;
  eventTime: string;
  createdAt: string;
  updatedAt: string | null;
  resolvedAt: string | null;
  advisoryMessage: string;
  affectedAreas: string;
  estimatedAffectedPopulation: number | null;
  alertBroadcastAt?: string | null;
  verifiedBy?: number | null;
}

export interface DashboardStats {
  totalActive: number;
  totalPending: number;
  totalResolved: number;
  activeFloods: number;
  activeEarthquakes: number;
  activeCyclones: number;
  activeWildfires: number;
}

export interface DisasterFilters {
  type: DisasterType | '';
  severity: DisasterSeverity | '';
  status: DisasterStatus | '';
  region: string;
}
