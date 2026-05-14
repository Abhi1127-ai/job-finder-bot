import { useState, useEffect } from 'react';
import { fetchSettings, saveSettings } from '../api/endpoints';
import './Settings.css';

export default function Settings() {
    const [form, setForm]       = useState({
        jobTitle:       'java Full Stack Developer',
        threshold:      8,
        telegramChatId: '',
        schedule:       '0 0 9 * * *',
        timezone:       'Asia/Kolkata',
        maxJobs:        10,
        telegramEnabled: true,
        dedupeEnabled:   true,
    });
    const [saving, setSaving]   = useState(false);
    const [saved, setSaved]     = useState(false);
    const [error, setError]     = useState('');
    const [tab, setTab]         = useState('search');

    useEffect(() => {
        fetchSettings()
            .then(r => setForm(prev => ({ ...prev, ...r.data })))
            .catch(() => {});
    }, []);

    const set = (key, val) => setForm(prev => ({ ...prev, [key]: val }));

    const handleSave = async () => {
        setSaving(true); setError(''); setSaved(false);
        try {
            await saveSettings(form);
            setSaved(true);
            setTimeout(() => setSaved(false), 3000);
        } catch {
            setError('Failed to save settings. Try again.');
        } finally {
            setSaving(false);
        }
    };

    const tabs = [
        { id: 'search',  icon: 'ti-search',   label: 'Search'        },
        { id: 'notify',  icon: 'ti-bell',     label: 'Notifications' },
        { id: 'schedule',icon: 'ti-clock',    label: 'Schedule'      },
        { id: 'account', icon: 'ti-user',     label: 'Account'       },
    ];

    return (
        <div className="page">
            <div className="topbar">
                <div className="topbar-left">
                    <div className="page-title">Settings</div>
                    <div className="page-sub">Configure your job hunt bot</div>
                </div>
                <div className="topbar-right">
                    {saved && (
                        <div className="saved-pill"><i className="ti ti-circle-check" />Saved</div>
                    )}
                    <button className="btn-secondary" onClick={() => setForm({ jobTitle:'java Full Stack Developer', threshold:8, telegramChatId:'', schedule:'0 0 9 * * *', timezone:'Asia/Kolkata', maxJobs:10, telegramEnabled:true, dedupeEnabled:true })}>
                        Reset
                    </button>
                    <button className="btn-primary" onClick={handleSave} disabled={saving}>
                        <i className={`ti ${saving ? 'ti-loader-2 spin' : 'ti-device-floppy'}`} />
                        {saving ? 'Saving...' : 'Save changes'}
                    </button>
                </div>
            </div>

            <div className="page-content">
                {error && <div className="error-banner"><i className="ti ti-alert-circle" />{error}</div>}

                <div className="settings-layout">
                    <div className="settings-sidebar">
                        {tabs.map(t => (
                            <button key={t.id} className={`s-nav-item ${tab === t.id ? 'active' : ''}`} onClick={() => setTab(t.id)}>
                                <i className={`ti ${t.icon}`} />{t.label}
                            </button>
                        ))}
                    </div>

                    <div className="settings-body">

                        {tab === 'search' && (
                            <>
                                <div className="s-card">
                                    <div className="s-card-title">Job search configuration</div>
                                    <div className="s-card-desc">Define what kind of jobs the bot should look for on LinkedIn.</div>

                                    <div className="field">
                                        <label>Job title / keywords</label>
                                        <input type="text" value={form.jobTitle} onChange={e => set('jobTitle', e.target.value)} placeholder="e.g. java Full Stack Developer" />
                                        <span className="hint">This is passed as the LinkedIn search query.</span>
                                    </div>

                                    <div className="field" style={{marginTop:14}}>
                                        <label>Max jobs to scrape per run</label>
                                        <input type="number" value={form.maxJobs} min={1} max={25} onChange={e => set('maxJobs', parseInt(e.target.value))} />
                                        <span className="hint">Keep this ≤ 10 on Gemini free tier to avoid quota issues.</span>
                                    </div>

                                    <div className="field" style={{marginTop:14}}>
                                        <label>Match score threshold (alert if ≥ this)</label>
                                        <div className="threshold-row">
                                            <input type="range" min={1} max={10} value={form.threshold} onChange={e => set('threshold', parseInt(e.target.value))} className="range-input" />
                                            <div className="threshold-badge">{form.threshold} / 10</div>
                                        </div>
                                        <span className="hint">Only jobs scoring {form.threshold} or above will trigger a Telegram alert.</span>
                                    </div>
                                </div>

                                <div className="s-card">
                                    <div className="s-card-title">Deduplication</div>
                                    <div className="toggle-row">
                                        <div>
                                            <div className="toggle-label">Skip already-seen jobs</div>
                                            <div className="toggle-desc">Jobs with the same URL won't be re-processed on future runs.</div>
                                        </div>
                                        <div className={`toggle ${form.dedupeEnabled ? 'on' : 'off'}`} onClick={() => set('dedupeEnabled', !form.dedupeEnabled)} />
                                    </div>
                                </div>
                            </>
                        )}

                        {tab === 'notify' && (
                            <div className="s-card">
                                <div className="s-card-title">Telegram notifications</div>
                                <div className="s-card-desc">Get instant alerts on Telegram when a high-match job is found.</div>

                                <div className="toggle-row" style={{marginBottom:16}}>
                                    <div>
                                        <div className="toggle-label">Enable Telegram alerts</div>
                                        <div className="toggle-desc">Sends a message to your Telegram chat when score ≥ threshold.</div>
                                    </div>
                                    <div className={`toggle ${form.telegramEnabled ? 'on' : 'off'}`} onClick={() => set('telegramEnabled', !form.telegramEnabled)} />
                                </div>

                                <div className="field">
                                    <label>Telegram Chat ID</label>
                                    <input
                                        type="text"
                                        value={form.telegramChatId}
                                        onChange={e => set('telegramChatId', e.target.value)}
                                        placeholder="e.g. 123456789"
                                        disabled={!form.telegramEnabled}
                                    />
                                    <span className="hint">
                    Get your Chat ID by messaging <strong>@userinfobot</strong> on Telegram.
                  </span>
                                </div>

                                <div className="info-box" style={{marginTop:14}}>
                                    <i className="ti ti-info-circle" />
                                    <span>The bot token is configured in your Spring Boot <code>application.properties</code> — not stored here.</span>
                                </div>
                            </div>
                        )}

                        {tab === 'schedule' && (
                            <div className="s-card">
                                <div className="s-card-title">Schedule configuration</div>
                                <div className="s-card-desc">Control when the bot runs automatically.</div>

                                <div className="field">
                                    <label>Cron expression</label>
                                    <input type="text" value={form.schedule} onChange={e => set('schedule', e.target.value)} placeholder="0 0 9 * * *" />
                                    <span className="hint">Default: <code>0 0 9 * * *</code> — runs every day at 9:00 AM.</span>
                                </div>

                                <div className="field" style={{marginTop:14}}>
                                    <label>Timezone</label>
                                    <select value={form.timezone} onChange={e => set('timezone', e.target.value)}>
                                        <option value="Asia/Kolkata">Asia/Kolkata (IST, UTC+5:30)</option>
                                        <option value="UTC">UTC</option>
                                        <option value="America/New_York">America/New_York (EST)</option>
                                        <option value="Europe/London">Europe/London (GMT)</option>
                                    </select>
                                </div>

                                <div className="schedule-preview">
                                    <i className="ti ti-clock" />
                                    <span>Next run: <strong>Tomorrow at 9:00 AM IST</strong></span>
                                </div>

                                <div className="info-box" style={{marginTop:12}}>
                                    <i className="ti ti-alert-triangle" />
                                    <span>Cron changes require a Spring Boot restart to take effect. Save here to persist, then restart the backend.</span>
                                </div>
                            </div>
                        )}

                        {tab === 'account' && (
                            <div className="s-card">
                                <div className="s-card-title">Account</div>
                                <div className="s-card-desc">Manage your profile and password.</div>

                                <div className="field">
                                    <label>Full name</label>
                                    <input type="text" placeholder="Abhishek Kumar" />
                                </div>
                                <div className="field" style={{marginTop:14}}>
                                    <label>Email</label>
                                    <input type="email" placeholder="you@example.com" />
                                </div>
                                <div className="field" style={{marginTop:14}}>
                                    <label>New password</label>
                                    <input type="password" placeholder="Leave blank to keep current" />
                                </div>
                                <div className="field" style={{marginTop:14}}>
                                    <label>Confirm new password</label>
                                    <input type="password" placeholder="Repeat new password" />
                                </div>

                                <div style={{marginTop:16, display:'flex', justifyContent:'flex-end'}}>
                                    <button className="btn-primary"><i className="ti ti-device-floppy" />Update profile</button>
                                </div>
                            </div>
                        )}

                    </div>
                </div>
            </div>
        </div>
    );
}
