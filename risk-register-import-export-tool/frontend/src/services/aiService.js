// Inside aiService.js
export const generateDescription = async (title) => {
    const response = await fetch('http://localhost:5000/ai/describe', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            // 🚨 ADD THIS LINE TO FIX THE 401 ERROR:
            'Authorization': 'Bearer dummy_test_token_123' 
        },
        body: JSON.stringify({ title })
    });
    return response.json();
};

export const getRecommendations = async (description) => {
    const response = await fetch('http://localhost:5000/ai/recommend', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            // 🚨 ADD THIS LINE TO FIX THE 401 ERROR:
            'Authorization': 'Bearer dummy_test_token_123'
        },
        body: JSON.stringify({ description })
    });
    return response.json();
};