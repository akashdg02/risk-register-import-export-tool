import logging
from flask import Flask, jsonify
from routes.risk_routes import risk_bp
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address

# Configure logging for audit and error tracking 
# Line 8 fix
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)

# Security: Rate limiting setup [cite: 18, 67]
# Blocks IPs exceeding 30 requests per minute 
limiter = Limiter(
    key_func=get_remote_address,
    app=app,
    default_limits=["30 per minute"],
    storage_uri="memory://"  # Uses in-memory storage for rate limiting 
)

# Registering the risk routes blueprint [cite: 35, 36]
app.register_blueprint(risk_bp, url_prefix='/ai')

@app.route('/health', methods=['GET'])
def health_check():
    """
    Standard health check endpoint for the Al microservice[cite: 14, 72].
    Provides model info and service status for the Java backend to monitor.
    """
    logger.info("Health check endpoint accessed")
    return jsonify({
        "status": "healthy",
        "uptime": "active",
        "model": "llama-3.3-70b-specdec", # Exact model specified in Groq docs [cite: 119]
        "port": 5000 # Al Service Port 
    }), 200

# Error handler for rate limiting to prevent HTTP 500 
@app.errorhandler(429)
def ratelimit_handler(e):
    return jsonify({"error": "Rate limit exceeded", "details": str(e.description)}), 429

if __name__ == '__main__':
    # Running on port 5000 as required by the Project Overview 
    app.run(host='0.0.0.0', port=5000, debug=False)