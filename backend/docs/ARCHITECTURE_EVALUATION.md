# Backend Architecture Evaluation: Supabase vs. Firebase for Aura Wallpapers

## 1. Executive Summary

Aura Wallpapers is designed to host, paginate, and distribute an expanding catalog of 8,000–10,000 high-resolution static and live video wallpapers to a large global mobile audience. The backend must fulfill strict criteria:
- Low-latency query execution (<50ms p95) for paginated feeds, categories, and full-text searches.
- Cost-effective read scalability (millions of daily list views without linear pricing penalties).
- Clean relational data integrity with foreign keys, composite unique constraints (e.g., user favorites, duplicate hash detection), and transactional updates (e.g., atomic download counters).
- High-bandwidth, zero-egress asset caching via an S3-compatible object store fronted by Cloudflare CDN.
- Rock-solid security using declarative Row-Level Security (RLS) policies.

### Selected Platform: **Supabase (PostgreSQL + PostgREST + Supabase Storage + Cloudflare CDN)**

| Criterion | Supabase (PostgreSQL 15+) | Firebase (Firestore + Cloud Storage) | Verdict |
| :--- | :--- | :--- | :--- |
| **Data Model & Integrity** | Relational: Foreign keys, joins, compound indexes, strict constraints | Document/NoSQL: Denormalization required, fan-out writes | **Supabase** (Prevents orphaned assets/categories) |
| **Search Capabilities** | Native PostgreSQL Full-Text Search (`tsvector`, `tsquery`), trigram (`pg_trgm`) | Requires third-party search provider (Algolia / Typesense) | **Supabase** (Zero extra cost/infrastructure) |
| **Read Pricing at Scale** | Compute-based or flat-tier ($25/mo Pro covers millions of reads) | Document read pricing ($0.06 per 100K reads, cost spirals) | **Supabase** (Predictable, orders of magnitude cheaper) |
| **Asset Storage & CDN** | S3-compatible, direct Cloudflare CDN reverse-proxy integration | Google Cloud Storage, CDN requires complex Cloud CDN setup | **Supabase** (Seamless custom CDN domain) |
| **Atomic Operations** | Stored procedures (`plpgsql`), triggers, row-level locks (`SELECT FOR UPDATE`) | Transactions / Distributed counters | **Supabase** (Native ACID compliance) |
| **Security Model** | SQL standard Row-Level Security (RLS) policies at the database level | Firebase Security Rules (custom DSL, complex testing) | **Supabase** (Battle-tested, standards-based) |
| **Client SDK** | Standard REST/PostgREST HTTP API + native Kotlin / OkHttp | Heavy proprietary Firebase SDK | **Supabase** (Lightweight APK footprint) |

---

## 2. Relational Schema Architecture

The catalog requires interconnected relations:
- **`wallpapers`** and **`live_wallpapers`** linked to **`categories`** via foreign keys.
- **`tags`** and many-to-many junction tables for instantaneous faceted discovery.
- **`download_logs`** and **`atomic counter triggers`** to prevent artificial inflation while maintaining real-time popularity rankings.
- Perceptual hash (`phash`) and SHA-256 binary hash columns with unique partial indexes to prevent duplicate uploads automatically at the database level.

Firestore’s NoSQL model would require duplicating category metadata into every single wallpaper document. Updating a category name or slug would require updating thousands of individual documents (with associated write costs and concurrency limits). In PostgreSQL, a single foreign key reference guarantees consistency and zero-cost cascading updates.

---

## 3. Storage and CDN Delivery Architecture

```
[ Android Client / Aura App ]
             │
             ├──► 1. Metadata Query: GET /rest/v1/wallpapers?order=download_count.desc&limit=30
             │         │
             │         ▼
             │   [ Cloudflare Edge Cache / CDN ] (TTL: 300s for catalog listings)
             │         │
             │         ▼ (on cache miss)
             │   [ Supabase PostgREST Engine ]
             │         │
             │         ▼
             │   [ PostgreSQL Database ]
             │
             └──► 2. Binary Asset Fetch: GET /storage/v1/object/public/wallpapers/previews/uuid.webp
                       │
                       ▼
                 [ Cloudflare Edge CDN (Cache-Control: public, max-age=31536000, immutable) ]
                       │
                       ▼ (on cache miss)
                 [ Supabase S3 Storage Bucket ]
```

### Compression & Format Matrix:
- **Static Thumbnails (Card View):** WebP, 480x854, 80% quality, sub-30KB file size.
- **Static Previews (Detail View):** WebP, 1080x1920, 85% quality, sub-250KB file size.
- **Static Masters (Download/Apply):** Original high-resolution WebP/PNG (up to 4K / 3840x2160).
- **Live Wallpaper Previews:** H.264 Baseline, 720x1280, 24fps, no audio track, sub-1.5MB file size, seamless loop.
- **Live Wallpaper Masters:** H.264/HEVC, 1080x1920 or 1440x2560, 30fps/60fps, no audio track.

---

## 4. Security & Permissions (Least Privilege)

1. **Client App Role (`anon`):**
   - Read-only (`SELECT`) on rows where `published = true`.
   - Cannot insert, update, or delete catalog items.
   - May execute specific RPC functions (e.g. `increment_download_count(wallpaper_id)` with IP rate limiting).
2. **Admin Role (`authenticated` + role claim `admin`):**
   - Full CRUD over catalog, categories, collections, and reports.
   - Dedicated service-role credentials used ONLY in the backend/admin dashboard, never embedded in the Android APK.
3. **Database RLS Policies:**
   - Active on all tables. Even if an attacker obtains the Supabase Anonymous Key, they cannot delete or alter wallpaper records.
