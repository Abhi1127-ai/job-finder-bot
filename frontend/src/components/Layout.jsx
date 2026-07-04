import { useEffect } from "react";
import { Outlet, useNavigate } from "react-router-dom";
import Sidebar from "./Sidebar";
import "./Layout.css";

export default function Layout() {
    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem("token");
        if (!token) {
            navigate("/login", { replace: true });
        }
    }, [navigate]);

    return (
        <div className="layout">
            <Sidebar />
            <main className="layout-main">
                <Outlet />
            </main>
        </div>
    );
}
