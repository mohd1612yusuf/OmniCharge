import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import { 
  ArrowLeft, CheckCircle, CreditCard, Building2, Wallet, Smartphone, 
  ShieldCheck, Activity, Loader, RefreshCw, FileText, Copy, Check, 
  BatteryCharging, CheckCircle2, Clock, XCircle, X, Zap 
} from 'lucide-react';

const loadRazorpayScript = () => {
  return new Promise((resolve) => {
    const script = document.createElement("script");
    script.src = "https://checkout.razorpay.com/v1/checkout.js";
    script.onload = () => resolve(true);
    script.onerror = () => resolve(false);
    document.body.appendChild(script);
  });
};

const Recharge = () => {
  const { user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const queryParams = new URLSearchParams(location.search);
  const operatorId = queryParams.get('operatorId');
  const planId = queryParams.get('planId');

  const [mobileNumber, setMobileNumber] = useState('');
  const [status, setStatus] = useState('idle'); // idle | processing | success | error
  const [message, setMessage] = useState('');
  const [history, setHistory] = useState([]);
  
  // New States
  const [planDetails, setPlanDetails] = useState(null);
  const [paymentMode, setPaymentMode] = useState('credit_card');
  const [rechargeRecord, setRechargeRecord] = useState(null);
  
  // UI States for new features
  const [selectedReceipt, setSelectedReceipt] = useState(null);
  const [copiedId, setCopiedId] = useState(null);

  useEffect(() => {
    if (user?.username) {
      fetchHistory();
    }
    if (planId) {
      fetchPlanDetails();
    }
  }, [user, planId]);

  const fetchPlanDetails = async () => {
    try {
      const response = await api.get(`/operators/plans/${planId}`);
      setPlanDetails(response.data);
    } catch (err) {
      console.error("Failed to fetch plan details", err);
    }
  };

  const fetchHistory = async () => {
    try {
      const userRes = await api.get(`/users/username/${user.username}`);
      const userId = userRes.data.id;
      
      const historyRes = await api.get(`/recharges/user/${userId}`);
      setHistory(historyRes.data);
    } catch (err) {
      console.error("Failed to fetch history", err);
    }
  };

  const handlePayWithRazorpay = async (e) => {
    e.preventDefault();
    if (mobileNumber.length !== 10) {
      setStatus('error');
      setMessage('Mobile number must be 10 digits.');
      return;
    }

    // 1. Load Razorpay SDK
    const isLoaded = await loadRazorpayScript();
    if (!isLoaded) {
      alert("Failed to load Razorpay. Check your internet connection.");
      return;
    }

    try {
      // 2. Create order on backend
      const response = await api.post("/payments/create-order", {
        amount: planDetails.price * 100, // convert ₹ to paise
      });

      const orderData = response.data;
      if (!orderData.orderId) throw new Error("Failed to create order");

      // 3. Open Razorpay Checkout popup
      const options = {
        key: orderData.keyId,
        amount: orderData.amount,
        currency: orderData.currency,
        name: "OmniCharge",
        description: `Recharge ₹${planDetails.price} for ${mobileNumber}`,
        order_id: orderData.orderId,
        handler: async function (paymentResponse) {
          // 4. Payment success — trigger recharge on your backend
          console.log("Payment successful:", paymentResponse);
          await initiateRecharge(paymentResponse.razorpay_payment_id);
        },
        prefill: {
          name: user?.username || "",
          email: user?.email || "",
          contact: mobileNumber,
        },
        theme: {
          color: "#4f46e5", // matches your app's primary color
        },
        modal: {
          ondismiss: function () {
            console.log("Payment popup closed by user.");
            setStatus('idle');
          },
        },
      };

      const rzp = new window.Razorpay(options);
      rzp.open();
    } catch (error) {
      console.error("Payment error:", error);
      setStatus('error');
      setMessage("Something went wrong. Please try again.");
    }
  };

  const initiateRecharge = async (paymentId) => {
    setStatus('processing');
    try {
      const userRes = await api.get(`/users/username/${user.username}`);
      const userId = userRes.data.id;

      const payload = {
        mobileNumber,
        operatorId: parseInt(operatorId),
        planId: parseInt(planId),
        userId: userId
      };

      const response = await api.post('/recharges/initiate', payload);
      setRechargeRecord(response.data);
      
      setStatus('success');
      setMessage('Payment Successful! Your recharge has been processed.');
      fetchHistory();
    } catch (err) {
      setStatus('error');
      setMessage(err.response?.data?.message || 'Failed to initiate recharge.');
    }
  };

  const handleRepeatRecharge = (record) => {
    setMobileNumber(record.mobileNumber);
    if (record.planId.toString() !== planId || record.operatorId.toString() !== operatorId) {
      navigate(`/recharge?operatorId=${record.operatorId}&planId=${record.planId}`);
    }
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const copyToClipboard = (text, id) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const getStatusIcon = (txnStatus) => {
    switch (txnStatus) {
      case 'SUCCESS': return <CheckCircle2 size={16} color="var(--success)" />;
      case 'PENDING': return <Clock size={16} color="var(--warning)" />;
      case 'FAILED': return <XCircle size={16} color="var(--error)" />;
      default: return null;
    }
  };

  // SUCCESS PAGE VIEW
  if (status === 'success') {
    return (
      <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '24px' }}>
        <div className="glass-panel animate-fade-in" style={{ padding: '40px', maxWidth: '500px', width: '100%', textAlign: 'center' }}>
          <CheckCircle size={64} color="var(--success)" style={{ margin: '0 auto 24px' }} />
          <h2 style={{ marginBottom: '16px', color: 'var(--success)' }}>Payment Successful!</h2>
          <p style={{ color: 'var(--text-muted)', marginBottom: '32px' }}>Your recharge has been initiated and payment is confirmed.</p>
          
          <div style={{ background: 'rgba(0,0,0,0.02)', padding: '24px', borderRadius: '12px', textAlign: 'left', marginBottom: '32px', border: '1px solid var(--border-glass)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px' }}>
              <span style={{ color: 'var(--text-muted)' }}>Mobile Number</span>
              <strong>{mobileNumber}</strong>
            </div>
            {planDetails && (
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px' }}>
                <span style={{ color: 'var(--text-muted)' }}>Plan</span>
                <strong>{planDetails.name}</strong>
              </div>
            )}
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px' }}>
              <span style={{ color: 'var(--text-muted)' }}>Amount Paid</span>
              <strong>₹{planDetails ? planDetails.price : '---'}</strong>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px' }}>
              <span style={{ color: 'var(--text-muted)' }}>Payment Mode</span>
              <strong style={{ textTransform: 'capitalize' }}>{paymentMode.replace('_', ' ')}</strong>
            </div>
            {rechargeRecord && (
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ color: 'var(--text-muted)' }}>Transaction ID</span>
                <strong style={{ fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  TXN-{rechargeRecord.id}
                  <button onClick={() => copyToClipboard(`TXN-${rechargeRecord.id}`, 'success_txn')} style={{ background: 'none', border: 'none', cursor: 'pointer', display: 'flex' }}>
                    {copiedId === 'success_txn' ? <Check size={14} color="var(--success)" /> : <Copy size={14} color="var(--text-muted)" />}
                  </button>
                </strong>
              </div>
            )}
          </div>
          
          <button className="btn btn-primary" onClick={() => navigate('/dashboard')} style={{ width: '100%' }}>
            Return to Dashboard
          </button>
        </div>
      </div>
    );
  }

  // MAIN INITIATE RECHARGE VIEW
  return (
    <div style={{ minHeight: '100vh', padding: '32px 0', position: 'relative' }}>
      
      {/* Receipt Modal */}
      {selectedReceipt && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', backdropFilter: 'blur(4px)' }}>
          <div className="glass-panel animate-fade-in" style={{ padding: '32px', width: '100%', maxWidth: '400px', background: 'var(--bg-card)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
              <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px', margin: 0 }}>
                <FileText size={20} color="var(--primary)" /> E-Receipt
              </h3>
              <button onClick={() => setSelectedReceipt(null)} style={{ background: 'none', border: 'none', cursor: 'pointer' }}>
                <X size={24} color="var(--text-muted)" />
              </button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', background: 'rgba(0,0,0,0.02)', padding: '20px', borderRadius: '12px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'var(--text-muted)' }}>Date</span>
                <strong>{new Date(selectedReceipt.requestTime).toLocaleString()}</strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'var(--text-muted)' }}>Mobile Number</span>
                <strong>{selectedReceipt.mobileNumber}</strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'var(--text-muted)' }}>Amount</span>
                <strong>₹{selectedReceipt.amount}</strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ color: 'var(--text-muted)' }}>Status</span>
                <span style={{ display: 'flex', alignItems: 'center', gap: '4px', color: selectedReceipt.status === 'SUCCESS' ? 'var(--success)' : 'var(--warning)', fontWeight: 'bold' }}>
                  {getStatusIcon(selectedReceipt.status)} {selectedReceipt.status}
                </span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ color: 'var(--text-muted)' }}>Txn ID</span>
                <strong style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                  TXN-{selectedReceipt.id}
                  <button onClick={() => copyToClipboard(`TXN-${selectedReceipt.id}`, selectedReceipt.id)} style={{ background: 'none', border: 'none', cursor: 'pointer', display: 'flex' }}>
                    {copiedId === selectedReceipt.id ? <Check size={14} color="var(--success)" /> : <Copy size={14} color="var(--text-muted)" />}
                  </button>
                </strong>
              </div>
            </div>

            <button onClick={() => setSelectedReceipt(null)} className="btn btn-primary" style={{ width: '100%', marginTop: '24px' }}>
              Close
            </button>
          </div>
        </div>
      )}

      <div className="container" style={{ maxWidth: '1000px' }}>
        
        <button 
          onClick={() => navigate('/dashboard')} 
          className="btn btn-secondary" 
          style={{ marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '8px', padding: '8px 16px' }}
        >
          <ArrowLeft size={18} /> Back
        </button>

        <div className="recharge-grid" style={{ display: 'grid', gridTemplateColumns: '1.2fr 0.8fr', gap: '32px' }}>
          
          {/* Left Column: Form */}
          <div className="glass-panel animate-fade-in" style={{ padding: '32px' }}>
            <h2 style={{ marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '12px' }}>
              <ShieldCheck color="var(--primary)" size={28} />
              Initiate Recharge
            </h2>

            {planDetails && (
              <div style={{ background: 'var(--primary)', color: 'white', padding: '16px 24px', borderRadius: '12px', marginBottom: '32px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <h3 style={{ margin: '0 0 4px 0', color: 'white' }}>{planDetails.name}</h3>
                  <p style={{ margin: 0, opacity: 0.9, fontSize: '0.9rem' }}>Validity: {planDetails.validityDays} Days • {planDetails.description}</p>
                </div>
                <div style={{ fontSize: '1.5rem', fontWeight: '700' }}>₹{planDetails.price}</div>
              </div>
            )}

            <form onSubmit={handlePayWithRazorpay} style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
              
              {status === 'error' && (
                <div style={{ background: 'rgba(239, 68, 68, 0.1)', color: 'var(--error)', padding: '12px', borderRadius: '8px', border: '1px solid rgba(239, 68, 68, 0.3)', fontSize: '0.9rem' }}>
                  {message}
                </div>
              )}

              <div>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: '500', color: 'var(--text-muted)' }}>Mobile Number</label>
                <div style={{ position: 'relative' }}>
                  <Smartphone size={20} color="var(--text-muted)" style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)' }} />
                  <input 
                    type="text" 
                    placeholder="e.g. 9876543210" 
                    value={mobileNumber}
                    onChange={(e) => setMobileNumber(e.target.value.replace(/\D/g, '').slice(0, 10))}
                    required
                    style={{ fontSize: '1.2rem', letterSpacing: '2px', padding: '16px 16px 16px 48px', width: '100%' }}
                  />
                </div>
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '16px', fontWeight: '500', color: 'var(--text-muted)' }}>Select Payment Method</label>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '16px' }}>
                  <div 
                    onClick={() => setPaymentMode('credit_card')}
                    style={{ border: `2px solid ${paymentMode === 'credit_card' ? 'var(--primary)' : 'var(--border-glass)'}`, borderRadius: '12px', padding: '16px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '12px', background: paymentMode === 'credit_card' ? 'rgba(59, 130, 246, 0.05)' : 'transparent', transition: 'all 0.2s' }}
                  >
                    <CreditCard size={24} color={paymentMode === 'credit_card' ? 'var(--primary)' : 'var(--text-muted)'} />
                    <span style={{ fontWeight: paymentMode === 'credit_card' ? '600' : '400' }}>Card</span>
                  </div>
                  <div 
                    onClick={() => setPaymentMode('upi')}
                    style={{ border: `2px solid ${paymentMode === 'upi' ? 'var(--primary)' : 'var(--border-glass)'}`, borderRadius: '12px', padding: '16px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '12px', background: paymentMode === 'upi' ? 'rgba(59, 130, 246, 0.05)' : 'transparent', transition: 'all 0.2s' }}
                  >
                    <Smartphone size={24} color={paymentMode === 'upi' ? 'var(--primary)' : 'var(--text-muted)'} />
                    <span style={{ fontWeight: paymentMode === 'upi' ? '600' : '400' }}>UPI</span>
                  </div>
                  <div 
                    onClick={() => setPaymentMode('netbanking')}
                    style={{ border: `2px solid ${paymentMode === 'netbanking' ? 'var(--primary)' : 'var(--border-glass)'}`, borderRadius: '12px', padding: '16px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '12px', background: paymentMode === 'netbanking' ? 'rgba(59, 130, 246, 0.05)' : 'transparent', transition: 'all 0.2s' }}
                  >
                    <Building2 size={24} color={paymentMode === 'netbanking' ? 'var(--primary)' : 'var(--text-muted)'} />
                    <span style={{ fontWeight: paymentMode === 'netbanking' ? '600' : '400' }}>Net Banking</span>
                  </div>
                  <div 
                    onClick={() => setPaymentMode('wallet')}
                    style={{ border: `2px solid ${paymentMode === 'wallet' ? 'var(--primary)' : 'var(--border-glass)'}`, borderRadius: '12px', padding: '16px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '12px', background: paymentMode === 'wallet' ? 'rgba(59, 130, 246, 0.05)' : 'transparent', transition: 'all 0.2s' }}
                  >
                    <Wallet size={24} color={paymentMode === 'wallet' ? 'var(--primary)' : 'var(--text-muted)'} />
                    <span style={{ fontWeight: paymentMode === 'wallet' ? '600' : '400' }}>Wallet</span>
                  </div>
                </div>
              </div>

              <button 
                type="submit" 
                className="btn btn-primary" 
                disabled={status === 'processing' || !planId}
                style={{ marginTop: '16px', padding: '16px', fontSize: '1.1rem' }}
              >
                {status === 'processing' ? (
                  <><Loader size={20} className="spin" /> Processing Payment...</>
                ) : (
                  <><CheckCircle size={20} /> Pay Securely ₹{planDetails ? planDetails.price : '---'}</>
                )}
              </button>
            </form>
          </div>

          {/* Right Column: History */}
          <div className="glass-panel animate-fade-in" style={{ padding: '32px' }}>
            <h2 style={{ marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Activity size={24} color="var(--primary)" /> Recent Recharges
            </h2>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              {history.length === 0 ? (
                <div style={{ padding: '48px 24px', textAlign: 'center', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '16px', color: 'var(--text-muted)' }}>
                  <div style={{ background: 'rgba(0,0,0,0.05)', padding: '24px', borderRadius: '50%' }}>
                    <BatteryCharging size={48} color="var(--text-muted)" style={{ opacity: 0.5 }} />
                  </div>
                  <div>
                    <h4 style={{ margin: '0 0 8px 0', color: 'var(--text-main)' }}>No Recharges Yet</h4>
                    <p style={{ margin: 0, fontSize: '0.9rem' }}>Your transaction history will appear here once you make your first payment.</p>
                  </div>
                </div>
              ) : (
                history.map((record, index) => (
                  <div key={record.id} style={{ padding: '16px', background: 'rgba(0,0,0,0.02)', borderRadius: '12px', border: '1px solid var(--border-glass)', transition: 'all 0.2s', display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    
                    {/* Top Row: Mobile & Amount */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <div style={{ background: 'white', padding: '8px', borderRadius: '50%', boxShadow: '0 2px 8px rgba(0,0,0,0.05)' }}>
                          <Smartphone size={16} color="var(--primary)" />
                        </div>
                        <div>
                          <h4 style={{ margin: '0 0 2px 0', fontSize: '1.1rem' }}>{record.mobileNumber}</h4>
                          <span style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '0.75rem', fontWeight: '600', color: record.status === 'SUCCESS' ? 'var(--success)' : record.status === 'PENDING' ? 'var(--warning)' : 'var(--error)' }}>
                            {getStatusIcon(record.status)} {record.status}
                          </span>
                        </div>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <h4 style={{ margin: '0 0 4px 0', color: 'var(--text-main)', fontSize: '1.2rem' }}>₹{record.amount}</h4>
                      </div>
                    </div>

                    {/* Bottom Row: Date & Actions */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid rgba(0,0,0,0.05)', paddingTop: '12px', marginTop: '4px' }}>
                      <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>
                        {new Date(record.requestTime).toLocaleDateString()}
                      </span>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <button 
                          onClick={() => setSelectedReceipt(record)}
                          style={{ background: 'none', border: '1px solid var(--border-glass)', padding: '6px 12px', borderRadius: '16px', fontSize: '0.75rem', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px', color: 'var(--text-main)', transition: 'all 0.2s' }}
                          onMouseOver={(e) => e.currentTarget.style.background = 'rgba(0,0,0,0.05)'}
                          onMouseOut={(e) => e.currentTarget.style.background = 'none'}
                        >
                          <FileText size={12} /> Receipt
                        </button>
                        <button 
                          onClick={() => handleRepeatRecharge(record)}
                          style={{ background: 'rgba(79, 70, 229, 0.1)', border: 'none', padding: '6px 12px', borderRadius: '16px', fontSize: '0.75rem', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px', color: 'var(--primary)', fontWeight: '600', transition: 'all 0.2s' }}
                          onMouseOver={(e) => e.currentTarget.style.background = 'rgba(79, 70, 229, 0.15)'}
                          onMouseOut={(e) => e.currentTarget.style.background = 'rgba(79, 70, 229, 0.1)'}
                        >
                          <RefreshCw size={12} /> Repeat
                        </button>
                      </div>
                    </div>

                  </div>
                ))
              )}
            </div>
          </div>

        </div>
      </div>
    </div>
  );
};

export default Recharge;
