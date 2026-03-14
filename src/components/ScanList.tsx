import { useEffect, useState } from 'react';
import { api } from '../api/client';
import type { ScanJob } from '../types';

interface Props {
  refreshKey: number;
}

export default function ScanList({ refreshKey }: Props) {
  const [scans, setScans] = useState<ScanJob[]>([]);

  useEffect(() => {
    api.getScans().then(setScans).catch(console.error);
  }, [refreshKey]);

  // Auto-refresh running scans
  useEffect(() => {
    const hasRunning = scans.some(s => s.status === 'RUNNING' || s.status === 'PENDING');
    if (!hasRunning) return;

    const interval = setInterval(() => {
      api.getScans().then(setScans).catch(console.error);
    }, 3000);

    return () => clearInterval(interval);
  }, [scans]);

  if (scans.length === 0) return null;

  return (
    <div className="scan-list">
      <h2>Scans</h2>
      <div className="table-wrap">
        <table className="scan-table">
          <thead>
            <tr>
              <th>Query</th>
              <th>Source</th>
              <th>AI</th>
              <th>Status</th>
              <th>Found</th>
              <th>Analyzed</th>
              <th>Leads</th>
            </tr>
          </thead>
          <tbody>
            {scans.map(s => (
              <tr key={s.id}>
                <td>{s.searchQuery}</td>
                <td>{s.dataSource}</td>
                <td>{s.aiProvider}</td>
                <td>
                  <span className={`status status-${s.status.toLowerCase()}`}>
                    {s.status}
                  </span>
                </td>
                <td>{s.profilesFound}</td>
                <td>{s.profilesAnalyzed}</td>
                <td>{s.leadsFound}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
