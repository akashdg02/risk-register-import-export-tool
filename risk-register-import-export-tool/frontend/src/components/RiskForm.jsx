import React, { useState } from 'react';
import * as aiService from '../services/aiService'; // 🚨 The crucial import we fixed!
import './index.css';

const RiskForm = () => {
  const [title, setTitle] = useState('Database Connection');
  const [description, setDescription] = useState('');
  const [recommendations, setRecommendations] = useState([]);
  const [isFallback, setIsFallback] = useState(false);
  const [loading, setLoading] = useState(false);

  // 🔌 Function 1: Fetch the AI Description
  const handleGenerateDescription = async () => {
    setLoading(true);
    try {
      const data = await aiService.generateDescription(title);
      setDescription(data.description);
      setIsFallback(data.is_fallback);
    } catch (error) {
      console.error("Frontend Fetch Error:", error);
      setDescription("Failed to connect to AI Microservice.");
    }
    setLoading(false);
  };

  // 🔌 Function 2: Fetch the AI Recommendations
  const handleGetRecommendations = async () => {
    if (!description) return alert("Please generate a description first!");
    
    setLoading(true);
    try {
      const data = await aiService.getRecommendations(description);
      setRecommendations(data); // Expecting an array of 3 objects
    } catch (error) {
      console.error("Frontend Fetch Error:", error);
    }
    setLoading(false);
  };

  return (
    <div className="risk-dashboard">
      <div className="header-container">
        <h2>Risk Register</h2>
        <span className="ai-badge">AI-POWERED</span>
      </div>

      <div className="form-group">
        <label>Risk Title</label>
        <div className="input-row">
          <input 
            type="text" 
            value={title} 
            onChange={(e) => setTitle(e.target.value)}
          />
          <button onClick={handleGenerateDescription} className="btn btn-primary">
            {loading ? 'Processing...' : 'Generate AI Description'}
          </button>
        </div>
      </div>

      <div className="form-group">
        <label>
          Description 
          {isFallback && <span className="fallback-warning">⚠️ Using fallback template</span>}
        </label>
        <textarea 
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />
      </div>

      <div className="form-group">
        <h3>AI Mitigation Strategies</h3>
        <button onClick={handleGetRecommendations} className="btn btn-primary" style={{width: "fit-content"}}>
            {loading ? 'Analyzing...' : 'AI Recommend'}
        </button>
        
        {/* Dynamically mapping the JSON array returned by the API */}
        {recommendations.map((rec, index) => (
          <div key={index} style={{marginTop: "15px", padding: "15px", border: "1px solid #e2e8f0", borderRadius: "6px"}}>
              <strong>Priority: <span style={{color: rec.priority === 'High' ? 'red' : '#d97706'}}>{rec.priority}</span></strong>
              <p style={{margin: "5px 0 0 0", color: "#475569"}}>{rec.action_type}: {rec.description}</p>
          </div>
        ))}
      </div>
    </div>
  );
};

export default RiskForm;