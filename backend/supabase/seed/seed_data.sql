-- ============================================================================
-- AURA WALLPAPERS: Comprehensive Seed Data
-- ============================================================================

-- 1. Insert Standard & Live Categories
INSERT INTO public.categories (id, name, slug, description, icon_name, is_live, display_order) VALUES
('c0000001-0000-0000-0000-000000000001', 'AMOLED', 'amoled', 'True black wallpapers optimized for OLED displays and maximum battery savings.', 'battery_saver', false, 1),
('c0000001-0000-0000-0000-000000000002', 'Minimal', 'minimal', 'Clean lines, subtle textures, and uncluttered negative space.', 'filter_drama', false, 2),
('c0000001-0000-0000-0000-000000000003', 'Mountains', 'mountains', 'Towering snow-capped peaks, rugged ridges, and alpine vistas.', 'terrain', false, 3),
('c0000001-0000-0000-0000-000000000004', 'Space', 'space', 'Deep cosmos, nebulas, stellar galaxies, and astronomical bodies.', 'public', false, 4),
('c0000001-0000-0000-0000-000000000005', 'Nature', 'nature', 'Lush forests, tranquil waterways, and serene flora.', 'nature', false, 5),
('c0000001-0000-0000-0000-000000000006', 'Abstract', 'abstract', 'Vibrant geometric compositions, generative forms, and 3D fractals.', 'palette', false, 6),
('c0000001-0000-0000-0000-000000000007', 'Dark', 'dark', 'Moody cinematic atmospheres, low-key lighting, and shadow aesthetics.', 'dark_mode', false, 7),
('c0000001-0000-0000-0000-000000000008', 'Architecture', 'architecture', 'Modernist monoliths, brutalist concrete, and sleek glass facades.', 'apartment', false, 8),
('c0000001-0000-0000-0000-000000000009', 'Ocean', 'ocean', 'Deep sea swells, turquoise coastlines, and abyssal currents.', 'water', false, 9),
('c0000001-0000-0000-0000-000000000010', 'Cars', 'cars', 'Exotic supercars, timeless vintage classics, and motorsport machines.', 'directions_car', false, 10),
('c0000001-0000-0000-0000-000000000011', 'Bikes', 'bikes', 'High-performance motorcycles, cafe racers, and urban cruisers.', 'two_wheeler', false, 11),
('c0000001-0000-0000-0000-000000000012', 'Technology', 'technology', 'Circuit traces, server cores, cybernetic nodes, and silicon precision.', 'memory', false, 12),
('c0000001-0000-0000-0000-000000000013', 'Gaming', 'gaming', 'Cyberpunk environments, retro arcade neon, and gaming battlegrounds.', 'sports_esports', false, 13),
('c0000001-0000-0000-0000-000000000014', 'Animals', 'animals', 'Majestic wildlife, apex predators, and macro animal photography.', 'pets', false, 14),
('c0000001-0000-0000-0000-000000000015', 'Flowers', 'flowers', 'Botanical macro shots, exquisite petals, and blooming colors.', 'local_florist', false, 15),
('c0000001-0000-0000-0000-000000000016', '3D Art', '3d-art', 'Rendered surrealism, liquid metallics, and procedural geometries.', 'view_in_ar', false, 16),
('c0000001-0000-0000-0000-000000000017', 'Fantasy', 'fantasy', 'Mythical realms, ancient temples, and ethereal landscapes.', 'auto_awesome', false, 17),
('c0000001-0000-0000-0000-000000000018', 'City', 'city', 'Metropolitan skylines, twilight streetscapes, and neon-lit avenues.', 'location_city', false, 18),
('c0000001-0000-0000-0000-000000000019', 'Luxury', 'luxury', 'Champagne gold accents, refined leather textures, and polished marble.', 'diamond', false, 19),
('c0000001-0000-0000-0000-000000000020', 'Aesthetic', 'aesthetic', 'Pastel vaporwave, golden hour warmth, and nostalgic tones.', 'style', false, 20),
('c0000001-0000-0000-0000-000000000021', 'Gradient', 'gradient', 'Silky smooth color blends, dynamic spectrums, and duotones.', 'gradient', false, 21),
('c0000001-0000-0000-0000-000000000022', 'Patterns', 'patterns', 'Tessellated motifs, architectural grids, and organic repetition.', 'texture', false, 22),
('c0000001-0000-0000-0000-000000000023', 'Seasonal', 'seasonal', 'Autumn foliage, winter frost, spring blossoms, and summer sun.', 'wb_sunny', false, 23),
('c0000001-0000-0000-0000-000000000024', 'Live Nature', 'live-nature', 'Subtle swaying foliage, cascading waterfalls, and rolling mist.', 'play_circle', true, 24),
('c0000001-0000-0000-0000-000000000025', 'Live Space', 'live-space', 'Slowly orbiting planets, pulsating pulsars, and drifting stars.', 'play_circle', true, 25),
('c0000001-0000-0000-0000-000000000026', 'Live Abstract', 'live-abstract', 'Fluid liquid simulations, shifting chromatic ribbons, and particle swarms.', 'play_circle', true, 26),
('c0000001-0000-0000-0000-000000000027', 'Live Motion', 'live-motion', 'Kinetic typography, sleek cybernetic lines, and ambient light loops.', 'play_circle', true, 27)
ON CONFLICT (id) DO NOTHING;

