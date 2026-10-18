# 🎮 PASEO SENSI — Gaming Utility & Calibration Engine

**App Name:** PASEO SENSI  
**Developer:** ZUCCHERO XANN — DEVELOPER  
**Version:** 1.0.0  
**Package Name:** `com.zuccheroxann.paseosensi`  
**Platform:** Android 8.0 (API 26) – Android 16 (API 36)  
**Bahasa UI:** Bahasa Indonesia  

---

## 📌 Ringkasan Aplikasi

**PASEO SENSI** adalah aplikasi utilitas gaming native Android yang dirancang khusus untuk para pemain mobile games yang menginginkan responsivitas sentuhan maksimal, kalibrasi reticle akurat, dan kontrol sistem nyata. 

Aplikasi ini mengintegrasikan **Shizuku API** resmi untuk menjalankan perintah sistem tingkat lanjut (`wm size`, `wm density`, `pointer_location`) secara sah melalui Wireless Debugging tanpa memerlukan hak akses root.

---

## 🚀 Fitur Utama

### 1. 🛡️ Home & Integrasi Shizuku
- **Status Layanan Shizuku Nyata:** Mendeteksi secara akurat apakah service Shizuku `Connected`, `Disconnected`, atau `Unsupported`.
- **Informasi Hardware & Sistem:** Membaca versi Android aktual, API Level, arsitektur ABI (misal: `arm64-v8a`), dan versi API Shizuku yang terpasang.
- **Pengecekan Izin Sistem:** Validasi status izin Shizuku aplikasi dengan tombol peminta izin instan.
- **Panduan Terintegrasi:** Langkah-langkah mudah memulai Shizuku via Wireless Debugging beserta perintah ADB copy-paste untuk pengguna PC.
- **Support Me & Laporan Bug:** Tautan resmi repositori GitHub dan kanal pelaporan isu.

### 2. 🎯 Game Launcher / Apps Manager
- **Inventaris Aplikasi Nyata:** Menggunakan Android `PackageManager` dan deklarasi query Android 11+ untuk memuat aplikasi dan game yang benar-benar terpasang di perangkat.
- **Filter Cerdas Game:** Memisahkan aplikasi umum dan game terpasang.
- **Pencarian Cepat:** Temukan aplikasi atau game berdasarkan nama atau package name.
- **Peluncuran Langsung:** Tombol buka game langsung dari aplikasi dengan intent resmi.

### 3. 🔬 Game-Labs

#### A. Resolution Manager (Visual)
- Menampilkan resolusi dan dimensi layar aktual (`screenWidth x screenHeight`).
- Pilihan preset resolusi yang valid dan aman (misalnya 1080x2400, 720x1600, 1080x1920).
- Penerapan perintah native `wm size` melalui Shizuku dengan dialog konfirmasi keamanan.
- Opsi pemulihan instan (*Reset ke Nilai Bawaan Pabrik*) menggunakan `wm size reset`.

#### B. DPI Manager (Visual)
- Membaca nilai density/DPI layar aktif.
- Slider dan input target DPI dengan batas aman tervalidasi (200 – 800 DPI).
- Perintah eksekusi `wm density` via Shizuku.
- Tombol *Reset Default* untuk mengembalikan DPI asli sistem.

#### C. ROG Monitoring (Visual)
- **RAM Realtime:** Membaca penggunaan RAM, memori tersedia, dan total RAM perangkat melalui `ActivityManager.MemoryInfo`.
- **Status & Suhu Baterai:** Pembacaan suhu baterai aktual (°C), level persentase, voltase (mV), dan status pengisian daya via `BatteryManager`.
- **CPU & Display:** Jumlah core prosesor, arsitektur ABI, dan refresh rate layar (Hz).
- **Transparansi Sistem:** Catatan jelas terkait batasan SELinux Android untuk privasi data level kernel.

#### D. Aim Assistant / Custom Crosshair (Visual)
- **Overlay Mengambang Resmi:** Menggunakan `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY` yang melayang di atas semua game tanpa menyentuh atau memodifikasi file game.
- **6 Pilihan Bentuk Reticle:** Classic Cross, Precision Dot, Circle Crosshair, Tactical T (bebas halangan pandangan atas), Sniper Scope, dan Holo Diamond.
- **Kustomisasi Lengkap:** Pilihan warna (Crimson Glow, Neon Emerald, Cyber Cyan, Electric Yellow, Pure White), slider ukuran (14–54dp), ketebalan garis, jarak garis (gap), transparansi (opacity), dan koreksi sumbu X/Y.
- **Pratinjau Interaktif:** Live canvas reticle dalam aplikasi yang merespons perubahan secara seketika.

