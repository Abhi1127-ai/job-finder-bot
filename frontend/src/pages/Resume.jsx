import { useState, useEffect } from 'react';
import { fetchSettings, saveSettings } from '../api/endpoints';
import './Resume.css';

export default function Resume() {
    const [resume, setResume]   = useState('');
    const [saving, setSaving]   = useState(false);
    const [saved, setSaved]     = useState(false);
    const [charCount, setCharCount] = useState(0);

    useEffect(() => {
        fetchSettings()
            .then(r => { const r2 = r.data.resume || ''; setResume(r2); setCharCount(r2.length); })
            .catch(() => {});
    }, []);

    const handleChange = val => { setResume(val); setCharCount(val.length); };

    const handleSave = async () => {
        setSaving(true); setSaved(false);
        try {
            await saveSettings({ resume });
            setSaved(true);
            setTimeout(() => setSaved(false), 3000);
        } catch { /* silent */ }
        finally { setSaving(false); }
    };

    return (
        <div className="page">
            <div className="topbar">
                <div className="topbar-left">
                    <div className="page-title">Resume</div>
                    <div className="page-sub">Used by the AI to score job matches</div>
                </div>
                <div className="topbar-right">
                    {saved && <div className="saved-pill"><i className="ti ti-circle-check" />Saved</div>}
                    <button className="btn-primary" onClick={handleSave} disabled={saving}>
                        <i className={`ti ${saving ? 'ti-loader-2 spin' : 'ti-device-floppy'}`} />
                        {saving ? 'Saving...' : 'Save resume'}
                    </button>
                </div>
            </div>

            <div className="page-content">
                <div className="resume-layout">
                    <div className="resume-editor-card card">
                        <div className="resume-editor-header">
                            <div className="s-card-title">Resume text</div>
                            <span className="char-count">{charCount} chars</span>
                        </div>
                        <p className="s-card-desc">
                            Paste your resume here. The AI will use this to evaluate how well each scraped job matches your background. Be detailed — include skills, frameworks, and experience level.
                        </p>
                        <textarea
                            className="resume-textarea"
                            value={resume}
                            onChange={e => handleChange(e.target.value)}
                            placeholder={`Expert Java Spring Boot developer with experience in:
- Spring Boot, Spring MVC, Spring Security
- MongoDB, MySQL, Redis
- Microservices architecture
- REST API design
- Playwright (browser automation)
- Spring AI, Gemini API integration
- Docker, Git
- 2 years experience

Projects:
- Job Finder Bot: AI-powered LinkedIn scraper that matches jobs against resume...`}
                            rows={18}
                        />
                    </div>

                    <div className="resume-tips card">
                        <div className="s-card-title">Tips for better matching</div>
                        <div className="tip-list">
                            <div className="tip-item">
                                <div className="tip-icon"><i className="ti ti-check" /></div>
                                <div>
                                    <div className="tip-title">List all your tech skills explicitly</div>
                                    <div className="tip-desc">Write "Spring Boot, MongoDB, Playwright" — not just "backend development".</div>
                                </div>
                            </div>
                            <div className="tip-item">
                                <div className="tip-icon"><i className="ti ti-check" /></div>
                                <div>
                                    <div className="tip-title">Include your experience level</div>
                                    <div className="tip-desc">e.g. "2 years experience" or "fresher" helps the AI judge seniority fit.</div>
                                </div>
                            </div>
                            <div className="tip-item">
                                <div className="tip-icon"><i className="ti ti-check" /></div>
                                <div>
                                    <div className="tip-title">Mention your project domains</div>
                                    <div className="tip-desc">AI integration, microservices, fintech — context improves matching accuracy.</div>
                                </div>
                            </div>
                            <div className="tip-item">
                                <div className="tip-icon ti-warning"><i className="ti ti-alert-triangle" /></div>
                                <div>
                                    <div className="tip-title">Don't paste your full PDF resume</div>
                                    <div className="tip-desc">Keep it under 1,500 characters to stay within Gemini's token limits.</div>
                                </div>
                            </div>
                        </div>

                        <div className="resume-length-indicator">
                            <div className="rli-label">
                                <span>Resume length</span>
                                <span className={charCount > 1500 ? 'rli-over' : 'rli-ok'}>
                  {charCount > 1500 ? '⚠ Too long' : '✓ Good'}
                </span>
                            </div>
                            <div className="rli-bar">
                                <div className="rli-fill" style={{ width: `${Math.min((charCount / 1500) * 100, 100)}%`, background: charCount > 1500 ? '#E24B4A' : '#639922' }} />
                            </div>
                            <div className="rli-sub">{charCount} / 1500 recommended max</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