-- 2. Insert Curated Static Wallpapers
INSERT INTO public.wallpapers (
    id, title, slug, description, category_id, tags,
    thumbnail_url, preview_url, original_url,
    width, height, aspect_ratio, file_size_bytes, format,
    sha256_hash, download_count, favorite_count, view_count, is_featured, is_premium, is_published
) VALUES
(
    'w0000001-0000-0000-0000-000000000001',
    'Obsidian Ridge',
    'obsidian-ridge',
    'Pitch-black monolithic volcanic ridge silhouetted against an illuminated starfield.',
    'c0000001-0000-0000-0000-000000000001', -- AMOLED
    ARRAY['amoled', 'mountains', 'dark', 'stars', 'oled', 'minimal'],
    'https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200&auto=format&fit=crop&q=85',
    'https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&q=95',
    2160, 3840, '9:16', 2415000, 'image/webp',
    'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855',
    34200, 12890, 89200, true, false, true
),
(
    'w0000001-0000-0000-0000-000000000002',
    'Alabaster Curves',
    'alabaster-curves',
    'Ultra-minimal architectural sweep of pure white concrete against neutral shadows.',
    'c0000001-0000-0000-0000-000000000002', -- Minimal
    ARRAY['minimal', 'architecture', 'clean', 'white', 'modern'],
    'https://images.unsplash.com/photo-1513694203232-719a280e022f?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1513694203232-719a280e022f?w=1200&auto=format&fit=crop&q=85',
    'https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&q=95',
    2160, 3840, '9:16', 1650000, 'image/webp',
    'a4b5c6d7e8f90123456789abcdef0123456789abcdef0123456789abcdef0123',
    28400, 9420, 71500, true, false, true
),
(
    'w0000001-0000-0000-0000-000000000003',
    'Cerulean Abyss',
    'cerulean-abyss',
    'Deep marine oceanic trench with gentle refraction rays penetrating the deep blue.',
    'c0000001-0000-0000-0000-000000000009', -- Ocean
    ARRAY['ocean', 'deep', 'water', 'blue', 'minimal', 'calm'],
    'https://images.unsplash.com/photo-1518837695005-2083093ee35b?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1518837695005-2083093ee35b?w=1200&auto=format&fit=crop&q=85',
    'https://images.unsplash.com/photo-1518837695005-2083093ee35b?auto=format&fit=crop&q=95',
    2160, 3840, '9:16', 2890000, 'image/webp',
    'b1c2d3e4f5a60123456789abcdef0123456789abcdef0123456789abcdef0123',
    19800, 7810, 52000, false, false, true
),
(
    'w0000001-0000-0000-0000-000000000004',
    'Hyperion Pillars',
    'hyperion-pillars',
    'High altitude redwood canopy shrouded in morning golden atmospheric fog.',
    'c0000001-0000-0000-0000-000000000005', -- Nature
    ARRAY['nature', 'forest', 'trees', 'mist', 'fog', 'morning', 'tranquil'],
    'https://images.unsplash.com/photo-1448375240586-882707db888b?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1448375240586-882707db888b?w=1200&auto=format&fit=crop&q=85',
    'https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&q=95',
    2160, 3840, '9:16', 3120000, 'image/webp',
    'c2d3e4f5a6b70123456789abcdef0123456789abcdef0123456789abcdef0123',
    41200, 16300, 99100, true, false, true
),
(
    'w0000001-0000-0000-0000-000000000005',
    'Monolith Neon',
    'monolith-neon',
    'Cybernetic dusk cityscape with subtle purple and crimson vertical accents.',
    'c0000001-0000-0000-0000-000000000018', -- City
    ARRAY['city', 'cyberpunk', 'neon', 'dark', 'urban', 'night'],
    'https://images.unsplash.com/photo-1519501025264-65ba15a82390?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1519501025264-65ba15a82390?w=1200&auto=format&fit=crop&q=85',
    'https://images.unsplash.com/photo-1519501025264-65ba15a82390?auto=format&fit=crop&q=95',
    2160, 3840, '9:16', 3450000, 'image/webp',
    'd3e4f5a6b7c80123456789abcdef0123456789abcdef0123456789abcdef0123',
    22400, 8910, 64000, false, false, true
),
(
    'w0000001-0000-0000-0000-000000000006',
    'Dolomite Zenith',
    'dolomite-zenith',
    'Sharp alpine limestone crest bathed in dramatic first light alpine glow.',
    'c0000001-0000-0000-0000-000000000003', -- Mountains
    ARRAY['mountains', 'alps', 'snow', 'sunrise', 'landscape', 'dramatic'],
    'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=1200&auto=format&fit=crop&q=85',
    'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&q=95',
    2160, 3840, '9:16', 2760000, 'image/webp',
    'e4f5a6b7c8d90123456789abcdef0123456789abcdef0123456789abcdef0123',
    31800, 11450, 78900, true, false, true
)
ON CONFLICT (id) DO NOTHING;

