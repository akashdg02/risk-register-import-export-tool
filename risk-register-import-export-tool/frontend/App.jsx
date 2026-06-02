import React from 'react';
// Make sure this path matches where your RiskForm is saved!
// It might be './pages/RiskForm' depending on your folder structure.
import RiskForm from './pages/RiskForm'; 
import './index.css';

function App() {
  return (
    <div className="bg-slate-50 min-h-screen">
      <RiskForm />
    </div>
  );
}

export default App;