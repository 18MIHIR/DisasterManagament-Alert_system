import { Disaster, DashboardStats, DisasterFilters, Alert, RescueTask, Report, UserProfile, RescueZone } from './types';

export const API_BASE_URL = 'http://localhost:8080/api';

const getAuthHeaders = (): Record<string, string> => {
  const token = localStorage.getItem('dm_token');
  return token ? { Authorization: `Bearer ${token}` } : {};
};

// central helper for checking responses; also handles unauthorized state
const handleResponse = async <T>(response: Response, defaultMsg: string): Promise<T> => {
  if (!response.ok) {
    // if auth failed, clear storage and force reload so UI syncs
    if (response.status === 401 || response.status === 403) {
      localStorage.removeItem('dm_token');
      localStorage.removeItem('dm_role');
      localStorage.removeItem('dm_name');
      localStorage.removeItem('dm_region');
      // reload application so that login view shows up
      window.location.reload();
      throw new Error('Authentication required');
    }
    const err = await response.json().catch(() => ({}));
    throw new Error(err.error || defaultMsg);
  }
  // If caller expected no JSON body, they can ignore the return value.
  return response.json();
};

export const disasterApi = {
  async getAll(filters?: DisasterFilters): Promise<Disaster[]> {
    const params = new URLSearchParams();
    if (filters?.type) params.append('type', filters.type);
    if (filters?.severity) params.append('severity', filters.severity);
    if (filters?.status) params.append('status', filters.status);
    if (filters?.region) params.append('region', filters.region);

    const queryString = params.toString();
    const url = `${API_BASE_URL}/disasters${queryString ? `?${queryString}` : ''}`;
    
    const response = await fetch(url);
    if (!response.ok) throw new Error('Failed to fetch disasters');
    return response.json();
  },

  async getActive(): Promise<Disaster[]> {
    const response = await fetch(`${API_BASE_URL}/disasters/active`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch active disasters');
    return response.json();
  },

  async getPending(): Promise<Disaster[]> {
    const response = await fetch(`${API_BASE_URL}/disasters/pending`);
    if (!response.ok) throw new Error('Failed to fetch pending disasters');
    return response.json();
  },

  async getRecent(hours: number = 24): Promise<Disaster[]> {
    const response = await fetch(`${API_BASE_URL}/disasters/recent?hours=${hours}`);
    if (!response.ok) throw new Error('Failed to fetch recent disasters');
    return response.json();
  },

  async getStats(): Promise<DashboardStats> {
    const response = await fetch(`${API_BASE_URL}/disasters/stats`);
    if (!response.ok) throw new Error('Failed to fetch stats');
    return response.json();
  },

  async create(data: {
    title: string;
    description?: string;
    type: string;
    severity: string;
    location: string;
    region?: string;
    country?: string;
    latitude?: number;
    longitude?: number;
    advisoryMessage?: string;
    affectedAreas?: string;
    estimatedAffectedPopulation?: number;
  }): Promise<Disaster> {
    const response = await fetch(`${API_BASE_URL}/disasters`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...getAuthHeaders(),
      },
      body: JSON.stringify(data),
    });
    if (!response.ok) {
      const err = await response.json().catch(() => ({}));
      throw new Error(err.error || 'Failed to create disaster report');
    }
    return response.json();
  },

  async getById(id: number): Promise<Disaster> {
    const response = await fetch(`${API_BASE_URL}/disasters/${id}`);
    if (!response.ok) throw new Error('Failed to fetch disaster');
    return response.json();
  },

  async verify(id: number): Promise<Disaster> {
    const response = await fetch(`${API_BASE_URL}/disasters/${id}/verify`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...getAuthHeaders(),
      },
    });
    if (!response.ok) throw new Error('Failed to verify disaster');
    return response.json();
  },

  async resolve(id: number): Promise<Disaster> {
    const response = await fetch(`${API_BASE_URL}/disasters/${id}/resolve`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...getAuthHeaders(),
      },
    });
    if (!response.ok) throw new Error('Failed to resolve disaster');
    return response.json();
  },

  async cancel(id: number): Promise<Disaster> {
    const response = await fetch(`${API_BASE_URL}/disasters/${id}/cancel`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...getAuthHeaders(),
      },
    });
    if (!response.ok) throw new Error('Failed to cancel disaster');
    return response.json();
  },

  async update(id: number, data: Partial<Disaster>): Promise<Disaster> {
    const response = await fetch(`${API_BASE_URL}/disasters/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        ...getAuthHeaders(),
      },
      body: JSON.stringify(data),
    });
    if (!response.ok) throw new Error('Failed to update disaster');
    return response.json();
  },

  async deleteAllPending(): Promise<{ deleted: number }> {
    const response = await fetch(`${API_BASE_URL}/disasters/pending`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });
    // note: deleted count returned as JSON
    return handleResponse(response, 'Failed to remove pending alerts');
  },

  async triggerFetch(): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/disasters/fetch-external`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...getAuthHeaders(),
      },
    });
    // there is no body on success, but handleResponse will still check status
    await handleResponse(response, 'Failed to trigger external fetch');
  },
};

