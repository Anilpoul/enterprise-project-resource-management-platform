import React, { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../api/client';
import { DEMO_ORGS } from '../api/mockData';

const TenantContext = createContext();

export function TenantProvider({ children }) {
  const [organizations, setOrganizations] = useState(DEMO_ORGS);
  const [activeOrg, setActiveOrg] = useState(() => {
    const saved = localStorage.getItem('active_org');
    return saved ? JSON.parse(saved) : DEMO_ORGS[0];
  });

  useEffect(() => {
    const loadOrgs = async () => {
      const orgs = await api.getOrganizations();
      if (orgs && orgs.length > 0) {
        setOrganizations(orgs);
        if (!activeOrg || !orgs.some(o => o.id === activeOrg.id)) {
          setActiveOrg(orgs[0]);
        }
      }
    };
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

  return (
    <TenantContext.Provider value={{ organizations, activeOrg, switchOrg }}>
      {children}
    </TenantContext.Provider>
  );
}

export const useTenant = () => useContext(TenantContext);
