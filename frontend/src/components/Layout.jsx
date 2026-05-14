import { Outlet } from 'react-router-dom';
import { useState, useEffect } from 'react';
import Sidebar from './Sidebar';
import { fetchStats } from '../api/endpoints';
import './Layout.css';

export default function Layout() {
    const [jobCount, setJobCount] = useState(0);

    useEffect(() => {
        fetchStats()
            .then(r => setJobCount(r.data.totalToday || 0))
            .catch(() => {});
    }, []);

    return (
        <div className="layout">
            <Sidebar jobCount={jobCount} />
            <div className="layout-main">
                <Outlet />
            </div>
        </div>
    );
}
