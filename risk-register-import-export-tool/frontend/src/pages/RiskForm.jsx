import React, { useState } from 'react';
import * as aiService from '../services/aiService';
import RecommendationList from '../components/RecommendationList';
import AiSkeleton from '../components/AiSkeleton'; 

const RiskForm = () => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState({ desc: false, rec: false });
  const [isFallback, setIsFallback] = useState(false);

  // Task: Generate Description using AI
  const handleGenerateDescription = async () => {
    if (!title) return alert("Please enter a risk title first.");
    
    setLoading({ ...loading, desc: true });
    try {
      const data = await aiService.generateDescription(title);
      setDescription(data.description);
      setIsFallback(data.is_fallback); 
    } catch (error) {
      console.error("Fetch error:", error);
      setDescription("Failed to connect to AI Microservice.");
    }
    setLoading({ ...loading, desc: false });
  };

  // Task: Fetch 3 Prioritized Recommendations
  const handleGetRecommendations = async () => {
    if (!description) return alert("Generate or enter a description first.");
    
    setLoading({ ...loading, rec: true });
    try {
      const data = await aiService.getRecommendations(description);
      setRecommendations(data);
    } catch (error) {
      console.error("Fetch error:", error);
    }
    setLoading({ ...loading, rec: false });
  };

  return (
    <div className="max-w-6xl mx-auto p-4 mt-8 font-sans">
      
      {/* 🚀 NEW HEADER: Clean and minimal (Project Status Removed) */}
      <div className="flex flex-col md:flex-row md:items-center justify-between mb-8 pb-5 border-b border-gray-200 gap-4">
        <div>
          <h2 className="text-3xl font-extrabold text-gray-900 tracking-tight">Risk Register</h2>
          <p className="text-gray-500 mt-1 text-sm">Identify, analyze, and mitigate system vulnerabilities.</p>
        </div>
        <div className="flex items-center gap-2 bg-indigo-50 border border-indigo-100 px-4 py-2 rounded-full shadow-sm">
          <div className="w-2 h-2 bg-indigo-600 rounded-full animate-pulse"></div>
          <span className="text-indigo-800 text-xs font-bold tracking-wider">AI-POWERED</span>
        </div>
      </div>

      {/* 🚀 DUAL-PANE DASHBOARD GRID */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        
        {/* ========================================== */}
        {/* LEFT PANE: Risk Definition (5 columns) */}
        {/* ========================================== */}
        <div className="lg:col-span-5 space-y-6">
          <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
            <h3 className="text-lg font-semibold text-gray-800 mb-5 border-b border-gray-100 pb-2">1. Define Risk</h3>
            
            {/* Title Input */}
            <div className="mb-5">
              <label className="block text-sm font-semibold text-gray-700 mb-2">Risk Title</label>
              <input
                type="text"
                className="w-full p-2.5 bg-gray-50 border border-gray-200 rounded-lg shadow-inner focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:bg-white transition-all"
                placeholder="e.g., Database Connection Timeout"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
              />
              <button
                onClick={handleGenerateDescription}
                disabled={loading.desc}
                className="w-full mt-3 py-2.5 bg-indigo-600 text-white font-semibold rounded-lg hover:bg-indigo-700 disabled:bg-indigo-300 transition-colors shadow-sm"
              >
                {loading.desc ? 'Processing AI...' : 'Generate AI Description'}
              </button>
            </div>

            {/* Description Area */}
            <div>
              <div className="flex justify-between items-end mb-2">
                <label className="block text-sm font-semibold text-gray-700">Technical Description</label>
                {isFallback && (
                  <span className="text-xs bg-amber-100 text-amber-800 px-2 py-1 rounded font-medium border border-amber-200">
                    ⚠️ Fallback template
                  </span>
                )}
              </div>
              <textarea
                rows="5"
                className="w-full p-2.5 bg-gray-50 border border-gray-200 rounded-lg shadow-inner focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:bg-white transition-all"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="AI generated description will appear here..."
              />
            </div>
          </div>

          <button className="w-full py-3.5 bg-slate-800 text-white font-bold rounded-xl hover:bg-slate-900 transition-colors shadow-md">
            Save Risk Record
          </button>
        </div>

        {/* ========================================== */}
        {/* RIGHT PANE: AI Analysis (7 columns) */}
        {/* ========================================== */}
        <div className="lg:col-span-7">
          <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 h-full">
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6 border-b border-gray-100 pb-3 gap-4">
              <h3 className="text-lg font-semibold text-gray-800">2. Mitigation Strategy</h3>
              <button
                onClick={handleGetRecommendations}
                disabled={loading.rec || !description}
                className="px-5 py-2.5 bg-emerald-600 text-white font-semibold rounded-lg hover:bg-emerald-700 disabled:bg-emerald-300 transition-colors shadow-sm flex items-center gap-2"
              >
                {loading.rec ? (
                  <>
                    <svg className="animate-spin h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
                    Analyzing...
                  </>
                ) : 'Request AI Recommend'}
              </button>
            </div>

            {/* Content Area */}
            <div className="min-h-[300px] bg-slate-50 rounded-lg border border-gray-100 p-4">
              {loading.rec ? (
                  <AiSkeleton type="cards" />
              ) : recommendations.length > 0 ? (
                  <RecommendationList recommendations={recommendations} />
              ) : (
                  <div className="h-full flex flex-col items-center justify-center text-gray-400 mt-16">
                    <svg className="w-12 h-12 mb-3 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
                    </svg>
                    <p>Enter a risk title and generate a description to unlock AI recommendations.</p>
                  </div>
              )}
            </div>
          </div>
        </div>

      </div>
    </div>
  );
};

export default RiskForm;