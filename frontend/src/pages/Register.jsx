import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Auth.css';

export default function Register() {
    const { register } = useAuth();
    const [name, setName]         = useState('');
    const [email, setEmail]       = useState('');
    const [password, setPassword] = useState('');
    const [error, setError]       = useState('');
    const [loading, setLoading]   = useState(false);

    const handleSubmit = async e => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            await register(name, email, password);
        } catch (err) {
            setError(err.response?.data?.message || 'Registration failed. Try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-screen">
            <div className="auth-card">
                <div className="auth-logo">
                    <div className="auth-logo-icon"><i className="ti ti-robot" /></div>
                    <div>
                        <div className="auth-logo-name">JobFinder</div>
                        <div className="auth-logo-sub">AI Bot</div>
                    </div>
                </div>

                <h1 className="auth-title">Create account</h1>
                <p className="auth-desc">Start finding your dream job automatically</p>

                {error && <div className="auth-error"><i className="ti ti-alert-circle" />{error}</div>}

                <form onSubmit={handleSubmit} className="auth-form">
                    <div className="field">
                        <label htmlFor="name">Full name</label>
                        <input
                            id="name" type="text" placeholder="Abhishek Kumar"
                            value={name} onChange={e => setName(e.target.value)} required
                        />
                    </div>
                    <div className="field">
                        <label htmlFor="email">Email</label>
                        <input
                            id="email" type="email" placeholder="you@example.com"
                            value={email} onChange={e => setEmail(e.target.value)} required
                        />
                    </div>
                    <div className="field">
                        <label htmlFor="password">Password</label>
                        <input
                            id="password" type="password" placeholder="min 8 characters"
                            value={password} onChange={e => setPassword(e.target.value)}
                            minLength={8} required
                        />
                    </div>
                    <button type="submit" className="btn-primary auth-submit" disabled={loading}>
                        {loading ? <><div className="spinner-sm" /> Creating account...</> : 'Create account'}
                    </button>
                </form>

                <p className="auth-switch">
                    Already have an account? <Link to="/login">Sign in</Link>
                </p>
            </div>
        </div>
    );
}
