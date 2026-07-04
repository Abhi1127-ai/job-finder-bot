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
        <div className="alerts-page">
            <div className="alerts-topbar">
                <div className="alerts-page-title">Alerts</div>
                <div className="alerts-page-sub">{alerts.length} Telegram alerts sent</div>
            </div>

            <div className="alerts-content">
                {loading ? (
                    <div className="alerts-loading">Loading...</div>
                ) : alerts.length === 0 ? (
                    <div className="alerts-empty">
                        <div className="alerts-empty-title">No alerts sent yet</div>
                        <div className="alerts-empty-desc">
                            Alerts appear here when the bot finds a job scoring &ge; your threshold.
                        </div>
                    </div>
                ) : (
                    <div className="alerts-list">
                        {alerts.map(job => (
                            <div className="alert-card" key={job.id}>

                                {/* left: small circle indicator */}
                                <div className="ac-dot" />

                                {/* center: all text */}
                                <div className="ac-body">
                                    <div className="ac-title">{job.title}</div>
                                    <div className="ac-meta">
                                        {job.company || 'LinkedIn'}
                                        {job.mode ? ` · ${job.mode}` : ' · Remote'}
                                    </div>
                                    {job.analysis && (
                                        <div className="ac-analysis">{job.analysis}</div>
                                    )}
                                </div>

                                {/* right: score + time + apply */}
                                <div className="ac-right">
                                    <div className="ac-score">
                                        {job.score}
                                        <span>/10</span>
                                    </div>
                                    <div className="ac-time">
                                        {job.scrapedAt
                                            ? new Date(job.scrapedAt).toLocaleString('en-IN', {
                                                day: 'numeric', month: 'short',
                                                hour: '2-digit', minute: '2-digit'
                                            })
                                            : ''}
                                    </div>
                                    <a
                                        href={job.url}
                                        target="_blank"
                                        rel="noreferrer"
                                        className="ac-apply-btn"
                                    >
                                        Apply
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
