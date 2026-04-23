import os
import json
import logging
from groq import Groq
from dotenv import load_dotenv

# MUST be called at the top of the file to load the .env variables
load_dotenv()

class GroqClient:
    def __init__(self):
        # Retrieve the key from the environment
        api_key = os.getenv("GROQ_API_KEY")
        
        if not api_key:
            raise ValueError("GROQ_API_KEY not found. Ensure your .env file is correct.")
        
        # Pass the key explicitly to the Groq client
        self.client = Groq(api_key=api_key)
        self.model = "llama-3.3-70b-specdec"

    def get_completion(self, prompt_text):
        try:
            chat_completion = self.client.chat.completions.create(
                messages=[{"role": "user", "content": prompt_text}],
                model=self.model,
                temperature=0.3, # Requirement for factual consistency [cite: 119]
                response_format={"type": "json_object"}
            )
            return json.loads(chat_completion.choices[0].message.content)
        except Exception as e:
            logging.error(f"AI Service Error: {e}")
            # Fallback to prevent HTTP 500 [cite: 112]
            return {"is_fallback": True, "description": "AI unavailable. Using fallback response."}