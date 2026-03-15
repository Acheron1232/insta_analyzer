import { useEffect, useState } from 'react';
import { api } from '../api/client';
import type { Profile } from '../types';

interface Props {
  refreshKey: number;
}

export default function ProfileTable({ refreshKey }: Props) {
  const [profiles, setProfiles] = useState<Profile[]>([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState<Profile | null>(null);

  useEffect(() => {
    setLoading(true);
    api.getLeads()
      .then(setProfiles)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [refreshKey]);

  if (loading) return <div className="loading">Loading leads...</div>;

  return (
    <div className="profile-section">
      <h2>Leads ({profiles.length})</h2>

      {profiles.length === 0 ? (
        <p className="empty">No leads found yet. Run a scan to find potential clients.</p>
      ) : (
        <div className="table-wrap">
          <table className="profile-table">
            <thead>
              <tr>
                <th>Username</th>
                <th>Name</th>
                <th>Followers</th>
                <th>Posts</th>
                <th>Score</th>
                <th>Ukraine</th>
                <th>Website</th>
                <th>CRM</th>
                <th>DM Sales</th>
                <th>Category</th>
              </tr>
            </thead>
            <tbody>
              {profiles.map(p => (
                <tr
                  key={p.id}
                  onClick={() => setSelected(p)}
                  className={selected?.id === p.id ? 'selected' : ''}
                >
                  <td>@{p.username}</td>
                  <td>{p.fullName}</td>
                  <td>{p.followerCount.toLocaleString()}</td>
                  <td>{p.mediaCount}</td>
                  <td>
                    <span className={`score score-${getScoreClass(p.analysis?.overallScore ?? 0)}`}>
                      {p.analysis?.overallScore ?? '-'}
                    </span>
                  </td>
                  <td>{p.analysis?.ukraineBased ? 'Yes' : 'No'}</td>
                  <td>{p.analysis?.lacksWebsite ? 'No' : 'Yes'}</td>
                  <td>{p.analysis?.lacksCrm ? 'No' : 'Yes'}</td>
                  <td>{p.analysis?.sellsViaDms ? 'Yes' : 'No'}</td>
                  <td>{p.analysis?.detectedCategory ?? '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {selected && selected.analysis && (
        <div className="profile-detail">
          <h3>@{selected.username} — Analysis</h3>
          <div className="detail-grid">
            <div><strong>Score:</strong> {selected.analysis.overallScore}/100</div>
            <div><strong>Small Business:</strong> {selected.analysis.smallBusiness ? 'Yes' : 'No'}</div>
            <div><strong>Ukraine-Based:</strong> {selected.analysis.ukraineBased ? 'Yes' : 'No'}</div>
            <div><strong>Lacks Website:</strong> {selected.analysis.lacksWebsite ? 'Yes' : 'No'}</div>
            <div><strong>Lacks CRM:</strong> {selected.analysis.lacksCrm ? 'Yes' : 'No'}</div>
            <div><strong>Lacks TG Bot:</strong> {selected.analysis.lacksTelegramBot ? 'Yes' : 'No'}</div>
            <div><strong>Sells via DMs:</strong> {selected.analysis.sellsViaDms ? 'Yes' : 'No'}</div>
            <div><strong>Active:</strong> {selected.analysis.active ? 'Yes' : 'No'}</div>
            <div><strong>AI Provider:</strong> {selected.analysis.aiProvider}</div>
          </div>
          <div className="reasoning">
            <strong>Bio:</strong> {selected.biography}
          </div>
          {selected.contactPhone && (
            <div className="reasoning">
              <strong>Phone:</strong> {selected.contactPhone}
            </div>
          )}
          {selected.telegramHandle && (
            <div className="reasoning">
              <strong>Telegram:</strong> @{selected.telegramHandle}
            </div>
          )}
          <div className="reasoning">
            <strong>AI Reasoning:</strong> {selected.analysis.reasoning}
          </div>
          {selected.externalUrl && (
            <div className="reasoning">
              <strong>URL:</strong> {selected.externalUrl}
            </div>
          )}
          <button className="close-btn" onClick={() => setSelected(null)}>Close</button>
        </div>
      )}
    </div>
  );
}

function getScoreClass(score: number): string {
  if (score >= 70) return 'high';
  if (score >= 40) return 'medium';
  return 'low';
}
