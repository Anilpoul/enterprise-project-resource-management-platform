import React, { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../api/client';
import { DEMO_NOTIFICATIONS } from '../api/mockData';
import { useAuth } from './AuthContext';

const NotificationContext = createContext();

export function NotificationProvider({ children }) {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState(DEMO_NOTIFICATIONS);
  const [unreadCount, setUnreadCount] = useState(2);
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);

  const refreshNotifications = async () => {
    if (!user) return;
    const items = await api.getNotifications(user.id);
    setNotifications(items || DEMO_NOTIFICATIONS);
    const count = await api.getUnreadCount(user.id);
    setUnreadCount(count ?? 0);
  };

  useEffect(() => {
    refreshNotifications();
  }, [user]);

  const markAsRead = (id) => {
    setNotifications(prev =>
      prev.map(n => n.id === id ? { ...n, isRead: true } : n)
    );
    setUnreadCount(prev => Math.max(0, prev - 1));
  };

  const markAllAsRead = () => {
    setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
    setUnreadCount(0);
  };

  return (
    <NotificationContext.Provider
      value={{
        notifications,
        unreadCount,
        isDrawerOpen,
        setIsDrawerOpen,
        markAsRead,
        markAllAsRead,
        refreshNotifications
      }}
    >
      {children}
    </NotificationContext.Provider>
  );
}

export const useNotifications = () => useContext(NotificationContext);
