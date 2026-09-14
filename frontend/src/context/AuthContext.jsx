import React, { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../api/client';
import { DEMO_USERS } from '../api/mockData';

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('user_profile');
    return saved ? JSON.parse(saved) : null;
  });

  const [token, setToken] = useState(() => localStorage.getItem('access_token') || null);

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

  const register = async (userData) => {
    const res = await api.register(userData);
    if (res.success && res.data) {
      const d = res.data;
      const profile = {
        id: d.userId,
        email: d.email || userData.email,
        firstName: d.firstName || userData.firstName,
        lastName: d.lastName || userData.lastName,
        role: d.roles?.[0] || 'ROLE_USER'
      };
      setToken(d.accessToken);
      setUser(profile);
      return { success: true, profile };
    }
    return { success: false, error: res.error || 'Registration failed' };
  };

  const login = async (credentials) => {
    const res = await api.login(credentials);
    if (res.success && res.data) {
      const d = res.data;
      const profile = {
        id: d.userId,
        email: d.email || credentials.email,
        firstName: d.firstName || 'User',
        lastName: d.lastName || '',
        role: d.roles?.[0] || 'ROLE_USER'
      };
      setToken(d.accessToken);
      setUser(profile);
      return { success: true, profile };
    }
    return { success: false, error: res.error || 'Authentication failed' };
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
    <AuthContext.Provider value={{
      user,
      token,
      isAuthenticated: !!user && !!token,
      register,
      login,
      logout,
      selectPersona
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
