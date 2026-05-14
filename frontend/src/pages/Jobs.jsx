import { useState, useEffect } from 'react';
import { fetchJobs, deleteJob } from '../api/endpoints';
import './Jobs.css';

function scoreClass(s) {
    if (s >= 9) return 'score-high'; if (s >= 7) return 'score-blue';
    if (s >= 5) return 'score-mid';  return 'score-low';
}
function badgeClass(s) {
    if (s >= 9) return 'badge badge-green'; if (s >= 7) return 'badge badge-blue';
    if (s >= 5) return 'badge badge-amber'; return 'badge badge-red';
}
function badgeLabel(s) {
    if (s >= 9) return 'Excellent'; if (s >= 7) return 'Good';
    if (s >= 5) return 'Average';   return 'Low';
}

export default function Jobs() {
    const [jobs, setJobs]           = useState([]);
    const [selected, setSelected]   = useState(null);
    const [filter, setFilter]       = useState('all');
    const [search, setSearch]       = useState('');
    const [sortBy, setSortBy]       = useState('score');
    const [loading, setLoading]     = useState(true);

    const loadJobs = () => {
        setLoading(true);
        fetchJobs().then(r => { setJobs(r.data); setLoading(false); }).catch(() => setLoading(false));
    };

    useEffect(() => { loadJobs(); }, []);

    const handleDelete = async (id, e) => {
        e.stopPropagation();
        await deleteJob(id);
        setJobs(prev => prev.filter(j => j.id !== id));
        if (selected?.id === id) setSelected(null);
    };

    const filtered = jobs
        .filter(j => {
            if (filter === 'high')    return j.score >= 8;
            if (filter === 'alerted') return j.alerted;
            if (filter === 'remote')  return j.mode?.toLowerCase() === 'remote';
            return true;
        })
        .filter(j => !search || j.title?.toLowerCase().includes(search.toLowerCase()) || j.company?.toLowerCase().includes(search.toLowerCase()))
        .sort((a, b) => sortBy === 'score' ? b.score - a.score : new Date(b.scrapedAt) - new Date(a.scrapedAt));

    return (
        <div className="page">
            <div className="topbar">
                <div className="topbar-left">
                    <div className="page-title">Jobs</div>
                    <div className="page-sub">{jobs.length} jobs in database</div>
                </div>
                <div className="topbar-right">
                    <button className="btn-secondary"><i className="ti ti-download" />Export CSV</button>
                </div>
            </div>

            <div className="page-content">
                <div className="jobs-toolbar">
                    <div className="search-box">
                        <i className="ti ti-search" />
                        <input placeholder="Search jobs, companies..." value={search} onChange={e => setSearch(e.target.value)} />
                    </div>
                    <div className="filter-chips">
                        {['all','high','alerted','remote'].map(f => (
                            <button key={f} className={`filter-chip ${filter === f ? 'active' : ''}`} onClick={() => setFilter(f)}>
                                {f === 'all' ? 'All' : f === 'high' ? '★ High match' : f === 'alerted' ? '✈ Alerted' : '🌐 Remote'}
                            </button>
                        ))}
                    </div>
                </div>

                <div className="jobs-sort-row">
                    <span className="result-count">{filtered.length} results</span>
                    <select className="sort-select" value={sortBy} onChange={e => setSortBy(e.target.value)}>
                        <option value="score">Sort: Score ↓</option>
                        <option value="date">Sort: Newest</option>
                    </select>
                </div>

                <div className={`jobs-layout ${selected ? 'has-detail' : ''}`}>
                    <div className="jobs-table">
                        <div className="jt-head">
                            <div className="th">Job</div>
                            <div className="th">Date</div>
                            <div className="th">Score</div>
                            <div className="th">Status</div>
                            <div className="th">Mode</div>
                            <div className="th" />
                        </div>

                        {loading ? (
                            <div className="empty-row"><div className="spinner" style={{margin:'0 auto'}} /></div>
                        ) : filtered.length === 0 ? (
                            <div className="empty-row">No jobs match your filter.</div>
                        ) : filtered.map(job => (
                            <div key={job.id} className={`jt-row ${selected?.id === job.id ? 'selected' : ''}`} onClick={() => setSelected(job)}>
                                <div className="jt-info">
                                    <div className="jt-title">{job.title}</div>
                                    <div className="jt-company"><i className="ti ti-building" />{job.company || 'LinkedIn'}</div>
                                </div>
                                <div className="jt-date">{job.scrapedAt ? new Date(job.scrapedAt).toLocaleDateString('en-IN', { day:'numeric', month:'short' }) : 'Today'}</div>
                                <div className="jt-score">
                                    <span className={`score-num ${scoreClass(job.score)}`}>{job.score}</span>
                                    <div className="mini-bar"><div className="mini-fill" style={{ width:`${job.score*10}%`, background: job.score>=8?'#639922':job.score>=6?'#378ADD':'#EF9F27' }} /></div>
                                </div>
                                <div><span className={badgeClass(job.score)}>{badgeLabel(job.score)}</span></div>
                                <div>{job.mode === 'Remote' ? <span className="mode-tag remote">Remote</span> : <span className="mode-tag onsite">On-site</span>}</div>
                                <div><button className="icon-btn" onClick={e => handleDelete(job.id, e)} title="Delete"><i className="ti ti-trash" /></button></div>
                            </div>
                        ))}
                    </div>

                    {selected && (
                        <div className="detail-panel">
                            <div className="dp-header">
                                <div>
                                    <div className="dp-title">{selected.title}</div>
                                    <div className="dp-company">{selected.company} · {selected.location} · {selected.mode}</div>
                                </div>
                                <div className="dp-score-box">
                                    <div className={`dp-score ${scoreClass(selected.score)}`}>{selected.score}</div>
                                    <div className="dp-score-label">/ 10</div>
                                </div>
                            </div>

                            {selected.analysis && (
                                <div className="dp-section">
                                    <div className="dp-section-title">AI analysis</div>
                                    <div className="dp-analysis">{selected.analysis}</div>
                                </div>
                            )}

                            <div className="dp-section">
                                <div className="dp-section-title">Details</div>
                                <div className="dp-details">
                                    <span>Mode</span><strong>{selected.mode || '—'}</strong>
                                    <span>Type</span><strong>{selected.type || 'Full-time'}</strong>
                                    <span>Alerted</span><strong style={{color: selected.alerted ? 'var(--green-600)' : 'var(--text-tertiary)'}}>{selected.alerted ? '✓ via Telegram' : 'No'}</strong>
                                    <span>Scraped</span><strong>{selected.scrapedAt ? new Date(selected.scrapedAt).toLocaleDateString() : '—'}</strong>
                                </div>
                            </div>

                            <a href={selected.url} target="_blank" rel="noreferrer" className="btn-primary dp-apply">
                                <i className="ti ti-external-link" />Apply on LinkedIn
                            </a>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
