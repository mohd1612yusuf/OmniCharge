import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import { Lock, User, ArrowRight, Zap } from 'lucide-react';

const Login = () => {
  const [formData, setFormData] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await api.post('/auth/signin', formData);
      const token = response.data.token;
      
      login(token);
      setTimeout(() => {
        const role = JSON.parse(atob(token.split('.')[1])).role;
        if (role === 'ROLE_ADMIN') {
          navigate('/admin');
        } else {
          navigate('/dashboard');
        }
      }, 100);
      
    } catch (err) {
      setError(err.response?.data || 'Failed to login. Please check credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', position: 'relative', overflow: 'hidden' }}>
      
      <div className="floating-shape" style={{ width: '300px', height: '300px', background: 'rgba(79, 70, 229, 0.15)', top: '-50px', left: '-50px' }}></div>
      <div className="floating-shape" style={{ width: '400px', height: '400px', background: 'rgba(236, 72, 153, 0.1)', bottom: '-100px', right: '-100px', animationDelay: '2s' }}></div>

      <div className="glass-panel animate-fade-in" style={{ padding: '40px', width: '100%', maxWidth: '450px', zIndex: 1 }}>
        
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <div style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', background: 'rgba(79, 70, 229, 0.1)', width: '64px', height: '64px', borderRadius: '50%', marginBottom: '16px' }}>
            <Zap size={32} color="var(--primary)" />
          </div>
          <h2>Welcome Back</h2>
          <p style={{ color: 'var(--text-muted)' }}>Login to access OmniCharge</p>
        </div>

        {error && (
          <div style={{ background: 'rgba(239, 68, 68, 0.1)', color: 'var(--error)', padding: '12px', borderRadius: '8px', marginBottom: '20px', border: '1px solid rgba(239, 68, 68, 0.3)', fontSize: '0.9rem' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          
          <div className="input-wrapper" style={{ position: 'relative' }}>
            <User size={20} style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
            <input 
              type="text" 
              name="username"
              placeholder="Username" 
              value={formData.username} 
              onChange={handleChange} 
              required 
              style={{ paddingLeft: '48px' }}
            />
          </div>

          <div className="input-wrapper" style={{ position: 'relative' }}>
            <Lock size={20} style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
            <input 
              type="password" 
              name="password"
              placeholder="Password" 
              value={formData.password} 
              onChange={handleChange} 
              required 
              style={{ paddingLeft: '48px' }}
            />
          </div>

          <button type="submit" className="btn btn-primary" disabled={loading} style={{ marginTop: '10px' }}>
            {loading ? 'Authenticating...' : (
              <>Sign In <ArrowRight size={20} /></>
            )}
          </button>
        </form>

        <div style={{ textAlign: 'center', marginTop: '24px', fontSize: '0.9rem', color: 'var(--text-muted)' }}>
          Don't have an account? <Link to="/register" style={{ fontWeight: '500' }}>Create one</Link>
        </div>
      </div>
    </div>
  );
};

export default Login;
