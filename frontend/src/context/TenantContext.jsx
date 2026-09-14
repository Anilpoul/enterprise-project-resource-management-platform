import React, { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../api/client';
import { DEMO_ORGS } from '../api/mockData';

const TenantContext = createContext();

export function TenantProvider({ children }) {
  const [organizations, setOrganizations] = useState([]);
  const [activeOrg, setActiveOrg] = useState(() => {
    const saved = localStorage.getItem('active_org');
    return saved ? JSON.parse(saved) : null;
  });

  const loadOrgs = async () => {
    const orgs = await api.getOrganizations();
    if (Array.isArray(orgs) && orgs.length > 0) {
      setOrganizations(orgs);
      // If current activeOrg is not in the list, set to the first one
      if (!activeOrg || !orgs.some(o => o.id === activeOrg.id)) {
        setActiveOrg(orgs[0]);
      }
    } else {
      setOrganizations(DEMO_ORGS);
      if (!activeOrg) setActiveOrg(DEMO_ORGS[0]);
    }
  };

  useEffect(() => {
    loadOrgs();
  }, []);

  useEffect(() => {
    if (activeOrg) {
      localStorage.setItem('active_org', JSON.stringify(activeOrg));
      localStorage.setItem('active_org_id', activeOrg.id);
    } else {
      localStorage.removeItem('active_org');
      localStorage.removeItem('active_org_id');
    }
  }, [activeOrg]);

  const switchOrg = (orgId) => {
    const found = organizations.find(o => o.id === orgId);
    if (found) {
      setActiveOrg(found);
    }
  };

  const createOrganization = async (orgData) => {
    const res = await api.createOrganization(orgData);
    if (res.success && res.data) {
      const newOrg = res.data;
      setOrganizations(prev => [newOrg, ...prev.filter(o => o.id !== newOrg.id)]);
      setActiveOrg(newOrg);
      return { success: true, data: newOrg };
    }
    return { success: false, error: res.error || 'Failed to create organization' };
  };

  return (
    <TenantContext.Provider value={{
      organizations,
      activeOrg,
      switchOrg,
      createOrganization,
      refreshOrganizations: loadOrgs
    }}>
      {children}
    </TenantContext.Provider>
  );
}

export const useTenant = () => useContext(TenantContext);
