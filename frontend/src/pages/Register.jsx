import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../services/api';
import { User, Lock, Mail, ArrowRight, Zap, Shield, Loader } from 'lucide-react';

const Register = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    role: 'ROLE_USER'
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      await api.post('/auth/signup', formData);
      setSuccess('Registration successful! Redirecting to login...');
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err) {
      setError(err.response?.data || 'Failed to register. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', padding: '40px 0', position: 'relative', overflow: 'hidden' }}>
      
      <div className="floating-shape" style={{ width: '300px', height: '300px', background: 'rgba(79, 70, 229, 0.15)', top: '-50px', left: '-50px' }}></div>
      <div className="floating-shape" style={{ width: '400px', height: '400px', background: 'rgba(236, 72, 153, 0.1)', bottom: '-100px', right: '-100px', animationDelay: '2s' }}></div>

      <div className="glass-panel animate-fade-in" style={{ padding: '40px', width: '100%', maxWidth: '500px', zIndex: 1 }}>
        
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <div style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', background: 'rgba(236, 72, 153, 0.2)', width: '64px', height: '64px', borderRadius: '50%', marginBottom: '16px' }}>
            <Zap size={32} color="var(--secondary)" />
          </div>
          <h2>Create Account</h2>
          <p style={{ color: 'var(--text-muted)' }}>Join OmniCharge Platform</p>
        </div>

        {error && (
          <div style={{ background: 'rgba(239, 68, 68, 0.1)', color: 'var(--error)', padding: '12px', borderRadius: '8px', marginBottom: '20px', border: '1px solid rgba(239, 68, 68, 0.3)', fontSize: '0.9rem' }}>
            {error}
          </div>
        )}

        {success && (
          <div style={{ background: 'rgba(16, 185, 129, 0.1)', color: 'var(--success)', padding: '12px', borderRadius: '8px', marginBottom: '20px', border: '1px solid rgba(16, 185, 129, 0.3)', fontSize: '0.9rem' }}>
            {success}
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
            <Mail size={20} style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
            <input 
              type="email" 
              name="email"
              placeholder="Email Address" 
              value={formData.email} 
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

          <div className="input-wrapper" style={{ position: 'relative' }}>
            <Shield size={20} style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
            <select 
              name="role" 
              value={formData.role} 
              onChange={handleChange}
              style={{ paddingLeft: '48px', appearance: 'none' }}
            >
              <option value="ROLE_USER">Regular User</option>
              <option value="ROLE_ADMIN">Administrator</option>
            </select>
          </div>

          <button type="submit" className="btn btn-primary" disabled={loading} style={{ marginTop: '10px' }}>
            {loading ? (
              <><Loader className="spin" size={20} /> Creating...</>
            ) : (
              <>Sign Up <ArrowRight size={20} /></>
            )}
          </button>
        </form>

        <div style={{ textAlign: 'center', marginTop: '24px', fontSize: '0.9rem', color: 'var(--text-muted)' }}>
          Already have an account? <Link to="/login" style={{ fontWeight: '500' }}>Sign In</Link>
        </div>
      </div>
    </div>
  );
};

export default Register;
