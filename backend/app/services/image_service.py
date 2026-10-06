import os

import requests


class ImageService:
    def __init__(self) -> None:
        self.image_api_key = os.getenv("IMAGE_API_KEY")
        self.image_api_url = os.getenv("IMAGE_API_URL")

    def generate(self, prompt: str, style: str | None = "cinematic") -> str:
        if self.image_api_url and self.image_api_key:
            return self._call_external_image_service(prompt, style)
        return self._mock_image_url(prompt, style)

    def _call_external_image_service(self, prompt: str, style: str | None) -> str:
        payload = {
            "prompt": f"{prompt}, style: {style}",
            "size": "1024x1024",
        }
        headers = {"Authorization": f"Bearer {self.image_api_key}", "Content-Type": "application/json"}
        response = requests.post(self.image_api_url, headers=headers, json=payload, timeout=30)
        response.raise_for_status()
        data = response.json()
        if "image_url" in data:
            return data["image_url"]
        if "data" in data and data["data"]:
            return data["data"][0].get("url") or data["data"][0].get("b64_json")
        raise ValueError("Image service did not return expected output")

    def _mock_image_url(self, prompt: str, style: str | None) -> str:
        return (
            f"https://placehold.co/1024x1024/png?text={prompt[:32].replace(' ', '+')}"
            f"+({style or 'cinematic'})"
        )
