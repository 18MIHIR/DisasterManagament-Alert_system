import React, { useEffect, useState } from 'react';
import './App.css';
import Dashboard from './components/Dashboard';
import { AuthResponse, UserProfile, Role } from './types';

const API_BASE_URL = 'http://localhost:8080/api';

type View = 'dashboard' | 'auth' | 'profile';

const App: React.FC = () => {
  const [currentView, setCurrentView] = useState<View>('dashboard');
  const [isLoginView, setIsLoginView] = useState(true);

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [region, setRegion] = useState('');
  const [role, setRole] = useState<Role>('CITIZEN');

  const [token, setToken] = useState<string | null>(localStorage.getItem('dm_token'));
  const [userRole, setUserRole] = useState<string | null>(localStorage.getItem('dm_role'));
  const [userName, setUserName] = useState<string | null>(localStorage.getItem('dm_name'));
  const [profile, setProfile] = useState<UserProfile | null>(null);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const isAuthenticated = Boolean(token);

  const loadProfile = async (activeToken: string) => {
    try {
      const response = await fetch(`${API_BASE_URL}/users/me`, {
        headers: {
          Authorization: `Bearer ${activeToken}`,
        },
      });

      if (!response.ok) {
        if (response.status === 401) {
          localStorage.removeItem('dm_token');
          localStorage.removeItem('dm_role');
          localStorage.removeItem('dm_name');
          localStorage.removeItem('dm_region');
          setToken(null);
          setUserRole(null);
          setUserName(null);
          setProfile(null);
        }
        return;
      }

      const data: UserProfile = await response.json();
      setProfile(data);
      setName(data.name);
      setPhone(data.phone);
      setRegion(data.region);
      setRole(data.role);
      setUserRole(data.role);
      setUserName(data.name);
    } catch {
      // Ignore network errors
    }
  };

  useEffect(() => {
    if (token) {
      loadProfile(token);
    }
  }, [token]);

  const handleAuthSuccess = (data: AuthResponse, context: 'login' | 'register') => {
    localStorage.setItem('dm_token', data.token);
    localStorage.setItem('dm_role', data.role);
    localStorage.setItem('dm_name', data.name);
    localStorage.setItem('dm_region', data.region);
    setToken(data.token);
    setUserRole(data.role);
    setUserName(data.name);
    loadProfile(data.token);
    setMessage(
      context === 'login'
        ? `Welcome back, ${data.name}!`
        : `Registration successful. Welcome, ${data.name}!`
    );
    setCurrentView('dashboard');
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setMessage(null);

    try {
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || 'Login failed');
      }

      const data: AuthResponse = await response.json();
      handleAuthSuccess(data, 'login');
    } catch (err: any) {
      setError(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setMessage(null);

    try {
      const response = await fetch(`${API_BASE_URL}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, email, password, phone, region, role }),
      });

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || 'Registration failed');
      }

      const data: AuthResponse = await response.json();
      handleAuthSuccess(data, 'register');
    } catch (err: any) {
      setError(err.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  const handleProfileSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) return;

    setLoading(true);
    setError(null);
    setMessage(null);

    try {
      const response = await fetch(`${API_BASE_URL}/users/me`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ name, phone, region }),
      });

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || 'Failed to update profile');
      }

      const data: UserProfile = await response.json();
      setProfile(data);
      setUserName(data.name);
      localStorage.setItem('dm_name', data.name);
      localStorage.setItem('dm_region', data.region);
      setMessage('Profile updated successfully.');
    } catch (err: any) {
      setError(err.message || 'Failed to update profile');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('dm_token');
    localStorage.removeItem('dm_role');
    localStorage.removeItem('dm_name');
    localStorage.removeItem('dm_region');
    setToken(null);
    setUserRole(null);
    setUserName(null);
    setProfile(null);
    setEmail('');
    setPassword('');
    setName('');
    setPhone('');
    setRegion('');
    setRole('CITIZEN');
    setIsLoginView(true);
    setMessage(null);
    setError(null);
    setCurrentView('dashboard');
  };

  const renderRoleButton = (value: Role, label: string) => (
    <button
      type="button"
      className={`role-button ${role === value ? 'selected' : ''}`}
      onClick={() => setRole(value)}
    >
      {label}
    </button>
  );

  const renderNavbar = () => (
    <nav className="navbar">
      <div className="nav-brand" onClick={() => setCurrentView('dashboard')}>
        <span className="brand-icon">🚨</span>
        <span className="brand-text">DisasterAlert</span>
      </div>
      <div className="nav-links">
        <button
          className={`nav-link ${currentView === 'dashboard' ? 'active' : ''}`}
          onClick={() => setCurrentView('dashboard')}
        >
          Dashboard
        </button>
        {isAuthenticated ? (
          <>
            <button
              className={`nav-link ${currentView === 'profile' ? 'active' : ''}`}
              onClick={() => setCurrentView('profile')}
            >
              Profile
            </button>
            <div className="nav-user">
              <span className="user-name">{userName}</span>
              <span className="user-role">{userRole}</span>
            </div>
            <button className="nav-link logout" onClick={handleLogout}>
              Logout
            </button>
          </>
        ) : (
          <button
            className={`nav-link ${currentView === 'auth' ? 'active' : ''}`}
            onClick={() => setCurrentView('auth')}
          >
            Login
          </button>
        )}
      </div>
    </nav>
  );

  const renderAuthView = () => (
    <div className="auth-page">
      <div className="auth-card">
        <h1 className="auth-title">
          {isLoginView ? 'Welcome Back' : 'Create Account'}
        </h1>

        {isLoginView ? (
          <form onSubmit={handleLogin}>
            <div className="input-group">
              <input
                type="email"
                placeholder="Email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <div className="input-group">
              <input
                type="password"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <button type="submit" className="primary-button" disabled={loading}>
              {loading ? 'Logging in...' : 'Login'}
            </button>
            <p className="toggle-text">
              Don't have an account?{' '}
              <span
                className="toggle-link"
                onClick={() => {
                  setIsLoginView(false);
                  setError(null);
                  setMessage(null);
                }}
              >
                Sign up
              </span>
            </p>
          </form>
        ) : (
          <form onSubmit={handleRegister}>
            <div className="input-group">
              <input
                type="text"
                placeholder="Full name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </div>
            <div className="input-group">
              <input
                type="email"
                placeholder="Email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <div className="input-group">
              <input
                type="password"
                placeholder="Password (min 6 characters)"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <div className="input-group">
              <input
                type="tel"
                placeholder="Phone Number"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                required
              />
            </div>
            <div className="input-group">
              <input
                type="text"
                placeholder="Location / Region"
                value={region}
                onChange={(e) => setRegion(e.target.value)}
                required
              />
            </div>
            <div className="role-selector">
              {renderRoleButton('ADMIN', 'Admin')}
              {renderRoleButton('RESPONDER', 'Responder')}
              {renderRoleButton('CITIZEN', 'Citizen')}
            </div>
            <button type="submit" className="primary-button" disabled={loading}>
              {loading ? 'Registering...' : 'Register'}
            </button>
            <p className="toggle-text">
              Already have an account?{' '}
              <span
                className="toggle-link"
                onClick={() => {
                  setIsLoginView(true);
                  setError(null);
                  setMessage(null);
                }}
              >
                Log in
              </span>
            </p>
          </form>
        )}

        {error && <div className="alert error">{error}</div>}
        {message && <div className="alert success">{message}</div>}
      </div>
    </div>
  );

  const renderProfileView = () => (
    <div className="auth-page">
      <div className="auth-card">
        <h1 className="auth-title">My Profile</h1>

        {profile && (
          <form onSubmit={handleProfileSave}>
            <div className="profile-meta">
              <span className="badge">{profile.role}</span>
              <span className="meta-item">{profile.email}</span>
            </div>

            <div className="input-group">
              <input
                type="text"
                placeholder="Name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </div>
            <div className="input-group">
              <input
                type="tel"
                placeholder="Phone Number"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                required
              />
            </div>
            <div className="input-group">
              <input
                type="text"
                placeholder="Location / Region"
                value={region}
                onChange={(e) => setRegion(e.target.value)}
                required
              />
            </div>

            <button type="submit" className="primary-button" disabled={loading}>
              {loading ? 'Saving...' : 'Save Profile'}
            </button>
            <button
              type="button"
              className="secondary-button"
              onClick={() => setCurrentView('dashboard')}
            >
              Back to Dashboard
            </button>
          </form>
        )}

        {error && <div className="alert error">{error}</div>}
        {message && <div className="alert success">{message}</div>}
      </div>
    </div>
  );

  return (
    <div className="app">
      {renderNavbar()}
      <main className="main-content">
        {currentView === 'dashboard' && <Dashboard userRole={userRole} />}
        {currentView === 'auth' && renderAuthView()}
        {currentView === 'profile' && renderProfileView()}
      </main>
    </div>
  );
};

export default App;
