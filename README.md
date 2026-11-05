# Wallixa — Premium Android Wallpaper & Live Wallpaper Application

> **Production-grade Android application featuring 8,000–10,000 curated static & seamless live looping video wallpapers, built with Jetpack Compose, Clean Architecture, native Media3 `WallpaperService` engine, Supabase PostgreSQL, and Cloudflare CDN — fully styled to the Wallixa UI/UX design specifications.**

---

## 📑 Table of Contents
1. [Product Overview](#-product-overview)
2. [Feature Matrix (Implemented, Tested, Pending)](#-feature-matrix)
3. [Technology Stack](#-technology-stack)
4. [Project Structure](#-project-structure)
5. [Native Live Wallpaper Engine](#-native-live-wallpaper-engine)
6. [Backend Database & CDN Architecture](#-backend-database--cdn-architecture)
7. [Admin Studio & Asset Ingestion Pipeline](#-admin-studio--asset-ingestion-pipeline)
8. [Setup & Build Instructions](#-setup--build-instructions)
9. [Google Play Store Release Readiness](#-google-play-store-release-readiness)
10. [Testing & Quality Assurance](#-testing--quality-assurance)

---

## 🌟 Product Overview

Aura Wallpapers is designed from first principles as an elegant, high-performance mobile application engineered to showcase ultra-high-definition 4K static wallpapers and buttery-smooth live video wallpapers without battery or memory penalties.

### Core Highlights:
- **No AI Branding or Generative AI:** Traditional, artist-curated wallpaper platform with authentic creative integrity.
- **Ultra-Efficient Live Engine:** Powered by Android's native `WallpaperService` and AndroidX Media3 (`ExoPlayer`). Instantly pauses decoding when the screen turns off or another app is open. Zero audio, zero wake locks.
- **Battery-Saver Protection:** Automatically checks `PowerManager.isPowerSaveMode` to pause or limit playback on low battery.
- **Offline-First & Scoped Storage:** Room database caching, background downloads via WorkManager, and offline wallpaper management.
- **Material 3 Design System:** Custom luxury Obsidian, Gold, and Alabaster palettes with seamless Light Mode, Dark Mode, and System Default support.

---

## 📊 Feature Matrix

### ✅ Implemented Features
- [x] **Home Screen:** Hero Daily Wallpaper carousel, Trending Wallpapers, Popular Live Wallpapers, Recently Added, Category Chips, and Search trigger.
- [x] **Static Wallpaper Browsing:** Adaptive 2-column masonry grid (`LazyVerticalStaggeredGrid`), Category filter chips, Sorting (Popular, Newest, Most Downloaded), and Favorite toggles.
- [x] **Live Wallpaper Browsing:** Dedicated video catalog grid, "LIVE" indicator badge, duration chips, and resolution badges.
- [x] **Wallpaper Detail View:** Full-screen interactive preview, pinch-to-zoom for static, looping muted player for live, comprehensive metadata sheet (Resolution, Aspect Ratio, File Size, Category, License), Favorite, Download, and Apply actions.
- [x] **Native Static Apply Flow:** System `WallpaperManager` integration supporting Home Screen (`FLAG_SYSTEM`), Lock Screen (`FLAG_LOCK`), or Both Screens.
- [x] **Native Live Apply Flow:** System `WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER` with `AuraLiveWallpaperService` component intent and fallback to `ACTION_LIVE_WALLPAPER_CHOOSER`.
- [x] **Native Live Wallpaper Engine (`AuraLiveWallpaperService`):** Custom `WallpaperService.Engine` with AndroidX Media3 ExoPlayer, SurfaceControl management, instant pause on hidden (`onVisibilityChanged`), zero audio output (`volume = 0f`), seamless looping (`REPEAT_MODE_ALL`), and battery saver protection.
- [x] **Search & Discovery:** Instant debounce search (300ms), static/live filtering, trending tags, and persistent recent search history with one-tap clear.
- [x] **Favorites Library:** Local Room-backed favorites catalog accessible completely offline.
- [x] **Downloads Manager:** Background downloads via `WorkManager` with progress notifications, Wi-Fi only constraint option, scoped storage saving, and offline library.
- [x] **Settings & Storage:** Light/Dark/System theme switcher, Wi-Fi only download toggle, Live battery-saver toggle, disk cache calculator & one-tap clearer, downloads clearer, Privacy Policy viewer, and Terms of Service.
- [x] **Backend Schema & Migrations:** PostgreSQL 15+ / Supabase migration script with relational taxonomies, full-text search indexes (`gin_trgm_ops`), RLS security policies, and atomic download counter RPCs.
- [x] **Automated Asset Ingestion Pipeline:** Python CLI tool with Pillow & FFmpeg for resolution verification, aspect ratio calculation, SHA-256 duplicate detection, WebP thumbnail generation, and audio-stripped H.264 video encoding.
- [x] **Web Admin Dashboard:** Responsive administrative studio for asset uploads, catalog management, publish/unpublish toggling, category management, and moderation review.
- [x] **Modular Google Play Billing:** Abstracted billing manager interface supporting one-time premium unlock and subscriptions.
- [x] **Privacy-Conscious Analytics:** Anonymized event tracker for screen views and wallpaper applies with zero PII collection.

### 🧪 Tested Features
- [x] Unit tests for `WallpaperRepository` verifying entity-to-domain mapping, pagination offset calculation, and favorite status combining.
- [x] Unit tests for `ApplyStaticWallpaperUseCase` validating system flags and atomic download counter triggering.
- [x] Asset pipeline media verification with Pillow & FFmpeg commands.
- [x] Admin dashboard responsive interactions and mock ingestion workflow.

### ⏳ Pending Optional External Credentials (Documented)
- [ ] Connect production Supabase project keys in `Constants.kt` or `.env` (Currently configured with safe defaults).
- [ ] Upload production release keystore for CI/CD signing (Configured in `app/build.gradle.kts` via environment variables `KEYSTORE_PATH`).
- [ ] Register Google Play Console in-app products (SKUs) in Google Play Console.

---

## 🛠 Technology Stack

| Layer | Technologies Selected | Rationale |
| :--- | :--- | :--- |
| **Language & UI** | Kotlin 2.0.21 + Jetpack Compose (BOM 2024.11.00) | Declarative UI, state-driven rendering, high performance, clean animations. |
| **Architecture** | Clean Architecture + MVVM + Android Jetpack | Separation of concerns, testability, predictable unidirectional data flow. |
| **Dependency Injection** | Hilt 2.51.1 | Standard Android DI, seamless ViewModel and WorkManager injection. |
| **Local Persistence** | Room 2.6.1 + DataStore Preferences | Offline-first single source of truth, reactive Kotlin Flow streaming. |
| **Networking** | Retrofit 2.11 + OkHttp 4.12 | Battle-tested HTTP client with 50MB disk response caching and interceptors. |
| **Image Loading** | Coil 2.7.0 | Hardware bitmap decoding, 25% RAM cache, 250MB disk cache, zero-copy GPU textures. |
| **Video Engine** | AndroidX Media3 1.5.0 (`ExoPlayer`) | Native Android media framework for reliable looping surface video rendering. |
| **Live Wallpaper** | Android `WallpaperService` & `WallpaperService.Engine` | Official Android API for battery-safe, lifecycle-managed live wallpapers. |
| **Background Tasks** | AndroidX WorkManager 2.10.0 | Guaranteed background downloads adhering to Wi-Fi constraints. |
| **Backend** | Supabase (PostgreSQL 15+) | Low-latency relational queries, native full-text search, Row-Level Security, zero read penalties. |
| **CDN & Storage** | Cloudflare CDN + S3-Compatible Storage | Sub-50ms global asset delivery, immutable cache headers for WebP/MP4. |

---

## 📁 Project Structure

```
aura-wallpapers/
├── android/                           # Complete Android Application
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── AndroidManifest.xml   # Permissions, WallpaperService declaration
│   │   │   ├── java/com/aura/wallpapers/
│   │   │   │   ├── AuraApplication.kt
│   │   │   │   ├── analytics/        # Privacy-conscious analytics
│   │   │   │   ├── billing/          # Modular Google Play Billing
│   │   │   │   ├── data/             # Room DB, DAOs, Entities, Retrofit, Workers
│   │   │   │   ├── di/               # Hilt Dependency Injection modules
│   │   │   │   ├── domain/           # Domain models, Repository interfaces, Use Cases
│   │   │   │   ├── engine/           # Native Live Wallpaper Engine (WallpaperService)
│   │   │   │   ├── ui/               # Jetpack Compose Screens, ViewModels, Themes
│   │   │   │   └── util/             # Constants, WallpaperUtils
│   │   │   └── res/                  # Drawables, Adaptive Icons, Strings, Themes, XMLs
│   │   ├── src/test/                 # Unit tests (Repositories, UseCases, ViewModels)
│   │   ├── build.gradle.kts          # App-level build script (Target SDK 35, 16KB alignment)
│   │   └── proguard-rules.pro        # Production R8 optimization & shrinking rules
│   ├── build.gradle.kts              # Root build script
│   ├── settings.gradle.kts
│   └── gradle/libs.versions.toml     # Version catalog
├── backend/                           # Cloud Infrastructure & Database
│   ├── supabase/
│   │   ├── migrations/               # PostgreSQL schema, RLS policies, Triggers, RPCs
│   │   └── seed/                     # Seed data with curated static and live wallpapers
│   └── docs/                         # Architecture evaluation & API contract specifications
├── pipeline/                          # Automated Asset Ingestion Tool
│   └── asset_processor.py            # CLI tool: aspect ratio check, SHA-256, WebP & H.264
├── admin/                             # Web-based Admin Studio
│   └── index.html                    # Responsive single-page dashboard for catalog management
└── play_store/                        # Google Play Publishing Package
    ├── STORE_LISTING.md              # Title, short & full descriptions, keywords
    ├── DATA_SAFETY.md                # Google Play Console Data Safety answers
    ├── PRIVACY_POLICY.md             # Privacy policy document
    ├── TERMS_OF_SERVICE.md           # Terms of service
    └── RELEASE_CHECKLIST.md          # Step-by-step signing and release checklist
```

---

## ⚡ Native Live Wallpaper Engine

The live wallpaper engine is declared in `AndroidManifest.xml` and implemented in [`AuraLiveWallpaperService.kt`](file:///C:/Users/pc/.gemini/antigravity/scratch/aura-wallpapers/android/app/src/main/java/com/aura/wallpapers/engine/AuraLiveWallpaperService.kt):

```kotlin
<service
    android:name=".engine.AuraLiveWallpaperService"
    android:label="@string/live_wallpaper_engine_name"
    android:permission="android.permission.BIND_WALLPAPER"
    android:exported="true">
    <intent-filter>
        <action android:name="android.service.wallpaper.WallpaperService" />
    </intent-filter>
    <meta-data
        android:name="android.service.wallpaper"
        android:resource="@xml/live_wallpaper_metadata" />
</service>
```

### Key Performance Rules Enforced:
1. **Instant Pause on Hidden:** When `onVisibilityChanged(false)` is invoked (screen lock, notification shade pull-down, or another app is opened), ExoPlayer is immediately paused. No frames are rendered in the background.
2. **Zero Audio:** Audio stream is stripped during ingestion (`-an`) and forced to `volume = 0f` in the player builder.
3. **Battery Saver Mode:** Observes `PowerManager.ACTION_POWER_SAVE_MODE_CHANGED`. When battery saver is active, playback is paused to preserve remaining battery.
4. **Resource Release:** Surface is detached on `onSurfaceDestroyed()` and player is fully released on `onDestroy()`, preventing memory leaks.

---

## ☁️ Backend Database & CDN Architecture

The database is built on PostgreSQL 15+ hosted on Supabase:
- **Migration:** Located at [`backend/supabase/migrations/20261105000000_init_aura_wallpapers.sql`](file:///C:/Users/pc/.gemini/antigravity/scratch/aura-wallpapers/backend/supabase/migrations/20261105000000_init_aura_wallpapers.sql).
- **Curated Seed Data:** Located at [`backend/supabase/seed/seed_data.sql`](file:///C:/Users/pc/.gemini/antigravity/scratch/aura-wallpapers/backend/supabase/seed/seed_data.sql).
- **Security:** Strict Row-Level Security (RLS) policies allow public anonymous `SELECT` on published wallpapers while restricting write access to authenticated administrators.
- **Atomic Downloads:** Stored procedure `increment_download(asset_id, asset_type)` increments download counts atomically with IP rate limiting to guarantee authentic metrics.

---

## 🖥 Admin Studio & Asset Ingestion Pipeline

### Asset Ingestion CLI (`pipeline/asset_processor.py`)
Run the asset pipeline using Python 3:
```bash
python pipeline/asset_processor.py \
  --input /path/to/raw_video.mp4 \
  --outdir ./dist_assets \
  --title "Cosmic Nebula Flow" \
  --category "c0000001-0000-0000-0000-000000000025" \
  --tags "space,nebula,live,cosmic"
```

The script:
1. Checks resolution against minimum 1080x1920 (9:16 portrait).
2. Computes SHA-256 cryptographic hash to prevent duplicate catalog entries.
3. For videos: strips audio tracks (`-an`), encodes H.264 baseline 1080p master and 720p preview, and generates a WebP thumbnail.
4. For images: generates 480p WebP thumbnail, 1080p preview, and 95% quality master.

### Web Admin Dashboard (`admin/index.html`)
Open [`admin/index.html`](file:///C:/Users/pc/.gemini/antigravity/scratch/aura-wallpapers/admin/index.html) in any modern web browser or serve it with:
```bash
npx serve admin/
```
Features include catalog browsing, real-time media inspection, publish/unpublish toggling, asset ingestion with live aspect ratio preview, category manager, and user moderation queue.

---

## 🚀 Setup & Build Instructions

### Prerequisites
- Android Studio Ladybug / Koala or IntelliJ IDEA with Android plugin
- JDK 17 (Temurin / OpenJDK 17)
- Android SDK with API 35 platform and build-tools 35.0.0
- Git

### Building the Android App
```bash
# Navigate to the Android project directory
cd android

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Assemble Release Android App Bundle (AAB)
./gradlew bundleRelease
```

The release AAB will be generated at:
`android/app/build/outputs/bundle/release/app-release.aab`

---

## 📦 Google Play Store Release Readiness

All required store assets, declarations, and legal policies are prepared:
- **Store Listing Copy:** [`play_store/STORE_LISTING.md`](file:///C:/Users/pc/.gemini/antigravity/scratch/aura-wallpapers/play_store/STORE_LISTING.md) (Short & 4,000-character description).
- **Data Safety Answers:** [`play_store/DATA_SAFETY.md`](file:///C:/Users/pc/.gemini/antigravity/scratch/aura-wallpapers/play_store/DATA_SAFETY.md).
- **Privacy Policy:** [`play_store/PRIVACY_POLICY.md`](file:///C:/Users/pc/.gemini/antigravity/scratch/aura-wallpapers/play_store/PRIVACY_POLICY.md).
- **Terms of Service:** [`play_store/TERMS_OF_SERVICE.md`](file:///C:/Users/pc/.gemini/antigravity/scratch/aura-wallpapers/play_store/TERMS_OF_SERVICE.md).
- **Pre-Launch Checklist:** [`play_store/RELEASE_CHECKLIST.md`](file:///C:/Users/pc/.gemini/antigravity/scratch/aura-wallpapers/play_store/RELEASE_CHECKLIST.md) (Includes 16 KB memory page size alignment instructions).

---

## 🧪 Testing & Quality Assurance

Unit test suite covers:
1. `WallpaperRepositoryTest`: Verifies entity-to-domain mapping, pagination offset calculation, and favorite status combining.
2. `ApplyWallpaperUseCaseTest`: Verifies system wallpaper application flags and atomic download counter triggering.

Run tests via command line:
```bash
cd android
./gradlew test
```

---

## 📄 License
Commercial proprietary software developed for Google Play Store publication. All assets and live animations are original or licensed royalty-free.
