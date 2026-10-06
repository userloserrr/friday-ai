import os
from typing import Any

import requests


class AIService:
    def __init__(self) -> None:
        self.anthropic_api_key = os.getenv("ANTHROPIC_API_KEY")
        self.gemma_api_key = os.getenv("GEMMA_API_KEY")
        self.gemma_api_url = os.getenv("GEMMA_API_URL")

    def generate_response(self, message: str, emotion: str | None = "neutral") -> str:
        if self.anthropic_api_key:
            return self._call_anthropic(message, emotion)
        if self.gemma_api_url:
            return self._call_gemma(message, emotion)
        return self._fallback_response(message, emotion)

    def _call_anthropic(self, message: str, emotion: str | None) -> str:
        url = "https://api.anthropic.com/v1/messages"
        headers = {
            "x-api-key": self.anthropic_api_key,
            "anthropic-version": "2023-06-01",
            "content-type": "application/json",
        }
        payload = {
            "model": "claude-3-5-sonnet-20241022",
            "max_tokens": 512,
            "messages": [{"role": "user", "content": f"Reply as Friday AI. Emotion: {emotion}. User: {message}"}],
        }
        response = requests.post(url, headers=headers, json=payload, timeout=30)
        response.raise_for_status()
        data = response.json()
        content = data["content"][0]["text"]
        return content

    def _call_gemma(self, message: str, emotion: str | None) -> str:
        if not self.gemma_api_url:
            return self._fallback_response(message, emotion)
        payload = {
            "prompt": f"You are Friday AI. Emotion: {emotion}. User: {message}",
            "max_tokens": 256,
        }
        headers = {"Authorization": f"Bearer {self.gemma_api_key or ''}", "Content-Type": "application/json"}
        response = requests.post(self.gemma_api_url, headers=headers, json=payload, timeout=30)
        response.raise_for_status()
        data = response.json()
        if "output" in data:
            return data["output"]
        if "text" in data:
            return data["text"]
        return self._fallback_response(message, emotion)

    def _fallback_response(self, message: str, emotion: str | None) -> str:
        return (
            f"Merhaba! Ben Friday AI. Şu anda hazır bir LLM bağlantısı yok, ama bu mesaj alındı: '{message}'. "
            f"Duygu modu: {emotion or 'neutral'}. Sonraki adımda Ana model ve Gemma entegrasyonu bağlanacak."
        )
