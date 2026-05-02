import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { Plus, Trash2, Edit, Shield, Users, Database, LogOut, CheckCircle, XCircle, ArrowLeft } from 'lucide-react';
import Navbar from '../components/Navbar';

const AdminDashboard = () => {
  const { logout } = useAuth();
  const navigate = useNavigate();
  
  const [operators, setOperators] = useState([]);
  const [newOpName, setNewOpName] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  // Manage Plans State
  const [selectedOperator, setSelectedOperator] = useState(null);
  const [plans, setPlans] = useState([]);
  const [newPlan, setNewPlan] = useState({ name: '', price: '', validityDays: '', description: '' });
  const [plansLoading, setPlansLoading] = useState(false);

  // Custom Confirm Modal State
  const [confirmDialog, setConfirmDialog] = useState({ isOpen: false, title: '', message: '', onConfirm: null });

  const triggerConfirm = (title, message, onConfirm) => {
    setConfirmDialog({ isOpen: true, title, message, onConfirm });
  };

  useEffect(() => {
    fetchOperators();
  }, []);

  const fetchOperators = async () => {
    try {
      setLoading(true);
      const response = await api.get('/operators');
      setOperators(response.data);
    } catch (err) {
      setError('Failed to load operators.');
    } finally {
      setLoading(false);
    }
  };

  const handleAddOperator = async (e) => {
    e.preventDefault();
    if (!newOpName.trim()) return;
    
    try {
      await api.post('/operators', { name: newOpName });
      setNewOpName('');
      fetchOperators();
    } catch (err) {
      setError('Failed to add operator. You must be an admin.');
    }
  };

  const handleDeleteOperator = (id) => {
    triggerConfirm(
      'Delete Operator',
      'Are you sure you want to delete this operator? All associated plans will also be deleted. This cannot be undone.',
      async () => {
        try {
          await api.delete(`/operators/${id}`);
          fetchOperators();
        } catch (err) {
          setError('Failed to delete operator.');
        }
      }
    );
  };

  // --- Manage Plans Functions ---
  const handleEditPlans = async (operator) => {
    setSelectedOperator(operator);
    fetchPlans(operator.id);
  };

  const fetchPlans = async (operatorId) => {
    try {
      setPlansLoading(true);
      const response = await api.get(`/operators/${operatorId}/plans`);
      setPlans(response.data);
    } catch (err) {
      setError('Failed to load plans for this operator.');
    } finally {
      setPlansLoading(false);
    }
  };

  const handleAddPlan = async (e) => {
    e.preventDefault();
    try {
      await api.post(`/operators/${selectedOperator.id}/plans`, newPlan);
      setNewPlan({ name: '', price: '', validityDays: '', description: '' });
      fetchPlans(selectedOperator.id);
    } catch (err) {
      setError('Failed to add plan.');
    }
  };

  const handleDeletePlan = (planId) => {
    triggerConfirm(
      'Delete Plan',
      'Are you sure you want to delete this plan? This action cannot be undone.',
      async () => {
        try {
          await api.delete(`/operators/plans/${planId}`);
          fetchPlans(selectedOperator.id);
        } catch (err) {
          setError('Failed to delete plan.');
        }
      }
    );
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div style={{ minHeight: '100vh', padding: '24px 0', position: 'relative' }}>
      
      {/* Custom Confirm Modal */}
      {confirmDialog.isOpen && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', zIndex: 2000, display: 'flex', alignItems: 'center', justifyContent: 'center', backdropFilter: 'blur(4px)' }}>
          <div className="glass-panel animate-fade-in" style={{ padding: '32px', width: '100%', maxWidth: '400px', background: 'var(--bg-card)' }}>
            <h3 style={{ margin: '0 0 16px 0', color: 'var(--text-main)' }}>{confirmDialog.title}</h3>
            <p style={{ color: 'var(--text-muted)', marginBottom: '24px' }}>{confirmDialog.message}</p>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
              <button className="btn btn-secondary" onClick={() => setConfirmDialog({ isOpen: false, title: '', message: '', onConfirm: null })}>
                Cancel
              </button>
              <button className="btn btn-danger" onClick={() => { confirmDialog.onConfirm(); setConfirmDialog({ isOpen: false, title: '', message: '', onConfirm: null }); }}>
                Confirm Delete
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="container">
        
        {/* Navigation Bar */}
        {/* Navigation Bar */}
        <Navbar pageName="Admin Portal" isAdminView={true} />

        {error && (
          <div style={{ background: 'rgba(239, 68, 68, 0.1)', color: 'var(--error)', padding: '16px', borderRadius: '8px', marginBottom: '24px', border: '1px solid rgba(239, 68, 68, 0.3)' }}>
            {error}
          </div>
        )}

        {!selectedOperator ? (
          /* Operators View */
          <div className="glass-panel animate-fade-in" style={{ padding: '32px' }}>
            <h2 style={{ marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              Manage Operators
            </h2>

            <form onSubmit={handleAddOperator} className="admin-add-form" style={{ display: 'flex', gap: '16px', marginBottom: '32px' }}>
              <input 
                type="text" 
                placeholder="New Operator Name..." 
                value={newOpName}
                onChange={(e) => setNewOpName(e.target.value)}
                style={{ maxWidth: '400px' }}
                required
              />
              <button type="submit" className="btn btn-primary">
                <Plus size={18} /> Add
              </button>
            </form>

            {loading ? (
              <p style={{ color: 'var(--text-muted)' }}>Loading records...</p>
            ) : (
              <div style={{ display: 'grid', gap: '16px' }}>
                {operators.map(op => (
                  <div key={op.id} className="list-row" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px', background: 'rgba(255, 255, 255, 0.05)', borderRadius: '8px', border: '1px solid var(--border-glass)' }}>
                    <h3 style={{ margin: 0 }}>{op.name} <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginLeft: '8px' }}>ID: {op.id}</span></h3>
                    <div style={{ display: 'flex', gap: '12px' }}>
                      <button className="btn btn-secondary" style={{ padding: '8px 12px' }} onClick={() => handleEditPlans(op)}>
                        <Edit size={16} /> Edit Plans
                      </button>
                      <button className="btn btn-danger" style={{ padding: '8px 12px' }} onClick={() => handleDeleteOperator(op.id)}>
                        <Trash2 size={16} /> Delete
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        ) : (
          /* Plans View */
          <div className="glass-panel animate-fade-in" style={{ padding: '32px' }}>
            <button onClick={() => setSelectedOperator(null)} className="btn btn-secondary" style={{ marginBottom: '24px', padding: '8px 12px' }}>
              <ArrowLeft size={16} /> Back to Operators
            </button>

            <h2 style={{ marginBottom: '24px' }}>
              Manage Plans for <span style={{ color: 'var(--primary)' }}>{selectedOperator.name}</span>
            </h2>

            <div style={{ background: 'rgba(0,0,0,0.02)', padding: '24px', borderRadius: '12px', border: '1px solid var(--border-glass)', marginBottom: '32px' }}>
              <h3 style={{ marginBottom: '16px' }}>Add New Plan</h3>
              <form onSubmit={handleAddPlan} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                <div className="admin-plan-grid" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '16px' }}>
                  <input type="text" placeholder="Plan Name (e.g. Hero Unlimited)" value={newPlan.name} onChange={(e) => setNewPlan({...newPlan, name: e.target.value})} required />
                  <input type="number" placeholder="Price (₹)" value={newPlan.price} onChange={(e) => setNewPlan({...newPlan, price: e.target.value})} required />
                  <input type="number" placeholder="Validity (Days)" value={newPlan.validityDays} onChange={(e) => setNewPlan({...newPlan, validityDays: e.target.value})} required />
                </div>
                <div style={{ display: 'flex', gap: '16px' }}>
                  <input type="text" placeholder="Description / Benefits" value={newPlan.description} onChange={(e) => setNewPlan({...newPlan, description: e.target.value})} required style={{ flex: 1 }} />
                  <button type="submit" className="btn btn-primary" style={{ minWidth: '120px' }}>
                    <Plus size={18} /> Add Plan
                  </button>
                </div>
              </form>
            </div>

            {plansLoading ? (
              <p style={{ color: 'var(--text-muted)' }}>Loading plans...</p>
            ) : (
              <div style={{ display: 'grid', gap: '16px' }}>
                {plans.length === 0 && <p style={{ color: 'var(--text-muted)' }}>No plans found for this operator.</p>}
                {plans.map(plan => (
                  <div key={plan.id} className="list-row" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px', background: 'rgba(255, 255, 255, 0.05)', borderRadius: '8px', border: '1px solid var(--border-glass)' }}>
                    <div>
                      <h3 style={{ margin: '0 0 4px 0', fontSize: '1.2rem' }}>{plan.name} <span style={{ color: 'var(--primary)', marginLeft: '8px' }}>₹{plan.price}</span></h3>
                      <p style={{ margin: 0, color: 'var(--text-muted)', fontSize: '0.9rem' }}>Validity: {plan.validityDays} Days | {plan.description}</p>
                    </div>
                    <button className="btn btn-danger" style={{ padding: '8px 12px' }} onClick={() => handleDeletePlan(plan.id)}>
                      <Trash2 size={16} /> Delete
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

      </div>
    </div>
  );
};

export default AdminDashboard;
