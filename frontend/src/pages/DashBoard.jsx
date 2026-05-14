import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { fetchStats, fetchJobs, triggerHunt } from '../api/endpoints';
import '../components/Layout.css';
import './Dashboard.css';

function StatCard({ icon, label, value, delta, deltaDown }) {
    return (
        <div className="stat-card">
            <div className="stat-label"><i className={`ti ${icon}`} />{label}</div>
            <div className="stat-value">{value}</div>
            {delta && <div className={`stat-delta ${deltaDown ? 'down' : ''}`}>{delta}</div>}
        </div>
    );
}

function scoreClass(s) {
    if (s >= 9) return 'score-high';
    if (s >= 7) return 'score-blue';
    if (s >= 5) return 'score-mid';
    return 'score-low';
}

function scoreBadge(s) {
    if (s >= 9) return 'badge badge-green';
    if (s >= 7) return 'badge badge-blue';
    if (s >= 5) return 'badge badge-amber';
    return 'badge badge-red';
}

function scoreBadgeLabel(s) {
    if (s >= 9) return 'Excellent';
    if (s >= 7) return 'Good';
    if (s >= 5) return 'Average';
    return 'Low';
}

export default function Dashboard() {
    const [stats, setStats]     = useState(null);
    const [jobs, setJobs]       = useState([]);
    const [running, setRunning] = useState(false);
    const [runMsg, setRunMsg]   = useState('');

    useEffect(() => {
        fetchStats().then(r => setStats(r.data)).catch(() => {});
        fetchJobs({ limit: 5, sort: 'score' }).then(r => setJobs(r.data)).catch(() => {});
    }, []);

    const handleRun = async () => {
        setRunning(true);
        setRunMsg('');
        try {
            await triggerHunt();
            setRunMsg('Job hunt started! Results will appear shortly.');
            setTimeout(() => {
                fetchStats().then(r => setStats(r.data)).catch(() => {});
                fetchJobs({ limit: 5, sort: 'score' }).then(r => setJobs(r.data)).catch(() => {});
                setRunMsg('');
            }, 8000);
        } catch {
            setRunMsg('Failed to trigger — check your Gemini quota.');
        } finally {
            setRunning(false);
        }
    };

    const lastRun = stats?.lastRunAt
        ? new Date(stats.lastRunAt).toLocaleString('en-IN', { hour: '2-digit', minute: '2-digit', day: 'numeric', month: 'short' })
        : '—';

    return (
        <div className="page">
            <div className="topbar">
                <div className="topbar-left">
                    <div className="page-title">Dashboard</div>
                    <div className="page-sub">Last run: {lastRun}</div>
                </div>
                <div className="topbar-right">
                    <div className="status-pill">
                        <div className="status-dot" />
                        <span>Quota OK</span>
                    </div>
                    <button className="btn-primary" onClick={handleRun} disabled={running}>
                        <i className={`ti ${running ? 'ti-loader-2 spin' : 'ti-player-play'}`} />
                        {running ? 'Running...' : 'Run now'}
                    </button>
                </div>
            </div>

            <div className="page-content">
                {runMsg && (
                    <div className={`run-banner ${runMsg.includes('Failed') ? 'run-banner-error' : 'run-banner-ok'}`}>
                        <i className={`ti ${runMsg.includes('Failed') ? 'ti-alert-circle' : 'ti-circle-check'}`} />
                        {runMsg}
                    </div>
                )}

                <div className="stats-grid">
                    <StatCard icon="ti-database"  label="Jobs scraped"  value={stats?.totalToday ?? '—'} delta="today's run" />
                    <StatCard icon="ti-star"      label="High matches"  value={stats?.highMatches ?? '—'} delta="score ≥ 8" />
                    <StatCard icon="ti-send"      label="Alerts sent"   value={stats?.alertsSent ?? '—'}  delta="via Telegram" />
                    <StatCard icon="ti-clock"     label="Next run"      value="9:00 AM" delta="daily · IST" />
                </div>

                <div className="section-header">
                    <div className="section-title">Top matches today</div>
                    <Link to="/jobs" className="section-link">View all <i className="ti ti-arrow-right" /></Link>
                </div>

                <div className="recent-jobs-table">
                    <div className="rjt-head">
                        <div className="th">Job</div>
                        <div className="th">Score</div>
                        <div className="th">Status</div>
                        <div className="th">Link</div>
                    </div>
                    {jobs.length === 0 ? (
                        <div className="empty-row">No jobs yet — hit Run now or wait for tomorrow's 9 AM run.</div>
                    ) : jobs.slice(0, 5).map(job => (
                        <div className="rjt-row" key={job.id}>
                            <div className="rjt-info">
                                <div className="rjt-title">{job.title}</div>
                                <div className="rjt-company">{job.company || 'LinkedIn'}</div>
                            </div>
                            <div className="rjt-score">
                                <span className={`score-num ${scoreClass(job.score)}`}>{job.score}</span>
                                <div className="mini-bar">
                                    <div className="mini-fill" style={{ width: `${job.score * 10}%`, background: job.score >= 8 ? '#639922' : job.score >= 6 ? '#378ADD' : '#EF9F27' }} />
                                </div>
                            </div>
                            <div><span className={scoreBadge(job.score)}>{scoreBadgeLabel(job.score)}</span></div>
                            <div><a href={job.url} target="_blank" rel="noreferrer" className="apply-link"><i className="ti ti-external-link" />Apply</a></div>
                        </div>
                    ))}
                </div>

                <div className="bottom-grid">
                    <div className="card">
                        <div className="card-title">Recent alerts</div>
                        {(stats?.recentAlerts || []).length === 0 ? (
                            <div className="empty-mini">No alerts sent yet.</div>
                        ) : (stats?.recentAlerts || []).map((a, i) => (
                            <div className="alert-item" key={i}>
                                <div className="alert-icon"><i className="ti ti-send" /></div>
                                <div>
                                    <div className="alert-title">{a.title}</div>
                                    <div className="alert-meta">Score {a.score}/10 · {a.sentAt}</div>
                                </div>
                            </div>
                        ))}
                    </div>

                    <div className="card">
                        <div className="card-title">Bot activity</div>
                        <div className="activity-row"><span>Last run</span><strong>{lastRun}</strong></div>
                        <div className="activity-row"><span>Schedule</span><strong>Daily 9:00 AM IST</strong></div>
                        <div className="activity-row"><span>Search query</span><strong>{stats?.jobTitle || '—'}</strong></div>
                        <div className="activity-row"><span>Alert threshold</span><strong>Score ≥ {stats?.threshold || 8}</strong></div>
                        <div className="activity-row"><span>Total in DB</span><strong>{stats?.totalJobs || 0} jobs</strong></div>
                    </div>
                </div>
            </div>
        </div>
    );
}
