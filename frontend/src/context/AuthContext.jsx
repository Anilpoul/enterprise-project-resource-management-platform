import React, { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../api/client';
import { DEMO_USERS } from '../api/mockData';

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('user_profile');
    return saved ? JSON.parse(saved) : DEMO_USERS[0];
  });

  const [token, setToken] = useState(() => localStorage.getItem('access_token') || 'demo-jwt-token-xyz');

  useEffect(() => {
    if (user) {
      localStorage.setItem('user_profile', JSON.stringify(user));
      localStorage.setItem('user_id', user.id);
    } else {
      localStorage.removeItem('user_profile');
      localStorage.removeItem('user_id');
    }
  }, [user]);

  useEffect(() => {
    if (token) {
      localStorage.setItem('access_token', token);
    } else {
      localStorage.removeItem('access_token');
    }
  }, [token]);

  const login = async (credentials) => {
    const res = await api.login(credentials);
    if (res.data) {
      setToken(res.data.accessToken || 'demo-token');
      const profile = {
        id: res.data.userId || 'u1-admin',
        email: res.data.email || credentials.email,
        firstName: res.data.firstName || 'Sarah',
        lastName: res.data.lastName || 'Connor',
        role: res.data.roles?.[0] || 'ROLE_ADMIN'
      };
      setUser(profile);
      return profile;
    }
    return null;
  };

  const selectPersona = (personaIndex) => {
    const selected = DEMO_USERS[personaIndex] || DEMO_USERS[0];
    setUser(selected);
    setToken('demo-token-' + selected.id);
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.clear();
  };

  return (
    <AuthContext.Provider value={{ user, token, isAuthenticated: !!user, login, logout, selectPersona }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
