# Aura Wallpapers - API Contracts & Endpoint Specifications

All endpoints use PostgREST conventions over HTTPS.
Base URL: `https://<supabase-project-id>.supabase.co/rest/v1`

## Common Headers
- `apikey: <SUPABASE_ANON_KEY>`
- `Authorization: Bearer <SUPABASE_ANON_KEY>`
- `Content-Type: application/json`
- `Prefer: return=representation` (when updating or inserting)

---

## 1. Wallpapers (Static)

### 1.1 List Wallpapers (Paginated & Filtered)
- **Endpoint:** `GET /wallpapers`
- **Query Parameters:**
  - `select`: `id,title,slug,description,category_id,tags,thumbnail_url,preview_url,original_url,width,height,aspect_ratio,file_size_bytes,format,download_count,is_featured,is_premium,created_at,categories(id,name,slug)`
  - `is_published`: `eq.true`
  - `category_id`: `eq.<UUID>` (optional filter)
  - `is_featured`: `eq.true` (optional filter)
  - `order`: `created_at.desc` (Newest) OR `download_count.desc` (Popular)
  - `limit`: `30`
  - `offset`: `<page_offset>` (e.g. 0, 30, 60...)
- **Response `200 OK`:**
```json
[
  {
    "id": "e6a2b3c4-1234-4567-89ab-cdef01234567",
    "title": "Obsidian Peaks",
    "slug": "obsidian-peaks",
    "description": "Dramatic dark mountain landscape under starry night sky.",
    "category_id": "c1a2b3c4-1111-2222-3333-444455556666",
    "tags": ["mountains", "night", "dark", "amoled", "stars"],
    "thumbnail_url": "https://cdn.aura-wallpapers.com/wallpapers/thumbnails/obsidian-peaks.webp",
    "preview_url": "https://cdn.aura-wallpapers.com/wallpapers/previews/obsidian-peaks.webp",
    "original_url": "https://cdn.aura-wallpapers.com/wallpapers/originals/obsidian-peaks.webp",
    "width": 2160,
    "height": 3840,
    "aspect_ratio": "9:16",
    "file_size_bytes": 1845200,
    "format": "image/webp",
    "download_count": 14205,
    "is_featured": true,
    "is_premium": false,
    "created_at": "2026-10-15T08:00:00Z",
    "categories": {
      "id": "c1a2b3c4-1111-2222-3333-444455556666",
      "name": "Mountains",
      "slug": "mountains"
    }
  }
]
```

---

## 2. Live Wallpapers (Video)

### 2.1 List Live Wallpapers
- **Endpoint:** `GET /live_wallpapers`
- **Query Parameters:**
  - `select`: `id,title,slug,description,category_id,tags,thumbnail_url,preview_video_url,original_video_url,width,height,duration_seconds,frame_rate,has_audio,codec,file_size_bytes,download_count,is_featured,is_premium,created_at,categories(id,name,slug)`
  - `is_published`: `eq.true`
  - `category_id`: `eq.<UUID>` (optional)
  - `order`: `download_count.desc` OR `created_at.desc`
  - `limit`: `20`
  - `offset`: `<page_offset>`
- **Response `200 OK`:**
```json
[
  {
    "id": "f7b3c4d5-2222-3333-4444-555566667777",
    "title": "Nebula Drift",
    "slug": "nebula-drift",
    "description": "Smooth seamless looping interstellar cosmic clouds.",
    "category_id": "d2b3c4d5-3333-4444-5555-666677778888",
    "tags": ["space", "nebula", "live", "cosmic", "ambient"],
    "thumbnail_url": "https://cdn.aura-wallpapers.com/live/thumbnails/nebula-drift.webp",
    "preview_video_url": "https://cdn.aura-wallpapers.com/live/previews/nebula-drift-720p.mp4",
    "original_video_url": "https://cdn.aura-wallpapers.com/live/originals/nebula-drift-1080p.mp4",
    "width": 1080,
    "height": 1920,
    "duration_seconds": 12.5,
    "frame_rate": 30.0,
    "has_audio": false,
    "codec": "video/mp4",
    "file_size_bytes": 12845000,
    "download_count": 9840,
    "is_featured": true,
    "is_premium": false,
    "created_at": "2026-10-20T12:00:00Z",
    "categories": {
      "id": "d2b3c4d5-3333-4444-5555-666677778888",
      "name": "Live Space",
      "slug": "live-space"
    }
  }
]
```

---

## 3. Categories

### 3.1 List All Categories
- **Endpoint:** `GET /categories`
- **Query Parameters:**
  - `select`: `id,name,slug,description,icon_name,cover_image_url,is_live,display_order,wallpaper_count`
  - `order`: `display_order.asc`
- **Response `200 OK`:**
```json
[
  {
    "id": "c1a2b3c4-1111-2222-3333-444455556666",
    "name": "Nature",
    "slug": "nature",
    "description": "Lush landscapes, forests, and untouched wilderness.",
    "icon_name": "nature",
    "cover_image_url": "https://cdn.aura-wallpapers.com/categories/nature.webp",
    "is_live": false,
    "display_order": 1,
    "wallpaper_count": 420
  }
]
```

---

## 4. Search and Full-Text Discovery

### 4.1 Search Catalog
- **Endpoint:** `GET /rpc/search_catalog`
- **Method:** `POST` or `GET` via RPC function
- **Parameters:**
```json
{
  "search_query": "amoled mountain",
  "content_type": "all",
  "limit_count": 30,
  "offset_count": 0
}
```

---

## 5. Metrics & Atomic Increment

### 5.1 Increment Download Counter
- **Endpoint:** `POST /rpc/increment_download`
- **Body:**
```json
{
  "asset_id": "e6a2b3c4-1234-4567-89ab-cdef01234567",
  "asset_type": "static"
}
```
- **Response `204 No Content`**
- Uses transactional atomic counter with server-side sliding window rate limiter (max 5 increments per IP per hour per asset) to guarantee authentic, tamper-free engagement statistics.