export const alertApi = {
  async getForCitizen(): Promise<Alert[]> {
    const response = await fetch(`${API_BASE_URL}/alerts/for-citizen`, { headers: getAuthHeaders() });
    if (!response.ok) throw new Error('Failed to fetch alerts');
    return response.json();
  },
  async getAll(): Promise<Alert[]> {
    const response = await fetch(`${API_BASE_URL}/alerts`, { headers: getAuthHeaders() });
    if (!response.ok) throw new Error('Failed to fetch alerts');
    return response.json();
  },
};

export const rescueZoneApi = {
  async list(): Promise<RescueZone[]> {
    const response = await fetch(`${API_BASE_URL}/rescue-zones`, { headers: getAuthHeaders() });
    if (!response.ok) throw new Error('Failed to fetch zones');
    return response.json();
  },
};

export const rescueTaskApi = {
  async listActiveForMap(): Promise<RescueTask[]> {
    const response = await fetch(`${API_BASE_URL}/rescue-tasks`, { headers: getAuthHeaders() });
    if (!response.ok) throw new Error('Failed to fetch rescue tasks');
    return response.json();
  },
  async getMyTasks(): Promise<RescueTask[]> {
    const response = await fetch(`${API_BASE_URL}/rescue-tasks/my-tasks`, { headers: getAuthHeaders() });
    if (!response.ok) throw new Error('Failed to fetch tasks');
    return response.json();
  },
  async create(
    responderId: number,
    disasterId: number,
    description?: string,
    zoneId?: number,
    rescueSiteLatitude?: number,
    rescueSiteLongitude?: number
  ): Promise<RescueTask> {
    const body: Record<string, unknown> = { responderId, disasterId, description };
    if (zoneId != null) body.zoneId = zoneId;
    if (rescueSiteLatitude != null && rescueSiteLongitude != null) {
      body.rescueSiteLatitude = rescueSiteLatitude;
      body.rescueSiteLongitude = rescueSiteLongitude;
    }
    const response = await fetch(`${API_BASE_URL}/rescue-tasks`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify(body),
    });
    if (!response.ok) {
      const err = await response.json().catch(() => ({}));
      throw new Error(err.error || 'Failed to assign task');
    }
    return response.json();
  },
  async acknowledge(id: number): Promise<RescueTask> {
    const response = await fetch(`${API_BASE_URL}/rescue-tasks/${id}/acknowledge`, {
      method: 'POST',
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to acknowledge task');
    return response.json();
  },
  async updateStatus(id: number, status: string): Promise<RescueTask> {
    const response = await fetch(`${API_BASE_URL}/rescue-tasks/${id}/status?status=${status}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error('Failed to update status');
    return response.json();
  },
  async getEfficiency(): Promise<{ totalAcknowledged: number; averageResponseMinutes: number }> {
    const response = await fetch(`${API_BASE_URL}/rescue-tasks/efficiency`, { headers: getAuthHeaders() });
    if (!response.ok) throw new Error('Failed to fetch efficiency');
    return response.json();
  },
};

export const reportApi = {
  async create(
    details: string,
    disasterId?: number,
    location?: string,
    latitude?: number,
    longitude?: number
  ): Promise<Report> {
    const response = await fetch(`${API_BASE_URL}/reports`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify({ details, disasterId, location, latitude, longitude }),
    });
    if (!response.ok) {
      const err = await response.json().catch(() => ({}));
      throw new Error(err.error || 'Failed to submit report');
    }
    return response.json();
  },
  async getMyReports(): Promise<Report[]> {
    const response = await fetch(`${API_BASE_URL}/reports/my-reports`, { headers: getAuthHeaders() });
    if (!response.ok) throw new Error('Failed to fetch reports');
    return response.json();
  },
  async getAssignedToMe(): Promise<Report[]> {
    const response = await fetch(`${API_BASE_URL}/reports/assigned-to-me`, { headers: getAuthHeaders() });
    if (!response.ok) throw new Error('Failed to fetch assigned reports');
    return response.json();
  },
  async createIncident(payload: {
    details: string;
    disasterId?: number;
    rescueTaskId?: number;
    location?: string;
    latitude?: number;
    longitude?: number;
    imageData?: string;
  }): Promise<Report> {
    const response = await fetch(`${API_BASE_URL}/reports/incident`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify(payload),
    });
    if (!response.ok) {
      const err = await response.json().catch(() => ({}));
      throw new Error(err.error || 'Failed to submit incident report');
    }
    return response.json();
  },
  async audit(): Promise<Report[]> {
    const response = await fetch(`${API_BASE_URL}/reports/audit`, { headers: getAuthHeaders() });
    if (!response.ok) throw new Error('Failed to fetch audit trail');
    return response.json();
  },
  async getById(id: number): Promise<Report> {
    const response = await fetch(`${API_BASE_URL}/reports/${id}`, { headers: getAuthHeaders() });
    if (!response.ok) throw new Error('Failed to fetch report');
    return response.json();
  },
};

export const userApi = {
  async getResponders(): Promise<UserProfile[]> {
    const response = await fetch(`${API_BASE_URL}/users/responders`, { headers: getAuthHeaders() });
    if (!response.ok) throw new Error('Failed to fetch responders');
    return response.json();
  },
};