-- 3. Insert Curated Seamless Live Wallpapers
INSERT INTO public.live_wallpapers (
    id, title, slug, description, category_id, tags,
    thumbnail_url, preview_video_url, original_video_url,
    width, height, duration_seconds, frame_rate, has_audio, codec,
    file_size_bytes, sha256_hash, is_seamless_loop, download_count, favorite_count, view_count, is_featured, is_premium, is_published
) VALUES
(
    'l0000001-0000-0000-0000-000000000001',
    'Cosmic Aurora Waves',
    'cosmic-aurora-waves',
    'Hypnotic emerald and indigo ribbons of polar luminescence gently rippling across the night sky.',
    'c0000001-0000-0000-0000-000000000025', -- Live Space
    ARRAY['live', 'space', 'aurora', 'green', 'loop', 'calm'],
    'https://images.unsplash.com/photo-1531366936337-7c912a4589a7?w=600&auto=format&fit=crop&q=80',
    'https://assets.mixkit.co/videos/preview/mixkit-stars-in-space-1610-large.mp4',
    'https://assets.mixkit.co/videos/download/mixkit-stars-in-space-1610.mp4',
    1080, 1920, 12.0, 30.0, false, 'video/mp4',
    8940000, 'f1a2b3c4d5e60123456789abcdef0123456789abcdef0123456789abcdef0123',
    true, 48200, 19400, 115000, true, false, true
),
(
    'l0000001-0000-0000-0000-000000000002',
    'Liquid Chrome Flow',
    'liquid-chrome-flow',
    'Ultra-smooth reflective metallic fluid slowly rolling across seamless topological curves.',
    'c0000001-0000-0000-0000-000000000026', -- Live Abstract
    ARRAY['live', 'abstract', 'metallic', 'silver', 'smooth', '3d'],
    'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80',
    'https://assets.mixkit.co/videos/preview/mixkit-flowing-water-surface-3171-large.mp4',
    'https://assets.mixkit.co/videos/download/mixkit-flowing-water-surface-3171.mp4',
    1080, 1920, 10.0, 30.0, false, 'video/mp4',
    7230000, 'f2a2b3c4d5e60123456789abcdef0123456789abcdef0123456789abcdef0123',
    true, 36100, 14200, 89000, true, false, true
),
(
    'l0000001-0000-0000-0000-000000000003',
    'Ethereal Redwood Mist',
    'ethereal-redwood-mist',
    'Slow rolling mountain fog drifting softly through towering ancient sequoia trees.',
    'c0000001-0000-0000-0000-000000000024', -- Live Nature
    ARRAY['live', 'nature', 'forest', 'fog', 'relaxing', 'cinematic'],
    'https://images.unsplash.com/photo-1473448912268-2022ce9509d8?w=600&auto=format&fit=crop&q=80',
    'https://assets.mixkit.co/videos/preview/mixkit-forest-stream-in-the-sunlight-529-large.mp4',
    'https://assets.mixkit.co/videos/download/mixkit-forest-stream-in-the-sunlight-529.mp4',
    1080, 1920, 14.0, 30.0, false, 'video/mp4',
    9810000, 'f3a2b3c4d5e60123456789abcdef0123456789abcdef0123456789abcdef0123',
    true, 29700, 11800, 74200, false, false, true
)
ON CONFLICT (id) DO NOTHING;
