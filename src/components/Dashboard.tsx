import { useEffect, useState } from 'react';
import { api } from '../api/client';
import type { DashboardStats } from '../types';

export default function Dashboard() {
  const [stats, setStats] = useState<DashboardStats | null>(null);

  useEffect(() => {
    api.getDashboardStats().then(setStats).catch(console.error);
  }, []);

  if (!stats) return <div className="dashboard loading">Loading stats...</div>;

  return (
    <div className="dashboard">
      <div className="stat-card">
        <div className="stat-value">{stats.totalProfiles}</div>
        <div className="stat-label">Profiles</div>
      </div>
      <div className="stat-card highlight">
        <div className="stat-value">{stats.totalLeads}</div>
        <div className="stat-label">Leads</div>
      </div>
      <div className="stat-card">
        <div className="stat-value">{stats.avgScore}</div>
        <div className="stat-label">Avg Score</div>
      </div>
      <div className="stat-card">
        <div className="stat-value">{stats.totalScans}</div>
        <div className="stat-label">Scans</div>
      </div>
      <div className="stat-card">
        <div className="stat-value">{stats.runningScans}</div>
        <div className="stat-label">Running</div>
      </div>
    </div>
  );
}
