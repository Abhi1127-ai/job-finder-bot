import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Sidebar.css';

const navItems = [
    { to: '/',         icon: 'ti-layout-dashboard', label: 'Dashboard' },
    { to: '/jobs',     icon: 'ti-briefcase',         label: 'Jobs'      },
    { to: '/alerts',   icon: 'ti-bell',              label: 'Alerts'    },
];

const configItems = [
    { to: '/settings', icon: 'ti-settings',  label: 'Settings' },
    { to: '/resume',   icon: 'ti-file-text', label: 'Resume'   },
];

export default function Sidebar({ jobCount }) {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => { logout(); navigate('/login'); };
    const initials = user?.name?.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2) || 'U';

    return (
        <aside className="sidebar">
            <div className="sidebar-logo">
                <div className="logo-icon"><i className="ti ti-robot" /></div>
                <div>
                    <div className="logo-name">JobFinder</div>
                    <div className="logo-sub">AI Bot</div>
                </div>
            </div>

            <nav className="sidebar-nav">
                <div className="nav-section-label">Main</div>
                {navItems.map(item => (
                    <NavLink
                        key={item.to}
                        to={item.to}
                        end={item.to === '/'}
                        className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
                    >
                        <i className={`ti ${item.icon}`} />
                        {item.label}
                        {item.label === 'Jobs' && jobCount > 0 && (
                            <span className="nav-badge">{jobCount}</span>
                        )}
                    </NavLink>
                ))}

                <div className="nav-section-label" style={{ marginTop: 16 }}>Config</div>
                {configItems.map(item => (
                    <NavLink
                        key={item.to}
                        to={item.to}
                        className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
                    >
                        <i className={`ti ${item.icon}`} />
                        {item.label}
                    </NavLink>
                ))}
            </nav>

            <div className="sidebar-footer">
                <div className="user-chip">
                    <div className="avatar">{initials}</div>
                    <div className="user-info">
                        <div className="user-name">{user?.name || 'User'}</div>
                        <div className="user-email">{user?.email || ''}</div>
                    </div>
                </div>
                <button className="logout-btn" onClick={handleLogout} title="Logout">
                    <i className="ti ti-logout" />
                </button>
            </div>
        </aside>
    );
}
