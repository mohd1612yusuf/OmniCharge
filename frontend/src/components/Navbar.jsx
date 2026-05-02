import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Zap, Bell, User, LogOut, ChevronRight, Settings } from 'lucide-react';

const Navbar = ({ pageName, isAdminView = false }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getInitial = () => {
    if (user && user.username) {
      return user.username.charAt(0).toUpperCase();
    }
    return 'U';
  };

  return (
    <nav className="glass-panel sticky-nav" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px 24px', marginBottom: '32px', borderRadius: '0 0 16px 16px', borderTop: 'none', borderLeft: 'none', borderRight: 'none' }}>
      

      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
        <div style={{ background: isAdminView ? 'var(--error)' : 'var(--primary)', padding: '8px', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Zap color="white" size={24} />
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontWeight: '500' }}>
          <h3 style={{ margin: 0, color: 'var(--text-main)' }}>OmniCharge</h3>
          {pageName && (
            <span className="nav-breadcrumb" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <ChevronRight size={16} color="var(--text-muted)" />
              <span style={{ color: 'var(--text-muted)' }}>{pageName}</span>
            </span>
          )}
        </div>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '24px', position: 'relative' }}>
        {user?.role === 'ROLE_ADMIN' && !isAdminView && (
          <button onClick={() => navigate('/admin')} className="btn btn-secondary" style={{ padding: '6px 12px', fontSize: '0.85rem' }}>
            Admin Portal
          </button>
        )}
        {isAdminView && (
          <button onClick={() => navigate('/dashboard')} className="btn btn-secondary" style={{ padding: '6px 12px', fontSize: '0.85rem' }}>
            User View
          </button>
        )}


        <div style={{ position: 'relative' }}>
          <Bell size={24} color="var(--text-muted)" className="bell-icon" />
          <div style={{ position: 'absolute', top: 0, right: 0, width: '8px', height: '8px', background: 'var(--error)', borderRadius: '50%', border: '2px solid var(--bg-card)' }}></div>
        </div>


        <div 
          className="avatar-circle" 
          onClick={() => setDropdownOpen(!dropdownOpen)}
        >
          {getInitial()}
        </div>


        {dropdownOpen && (
          <div className="nav-dropdown">
            <div style={{ padding: '12px 16px', borderBottom: '1px solid var(--border-glass)', marginBottom: '8px' }}>
              <strong style={{ display: 'block', color: 'var(--text-main)', fontSize: '0.9rem' }}>{user?.username}</strong>
              <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>{user?.role === 'ROLE_ADMIN' ? 'Administrator' : 'Standard User'}</span>
            </div>
            
            <div className="dropdown-item" onClick={() => { setDropdownOpen(false); }}>
              <User size={16} /> Profile
            </div>
            <div className="dropdown-item" onClick={() => { setDropdownOpen(false); }}>
              <Settings size={16} /> Settings
            </div>
            
            <div style={{ height: '1px', background: 'var(--border-glass)', margin: '8px 0' }}></div>
            
            <div className="dropdown-item danger" onClick={handleLogout}>
              <LogOut size={16} /> Logout
            </div>
          </div>
        )}

      </div>
    </nav>
  );
};

export default Navbar;
