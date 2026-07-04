import { NavLink, useNavigate } from "react-router-dom";
import "./Sidebar.css";

const nav = [
    {
        section: "Main",
        items: [
            { label: "Dashboard", to: "/" },
            { label: "Jobs", to: "/jobs" },
            { label: "Alerts", to: "/alerts" },
        ],
    },
    {
        section: "Config",
        items: [
            { label: "Settings", to: "/settings" },
            { label: "Resume", to: "/resume" },
        ],
    },
];

export default function Sidebar() {
    const navigate = useNavigate();

    const raw = localStorage.getItem("user");
    const user = raw ? JSON.parse(raw) : null;
    const name = user?.name ?? "User";
    const email = user?.email ?? "";
    const initials = name
        .split(" ")
        .filter(Boolean)
        .map((w) => w[0])
        .join("")
        .slice(0, 2)
        .toUpperCase();

    const handleLogout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        navigate("/login");
    };

    return (
        <aside className="sidebar">
            <div className="sb-brand">Job Finder AI</div>

            <nav className="sb-nav">
                {nav.map((group) => (
                    <div key={group.section} className="sb-group">
                        <div className="sb-section">{group.section}</div>
                        {group.items.map((item) => (
                            <NavLink
                                key={item.to}
                                to={item.to}
                                end={item.to === "/"}
                                className={({ isActive }) =>
                                    isActive ? "sb-item active" : "sb-item"
                                }
                            >
                                {item.label}
                            </NavLink>
                        ))}
                    </div>
                ))}
            </nav>

            <div className="sb-bottom">
                <div className="sb-user">
                    <div className="sb-avatar">{initials}</div>
                    <div className="sb-user-info">
                        <div className="sb-user-name">{name}</div>
                        <div className="sb-user-email">{email}</div>
                    </div>
                </div>
                <button className="sb-logout" onClick={handleLogout}>
                    Sign out
                </button>
            </div>
        </aside>
    );
}
