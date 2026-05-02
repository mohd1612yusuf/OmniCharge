import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { LogOut, Wifi, Smartphone, CheckCircle, CreditCard, Clock, Search } from 'lucide-react';
import Navbar from '../components/Navbar';

const Dashboard = () => {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  
  const [operators, setOperators] = useState([]);
  const [selectedOperator, setSelectedOperator] = useState(null);
  const [plans, setPlans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const [searchAmount, setSearchAmount] = useState('');

  useEffect(() => {
    fetchOperators();
  }, []);

  const fetchOperators = async () => {
    try {
      setLoading(true);
      const response = await api.get('/operators');
      setOperators(response.data);
    } catch (err) {
      setError('Failed to load operators. Ensure backend services are running.');
    } finally {
      setLoading(false);
    }
  };

  const handleOperatorSelect = async (operator) => {
    setSelectedOperator(operator);
    setSearchAmount('');
    try {
      setLoading(true);
      const response = await api.get(`/operators/${operator.id}/plans`);
      setPlans(response.data);
    } catch (err) {
      setError('Failed to load plans for this operator.');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const initiateRecharge = (plan) => {
    navigate(`/recharge?operatorId=${selectedOperator.id}&planId=${plan.id}`);
  };

  const filteredPlans = plans.filter(plan => plan.price.toString().includes(searchAmount));
  const maxPrice = plans.length > 0 ? Math.max(...plans.map(p => p.price)) : 0;

  return (
    <div style={{ minHeight: '100vh', padding: '24px 0' }}>
      <div className="container">
        
        <Navbar pageName="Dashboard" />

        {error && (
          <div className="animate-fade-in" style={{ position: 'fixed', bottom: '24px', right: '24px', background: 'var(--bg-card)', backdropFilter: 'var(--blur-glass)', borderLeft: '4px solid var(--error)', padding: '16px 24px', borderRadius: '8px', boxShadow: '0 10px 30px rgba(0,0,0,0.1)', zIndex: 1000, display: 'flex', alignItems: 'center', gap: '16px' }}>
            <span style={{ color: 'var(--text-main)', fontWeight: '500' }}>{error}</span>
            <button onClick={() => setError('')} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)', fontSize: '1.2rem' }}>&times;</button>
          </div>
        )}

        <div className="dashboard-grid" style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '32px' }}>
          
          <div>
            <h2 style={{ marginBottom: '20px' }}>Select Operator</h2>
            {loading && !selectedOperator ? (
              <div style={{ color: 'var(--text-muted)' }}>Loading operators...</div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                {operators.map(op => (
                  <div 
                    key={op.id}
                    className="glass-panel animate-fade-in"
                    onClick={() => handleOperatorSelect(op)}
                    style={{ 
                      padding: '20px', 
                      cursor: 'pointer', 
                      transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                      borderColor: selectedOperator?.id === op.id ? 'var(--primary)' : 'var(--border-glass)',
                      background: selectedOperator?.id === op.id ? 'rgba(79, 70, 229, 0.1)' : 'var(--bg-card)',
                      transform: selectedOperator?.id === op.id ? 'scale(1.03)' : 'scale(1)',
                      boxShadow: selectedOperator?.id === op.id ? '0 10px 25px rgba(79, 70, 229, 0.15)' : 'none'
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                      <Wifi size={24} color={selectedOperator?.id === op.id ? 'var(--primary)' : 'var(--text-muted)'} />
                      <div>
                        <h3 style={{ margin: 0 }}>{op.name}</h3>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div>
            {selectedOperator ? (
              <>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                  <h2 style={{ margin: 0 }}>{selectedOperator.name} Plans</h2>
                  <div style={{ position: 'relative', width: '250px' }}>
                    <Search size={18} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }} />
                    <input 
                      type="text" 
                      placeholder="Search by amount (₹)..."
                      value={searchAmount}
                      onChange={(e) => setSearchAmount(e.target.value)}
                      style={{ width: '100%', padding: '10px 10px 10px 40px', fontSize: '0.95rem' }}
                    />
                  </div>
                </div>

                {loading ? (
                  <div style={{ color: 'var(--text-muted)' }}>Loading plans...</div>
                ) : filteredPlans.length > 0 ? (
                  <div className="plans-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '24px', paddingTop: '12px' }}>
                    {filteredPlans.map((plan, i) => {
                      const isPremium = plan.price > 500;
                      const isRecommended = plan.price === maxPrice;
                      const isBestseller = plan.validityDays > 28;
                      const dailyCost = (plan.price / plan.validityDays).toFixed(1);

                      return (
                        <div 
                          key={plan.id} 
                          className={`glass-panel plan-card animate-fade-in ${isPremium ? 'premium-card' : ''}`}
                          style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '16px', animationDelay: `${i * 0.1}s` }}
                        >
                          <div style={{ position: 'absolute', top: '-12px', right: '16px', display: 'flex', gap: '8px' }}>
                            {isRecommended && (
                              <span style={{ background: 'linear-gradient(135deg, #FFD700, #FF8C00)', color: 'white', padding: '4px 14px', borderRadius: '16px', fontSize: '0.75rem', fontWeight: 'bold', boxShadow: '0 4px 10px rgba(255, 140, 0, 0.3)' }}>
                                ⭐ Recommended
                              </span>
                            )}
                            {isBestseller && !isRecommended && (
                              <span style={{ background: 'linear-gradient(135deg, #ec4899, #8b5cf6)', color: 'white', padding: '4px 14px', borderRadius: '16px', fontSize: '0.75rem', fontWeight: 'bold', boxShadow: '0 4px 10px rgba(236, 72, 153, 0.3)' }}>
                                🔥 Bestseller
                              </span>
                            )}
                          </div>

                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                            <div>
                              <h3 style={{ color: 'var(--text-main)', fontSize: '1.8rem', margin: '0 0 4px 0', lineHeight: '1' }}>₹{plan.price}</h3>
                              <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Just ₹{dailyCost} / day</span>
                            </div>
                            <span style={{ background: 'rgba(236, 72, 153, 0.1)', color: 'var(--secondary)', padding: '6px 14px', borderRadius: '16px', fontSize: '0.85rem', fontWeight: '600' }}>
                              {plan.validityDays} Days
                            </span>
                          </div>
                          
                          <div style={{ height: '1px', background: 'var(--border-glass)' }}></div>
                          
                          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '8px' }}>
                              <CheckCircle size={16} color="var(--success)" style={{ marginTop: '3px', minWidth: '16px' }} />
                              <span><strong style={{ color: 'var(--text-main)' }}>Benefits:</strong> {plan.description}</span>
                            </div>
                          </div>

                          <button 
                            className={`btn ${isPremium ? 'premium-btn' : 'btn-primary'}`} 
                            style={{ marginTop: 'auto', width: '100%', padding: '14px' }}
                            onClick={() => initiateRecharge(plan)}
                          >
                            <CreditCard size={18} /> Recharge Now
                          </button>
                        </div>
                      );
                    })}
                  </div>
                ) : (
                  <div className="glass-panel" style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
                    No plans found matching "₹{searchAmount}".
                  </div>
                )}
              </>
            ) : (
              <div className="glass-panel" style={{ padding: '60px 40px', textAlign: 'center', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '16px' }}>
                <div style={{ background: 'rgba(255, 255, 255, 0.05)', padding: '24px', borderRadius: '50%' }}>
                  <Smartphone size={48} color="var(--text-muted)" />
                </div>
                <h3>Select an Operator</h3>
                <p style={{ color: 'var(--text-muted)' }}>Choose your telecom provider from the left panel to browse available recharge plans.</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
