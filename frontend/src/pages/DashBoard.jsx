import { useState, useEffect } from "react";
import api from "../api/axios";
import "./DashBoard.css";

const DOMAINS = [
    { name: "Java Full Stack",    channel: "@fullstack_devolopment", link: "https://t.me/fullstack_devolopment" },
    { name: "Backend Java",       channel: "@beckend_development",   link: "https://t.me/beckend_development" },
    { name: "Python Data Analyst",channel: "@dataanal_dev",          link: "https://t.me/dataanal_dev" },
    { name: "ML AI Engineer",     channel: "@aiml_engjobs",          link: "https://t.me/aiml_engjobs" },
    { name: "Web Developer",      channel: "@webdev_jobsss",         link: "https://t.me/webdev_jobsss" },
];

export default function DashBoard() {
    const [stats, setStats]   = useState(null);
    const [jobs, setJobs]     = useState([]);
    const [running, setRunning] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError]   = useState("");

    useEffect(() => { fetchData(); }, []);

    const fetchData = async () => {
        setLoading(true);
        setError("");
        try {
            const [statsRes, jobsRes] = await Promise.all([
                api.get("/api/jobs/stats"),
                api.get("/api/jobs?limit=5&sort=score"),
            ]);
            setStats(statsRes.data);
            setJobs(jobsRes.data);
        } catch {
            setError("Failed to load dashboard data.");
        } finally {
            setLoading(false);
        }
    };

    const handleRunNow = async () => {
        setRunning(true);
        try {
            await api.post("/api/jobs/hunt");
            await fetchData();
        } catch { /* long-running call may timeout — that's fine */ }
        finally { setRunning(false); }
    };

    const scoreClass = (s) => s >= 8 ? "score-badge score-high" : "score-badge score-mid";

    return (
        <div className="dash-content">
            <div className="dash-topbar">
                <div className="dash-title">Dashboard</div>
                <div className="dash-topbar-right">
                    <span className="status-dot" />
                    <span className="status-label">Next run: 9:00 AM IST</span>
                    <button className="run-btn" onClick={handleRunNow} disabled={running}>
                        {running ? "Running..." : "Run now"}
                    </button>
                </div>
            </div>

            {error && <div className="dash-error">{error}</div>}

            {loading ? (
                <div className="dash-loading">Loading...</div>
            ) : (
                <div className="dash-body">
                    {/* ── Metrics ── */}
                    <div className="metrics">
                        <div className="metric">
                            <div className="metric-label">Jobs scraped</div>
                            <div className="metric-value">{stats?.totalJobs ?? 0}</div>
                            <div className="metric-sub up">+{stats?.todayJobs ?? 0} today</div>
                        </div>
                        <div className="metric">
                            <div className="metric-label">High matches</div>
                            <div className="metric-value">{stats?.highMatches ?? 0}</div>
                            <div className="metric-sub">Score &gt;= 8</div>
                        </div>
                        <div className="metric">
                            <div className="metric-label">Alerts sent</div>
                            <div className="metric-value">{stats?.alertsSent ?? 0}</div>
                            <div className="metric-sub">via Telegram</div>
                        </div>
                        <div className="metric">
                            <div className="metric-label">Active domains</div>
                            <div className="metric-value">5</div>
                            <div className="metric-sub">Daily 9:00 AM</div>
                        </div>
                    </div>

                    {/* ── Top matches ── */}
                    <div className="section-header">
                        <div className="section-title">Top matches today</div>
                        <a className="view-all" href="/jobs">View all</a>
                    </div>

                    <div className="table-wrap">
                        <table className="jobs-table">
                            <thead>
                            <tr>
                                <th>Role</th>
                                <th>Platform</th>
                                <th>Score</th>
                                <th>Status</th>
                            </tr>
                            </thead>
                            <tbody>
                            {jobs.length === 0 ? (
                                <tr>
                                    <td colSpan={4} className="jobs-empty">
                                        No jobs yet — hit Run now or wait for tomorrow's 9 AM run.
                                    </td>
                                </tr>
                            ) : (
                                jobs.map((job) => (
                                    <tr key={job.id ?? job._id ?? job.url}>
                                        <td>
                                            <a className="job-link" href={job.url} target="_blank" rel="noreferrer">
                                                {job.title}
                                            </a>
                                        </td>
                                        <td><span className="platform-tag">{job.source ?? "LinkedIn"}</span></td>
                                        <td><span className={scoreClass(job.score)}>{job.score}/10</span></td>
                                        <td>
                                            {job.alerted ? (
                                                <><span className="alerted-dot" /><span className="alerted-label">Alerted</span></>
                                            ) : (
                                                <><span className="saved-dot" /><span className="saved-label">Saved</span></>
                                            )}
                                        </td>
                                    </tr>
                                ))
                            )}
                            </tbody>
                        </table>
                    </div>

                    {/* ── Active domains ── */}
                    <div className="section-header">
                        <div className="section-title">Active domains</div>
                    </div>
                    <div className="domains-grid">
                        {DOMAINS.map((d) => {
                            const count = stats?.domainCounts?.[d.channel] ?? 0;
                            return (
                                <div className="domain-card" key={d.channel}>
                                    <div className="domain-info">
                                        <div className="domain-name">{d.name}</div>
                                        <div className="domain-channel">{d.channel}</div>
                                    </div>
                                    <div className="domain-right">
                                        <div className="domain-count">{count}</div>
                                        <a
                                            className="tg-join-btn"
                                            href={d.link}
                                            target="_blank"
                                            rel="noreferrer"
                                        >
                                            Join
                                        </a>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                </div>
            )}
        </div>
    );
}
