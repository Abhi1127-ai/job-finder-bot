import api from './axios';

// Auth
export const login    = (email, password)         => api.post('/api/auth/login',    { email, password });
export const register = (name, email, password)   => api.post('/api/auth/register', { name, email, password });

// Jobs
export const fetchJobs  = (params = {}) => api.get('/api/jobs', { params });
export const fetchStats = ()             => api.get('/api/jobs/stats');
export const deleteJob  = (id)           => api.delete(`/api/jobs/${id}`);
export const triggerHunt = ()            => api.post('/api/jobs/hunt');

// Settings
export const fetchSettings  = ()       => api.get('/api/settings');
export const saveSettings   = (data)   => api.put('/api/settings', data);
