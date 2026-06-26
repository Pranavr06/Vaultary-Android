# 📱 Vaultary Android

Vaultary Android is the native Android client for the Vaultary platform. It connects to the same backend APIs and shared database used by the Vaultary web application, allowing users to seamlessly access and manage their data across both web and mobile platforms.

Any changes made through the Android application are instantly reflected on the web platform, and vice versa, ensuring a synchronized cross-platform experience.

> 🚧 **Project Status:** Currently under active development.

## ✨ Key Features

* Cross-platform data synchronization
* Shared backend with Vaultary Web
* Secure user authentication & JWT-based session management
* **Two-Factor Authentication (2FA)**
* **AES-256 Encrypted Password Vault**
* **AES-256 Encrypted Secure Notes**
* **Biometric App Lock (Fingerprint/FaceID)**
* Password strength analysis & Breach detection services
* Modern Android UI using Jetpack Compose

## 🏗️ Architecture

```text
 Vaultary Web (HTML/JS/CSS)
           │
           ▼
      Flask API Backend
           │
           ▼
    Shared Database (Supabase)
           ▲
           │
Vaultary Android (Kotlin + Jetpack Compose)
```

##  🛠️  Tech Stack

### 📱 Android

* Kotlin
* Jetpack Compose
* Material 3
* Android Biometric API

### 🌐 Networking

* Retrofit
* OkHttp

### ☁️ Backend & Cloud

* Python Flask API
* Supabase (PostgreSQL)
* JWT Authentication

---

##  🔄  Synchronization

The Android application and the Vaultary web application use the same backend services and database. Any data created, updated, or deleted from one platform is automatically encrypted and reflected across all connected platforms.
