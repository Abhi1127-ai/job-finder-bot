import { useState, useEffect } from 'react';
import { fetchJobs } from '../api/endpoints';
import './Alerts.css';

export default function Alerts() {
    const [alerts, setAlerts] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchJobs({ alerted: true, sort: 'date' })
            .then(r => { setAlerts(r.data); setLoading(false); })
            .catch(() => setLoading(false));
    }, []);

    return (
        <div className="page">
            <div className="topbar">
                <div className="topbar-left">
                    <div className="page-title">Alerts</div>
                    <div className="page-sub">{alerts.length} Telegram alerts sent</div>
                </div>
            </div>

            <div className="page-content">
                {loading ? (
                    <div style={{display:'flex',justifyContent:'center',padding:40}}><div className="spinner" /></div>
                ) : alerts.length === 0 ? (
                    <div className="alerts-empty">
                        <i className="ti ti-bell-off" />
                        <div className="alerts-empty-title">No alerts sent yet</div>
                        <div className="alerts-empty-desc">Alerts appear here when the bot finds a job scoring ≥ your threshold.</div>
                    </div>
                ) : (
                    <div className="alerts-list">
                        {alerts.map(job => (
                            <div className="alert-card card" key={job.id}>
                                <div className="ac-left">
                                    <div className="ac-icon"><i className="ti ti-send" /></div>
                                    <div>
                                        <div className="ac-title">{job.title}</div>
                                        <div className="ac-company"><i className="ti ti-building" />{job.company || 'LinkedIn'} · {job.mode || 'Remote'}</div>
                                        {job.analysis && <div className="ac-analysis">{job.analysis}</div>}
                                    </div>
                                </div>
                                <div className="ac-right">
                                    <div className="ac-score">{job.score}<span>/10</span></div>
                                    <div className="ac-time">{job.scrapedAt ? new Date(job.scrapedAt).toLocaleString('en-IN', { day:'numeric', month:'short', hour:'2-digit', minute:'2-digit' }) : ''}</div>
                                    <a href={job.url} target="_blank" rel="noreferrer" className="btn-primary ac-apply">
                                        <i className="ti ti-external-link" />Apply
                                    </a>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}
