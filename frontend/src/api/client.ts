import type { DashboardStats, Profile, ScanJob, ScanRequest, AnalyzeRequest, UkraineScanRequest } from '../types';

const BASE = '/api';

async function fetchJson<T>(url: string, init?: RequestInit): Promise<T> {
  const res = await fetch(url, init);
  if (!res.ok) throw new Error(`HTTP ${res.status}: ${res.statusText}`);
  return res.json();
}

export const api = {
  getDashboardStats: () =>
    fetchJson<DashboardStats>(`${BASE}/dashboard/stats`),

  getLeads: () =>
    fetchJson<Profile[]>(`${BASE}/profiles/leads`),

  getProfiles: (page = 0, size = 20) =>
    fetchJson<{ content: Profile[]; totalPages: number; totalElements: number }>(
      `${BASE}/profiles?page=${page}&size=${size}`
    ),

  getProfile: (id: number) =>
    fetchJson<Profile>(`${BASE}/profiles/${id}`),

  analyzeProfile: (req: AnalyzeRequest) =>
    fetchJson<Profile>(`${BASE}/profiles/analyze`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req),
    }),

  startScan: (req: ScanRequest) =>
    fetchJson<ScanJob>(`${BASE}/scans`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req),
    }),

  startUkraineScan: (req: UkraineScanRequest) =>
    fetchJson<ScanJob>(`${BASE}/scans/ukraine`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req),
    }),

  getScans: () =>
    fetchJson<ScanJob[]>(`${BASE}/scans`),

  getScan: (id: number) =>
    fetchJson<ScanJob>(`${BASE}/scans/${id}`),
};
