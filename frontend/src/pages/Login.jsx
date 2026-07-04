import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import "./Login.css";

export default function Login() {
    const [mode, setMode] = useState("login");
    const [form, setForm] = useState({ name: "", email: "", password: "" });
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleChange = (e) =>
        setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setLoading(true);
        try {
            const endpoint = mode === "login" ? "/api/auth/login" : "/api/auth/register";
            const payload =
                mode === "login"
                    ? { email: form.email, password: form.password }
                    : { name: form.name, email: form.email, password: form.password };

            const res = await api.post(endpoint, payload);
            const data = res.data;

            localStorage.setItem("token", data.token);
            localStorage.setItem("user", JSON.stringify({ email: data.email, name: data.name }));
            navigate("/");
        } catch (err) {
            setError(
                err.response?.data?.message ||
                (mode === "login" ? "Invalid email or password." : "Registration failed.")
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-wrap">
            <div className="auth-left">
                <div className="auth-brand">Job Finder AI</div>
                <div className="auth-left-content">
                    <h1 className="auth-left-title">
                        {mode === "login" ? "Welcome back." : "Get started."}
                    </h1>
                    <p className="auth-left-sub">
                        {mode === "login"
                            ? "Sign in to view your matches and alerts."
                            : "Create an account to start tracking opportunities."}
                    </p>
                    <button
                        className="auth-switch-btn"
                        onClick={() => {
                            setMode(mode === "login" ? "register" : "login");
                            setError("");
                            setForm({ name: "", email: "", password: "" });
                        }}
                    >
                        {mode === "login" ? "Create account" : "Sign in instead"}
                    </button>
                </div>
            </div>

            <div className="auth-right">
                <div className="auth-form-header">
                    <h2 className="auth-form-title">
                        {mode === "login" ? "Sign in" : "Create account"}
                    </h2>
                    <p className="auth-form-sub">
                        {mode === "login"
                            ? "Enter your credentials to continue"
                            : "Fill in your details to get started"}
                    </p>
                </div>

                <form onSubmit={handleSubmit} className="auth-form">
                    {mode === "register" && (
                        <div className="auth-field">
                            <label htmlFor="name">Name</label>
                            <input
                                id="name"
                                name="name"
                                type="text"
                                placeholder="Abhishek Kumar"
                                value={form.name}
                                onChange={handleChange}
                                required
                                autoComplete="name"
                            />
                        </div>
                    )}

                    <div className="auth-field">
                        <label htmlFor="email">Email</label>
                        <input
                            id="email"
                            name="email"
                            type="email"
                            placeholder="you@example.com"
                            value={form.email}
                            onChange={handleChange}
                            required
                            autoComplete="email"
                        />
                    </div>

                    <div className="auth-field">
                        <label htmlFor="password">Password</label>
                        <input
                            id="password"
                            name="password"
                            type="password"
                            placeholder="Enter your password"
                            value={form.password}
                            onChange={handleChange}
                            required
                            autoComplete={mode === "login" ? "current-password" : "new-password"}
                        />
                    </div>

                    {error && <div className="auth-error">{error}</div>}

                    <button className="auth-submit" type="submit" disabled={loading}>
                        {loading ? "Please wait..." : mode === "login" ? "Sign in" : "Create account"}
                    </button>
                </form>
            </div>
        </div>
    );
}
