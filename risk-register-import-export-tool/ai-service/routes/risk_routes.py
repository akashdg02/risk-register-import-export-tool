import datetime
from flask import Blueprint, request, jsonify
from services.groq_client import GroqClient

risk_bp = Blueprint('risk_bp', __name__)
ai_client = GroqClient()

@risk_bp.route('/describe', methods=['POST'])
def describe_risk():
    """Day 3 Task: POST /describe endpoint[cite: 67]."""
    data = request.get_json()
    if not data or 'title' not in data:
        return jsonify({"error": "Missing title"}), 400

    # Logic to load prompt and call AI
    # You must have prompts/describe_prompt.txt created
    ai_response = ai_client.get_completion(f"Describe this risk: {data['title']}")
    ai_response['generated_at'] = datetime.datetime.utcnow().isoformat()
    
    return jsonify(ai_response), 200

@risk_bp.route('/recommend', methods=['POST'])
def recommend_actions():
    """Day 4 Task: POST /recommend endpoint[cite: 72]."""
    data = request.get_json()
    if not data or 'description' not in data:
        return jsonify({"error": "Missing description"}), 400

    # Logic to return 3 recommendations as a JSON array
    ai_response = ai_client.get_completion(f"Recommend 3 actions for: {data['description']}")
    
    return jsonify(ai_response), 200