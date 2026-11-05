# Aura Wallpapers — System Architecture & Technical Specifications

## 1. System Topology Overview

```
                                      ┌────────────────────────────────┐
                                      │ Cloudflare CDN Edge Cache      │
                                      │ • TTL 300s for JSON manifests  │
                                      │ • 1 Year for Immutable WebP/MP4│
                                      └──────────────┬─────────────────┘
                                                     ▲
                                                     │ HTTPS (REST / PostgREST)
                       ┌─────────────────────────────┴─────────────────────────────┐
                       ▼                                                           ▼
       ┌───────────────────────────────┐                           ┌───────────────────────────────┐
       │   Supabase PostgreSQL 15+     │                           │   Supabase / S3 Object Store  │
       │   • Relational Taxonomies     │                           │   • /wallpapers/thumbnails/   │
       │   • Full-Text Search (trgm)   │                           │   • /wallpapers/previews/     │
       │   • Atomic Download Counter   │                           │   • /wallpapers/originals/    │
       │   • Row-Level Security (RLS)  │                           │   • /live/previews/ (720p)    │
       └───────────────────────────────┘                           │   • /live/originals/ (1080p)  │
                                                                   └───────────────────────────────┘
                                                                                   ▲
                                                                                   │
                                                                ┌──────────────────┴──────────────────┐
                                                                │  Admin Studio & Asset Ingestion CLI │
                                                                │  • Pillow WebP generation           │
                                                                │  • FFmpeg H.264 baseline -an audio  │
                                                                │  • SHA-256 duplicate detection      │
                                                                └─────────────────────────────────────┘

                                         ▲
                                         │ HTTPS (TLS 1.3)
                                         │
┌────────────────────────────────────────┴────────────────────────────────────────────────────────┐
│                              AURA ANDROID APPLICATION ARCHITECTURE                              │
│                                                                                                 │
│  ┌───────────────────────────────────────────────────────────────────────────────────────────┐  │
│  │ UI Layer (Jetpack Compose, Material 3, MVVM)                                              │  │
│  │ • Home, Wallpapers, Live, Favorites, Downloads, Search, Detail, Settings Screens         │  │
│  │ • Coil 2.7 Hardware Bitmap Memory/Disk Cache                                             │  │
│  └─────────────────────────────────────────────┬─────────────────────────────────────────────┘  │
│                                                │ StateFlow / UiState                            │
│  ┌─────────────────────────────────────────────▼─────────────────────────────────────────────┐  │
│  │ Domain Layer (Clean Architecture Use Cases)                                               │  │
│  │ • GetWallpapersUseCase, ApplyStaticWallpaperUseCase, PrepareLiveWallpaperUseCase         │  │
│  └─────────────────────────────────────────────┬─────────────────────────────────────────────┘  │
│                                                │ Repository Interfaces                          │
│  ┌─────────────────────────────────────────────▼─────────────────────────────────────────────┐  │
│  │ Data Layer (Offline-First Single Source of Truth)                                         │  │
│  │ • Room Database (Wallpapers, LiveWallpapers, Categories, Favorites, Downloads, History)   │  │
│  │ • OkHttp Streaming & WorkManager DownloadWorker (Wi-Fi constraint, Scoped Storage)        │  │
│  │ • Retrofit 2 + Supabase PostgREST ApiService                                              │  │
│  └───────────────────────────────────────────────────────────────────────────────────────────┘  │
│                                                                                                 │
│  ┌───────────────────────────────────────────────────────────────────────────────────────────┐  │
│  │ Native Android Live Wallpaper Engine (AuraLiveWallpaperService)                           │  │
│  │ • Lifecycle-Aware WallpaperService.Engine                                                 │  │
│  │ • AndroidX Media3 ExoPlayer with SurfaceControl / SurfaceHolder                           │  │
│  │ • Zero Audio (volume = 0f), Seamless Looping (REPEAT_MODE_ALL)                            │  │
│  │ • onVisibilityChanged(false) -> Instant Decoder Pause (Zero Background Rendering)         │  │
│  │ • PowerManager Battery Saver Detection -> Frame Throttling                                │  │
│  └───────────────────────────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Native Live Wallpaper Engine Deep-Dive

### Lifecycle Management
The native live wallpaper engine (`AuraLiveWallpaperService`) implements `android.service.wallpaper.WallpaperService`:

1. **Surface Creation (`onSurfaceCreated`):**
   - Retrieves the active wallpaper file path from local preferences.
   - Instantiates an isolated `ExoPlayer` instance.
   - Binds the `SurfaceHolder` directly to the player: `player.setVideoSurfaceHolder(holder)`.
   - Forces `volume = 0f` and `repeatMode = Player.REPEAT_MODE_ALL`.
   - Sets video scaling mode to `VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING` to fill all device aspect ratios cleanly without letterboxing.

2. **Visibility State Handling (`onVisibilityChanged`):**
   - When the user returns to the Home Screen or Lock Screen (`visible = true`), the engine verifies device power state and calls `player.play()`.
   - When the user opens an app, locks the device, or switches tasks (`visible = false`), the engine **immediately calls `player.pause()`**.
   - No video frames are decoded, rendered, or passed to the GPU when the wallpaper surface is obscured.

3. **Battery Saver Protection:**
   - Registers a `BroadcastReceiver` listening for `PowerManager.ACTION_POWER_SAVE_MODE_CHANGED`.
   - When the device battery falls below 20% and system battery saver is activated, live wallpaper playback is automatically paused, saving maximum energy.

4. **Surface Destruction & Cleanup (`onSurfaceDestroyed` & `onDestroy`):**
   - Clears surface reference: `player.clearVideoSurfaceHolder(holder)`.
   - Explicitly stops and releases player: `player.stop(); player.release()`.
   - Unregisters broadcast receivers to prevent memory leaks.

---

## 3. Static Wallpaper Apply Flow

```
User Taps "Apply"
       │
       ▼
[ ApplyWallpaperBottomSheet ]
       │
       ├─► Home Screen Selected        ──► flags = WallpaperManager.FLAG_SYSTEM
       ├─► Lock Screen Selected        ──► flags = WallpaperManager.FLAG_LOCK
       └─► Home & Lock Screen Selected ──► flags = WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK
       │
       ▼
WallpaperUtils.applyStaticWallpaper(...)
       │
       ├─► Downloads high-resolution original stream via OkHttp
       ├─► Decodes bitmap with inJustDecodeBounds & inSampleSize optimization
       ├─► Calls WallpaperManager.getInstance(context).setBitmap(bitmap, null, true, flags)
       ├─► Recycles bitmap immediately to reclaim RAM
       └─► Calls Supabase RPC increment_download(id) atomically
```

---

## 4. Performance & Memory Budget

| Metric | Target Budget | Measured Architecture Characteristics |
| :--- | :--- | :--- |
| **Cold Startup Time** | < 800ms | SplashScreen API + Lazy Compose initialization, zero blocking disk/network I/O |
| **Scrolling Frame Rate** | 60–120 FPS | Hardware bitmap decoding, stable item keys in `LazyVerticalStaggeredGrid` |
| **Memory Footprint (Idle)** | < 65 MB | Strict image cache ceiling (25% RAM), memory recycled on navigation |
| **Live Engine Memory** | < 45 MB | Hardware video decoding surface buffer, zero audio pipelines |
| **APK Size (Compressed AAB)** | < 12 MB | Zero bundled wallpaper originals, R8 resource shrinking enabled |
| **Battery Draw (Live)** | < 1.5% per 24hr | Zero background rendering when screen is locked or another app is open |
