import { useState } from 'react';
import Dashboard from './components/Dashboard';
import ScanForm from './components/ScanForm';
import ScanList from './components/ScanList';
import ProfileTable from './components/ProfileTable';
import './App.css';

function App() {
  const [refreshKey, setRefreshKey] = useState(0);
  const refresh = () => setRefreshKey(k => k + 1);

  return (
    <div className="app-shell">
      <div className="bg-shape bg-shape-one" />
      <div className="bg-shape bg-shape-two" />

      <main className="app">
        <header className="hero">
          <p className="hero-kicker">UA Lead Radar</p>
          <h1>Instagram Growth Engine for Ukrainian Small Business</h1>
          <p className="hero-subtitle">
            Fast discovery of leads without website, CRM, and Telegram bot. Ranked and deduplicated automatically.
          </p>
          <div className="hero-chips">
            <span>Ukraine Focus</span>
            <span>No Duplicates</span>
            <span>AI Scoring</span>
          </div>
        </header>

        <section className="panel panel-dashboard">
          <Dashboard />
        </section>

        <section className="panel panel-scan">
          <ScanForm onScanStarted={refresh} />
        </section>

        <section className="panel panel-scans">
          <ScanList refreshKey={refreshKey} />
        </section>

        <section className="panel panel-leads">
          <ProfileTable refreshKey={refreshKey} />
        </section>
      </main>
    </div>
  );
}

export default App;
