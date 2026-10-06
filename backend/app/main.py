from fastapi import FastAPI
from pydantic import BaseModel

from app.config import BACKEND_HOST, BACKEND_PORT
from app.services.ai_service import AIService
from app.services.image_service import ImageService

app = FastAPI(title="Friday AI Backend", version="0.1.0")

ai_service = AIService()
image_service = ImageService()


class ChatRequest(BaseModel):
    message: str
    emotion: str | None = "neutral"


class ImageRequest(BaseModel):
    prompt: str
    style: str | None = "cinematic"


@app.get("/health")
def health_check():
    return {"status": "ok", "service": "friday-ai"}


@app.post("/chat")
def chat(request: ChatRequest):
    response = ai_service.generate_response(request.message, request.emotion)
    return {
        "reply": response,
        "emotion": request.emotion,
    }


@app.post("/generate-image")
def generate_image(request: ImageRequest):
    result = image_service.generate(request.prompt, request.style)
    return {"image_url": result}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("app.main:app", host=BACKEND_HOST, port=BACKEND_PORT, reload=True)
