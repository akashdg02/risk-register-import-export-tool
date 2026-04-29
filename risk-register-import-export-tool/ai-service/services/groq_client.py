import os
import json
import logging
from groq import Groq
from dotenv import load_dotenv

# MUST be called at the top of the file to load the .env variables
load_dotenv()

class GroqClient:
    def __init__(self):
        # 1. Properly retrieve the key from environment variables [cite: 106]
        api_key = os.getenv("GROQ_API_KEY")
        
        if not api_key:
            raise ValueError("GROQ_API_KEY not found. Ensure your .env file is correct.")
        
        # 2. Initialize the client with the key
        self.client = Groq(api_key=api_key)
        
        # 3. Use the updated versatile model (the specdec one is decommissioned) 
        self.model = "llama-3.3-70b-versatile"

    def get_completion(self, prompt_text):
        try:
            chat_completion = self.client.chat.completions.create(
                messages=[{"role": "user", "content": prompt_text}],
                model=self.model,
                # Requirement: 0.3 for factual consistency 
                temperature=0.3, 
                response_format={"type": "json_object"}
            )
            return json.loads(chat_completion.choices[0].message.content)
        except Exception as e:
            logging.error(f"AI Service Error: {e}")
            # Requirement: Return fallback JSON instead of HTTP 500 [cite: 112]
            return {
                "is_fallback": True, 
                "description": "AI service is currently reaching capacity. Please try again later."
            }