#### E. Sensi Pack (Konfigurasi)
- Profil sensitivitas drag-shot dan recoil control: *Aggressive Rush (High)*, *Balanced Precision (Medium)*, *Sniper Control (Steady)*, dan *ZX Pro Tournament Spec*.
- Slider kalibrasi 6 sumbu sensitivitas: Sensitivitas Umum, Red Dot Scope, 2x Scope, 4x Scope, Sniper/AWM, dan Kamera Bebas.
- Simpan konfigurasi kustom ke penyimpanan lokal aman.

#### F. Touch Up (Konfigurasi)
- Informasi kecepatan pointer bawaan Android.
- Saklar toggle `pointer_location` ADB untuk memantau garis koordinat sentuhan layar saat latihan aim.
- Rekomendasi pengaturan skala animasi Android untuk respon sentuh tercepat.

#### G. Aim Trick 15% (Konfigurasi)
- Profil kalibrasi mikro-akselerasi kurva sentuhan dinamis hingga 15%.
- **100% Bebas Cheat & Ramah Fair Play:** Tidak mengandung manipulasi memori, injeksi APK, atau aimbot yang melanggar ketentuan pengembang game.

---

## 📋 Persyaratan Sistem & Kompatibilitas

| Komponen | Persyaratan Minimum | Rekomendasi |
|---|---|---|
| **Sistem Operasi** | Android 8.0 (Oreo / API 26) | Android 11 – Android 15 (Wireless Debugging) |
| **Arsitektur CPU** | arm64-v8a, armeabi-v7a, x86_64 | arm64-v8a |
| **Shizuku** | Versi 13.5+ (Opsional untuk fitur ADB) | Shizuku Manager via Wireless Debugging |
| **Izin Overlay** | Izin "Tampilkan di atas aplikasi lain" | Diberikan saat mengaktifkan Aim Assistant |

---

## 🛠️ Panduan Memulai Shizuku (Tanpa Root)

1. **Aktifkan Opsi Pengembang:**
   - Masuk ke **Pengaturan HP** > **Tentang Ponsel**.
   - Ketuk **Nomor Kompilasi (Build Number)** sebanyak 7 kali berturut-turut hingga muncul pesan "Anda adalah seorang pengembang".
2. **Aktifkan Proses Debug Nirkabel (Wireless Debugging):**
   - Buka **Pengaturan** > **Sistem / Opsi Pengembang**.
   - Hubungkan HP ke jaringan Wi-Fi.
   - Aktifkan menu **Proses debug nirkabel (Wireless debugging)**.
3. **Hubungkan dengan Aplikasi Shizuku:**
   - Buka aplikasi **Shizuku** di HP Anda.
   - Pilih menu **Pairing (Penyandingan)** > Ketuk **Opsi Pengembang**.
   - Di Opsi Pengembang, ketuk "Sandingkan perangkat dengan kode penyandingan".
   - Masukkan 6 digit kode ke notifikasi Shizuku.
4. **Mulai Layanan (Start Shizuku):**
   - Di Shizuku, tekan tombol **Start**.
   - Buka kembali **PASEO SENSI** dan ketuk **Cek Status**, lalu ketuk **Minta Izin**.

*Untuk pengguna PC, Anda juga dapat memulai Shizuku via kabel USB dengan perintah:*
```bash
adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
```

---

## 📦 Cara Membangun Proyek (Build APK)

### 1. Build Lokal Menggunakan Gradle
Pastikan Anda telah memasang **JDK 17** dan **Android SDK**.
```bash
# Clone repositori
git clone https://github.com/zuccheroxann/PASEO-SENSI.git
cd PASEO-SENSI

# Beri izin eksekusi Gradle Wrapper
chmod +x gradlew

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease
```
File APK akan berada pada direktori:
- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`

### 2. Build Otomatis via GitHub Actions
Proyek ini dilengkapi file alur kerja `.github/workflows/build-apk.yml`.
- Setiap kali Anda melakukan `push` ke branch utama atau membuat tag versi baru (`v1.0.0`), GitHub Actions akan secara otomatis mengompilasi dan menguji APK.
- APK yang dihasilkan dapat diunduh langsung melalui tab **Actions > Artifacts** atau di halaman **Releases**.

---

## 👤 Identitas & Hak Cipta

- **App Name:** PASEO SENSI
- **Created by:** ZUCCHERO XANN
- **Title:** DEVELOPER
- **Version:** 1.0.0
- **Logo & Branding:** Hak cipta resmi milik ZUCCHERO XANN (ZX).
