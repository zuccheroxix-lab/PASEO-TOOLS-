# PASEO SENSI — Native C++ Core Engine
**Developer: ZUCCHERO XANN**

## 1. Arsitektur Multiplatform (Android & PC)

Modul C++ `SensitivityEngine` dirancang dengan arsitektur **Zero-Dependency Portable C++17**, terpisah secara bersih dari platform adapter:

- `sensitivity_engine.h` & `sensitivity_engine.cpp`: Logika inti matematika murni tanpa header Android atau Windows (`<cmath>`, `<string>`, `<algorithm>`). Menghitung transformasi kurva (Dynamic S-Curve, Exponential, Controlled Smooth), deadzone filter, velocity calculation, acceleration factor, dan exponential jitter smoothing.
- `sensitivity_bridge_jni.cpp`: Adapter JNI khusus untuk Android runtime (dalvik/art) menghubungkan Kotlin dengan C++ native library (`libpaseosensi_native.so`).
- `sensitivity_engine_desktop_cli.cpp`: Test harness mandiri untuk PC (Windows / Linux) untuk menguji kalkulasi secara independen.

## 2. Cara Kompilasi di PC / Desktop

### Linux / WSL:
```bash
cd app/src/main/cpp
g++ -std=c++17 -O3 sensitivity_engine.cpp sensitivity_engine_desktop_cli.cpp -o paseo_cli
./paseo_cli
```

### Windows (MSVC / MinGW):
```cmd
cl /EHsc /std:c++17 sensitivity_engine.cpp sensitivity_engine_desktop_cli.cpp /Fe:paseo_cli.exe
paseo_cli.exe
```

## 3. Batasan Teknis & Keamanan (Android vs PC)

- **Android (Non-Root)**: Sesuai arsitektur keamanan sandbox Android, aplikasi biasa tanpa izin root/sistem tidak dapat menginjeksi atau mencegat koordinat sentuhan secara global ke proses game lain. Modul C++ ini berfungsi menghitung kurva transformasi sentuh, simulasi aiming pad internal, tuning kalibrasi sensitivitas, dan manajemen profil per game.
- **Android (Shizuku / ADB)**: Memberikan akses aman ke setting display (`wm size`, `wm density`) dan pointer speed sistem tanpa perlu root.
- **PC / Windows**: Pada OS desktop, mouse hook (`SetWindowsHookEx` / Raw Input) secara teknis dapat digunakan untuk transformasi kursor global jika dibangun sebagai desktop client tersendiri. Namun APK Android tidak dapat berjalan langsung sebagai aplikasi Windows tanpa layer emulasi/WSA.
