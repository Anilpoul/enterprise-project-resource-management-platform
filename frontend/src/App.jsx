import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { TenantProvider } from './context/TenantContext';
import { NotificationProvider } from './context/NotificationContext';

import Navbar from './components/Navbar';
import Sidebar from './components/Sidebar';
import NotificationDrawer from './components/NotificationDrawer';

import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import ProjectsPage from './pages/ProjectsPage';
import ProjectDetailPage from './pages/ProjectDetailPage';
import BoardPage from './pages/BoardPage';
import SprintsPage from './pages/SprintsPage';
import ResourcesPage from './pages/ResourcesPage';
import AnalyticsPage from './pages/AnalyticsPage';
import AuditPage from './pages/AuditPage';
import NotificationsPage from './pages/NotificationsPage';

function AppLayout({ children }) {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="app-container">
      <Sidebar />
      <div className="main-layout">
        <Navbar />
        <main className="page-content">
          {children}
        </main>
      </div>
      <NotificationDrawer />
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <TenantProvider>
        <NotificationProvider>
          <BrowserRouter>
            <Routes>
              <Route path="/login" element={<LoginPage />} />

              <Route path="/" element={<AppLayout><DashboardPage /></AppLayout>} />
              <Route path="/projects" element={<AppLayout><ProjectsPage /></AppLayout>} />
              <Route path="/projects/:id" element={<AppLayout><ProjectDetailPage /></AppLayout>} />
              <Route path="/boards" element={<AppLayout><BoardPage /></AppLayout>} />
              <Route path="/sprints" element={<AppLayout><SprintsPage /></AppLayout>} />
              <Route path="/resources" element={<AppLayout><ResourcesPage /></AppLayout>} />
              <Route path="/analytics" element={<AppLayout><AnalyticsPage /></AppLayout>} />
              <Route path="/audit" element={<AppLayout><AuditPage /></AppLayout>} />
              <Route path="/notifications" element={<AppLayout><NotificationsPage /></AppLayout>} />

              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </BrowserRouter>
        </NotificationProvider>
      </TenantProvider>
    </AuthProvider>
  );
}
