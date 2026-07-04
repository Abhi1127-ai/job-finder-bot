import { useState } from "react";
import api from "../api/axios";
import "./Settings.css";

const TABS = ["Search", "Notifications", "Schedule", "Account"];

export default function Settings() {
    const [tab, setTab] = useState("Search");
    const [saved, setSaved] = useState(false);
    const [saving, setSaving] = useState(false);

    const raw = localStorage.getItem("user");
    const user = raw ? JSON.parse(raw) : {};

    const [account, setAccount] = useState({
        name: user.name ?? "",
        email: user.email ?? "",
        newPassword: "",
        confirmPassword: "",
    });

    const [search, setSearch] = useState({
        jobTitle: "java Full Stack Developer",
        maxJobs: 10,
        threshold: 8,
        deduplication: true,
    });

    const [notifications, setNotifications] = useState({
        telegramEnabled: true,
        emailEnabled: false,
        alertsOnly: true,
    });

    const [schedule, setSchedule] = useState({
        cronTime: "09:00",
        timezone: "Asia/Kolkata",
    });

    const showSaved = () => {
        setSaved(true);
        setTimeout(() => setSaved(false), 2500);
    };

    const handleSave = async () => {
        setSaving(true);
        try {
            if (tab === "Account") {
                if (account.newPassword && account.newPassword !== account.confirmPassword) {
                    alert("Passwords do not match.");
                    return;
                }
                await api.put("/api/auth/profile", {
                    name: account.name,
                    email: account.email,
                    ...(account.newPassword ? { password: account.newPassword } : {}),
                });
                localStorage.setItem("user", JSON.stringify({ name: account.name, email: account.email }));
            }
            showSaved();
        } catch {
            alert("Failed to save. Please try again.");
        } finally {
            setSaving(false);
        }
    };

    return (
        <div className="settings-page">
            <div className="settings-topbar">
                <div>
                    <div className="settings-title">Settings</div>
                    <div className="settings-sub">Configure your job hunt bot</div>
                </div>
                <div className="settings-actions">
                    {saved && <span className="saved-msg">Saved</span>}
                    <button className="settings-save-btn" onClick={handleSave} disabled={saving}>
                        {saving ? "Saving..." : "Save changes"}
                    </button>
                </div>
            </div>

            <div className="settings-body">
                <div className="settings-sidebar">
                    {TABS.map((t) => (
                        <button
                            key={t}
                            className={t === tab ? "stab active" : "stab"}
                            onClick={() => setTab(t)}
                        >
                            {t}
                        </button>
                    ))}
                </div>

                <div className="settings-panel">
                    {tab === "Search" && (
                        <div className="panel-section">
                            <div className="panel-title">Job search configuration</div>
                            <div className="panel-desc">Define what kind of jobs the bot should look for.</div>

                            <div className="field-group">
                                <label>Job title / keywords</label>
                                <input
                                    type="text"
                                    value={search.jobTitle}
                                    onChange={(e) => setSearch((s) => ({ ...s, jobTitle: e.target.value }))}
                                />
                                <div className="field-hint">Passed as the LinkedIn search query.</div>
                            </div>

                            <div className="field-group">
                                <label>Max jobs to scrape per run</label>
                                <input
                                    type="number"
                                    min={1}
                                    max={20}
                                    value={search.maxJobs}
                                    onChange={(e) => setSearch((s) => ({ ...s, maxJobs: +e.target.value }))}
                                    style={{ maxWidth: 100 }}
                                />
                                <div className="field-hint">Keep this at 10 on free tier to avoid quota issues.</div>
                            </div>

                            <div className="field-group">
                                <label>Match score threshold (alert if &gt;= this)</label>
                                <div className="slider-row">
                                    <input
                                        type="range"
                                        min={1}
                                        max={10}
                                        value={search.threshold}
                                        onChange={(e) => setSearch((s) => ({ ...s, threshold: +e.target.value }))}
                                    />
                                    <span className="slider-val">{search.threshold} / 10</span>
                                </div>
                                <div className="field-hint">Only jobs scoring this or above will trigger an alert.</div>
                            </div>

                            <div className="field-group">
                                <div className="toggle-row">
                                    <div>
                                        <div className="toggle-label">Skip already-seen jobs</div>
                                        <div className="field-hint">Jobs with the same URL won't be re-processed on future runs.</div>
                                    </div>
                                    <button
                                        className={search.deduplication ? "toggle on" : "toggle"}
                                        onClick={() => setSearch((s) => ({ ...s, deduplication: !s.deduplication }))}
                                    >
                                        <span className="toggle-knob" />
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}

                    {tab === "Notifications" && (
                        <div className="panel-section">
                            <div className="panel-title">Notification preferences</div>
                            <div className="panel-desc">Choose how you want to receive job alerts.</div>

                            <div className="field-group">
                                <div className="toggle-row">
                                    <div>
                                        <div className="toggle-label">Telegram alerts</div>
                                        <div className="field-hint">Send matched jobs to your Telegram channel.</div>
                                    </div>
                                    <button
                                        className={notifications.telegramEnabled ? "toggle on" : "toggle"}
                                        onClick={() => setNotifications((n) => ({ ...n, telegramEnabled: !n.telegramEnabled }))}
                                    >
                                        <span className="toggle-knob" />
                                    </button>
                                </div>
                            </div>

                            <div className="field-group">
                                <div className="toggle-row">
                                    <div>
                                        <div className="toggle-label">High matches only</div>
                                        <div className="field-hint">Only send alerts for jobs above the score threshold.</div>
                                    </div>
                                    <button
                                        className={notifications.alertsOnly ? "toggle on" : "toggle"}
                                        onClick={() => setNotifications((n) => ({ ...n, alertsOnly: !n.alertsOnly }))}
                                    >
                                        <span className="toggle-knob" />
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}

                    {tab === "Schedule" && (
                        <div className="panel-section">
                            <div className="panel-title">Run schedule</div>
                            <div className="panel-desc">Control when the bot runs each day.</div>

                            <div className="field-group">
                                <label>Daily run time</label>
                                <input
                                    type="time"
                                    value={schedule.cronTime}
                                    onChange={(e) => setSchedule((s) => ({ ...s, cronTime: e.target.value }))}
                                    style={{ maxWidth: 140 }}
                                />
                                <div className="field-hint">The bot will scrape all domains once at this time every day.</div>
                            </div>

                            <div className="field-group">
                                <label>Timezone</label>
                                <select
                                    value={schedule.timezone}
                                    onChange={(e) => setSchedule((s) => ({ ...s, timezone: e.target.value }))}
                                    style={{ maxWidth: 220 }}
                                >
                                    <option value="Asia/Kolkata">Asia/Kolkata (IST)</option>
                                    <option value="UTC">UTC</option>
                                    <option value="America/New_York">America/New_York (EST)</option>
                                    <option value="America/Los_Angeles">America/Los_Angeles (PST)</option>
                                    <option value="Europe/London">Europe/London (GMT)</option>
                                </select>
                            </div>
                        </div>
                    )}

                    {tab === "Account" && (
                        <div className="panel-section">
                            <div className="panel-title">Account</div>
                            <div className="panel-desc">Manage your profile and password.</div>

                            <div className="field-group">
                                <label>Full name</label>
                                <input
                                    type="text"
                                    value={account.name}
                                    onChange={(e) => setAccount((a) => ({ ...a, name: e.target.value }))}
                                    style={{ maxWidth: 320 }}
                                />
                            </div>

                            <div className="field-group">
                                <label>Email</label>
                                <input
                                    type="email"
                                    value={account.email}
                                    onChange={(e) => setAccount((a) => ({ ...a, email: e.target.value }))}
                                    style={{ maxWidth: 320 }}
                                />
                            </div>

                            <div className="panel-divider" />
                            <div className="panel-sub-title">Change password</div>

                            <div className="field-group">
                                <label>New password</label>
                                <input
                                    type="password"
                                    placeholder="Leave blank to keep current"
                                    value={account.newPassword}
                                    onChange={(e) => setAccount((a) => ({ ...a, newPassword: e.target.value }))}
                                    style={{ maxWidth: 320 }}
                                />
                            </div>

                            <div className="field-group">
                                <label>Confirm new password</label>
                                <input
                                    type="password"
                                    placeholder="Repeat new password"
                                    value={account.confirmPassword}
                                    onChange={(e) => setAccount((a) => ({ ...a, confirmPassword: e.target.value }))}
                                    style={{ maxWidth: 320 }}
                                />
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
