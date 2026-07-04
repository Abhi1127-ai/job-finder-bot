import { useState, useRef } from "react";
import "./Resume.css";

const EMPTY = {
    name: "",
    email: "",
    phone: "",
    linkedin: "",
    github: "",
    summary: "",
    skills: "",
    projects: [{ name: "", tech: "", description: "" }],
    achievements: "",
    experience: "",
};

export default function Resume() {
    const raw = localStorage.getItem("user");
    const user = raw ? JSON.parse(raw) : {};

    const [data, setData] = useState({
        ...EMPTY,
        name: user.name ?? "",
        email: user.email ?? "",
    });

    const [saved, setSaved] = useState(false);
    const previewRef = useRef(null);

    const set = (key, val) => setData((d) => ({ ...d, [key]: val }));

    const setProject = (i, key, val) => {
        const updated = [...data.projects];
        updated[i] = { ...updated[i], [key]: val };
        setData((d) => ({ ...d, projects: updated }));
    };

    const addProject = () =>
        setData((d) => ({
            ...d,
            projects: [...d.projects, { name: "", tech: "", description: "" }],
        }));

    const removeProject = (i) =>
        setData((d) => ({
            ...d,
            projects: d.projects.filter((_, idx) => idx !== i),
        }));

    const handleSave = () => {
        localStorage.setItem("resume_data", JSON.stringify(data));
        setSaved(true);
        setTimeout(() => setSaved(false), 2500);
    };

    const handleDownload = () => {
        const preview = previewRef.current;
        if (!preview) return;
        const original = document.title;
        document.title = `${data.name || "Resume"} - Resume`;
        window.print();
        document.title = original;
    };

    return (
        <div className="resume-page">
            <div className="resume-topbar">
                <div>
                    <div className="resume-title">Resume</div>
                    <div className="resume-sub">Used by the AI to score job matches</div>
                </div>
                <div className="resume-actions">
                    {saved && <span className="resume-saved-msg">Saved</span>}
                    <button className="resume-btn-secondary" onClick={handleSave}>
                        Save
                    </button>
                    <button className="resume-btn-primary" onClick={handleDownload}>
                        Download PDF
                    </button>
                </div>
            </div>

            <div className="resume-body">
                {/* ── Editor ── */}
                <div className="resume-editor">

                    <div className="editor-section">
                        <div className="editor-section-title">Contact</div>
                        <div className="editor-row-2">
                            <div className="field-group">
                                <label>Full name</label>
                                <input value={data.name} onChange={(e) => set("name", e.target.value)} placeholder="Abhishek Kumar" />
                            </div>
                            <div className="field-group">
                                <label>Email</label>
                                <input value={data.email} onChange={(e) => set("email", e.target.value)} placeholder="you@gmail.com" />
                            </div>
                        </div>
                        <div className="editor-row-2">
                            <div className="field-group">
                                <label>Phone</label>
                                <input value={data.phone} onChange={(e) => set("phone", e.target.value)} placeholder="+91 98765 43210" />
                            </div>
                            <div className="field-group">
                                <label>LinkedIn URL</label>
                                <input value={data.linkedin} onChange={(e) => set("linkedin", e.target.value)} placeholder="linkedin.com/in/abhishek1127-ai" />
                            </div>
                        </div>
                        <div className="field-group">
                            <label>GitHub URL</label>
                            <input value={data.github} onChange={(e) => set("github", e.target.value)} placeholder="github.com/Abhi1127-ai" />
                        </div>
                    </div>

                    <div className="editor-section">
                        <div className="editor-section-title">Summary</div>
                        <div className="field-group">
              <textarea
                  rows={3}
                  value={data.summary}
                  onChange={(e) => set("summary", e.target.value)}
                  placeholder="Java Spring Boot developer with experience in MongoDB, REST APIs, Microservices, Spring Security, and AI integration. Fresher seeking Backend Developer or SDE internship."
              />
                        </div>
                    </div>

                    <div className="editor-section">
                        <div className="editor-section-title">Tech Stack</div>
                        <div className="field-group">
              <textarea
                  rows={3}
                  value={data.skills}
                  onChange={(e) => set("skills", e.target.value)}
                  placeholder="Java, Spring Boot, MongoDB, MySQL, REST APIs, Spring Security, JWT, Playwright, Docker, Git, Groq AI, Gemini API"
              />
                            <div className="field-hint">Comma-separated. AI uses this to match your profile against job requirements.</div>
                        </div>
                    </div>

                    <div className="editor-section">
                        <div className="editor-section-title-row">
                            <div className="editor-section-title">Projects</div>
                            <button className="add-btn" onClick={addProject}>+ Add</button>
                        </div>
                        {data.projects.map((p, i) => (
                            <div className="project-card" key={i}>
                                <div className="project-card-header">
                                    <span className="project-num">Project {i + 1}</span>
                                    {data.projects.length > 1 && (
                                        <button className="remove-btn" onClick={() => removeProject(i)}>Remove</button>
                                    )}
                                </div>
                                <div className="editor-row-2">
                                    <div className="field-group">
                                        <label>Project name</label>
                                        <input value={p.name} onChange={(e) => setProject(i, "name", e.target.value)} placeholder="Job Finder AI Bot" />
                                    </div>
                                    <div className="field-group">
                                        <label>Tech used</label>
                                        <input value={p.tech} onChange={(e) => setProject(i, "tech", e.target.value)} placeholder="Spring Boot, MongoDB, Playwright" />
                                    </div>
                                </div>
                                <div className="field-group">
                                    <label>Description</label>
                                    <textarea
                                        rows={2}
                                        value={p.description}
                                        onChange={(e) => setProject(i, "description", e.target.value)}
                                        placeholder="Built an autonomous job-scraping bot that scrapes LinkedIn, Internshala, and Unstop, scores matches using Groq AI, and sends Telegram alerts."
                                    />
                                </div>
                            </div>
                        ))}
                    </div>

                    <div className="editor-section">
                        <div className="editor-section-title">Achievements</div>
                        <div className="field-group">
              <textarea
                  rows={3}
                  value={data.achievements}
                  onChange={(e) => set("achievements", e.target.value)}
                  placeholder="- Interned at Sparktiit (Powered by Wipro)&#10;- Interned at Prodigy InfoTech&#10;- B.Tech CSE, ITM University Gwalior (CGPA 7.42)"
              />
                        </div>
                    </div>

                    <div className="editor-section">
                        <div className="editor-section-title">Experience</div>
                        <div className="field-group">
              <textarea
                  rows={3}
                  value={data.experience}
                  onChange={(e) => set("experience", e.target.value)}
                  placeholder="Sparktiit (Powered by Wipro) — Backend Intern&#10;Worked on Java Spring Boot services and REST API development."
              />
                        </div>
                    </div>
                </div>

                {/* ── Preview ── */}
                <div className="resume-preview-wrap">
                    <div className="preview-label">Preview</div>
                    <div className="resume-preview" ref={previewRef} id="resume-print">

                        <div className="rp-header">
                            <div className="rp-name">{data.name || "Your Name"}</div>
                            <div className="rp-contact">
                                {data.email && <span>{data.email}</span>}
                                {data.phone && <><span className="rp-sep">·</span><span>{data.phone}</span></>}
                                {data.linkedin && (
                                    <><span className="rp-sep">·</span>
                                        <a href={`https://${data.linkedin.replace(/^https?:\/\//, "")}`} target="_blank" rel="noreferrer">
                                            LinkedIn
                                        </a></>
                                )}
                                {data.github && (
                                    <><span className="rp-sep">·</span>
                                        <a href={`https://${data.github.replace(/^https?:\/\//, "")}`} target="_blank" rel="noreferrer">
                                            GitHub
                                        </a></>
                                )}
                            </div>
                        </div>

                        {data.summary && (
                            <div className="rp-section">
                                <div className="rp-section-title">Summary</div>
                                <div className="rp-text">{data.summary}</div>
                            </div>
                        )}

                        {data.skills && (
                            <div className="rp-section">
                                <div className="rp-section-title">Tech Stack</div>
                                <div className="rp-skills">
                                    {data.skills.split(",").map((s) => s.trim()).filter(Boolean).map((s) => (
                                        <span className="rp-skill-tag" key={s}>{s}</span>
                                    ))}
                                </div>
                            </div>
                        )}

                        {data.projects.some((p) => p.name) && (
                            <div className="rp-section">
                                <div className="rp-section-title">Projects</div>
                                {data.projects.filter((p) => p.name).map((p, i) => (
                                    <div className="rp-project" key={i}>
                                        <div className="rp-project-header">
                                            <span className="rp-project-name">{p.name}</span>
                                            {p.tech && <span className="rp-project-tech">{p.tech}</span>}
                                        </div>
                                        {p.description && <div className="rp-text">{p.description}</div>}
                                    </div>
                                ))}
                            </div>
                        )}

                        {data.experience && (
                            <div className="rp-section">
                                <div className="rp-section-title">Experience</div>
                                <div className="rp-text rp-pre">{data.experience}</div>
                            </div>
                        )}

                        {data.achievements && (
                            <div className="rp-section">
                                <div className="rp-section-title">Achievements</div>
                                <div className="rp-text rp-pre">{data.achievements}</div>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}
