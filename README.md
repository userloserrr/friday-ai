# Friday AI

Friday, Android telefon üzerinde çalışan, yüz ifadesi ve görsel üretimi destekleyen bir yapay zeka asistanı için başlangıç projesidir.

Mimari:
- Android: Jetpack Compose
- Backend: FastAPI
- LLM: Anthropic Sonnet / Google Gemma / cloud API
- Görsel üretim: image API
- Yüz animasyonu: Android 2D avatar çerçevesi

Klasör yapısı:
- `backend/` : Python API sunucusu
- `android/` : Android app projesi

Kurulum (Windows/macOS/Linux):

1) Python ortamı oluştur
```bash
cd backend
python -m venv .venv
# macOS/Linux
source .venv/bin/activate
# Windows PowerShell
# .venv\Scripts\Activate.ps1
pip install -r requirements.txt
```

2) Ortam değişkenleri oluştur
```bash
cp .env.example .env
```
`backend/.env` içindeki anahtarları doldur.

3) Backend çalıştır
```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

4) HTTP kontrol
```bash
curl http://localhost:8000/health
```

5) Android uygulaması
Android Studio açılır, `android/` klasörü import edilir.
`android/local.properties` dosyası oluşturulup SDK yolu yazılır:

```properties
sdk.dir=/Users/<kullanici>/Library/Android/sdk
```
veya Windows için:
```properties
sdk.dir=C:\Users\<kullanici>\AppData\Local\Android\Sdk
```

6) Emülatör için erişim
- Emülatör: `http://10.0.2.2:8000/chat`
- Gerçek telefon: bilgisayarın LAN IP'si (ör. `http://192.168.1.25:8000/chat`)

7) Test isteği
```bash
curl -X POST http://localhost:8000/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Merhaba Friday, bugün ne yapabiliriz?"}'
```

Notlar:
- Anthropic Sonnet API için model ismini gerçek model adıyla güncelle.
- Gemma ve image servisleri ayrı endpoint / API key ile çalışır.
- Mobil cihazda gerçek çalıştırma için Android app içinde backend URL'sini değiştir.
