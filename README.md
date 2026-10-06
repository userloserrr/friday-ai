# Friday AI

Friday, Android telefon üzerinde çalışan, yüz ifadesi ve görsel üretimi destekleyen bir yapay zeka asistanı için başlangıç projesidir.

Temel mimari:
- Android uygulaması: Jetpack Compose ile arayüz
- Backend: Python + FastAPI
- Ana konuşma modeli: Anthropic Sonnet 5.5 via API
- Yerel/yardımcı model: Google Gemma via Google AI Studio / Vertex AI
- Görsel üretimi: Gemini / Stability / OpenAI gibi servisler üzerinden
- Yüz animasyonu: Android Compose üzerinde 2D avatar / yüz ifadeleri

Proje yapısı:
- `android/` : Android uygulama prototipi
- `backend/` : FastAPI tabanlı arka uç servisleri
- `README.md` : genel kullanım ve kurulum rehberi

Öncelikler:
1. `backend` servisini çalıştır
2. Android uygulamasını cihaz veya emülatörde çalıştır
3. `FRIDAY_API_URL` ve API anahtarlarını tanımla
4. Yüz animasyonu için avatar sistemini genişlet

Not: Sonnet 5.5 doğrudan Android cihaz üzerinde çalışmaz. En doğru yöntem bulut API çağrısıdır. Gemma ise yerel/edge veya cloud olarak kullanılabilir.

Akış:
- Kullanıcı konuşur
- Android app, arka uca metin gönderir
- Backend, Sonnet 5.5 ile cevap üretir
- İstersen Gemma ile küçük yardımcı yanıt üretir
- Görsel istenirse image service çağrılır
- Yüz animasyonu ekranda gösterilir

Hızlı başlangıç:

Backend:
```bash
cd backend
python -m venv .venv
source .venv/bin/activate  # Windows: .venv\Scripts\activate
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Android:
```bash
cd android
./gradlew assembleDebug
```

Çevre değişkenleri:
```bash
export ANTHROPIC_API_KEY="..."
export GEMMA_API_KEY="..."
export GEMMA_API_URL="https://..."
export IMAGE_API_KEY="..."
export IMAGE_API_URL="https://..."
```

Geliştirme notları:
- Android tarafında gerçek cihaz için `10.0.2.2` yerine bilgisayarın LAN IP'sini kullan
- Emülatör için `http://10.0.2.2:8000`
- Üretim ortamında `https` kullan
- Yüz ifadeleri, konuşma metni ve ses tone parametreleri ile bağlanabilir

Temel endpointler:
- `GET /health`
- `POST /chat`
- `POST /generate-image`

Örnek istek:
```bash
curl -X POST http://localhost:8000/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Merhaba Friday, bugün hava nasıl?"}'
```

İleride eklenebilir:
- Sesli komut tanıma
- Lottie yüz animasyonu
- Local Gemma TFLite entegrasyonu
- Görsel üretim sonucu galeri
- Kullanıcı kişiselleştirme
- Offline yardımcı mod

Bu proje, üretime hazır bir uygulama değil; geliştirilmeye açık, çalışan bir başlangıç iskeletidir.
