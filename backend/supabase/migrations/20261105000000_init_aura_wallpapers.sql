-- ============================================================================
-- AURA WALLPAPERS: Database Schema, Indexes, Triggers, and RLS Policies
-- Target: PostgreSQL 15+ / Supabase
-- ============================================================================

-- 1. Enable Required Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- 2. Create Categories Table
CREATE TABLE IF NOT EXISTS public.categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(80) NOT NULL UNIQUE,
    slug VARCHAR(80) NOT NULL UNIQUE,
    description TEXT,
    icon_name VARCHAR(50) DEFAULT 'category',
    cover_image_url TEXT,
    is_live BOOLEAN NOT NULL DEFAULT false,
    display_order INT NOT NULL DEFAULT 0,
    wallpaper_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 3. Create Static Wallpapers Table
CREATE TABLE IF NOT EXISTS public.wallpapers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(150) NOT NULL,
    slug VARCHAR(180) NOT NULL,
    description TEXT,
    category_id UUID NOT NULL REFERENCES public.categories(id) ON DELETE RESTRICT,
    tags TEXT[] NOT NULL DEFAULT '{}',
    
    -- Asset URLs
    thumbnail_url TEXT NOT NULL,
    preview_url TEXT NOT NULL,
    original_url TEXT NOT NULL,
    
    -- Technical Specifications
    width INT NOT NULL,
    height INT NOT NULL,
    aspect_ratio VARCHAR(10) NOT NULL DEFAULT '9:16',
    file_size_bytes BIGINT NOT NULL,
    format VARCHAR(20) NOT NULL DEFAULT 'image/webp',
    sha256_hash VARCHAR(64) UNIQUE NOT NULL,
    perceptual_hash VARCHAR(64),
    
    -- Legal & Licensing
    license_type VARCHAR(50) NOT NULL DEFAULT 'Standard Royalty-Free',
    author_name VARCHAR(100) DEFAULT 'Aura Curated',
    source_attribution TEXT,
    
    -- Metrics & Status
    download_count BIGINT NOT NULL DEFAULT 0,
    favorite_count BIGINT NOT NULL DEFAULT 0,
    view_count BIGINT NOT NULL DEFAULT 0,
    is_featured BOOLEAN NOT NULL DEFAULT false,
    is_premium BOOLEAN NOT NULL DEFAULT false,
    is_published BOOLEAN NOT NULL DEFAULT true,
    
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 4. Create Live Wallpapers Table (Video)
CREATE TABLE IF NOT EXISTS public.live_wallpapers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(150) NOT NULL,
    slug VARCHAR(180) NOT NULL,
    description TEXT,
    category_id UUID NOT NULL REFERENCES public.categories(id) ON DELETE RESTRICT,
    tags TEXT[] NOT NULL DEFAULT '{}',
    
    -- Video Asset URLs
    thumbnail_url TEXT NOT NULL,
    preview_video_url TEXT NOT NULL,
    original_video_url TEXT NOT NULL,
    
    -- Technical Specifications
    width INT NOT NULL,
    height INT NOT NULL,
    duration_seconds NUMERIC(5,2) NOT NULL,
    frame_rate NUMERIC(4,1) NOT NULL DEFAULT 30.0,
    has_audio BOOLEAN NOT NULL DEFAULT false,
    codec VARCHAR(30) NOT NULL DEFAULT 'video/mp4',
    file_size_bytes BIGINT NOT NULL,
    sha256_hash VARCHAR(64) UNIQUE NOT NULL,
    is_seamless_loop BOOLEAN NOT NULL DEFAULT true,
    
    -- Legal & Licensing
    license_type VARCHAR(50) NOT NULL DEFAULT 'Standard Royalty-Free',
    author_name VARCHAR(100) DEFAULT 'Aura Motion Studio',
    source_attribution TEXT,
    
    -- Metrics & Status
    download_count BIGINT NOT NULL DEFAULT 0,
    favorite_count BIGINT NOT NULL DEFAULT 0,
    view_count BIGINT NOT NULL DEFAULT 0,
    is_featured BOOLEAN NOT NULL DEFAULT false,
    is_premium BOOLEAN NOT NULL DEFAULT false,
    is_published BOOLEAN NOT NULL DEFAULT true,
    
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 5. Collections
CREATE TABLE IF NOT EXISTS public.collections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(120) NOT NULL,
    slug VARCHAR(140) NOT NULL UNIQUE,
    description TEXT,
    cover_image_url TEXT,
    is_featured BOOLEAN NOT NULL DEFAULT false,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

CREATE TABLE IF NOT EXISTS public.collection_wallpapers (
    collection_id UUID REFERENCES public.collections(id) ON DELETE CASCADE,
    wallpaper_id UUID REFERENCES public.wallpapers(id) ON DELETE CASCADE,
    position INT NOT NULL DEFAULT 0,
    PRIMARY KEY (collection_id, wallpaper_id)
);

