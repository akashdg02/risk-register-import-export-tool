import os
import json
import logging
from groq import Groq
from dotenv import load_dotenv  # Fixed the 'dotenimport' typo here

# Load environment variables from .env file [cite: 107]
load_dotenv()

class GroqClient:
    def __init__(self):
        """
        Initializes the Groq client using LLaMA-3.3-70b as per project specs. [cite: 18]
        """
        api_key = os.getenv("GROQ_API_KEY")
        if not api_key:
            logging.error("GROQ_API_KEY is missing from environment variables.")
        
        self.client = Groq(api_key=api_key)
        self.model = "llama-3.3-70b-specdec"

    def get_completion(self, prompt_text):
        """
        Executes the AI call with error handling and fallback logic. [cite: 111, 112]
        """
        try:
            # Implementing API call with JSON mode and specific temperature [cite: 119]
            chat_completion = self.client.chat.completions.create(
                messages=[
                    {
                        "role": "system", 
                        "content": "You are a risk management expert. Always return JSON."
                    },
                    {
                        "role": "user", 
                        "content": prompt_text
                    }
                ],
                model=self.model,
                temperature=0.3,  # Set to 0.3 for factual consistency [cite: 119]
                response_format={"type": "json_object"}
            )
            
            # Parsing response content 
            return json.loads(chat_completion.choices[0].message.content)

        except Exception as e:
            # Log the error for the Security Reviewer/Al Developer 2 
            logging.error(f"Groq API call failed: {str(e)}")
            
            # Critical Requirement: Never return HTTP 500; use fallback [cite: 112, 113]
            return {
                "is_fallback": True, 
                "description": "The AI service is currently unavailable. Please try again later.",
                "recommendations": []
            }