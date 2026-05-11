import os
import logging
from dotenv import load_dotenv

load_dotenv()

from flask import Flask, jsonify
from flask_cors import CORS
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address
from ai_routes import ai_bp 

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)

limiter = Limiter(
    key_func=get_remote_address,
    app=app,
    default_limits=["30 per minute"],
    storage_uri="memory://" 
)

app.register_blueprint(ai_bp, url_prefix='/ai')

@app.errorhandler(429)
def ratelimit_handler(e):
    logger.warning("Rate limit triggered.")
    return jsonify({
        "error": "Rate limit exceeded", 
        "details": "Please wait a moment before trying again.",
        "is_fallback": True
    }), 429

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)