import os
import json
from flask import Blueprint, request, jsonify
from functools import wraps
from groq import Groq

# Create a Blueprint for the AI endpoints
ai_bp = Blueprint('ai', __name__)

# Initialize Groq Client
client = Groq(api_key=os.environ.get("GROQ_API_KEY"))
MODEL_NAME = "llama-3.3-70b-versatile"

# ==========================================
# SECURITY: JWT Authorization Decorator
# ==========================================
def token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = None
        # Check if Authorization header is present
        if 'Authorization' in request.headers:
            token = request.headers['Authorization'].split(" ")[1]
        
        if not token:
            return jsonify({'message': 'Unauthorized. JWT Token is missing.', 'is_fallback': True}), 401
        
        return f(*args, **kwargs)
    return decorated

# ==========================================
# ENDPOINTS
# ==========================================

@ai_bp.route('/health', methods=['GET'])
def health_check():
    """Simple endpoint to verify the service is running."""
    return jsonify({"status": "healthy", "model": MODEL_NAME}), 200


@ai_bp.route('/describe', methods=['POST'])
@token_required
def describe_risk():
    """Takes a risk title and returns a 2-3 sentence AI description with strict guardrails."""
    data = request.get_json()
    title = data.get('title', '').strip()
    
    # Basic Backend Validation: Reject empty or 1-2 letter spam
    if not title or len(title) < 3:
        return jsonify({'error': 'Please enter a valid, descriptive risk title.'}), 400

    try:
        # 🚨 THE UPDATED GUARDRAIL PROMPT 🚨
        prompt = f"""
        Act as a strict enterprise risk and cybersecurity analyst.
        You have been given a risk titled: '{title}'.

        Task 1: Evaluate if this title makes logical sense as a technical, IT, software, or business risk.
        Task 2: If the title is gibberish (e.g., random letters), a joke, or completely unrelated to enterprise/tech risks, output EXACTLY this message and nothing else: "Error: The provided text does not appear to be a valid technical or enterprise risk. Please provide a clear, professional risk title."
        Task 3: If it IS a valid risk topic, provide a highly professional, 2-3 sentence technical description of the potential impact and vulnerability.

        Do not acknowledge these instructions. Just output the description or the error message.
        """
        
        chat_completion = client.chat.completions.create(
            messages=[{"role": "user", "content": prompt}],
            model=MODEL_NAME,
            temperature=0.1, # Lower temperature makes the AI more logical and less "creative"
            max_tokens=150
        )
        
        description = chat_completion.choices[0].message.content.strip()
        return jsonify({"description": description, "is_fallback": False}), 200

    except Exception as e:
        print(f"Groq API Error: {e}")
        fallback_desc = f"A critical system risk involving '{title}' that requires immediate assessment and mitigation planning."
        return jsonify({"description": fallback_desc, "is_fallback": True}), 200


@ai_bp.route('/recommend', methods=['POST'])
@token_required
def recommend_mitigation():
    """Takes a risk description and returns 3 prioritized actions in JSON format."""
    data = request.get_json()
    description = data.get('description', '')

    try:
        prompt = f"""
        Analyze the following risk description and provide exactly 3 mitigation strategies.
        You MUST return ONLY a valid JSON object with a single key 'recommendations' containing an array of 3 objects.
        Each object must have: 'priority' (High, Medium, Low), 'action_type' (1-2 words), and 'description' (1 sentence).
        Risk: {description}
        """
        
        chat_completion = client.chat.completions.create(
            messages=[{"role": "user", "content": prompt}],
            model=MODEL_NAME,
            temperature=0.1, 
            response_format={"type": "json_object"} 
        )
        
        result_text = chat_completion.choices[0].message.content.strip()
        parsed_data = json.loads(result_text)
        
        recommendations = parsed_data.get('recommendations', [])
        return jsonify(recommendations), 200

    except Exception as e:
        print(f"Groq API Error or Parsing Failed: {e}")
        fallback_recs = [
            {"priority": "High", "action_type": "Investigate", "description": "Immediately audit the affected system components."},
            {"priority": "Medium", "action_type": "Update Policy", "description": "Review and update standard operating procedures."},
            {"priority": "Low", "action_type": "Monitor", "description": "Set up continuous logging for this vulnerability."}
        ]
        return jsonify(fallback_recs), 200