# 🎮 PASEO SENSI — Gaming Utility & Calibration Engine

**App Name:** PASEO SENSI  
**Developer:** ZUCCHERO XANN — DEVELOPER  
**Version:** 1.0.0  
**Package Name:** `com.zuccheroxann.paseosensi`  
**Platform:** Android 8.0 (API 26) – Android 16 (API 36)  
**Bahasa UI:** Bahasa Indonesia  
**Repository:** [zuccheroxix-lab/PASEO-TOOLS-](https://github.com/zuccheroxix-lab/PASEO-TOOLS-)  

---

## 📥 DOWNLOAD LANGSUNG APK (ANDROID)

Unduh versi resmi aplikasi langsung ke perangkat Android Anda tanpa perlu kompilasi manual:

| Berkas | Deskripsi | Tautan Unduh Langsung |
|---|---|---|
| **PASEO-SENSI-v1.0.0.apk** | **Versi Release Resmi (Siap Pakai)** | 🚀 **[Download Release APK](https://github.com/zuccheroxix-lab/PASEO-TOOLS-/releases/download/v1.0.0/PASEO-SENSI-v1.0.0.apk)** |
| **PASEO-SENSI-v1.0.0-debug.apk** | Versi Debug Pengujian | 🛠️ **[Download Debug APK](https://github.com/zuccheroxix-lab/PASEO-TOOLS-/releases/download/v1.0.0/PASEO-SENSI-v1.0.0-debug.apk)** |
| **PASEO-SENSI-v1.0.0-source.zip** | Arsip Source Code Lengkap | 📦 **[Download Source ZIP](https://github.com/zuccheroxix-lab/PASEO-TOOLS-/releases/download/v1.0.0/PASEO-SENSI-v1.0.0-source.zip)** |

> 🌐 **Halaman GitHub Releases Lengkap:**  
> [https://github.com/zuccheroxix-lab/PASEO-TOOLS-/releases/tag/v1.0.0](https://github.com/zuccheroxix-lab/PASEO-TOOLS-/releases/tag/v1.0.0)

---

## 📱 Petunjuk Instalasi di HP Android

1. **Unduh Berkas APK:**
   - Ketuk tautan **[Download Release APK](https://github.com/zuccheroxix-lab/PASEO-TOOLS-/releases/download/v1.0.0/PASEO-SENSI-v1.0.0.apk)** langsung dari peramban HP Anda (Google Chrome, Samsung Internet, Mi Browser, dll.).
2. **Peringatan Keamanan Peramban:**
   - Jika peramban memunculkan dialog *"File mungkin berbahaya / File might be harmful"* (peringatan standar Android untuk instalasi di luar Play Store), pilih opsi **Tetap unduh / Download anyway**.
3. **Buka Berkas APK:**
   - Setelah unduhan selesai, buka notifikasi atau cari file di aplikasi **Pengelola File (File Manager)** pada folder **Download**.
4. **Izinkan Instalasi dari Sumber Tidak Dikenal:**
   - Jika sistem Android meminta izin instalasi, ketuk **Pengaturan (Settings)** dan aktifkan opsi **"Izinkan dari sumber ini / Allow from this source"**.
5. **Selesaikan Instalasi:**
   - Ketuk **Instal (Install)** dan tunggu beberapa detik hingga selesai.
   - Buka aplikasi **PASEO SENSI** dan mulai konfigurasi gaming Anda!

---

## 🚀 Fitur Lengkap PASEO SENSI

### 1. 🛡️ Home & Integrasi Shizuku
- **Status Layanan Shizuku Nyata:** Mendeteksi secara akurat apakah service Shizuku `Connected`, `Disconnected`, atau `Unsupported` melalui IPC Binder resmi.
- **Informasi Hardware & Sistem:** Membaca versi Android aktual, API Level, arsitektur ABI (`arm64-v8a`), serta versi API Shizuku yang aktif.
- **Pengecekan Izin Sistem:** Validasi status izin Shizuku aplikasi dengan tombol peminta izin instan.
- **Panduan Terintegrasi:** Langkah-langkah mudah memulai Shizuku via Wireless Debugging beserta perintah ADB copy-paste untuk pengguna PC.
- **Support Me & Laporan Bug:** Tautan resmi repositori GitHub `zuccheroxix-lab/PASEO-TOOLS-` dan kanal pelaporan isu.

### 2. 🎯 Game Launcher / Apps Manager
- **Inventaris Aplikasi Nyata:** Menggunakan Android `PackageManager` dan deklarasi query Android 11+ untuk memuat aplikasi dan game yang benar-benar terpasang di perangkat.
- **Filter Cerdas Game:** Memisahkan aplikasi umum dan game terpasang secara otomatis.
- **Pencarian Cepat:** Temukan aplikasi atau game berdasarkan nama atau package name.
- **Peluncuran Langsung:** Tombol buka game langsung dari aplikasi dengan intent resmi.

### 3. 🔬 Game-Labs

#### A. Resolution Manager (Visual)
- Menampilkan resolusi dan dimensi layar aktual (`screenWidth x screenHeight`).
- Pilihan preset resolusi yang valid dan aman (misalnya 1080x2400, 720x1600, 1080x1920).
- Penerapan perintah native `wm size` melalui Shizuku dengan dialog konfirmasi keamanan.
- Opsi pemulihan instan (*Reset ke Nilai Bawaan*) menggunakan `wm size reset`.

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
| **Sistem Operasi** | Android 8.0 (Oreo / API 26) | Android 11 – Android 16 (Wireless Debugging) |
| **Arsitektur CPU** | arm64-v8a, armeabi-v7a, x86_64 | arm64-v8a |
| **Shizuku** | Versi 13.5+ (Opsional untuk fitur ADB) | Shizuku Manager via Wireless Debugging |
| **Izin Overlay** | Izin "Tampilkan di atas aplikasi lain" | Diberikan saat mengaktifkan Aim Assistant |

---

## 🛠️ Panduan Setup Shizuku (Tanpa Root)

1. **Aktifkan Opsi Pengembang:**
   - Masuk ke **Pengaturan HP** > **Tentang Ponsel**.
   - Ketuk **Nomor Kompilasi (Build Number)** sebanyak 7 kali berturut-turut.
2. **Aktifkan Proses Debug Nirkabel (Wireless Debugging):**
   - Buka **Pengaturan** > **Sistem / Opsi Pengembang**.
   - Hubungkan HP ke jaringan Wi-Fi.
   - Aktifkan menu **Proses debug nirkabel (Wireless debugging)**.
3. **Hubungkan dengan Aplikasi Shizuku:**
   - Buka aplikasi **Shizuku** di HP Anda.
   - Pilih menu **Pairing (Penyandingan)** > Masukkan 6 digit kode dari Wireless Debugging.
4. **Mulai Layanan (Start Shizuku):**
   - Di Shizuku, tekan tombol **Start**.
   - Buka kembali **PASEO SENSI** dan ketuk **Cek Status**, lalu ketuk **Minta Izin**.

*Untuk pengguna PC via kabel data:*
```bash
adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
```

---

## 📦 Cara Membangun Proyek (Build Lokal)

```bash
# Clone repositori
git clone https://github.com/zuccheroxix-lab/PASEO-TOOLS-.git
cd PASEO-TOOLS-

# Beri izin eksekusi Gradle Wrapper
chmod +x gradlew

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease
```
Berkas APK akan berada di:
- `app/build/outputs/apk/release/PASEO-SENSI-v1.0.0.apk`
- `app/build/outputs/apk/debug/PASEO-SENSI-v1.0.0-debug.apk`

---

## 👤 Identitas & Hak Cipta

- **App Name:** PASEO SENSI
- **Created by:** ZUCCHERO XANN
- **Title:** DEVELOPER
- **Version:** 1.0.0
- **Official Repository:** [zuccheroxix-lab/PASEO-TOOLS-](https://github.com/zuccheroxix-lab/PASEO-TOOLS-)