-- 6. Content Reports Table (App Store Compliance & User Safety)
CREATE TABLE IF NOT EXISTS public.content_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id UUID NOT NULL,
    asset_type VARCHAR(10) NOT NULL CHECK (asset_type IN ('static', 'live')),
    reason VARCHAR(50) NOT NULL,
    details TEXT,
    reporter_ip_hash VARCHAR(64),
    status VARCHAR(20) NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'reviewed', 'dismissed', 'removed')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 7. High-Performance Indexes
CREATE INDEX IF NOT EXISTS idx_wallpapers_category ON public.wallpapers (category_id) WHERE is_published = true;
CREATE INDEX IF NOT EXISTS idx_wallpapers_created ON public.wallpapers (created_at DESC) WHERE is_published = true;
CREATE INDEX IF NOT EXISTS idx_wallpapers_downloads ON public.wallpapers (download_count DESC) WHERE is_published = true;
CREATE INDEX IF NOT EXISTS idx_wallpapers_featured ON public.wallpapers (is_featured, created_at DESC) WHERE is_published = true;
CREATE INDEX IF NOT EXISTS idx_wallpapers_tags_gin ON public.wallpapers USING gin (tags);
CREATE INDEX IF NOT EXISTS idx_wallpapers_title_trgm ON public.wallpapers USING gin (title gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_live_category ON public.live_wallpapers (category_id) WHERE is_published = true;
CREATE INDEX IF NOT EXISTS idx_live_created ON public.live_wallpapers (created_at DESC) WHERE is_published = true;
CREATE INDEX IF NOT EXISTS idx_live_downloads ON public.live_wallpapers (download_count DESC) WHERE is_published = true;
CREATE INDEX IF NOT EXISTS idx_live_featured ON public.live_wallpapers (is_featured, created_at DESC) WHERE is_published = true;
CREATE INDEX IF NOT EXISTS idx_live_tags_gin ON public.live_wallpapers USING gin (tags);
CREATE INDEX IF NOT EXISTS idx_live_title_trgm ON public.live_wallpapers USING gin (title gin_trgm_ops);

-- 8. Stored Procedure: Increment Download with IP Rate Limiting
CREATE TABLE IF NOT EXISTS public.download_rate_limits (
    ip_hash VARCHAR(64) NOT NULL,
    asset_id UUID NOT NULL,
    last_downloaded_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    PRIMARY KEY (ip_hash, asset_id)
);

CREATE OR REPLACE FUNCTION public.increment_download(
    p_asset_id UUID,
    p_asset_type VARCHAR(10),
    p_ip_hash VARCHAR(64) DEFAULT NULL
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    -- Optional Rate-Limiting window: 10 minutes per unique IP per asset
    IF p_ip_hash IS NOT NULL THEN
        IF EXISTS (
            SELECT 1 FROM public.download_rate_limits
            WHERE ip_hash = p_ip_hash 
              AND asset_id = p_asset_id 
              AND last_downloaded_at > timezone('utc'::text, now()) - INTERVAL '10 minutes'
        ) THEN
            -- Skip increment if too frequent from same IP, but return success to avoid client error
            RETURN;
        END IF;

        INSERT INTO public.download_rate_limits (ip_hash, asset_id, last_downloaded_at)
        VALUES (p_ip_hash, p_asset_id, timezone('utc'::text, now()))
        ON CONFLICT (ip_hash, asset_id) 
        DO UPDATE SET last_downloaded_at = timezone('utc'::text, now());
    END IF;

    -- Atomic Counter Increment
    IF p_asset_type = 'static' THEN
        UPDATE public.wallpapers
        SET download_count = download_count + 1
        WHERE id = p_asset_id;
    ELSIF p_asset_type = 'live' THEN
        UPDATE public.live_wallpapers
        SET download_count = download_count + 1
        WHERE id = p_asset_id;
    END IF;
END;
$$;

-- 9. Category Counter Triggers
CREATE OR REPLACE FUNCTION public.update_category_counts()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
        UPDATE public.categories
        SET wallpaper_count = (
            SELECT COUNT(*) FROM public.wallpapers WHERE category_id = NEW.category_id AND is_published = true
        ) + (
            SELECT COUNT(*) FROM public.live_wallpapers WHERE category_id = NEW.category_id AND is_published = true
        )
        WHERE id = NEW.category_id;
    END IF;

    IF TG_OP = 'DELETE' THEN
        UPDATE public.categories
        SET wallpaper_count = (
            SELECT COUNT(*) FROM public.wallpapers WHERE category_id = OLD.category_id AND is_published = true
        ) + (
            SELECT COUNT(*) FROM public.live_wallpapers WHERE category_id = OLD.category_id AND is_published = true
        )
        WHERE id = OLD.category_id;
    END IF;
    RETURN NULL;
END;
$$;

CREATE TRIGGER trg_wallpaper_category_count
AFTER INSERT OR UPDATE OR DELETE ON public.wallpapers
FOR EACH ROW EXECUTE FUNCTION public.update_category_counts();

CREATE TRIGGER trg_live_wallpaper_category_count
AFTER INSERT OR UPDATE OR DELETE ON public.live_wallpapers
FOR EACH ROW EXECUTE FUNCTION public.update_category_counts();

-- 10. Unified Search Stored Procedure
CREATE OR REPLACE FUNCTION public.search_catalog(
    search_query TEXT,
    content_type TEXT DEFAULT 'all', -- 'all', 'static', 'live'
    limit_count INT DEFAULT 30,
    offset_count INT DEFAULT 0
)
RETURNS TABLE (
    id UUID,
    title VARCHAR,
    slug VARCHAR,
    thumbnail_url TEXT,
    preview_url TEXT,
    original_url TEXT,
    category_name VARCHAR,
    is_live BOOLEAN,
    download_count BIGINT,
    created_at TIMESTAMPTZ
)
LANGUAGE sql
STABLE
AS $$
    WITH static_results AS (
        SELECT 
            w.id,
            w.title,
            w.slug,
            w.thumbnail_url,
            w.preview_url,
            w.original_url,
            c.name AS category_name,
            false AS is_live,
            w.download_count,
            w.created_at
        FROM public.wallpapers w
        JOIN public.categories c ON w.category_id = c.id
        WHERE w.is_published = true
          AND (content_type = 'all' OR content_type = 'static')
          AND (
              w.title ILIKE '%' || search_query || '%'
              OR search_query = ANY(w.tags)
              OR c.name ILIKE '%' || search_query || '%'
          )
    ),
    live_results AS (
        SELECT 
            lw.id,
            lw.title,
            lw.slug,
            lw.thumbnail_url,
            lw.preview_video_url AS preview_url,
            lw.original_video_url AS original_url,
            c.name AS category_name,
            true AS is_live,
            lw.download_count,
            lw.created_at
        FROM public.live_wallpapers lw
        JOIN public.categories c ON lw.category_id = c.id
        WHERE lw.is_published = true
          AND (content_type = 'all' OR content_type = 'live')
          AND (
              lw.title ILIKE '%' || search_query || '%'
              OR search_query = ANY(lw.tags)
              OR c.name ILIKE '%' || search_query || '%'
          )
    )
    SELECT * FROM static_results
    UNION ALL
    SELECT * FROM live_results
    ORDER BY download_count DESC, created_at DESC
    LIMIT limit_count
    OFFSET offset_count;
$$;

-- 11. Row Level Security (RLS) Declarative Policies
ALTER TABLE public.categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.wallpapers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.live_wallpapers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.collections ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.collection_wallpapers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.content_reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.download_rate_limits ENABLE ROW LEVEL SECURITY;

-- Anonymous users (mobile app) can only READ published records
CREATE POLICY "Public Read Categories" ON public.categories
    FOR SELECT USING (true);

CREATE POLICY "Public Read Published Wallpapers" ON public.wallpapers
    FOR SELECT USING (is_published = true);

CREATE POLICY "Public Read Published Live Wallpapers" ON public.live_wallpapers
    FOR SELECT USING (is_published = true);

CREATE POLICY "Public Read Collections" ON public.collections
    FOR SELECT USING (true);

CREATE POLICY "Public Read Collection Items" ON public.collection_wallpapers
    FOR SELECT USING (true);

-- Anonymous users can submit content reports
CREATE POLICY "Public Insert Reports" ON public.content_reports
    FOR INSERT WITH CHECK (true);

-- Authenticated admins have full read/write privileges
CREATE POLICY "Admin Full Access Categories" ON public.categories
    FOR ALL TO authenticated USING (auth.jwt() ->> 'role' = 'admin')
    WITH CHECK (auth.jwt() ->> 'role' = 'admin');

CREATE POLICY "Admin Full Access Wallpapers" ON public.wallpapers
    FOR ALL TO authenticated USING (auth.jwt() ->> 'role' = 'admin')
    WITH CHECK (auth.jwt() ->> 'role' = 'admin');

CREATE POLICY "Admin Full Access Live Wallpapers" ON public.live_wallpapers
    FOR ALL TO authenticated USING (auth.jwt() ->> 'role' = 'admin')
    WITH CHECK (auth.jwt() ->> 'role' = 'admin');

CREATE POLICY "Admin Full Access Collections" ON public.collections
    FOR ALL TO authenticated USING (auth.jwt() ->> 'role' = 'admin')
    WITH CHECK (auth.jwt() ->> 'role' = 'admin');

CREATE POLICY "Admin Full Access Reports" ON public.content_reports
    FOR ALL TO authenticated USING (auth.jwt() ->> 'role' = 'admin')
    WITH CHECK (auth.jwt() ->> 'role' = 'admin');
