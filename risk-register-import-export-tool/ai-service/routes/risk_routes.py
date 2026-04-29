import datetime
import os
import logging
from flask import Blueprint, request, jsonify
from services.groq_client import GroqClient

risk_bp = Blueprint('risk_bp', __name__)
ai_client = GroqClient()

def load_prompt(template_name, **kwargs):
    """
    Utility to load text files from the /prompts directory 
    and inject variables using .format()
    """
    try:
        # Construct path to the prompt file
        base_path = os.path.dirname(os.path.dirname(__file__))
        prompt_path = os.path.join(base_path, 'prompts', f"{template_name}.txt")
        
        with open(prompt_path, "r") as f:
            content = f.read()
            return content.format(**kwargs)
    except FileNotFoundError:
        logging.error(f"Prompt template {template_name} not found.")
        return None
    except KeyError as e:
        logging.error(f"Missing variable for prompt formatting: {e}")
        return None

@risk_bp.route('/describe', methods=['POST'])
def describe_risk():
    """Day 3 Task: Build POST /describe endpoint."""
    data = request.get_json()
    if not data or 'title' not in data:
        return jsonify({"error": "Missing title"}), 400

    # Load from file instead of hardcoding the string
    prompt = load_prompt("describe_prompt", title=data['title'])
    if not prompt:
        return jsonify({"error": "Configuration error: prompt missing"}), 500

    ai_response = ai_client.get_completion(prompt)
    ai_response['generated_at'] = datetime.datetime.utcnow().isoformat()
    
    return jsonify(ai_response), 200

@risk_bp.route('/recommend', methods=['POST'])
def recommend_actions():
    """Day 4 Task: POST /recommend endpoint."""
    data = request.get_json()
    if not data or 'description' not in data:
        return jsonify({"error": "Missing description"}), 400

    prompt = load_prompt("recommend_prompt", description=data['description'])
    if not prompt:
        return jsonify({"error": "Configuration error: prompt missing"}), 500

    ai_response = ai_client.get_completion(prompt)
    return jsonify(ai_response), 200

@risk_bp.route('/generate-report', methods=['POST'])
def generate_full_report():
    """Day 6 Task: Synthesize title, desc, and actions into a report."""
    data = request.get_json()
    
    # Validation for complex report data
    required_fields = ['title', 'description', 'recommendations']
    if not data or not all(field in data for field in required_fields):
        return jsonify({"error": "Missing data for report generation"}), 400

    prompt = load_prompt("report_prompt", 
                         title=data['title'], 
                         description=data['description'], 
                         recommendations=data['recommendations'])
    
    if not prompt:
        return jsonify({"error": "Configuration error: report prompt missing"}), 500

    ai_response = ai_client.get_completion(prompt)
    return jsonify(ai_response), 200