import { useState } from 'react';
import { api } from '../api/client';

interface Props {
  onScanStarted: () => void;
}

export default function ScanForm({ onScanStarted }: Props) {
  const [query, setQuery] = useState('');
  const [dataSource, setDataSource] = useState<'INSTAGRAM4J' | 'APIFY' | 'RAPIDAPI'>('APIFY');
  const [aiProvider, setAiProvider] = useState<'OLLAMA' | 'GEMINI'>('OLLAMA');
  const [limit, setLimit] = useState(60);
  const [loading, setLoading] = useState(false);
  const [mode, setMode] = useState<'scan' | 'ua' | 'single'>('ua');
  const [username, setUsername] = useState('');
  const [niches, setNiches] = useState('beauty, coffee, flowers, bakery');
  const [cities, setCities] = useState('kyiv, lviv, odesa, dnipro');

  const handleScan = async () => {
    if (!query.trim()) return;
    setLoading(true);
    try {
      await api.startScan({ query: query.trim(), dataSource, aiProvider, limit });
      onScanStarted();
      setQuery('');
    } catch (e) {
      console.error('Scan failed:', e);
    } finally {
      setLoading(false);
    }
  };

  const handleAnalyze = async () => {
    if (!username.trim()) return;
    setLoading(true);
    try {
      await api.analyzeProfile({ username: username.trim(), dataSource, aiProvider });
      onScanStarted();
      setUsername('');
    } catch (e) {
      console.error('Analysis failed:', e);
    } finally {
      setLoading(false);
    }
  };

  const handleUkraineScan = async () => {
    setLoading(true);
    try {
      await api.startUkraineScan({
        dataSource,
        aiProvider,
        limit,
        niches: parseCsv(niches),
        cities: parseCsv(cities),
      });
      onScanStarted();
    } catch (e) {
      console.error('Ukraine scan failed:', e);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="scan-form">
      <div className="form-tabs">
        <button className={mode === 'scan' ? 'active' : ''} onClick={() => setMode('scan')}>
          Batch Scan
        </button>
        <button className={mode === 'ua' ? 'active' : ''} onClick={() => setMode('ua')}>
          Ukraine Auto Scan
        </button>
        <button className={mode === 'single' ? 'active' : ''} onClick={() => setMode('single')}>
          Single Profile
        </button>
      </div>

      <div className="form-row">
        <select value={dataSource} onChange={e => setDataSource(e.target.value as typeof dataSource)}>
          <option value="APIFY">Apify</option>
          <option value="RAPIDAPI">RapidAPI</option>
          <option value="INSTAGRAM4J">Instagram4j</option>
        </select>
        <select value={aiProvider} onChange={e => setAiProvider(e.target.value as typeof aiProvider)}>
          <option value="OLLAMA">Ollama</option>
          <option value="GEMINI">Gemini</option>
        </select>
      </div>

      {mode === 'scan' ? (
        <div className="form-row">
          <input
            type="text"
            placeholder="Hashtag or keyword (e.g. handmade_kyiv)"
            value={query}
            onChange={e => setQuery(e.target.value)}
          />
          <input
            type="number"
            placeholder="Limit"
            value={limit}
            onChange={e => setLimit(Number(e.target.value))}
            min={1}
            max={200}
            style={{ width: '80px' }}
          />
          <button onClick={handleScan} disabled={loading || !query.trim()}>
            {loading ? 'Scanning...' : 'Start Scan'}
          </button>
        </div>
      ) : mode === 'ua' ? (
        <>
          <div className="form-row">
            <input
              type="text"
              placeholder="Niches (comma-separated)"
              value={niches}
              onChange={e => setNiches(e.target.value)}
            />
            <input
              type="text"
              placeholder="Cities (comma-separated)"
              value={cities}
              onChange={e => setCities(e.target.value)}
            />
          </div>
          <div className="form-row" style={{ marginTop: '10px' }}>
            <input
              type="number"
              placeholder="Limit"
              value={limit}
              onChange={e => setLimit(Number(e.target.value))}
              min={1}
              max={500}
              style={{ width: '120px' }}
            />
            <button onClick={handleUkraineScan} disabled={loading}>
              {loading ? 'Scanning...' : 'Start Ukraine Scan'}
            </button>
          </div>
        </>
      ) : (
        <div className="form-row">
          <input
            type="text"
            placeholder="@username"
            value={username}
            onChange={e => setUsername(e.target.value)}
          />
          <button onClick={handleAnalyze} disabled={loading || !username.trim()}>
            {loading ? 'Analyzing...' : 'Analyze'}
          </button>
        </div>
      )}
    </div>
  );
}

function parseCsv(value: string): string[] {
  return value
    .split(',')
    .map(item => item.trim())
    .filter(Boolean);
}
