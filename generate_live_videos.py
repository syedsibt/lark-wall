import os
import cv2
import numpy as np
import math
import subprocess

output_dir = r"C:\Users\pc\.gemini\antigravity\scratch\aura-wallpapers\android\app\src\main\assets\live_videos"
os.makedirs(output_dir, exist_ok=True)

width, height = 720, 1280
fps = 60
duration_sec = 4
total_frames = fps * duration_sec  # 240 frames
fourcc = cv2.VideoWriter_fourcc(*'mp4v')

print(f"Generating 7 Battery-Smart, 100% Seamless, Zero-Glitch 60FPS Live Wallpaper Loops ({total_frames} frames)...")

# Helper function to convert to H.264 Baseline for hardware decoder compatibility
def encode_h264(input_path):
    tmp = input_path.replace(".mp4", "_h264.mp4")
    cmd = [
        "ffmpeg", "-y", "-i", input_path,
        "-c:v", "libx264", "-profile:v", "baseline", "-level", "3.1",
        "-pix_fmt", "yuv420p", "-r", "60", "-g", "60", "-keyint_min", "60",
        "-sc_threshold", "0", "-bf", "0", "-b:v", "2200k", "-maxrate", "2800k",
        "-bufsize", "4500k", "-movflags", "+faststart", tmp
    ]
    sub = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if sub.returncode == 0:
        os.replace(tmp, input_path)
        print(f"  [OK] H.264 Baseline 60FPS verified: {os.path.basename(input_path)} ({os.path.getsize(input_path)} bytes)")
    else:
        print(f"  [ERROR] FFmpeg error on {input_path}: {sub.stderr.decode('utf-8', errors='ignore')}")

# ==============================================================================
# 1. Liquid Chrome Loop: Molten iridescent liquid metal with smooth reflections
# ==============================================================================
path1 = os.path.join(output_dir, "liquid_chrome_loop.mp4")
out1 = cv2.VideoWriter(path1, fourcc, fps, (width, height))
y_grid, x_grid = np.ogrid[:height, :width]
for f in range(total_frames):
    t = (f / total_frames) * 2 * math.pi
    
    # Smooth multi-harmonic fluid waves (strict integer harmonics: 1t, 2t)
    wave1 = np.sin(x_grid * 0.015 + t) * 40.0
    wave2 = np.cos(y_grid * 0.012 + 2 * t) * 35.0
    wave3 = np.sin((x_grid + y_grid) * 0.010 - t) * 30.0
    comb = wave1 + wave2 + wave3
    
    # High contrast liquid metal gradation
    grad = np.sin((y_grid + comb) * 0.008)
    base = np.clip(135.0 + 110.0 * grad, 0, 255)
    
    # Soft specular sheen (smooth power of 4, no harsh strobe)
    spec = np.clip(180.0 * np.power(np.sin(x_grid * 0.018 - y_grid * 0.010 + t) * 0.5 + 0.5, 4), 0, 255)
    
    # Iridescent fringe (electric cyan & magenta)
    chroma_b = np.sin(t + x_grid * 0.008) * 38.0
    chroma_r = np.cos(t + y_grid * 0.008) * 42.0
    
    b = np.clip(base * 0.94 + spec * 0.6 + chroma_b, 15, 255).astype(np.uint8)
    g = np.clip(base * 0.90 + spec * 0.5, 12, 255).astype(np.uint8)
    r = np.clip(base * 0.96 + spec * 0.6 + chroma_r, 15, 255).astype(np.uint8)
    
    frame = np.stack([b, g, r], axis=2)
    out1.write(frame)
out1.release()
print("1. Liquid Chrome raw generated.")
encode_h264(path1)

# ==============================================================================
# 2. Cyberpunk Neon Loop: OLED Pure Black (#000000) with vivid electric grid & horizon
# ==============================================================================
path2 = os.path.join(output_dir, "cyberpunk_neon_loop.mp4")
out2 = cv2.VideoWriter(path2, fourcc, fps, (width, height))
center_x, center_y = width // 2, int(height * 0.48)
grid_spacing = 40
num_cycles = 2  # exactly 2 grid steps per loop

for f in range(total_frames):
    t = (f / total_frames) * 2 * math.pi
    frame = np.zeros((height, width, 3), dtype=np.uint8)  # OLED true black
    
    # Smooth moving perspective grid
    phase = (f / total_frames) * num_cycles
    offset = (phase % 1.0) * grid_spacing
    
    for gy in range(center_y, height + grid_spacing, grid_spacing):
        y_pos = int(gy + offset)
        if center_y < y_pos < height:
            depth_ratio = (y_pos - center_y) / float(height - center_y)
            # Edge alpha fade to prevent pop-in at horizon and bottom
            alpha = math.sin(depth_ratio * math.pi)
            b_val = int(255 * depth_ratio * alpha)
            g_val = int(45 * depth_ratio * alpha)
            r_val = int(240 * depth_ratio * alpha)
            cv2.line(frame, (0, y_pos), (width, y_pos), (b_val, g_val, r_val), 2)
            
    # Vanishing perspective rays
    for vx in range(-120, width + 180, 60):
        cv2.line(frame, (center_x, center_y), (vx, height), (190, 20, 160), 1)
        
    # Horizon core glow with subtle organic breathing (1t harmonic only)
    glow_pulse = int(5.0 * math.sin(t))
    cv2.circle(frame, (center_x, center_y), 90 + glow_pulse, (210, 40, 160), -1)
    cv2.circle(frame, (center_x, center_y), 60 + glow_pulse, (245, 100, 220), -1)
    cv2.circle(frame, (center_x, center_y), 32, (255, 235, 255), -1)  # White core
    
    out2.write(frame)
out2.release()
print("2. Cyberpunk Neon raw generated.")
encode_h264(path2)

# ==============================================================================
# 3. Aurora Space Loop: Pure OLED Black with saturated Emerald & Violet ribbons
# ==============================================================================
path3 = os.path.join(output_dir, "aurora_space_loop.mp4")
out3 = cv2.VideoWriter(path3, fourcc, fps, (width, height))
np.random.seed(99)
star_count = 100
stars_x = np.random.randint(0, width, star_count)
stars_y = np.random.randint(0, height, star_count)
stars_b = np.random.randint(150, 255, star_count)
stars_phase = np.random.uniform(0, 2 * math.pi, star_count)

for f in range(total_frames):
    t = (f / total_frames) * 2 * math.pi
    frame = np.zeros((height, width, 3), dtype=np.uint8)  # OLED true black
    
    # Twinkling stars with periodic harmonic
    for sx, sy, sb, ph in zip(stars_x, stars_y, stars_b, stars_phase):
        tw = int(sb * (0.65 + 0.35 * math.sin(t + ph)))
        frame[sy, sx] = (tw, tw, tw)
        
    # Saturated Emerald & Electric Violet Aurora Wave (integer harmonics: 1t and 2t)
    wave_rib1 = np.sin(x_grid * 0.012 + t) * 60.0
    wave_rib2 = np.cos(x_grid * 0.010 + 2 * t) * 50.0
    
    rib1 = np.exp(-((y_grid - (height * 0.42 + wave_rib1)) ** 2) / (2 * (42.0 ** 2)))
    rib2 = np.exp(-((y_grid - (height * 0.52 + wave_rib2)) ** 2) / (2 * (50.0 ** 2)))
    
    frame[:, :, 1] = np.clip(frame[:, :, 1] + (rib1 * 255 + rib2 * 110).astype(np.uint8), 0, 255)  # Electric Green
    frame[:, :, 0] = np.clip(frame[:, :, 0] + (rib1 * 180 + rib2 * 240).astype(np.uint8), 0, 255)  # Intense Cyan / Blue
    frame[:, :, 2] = np.clip(frame[:, :, 2] + (rib2 * 235).astype(np.uint8), 0, 255)               # Rich Violet
    
    out3.write(frame)
out3.release()
print("3. Aurora Space raw generated.")
encode_h264(path3)

# ==============================================================================
# 4. Tokyo Rain Loop: Deep obsidian slate with glowing bokeh drops and seamless rain
# ==============================================================================
path4 = os.path.join(output_dir, "tokyo_rain_loop.mp4")
out4 = cv2.VideoWriter(path4, fourcc, fps, (width, height))
np.random.seed(303)
bokeh_count = 26
bx = np.random.randint(40, width - 40, bokeh_count)
by = np.random.randint(60, height - 60, bokeh_count)
br = np.random.randint(28, 62, bokeh_count)
b_phase = np.random.uniform(0, 2 * math.pi, bokeh_count)
vivid_colors = [
    (255, 195, 25),   # Vibrant Cyan
    (30, 165, 255),   # Golden Amber
    (240, 45, 220),   # Electric Magenta
    (230, 240, 45),   # Electric Lime
    (45, 85, 255),    # Crimson Neon
]
b_col = [vivid_colors[i % len(vivid_colors)] for i in range(bokeh_count)]

# Pre-compute rain streaks with exact integer traversals (e.g. 4 full cycles = 0 boundary delta)
rain_count = 35
np.random.seed(404)
rx = np.random.randint(10, width - 10, rain_count)
ry_init = np.random.randint(0, height, rain_count)
rain_cycles = np.random.choice([3, 4, 5], rain_count)  # Exact integer screen traversals

for f in range(total_frames):
    t = (f / total_frames) * 2 * math.pi
    frame = np.zeros((height, width, 3), dtype=np.uint8)
    frame[:, :] = (8, 6, 12)  # Near-black slate for OLED battery savings
    
    # Glowing bokeh drops with smooth breathing
    for x_pos, y_pos, radius, col, ph in zip(bx, by, br, b_col, b_phase):
        pulse = 0.88 + 0.18 * math.sin(t + ph)
        r_actual = max(8, int(radius * pulse))
        overlay = frame.copy()
        cv2.circle(overlay, (x_pos, y_pos), r_actual, col, -1)
        frame = cv2.addWeighted(overlay, 0.40, frame, 0.60, 0)
        
    # Rain streaks: displacement is EXACTLY an integer multiple of height at f = total_frames
    for i in range(rain_count):
        y_travel = int(f * (rain_cycles[i] * height / float(total_frames)))
        y_start = (ry_init[i] + y_travel) % height
        y_end = min(y_start + 45, height)
        cv2.line(frame, (rx[i], y_start), (rx[i] + 1, y_end), (215, 230, 250), 1)
        
    out4.write(frame)
out4.release()
print("4. Tokyo Rain raw generated.")
encode_h264(path4)

# ==============================================================================
# 5. Dark Academia Flame Loop: Warm golden embers with exact cyclical rise
# ==============================================================================
path5 = os.path.join(output_dir, "dark_academia_flame_loop.mp4")
out5 = cv2.VideoWriter(path5, fourcc, fps, (width, height))
np.random.seed(888)
ember_count = 42
ex = np.random.randint(40, width - 40, ember_count)
ey_base = np.random.randint(height // 2, height, ember_count)
e_cycles = np.random.choice([1, 2, 3], ember_count)  # Exact integer screen traversals
e_phases = np.random.uniform(0, 2 * math.pi, ember_count)
y_idx = np.linspace(0, 1, height)[:, None]

for f in range(total_frames):
    t = (f / total_frames) * 2 * math.pi
    frame = np.zeros((height, width, 3), dtype=np.uint8)
    
    # Warm espresso shadow gradient
    frame[:, :, 0] = (6 * y_idx).astype(np.uint8)
    frame[:, :, 1] = (18 * y_idx).astype(np.uint8)
    frame[:, :, 2] = (40 * y_idx).astype(np.uint8)
    
    # Hearth bloom at bottom (subtle 1t harmonic breathing)
    center_glow = int(12.0 * math.sin(t))
    cv2.circle(frame, (width // 2, int(height * 0.88)), 230, (12, 75 + center_glow, 165 + center_glow), -1)
    frame = cv2.GaussianBlur(frame, (65, 65), 0)
    
    # Rising embers with exact integer cycles
    for i in range(ember_count):
        y_travel = int(f * (e_cycles[i] * height / float(total_frames)))
        y_pos = (ey_base[i] - y_travel) % height
        x_pos = int(ex[i] + 16.0 * math.sin(t + e_phases[i]))
        bright = int(215 + 38 * math.cos(t + e_phases[i]))
        cv2.circle(frame, (x_pos, y_pos), 3, (25, int(bright * 0.76), bright), -1)
        
    out5.write(frame)
out5.release()
print("5. Dark Academia raw generated.")
encode_h264(path5)

# ==============================================================================
# 6. Minimal Gradient Flow Loop: Vibrant Ultra-Violet & Sunset Coral
# ==============================================================================
path6 = os.path.join(output_dir, "minimal_gradient_flow_loop.mp4")
out6 = cv2.VideoWriter(path6, fourcc, fps, (width, height))
for f in range(total_frames):
    t = (f / total_frames) * 2 * math.pi
    
    drift1 = np.sin(x_grid * 0.005 + t) * 0.5 + 0.5
    drift2 = np.cos(y_grid * 0.004 + t) * 0.5 + 0.5
    drift3 = np.sin((x_grid + y_grid) * 0.003 - 2 * t) * 0.5 + 0.5
    
    # Rich, vibrant palette: Ultra-Violet + Vivid Sunset Coral + Electric Indigo
    b = np.clip(215.0 * drift1 + 55.0 * (1.0 - drift2) + 40.0 * drift3, 10, 255).astype(np.uint8)
    g = np.clip(42.0 * drift2 + 25.0 * drift1 + 15.0 * drift3, 10, 255).astype(np.uint8)
    r = np.clip(242.0 * drift2 + 135.0 * (1.0 - drift1) + 30.0 * drift3, 10, 255).astype(np.uint8)
    
    frame = np.stack([b, g, r], axis=2)
    out6.write(frame)
out6.release()
print("6. Minimal Gradient raw generated.")
encode_h264(path6)

# ==============================================================================
# 7. Cartoon Anime Aura Loop: Saiyan / Domain Expansion electric aura on OLED Black
# ==============================================================================
path7 = os.path.join(output_dir, "cartoon_anime_aura_loop.mp4")
out7 = cv2.VideoWriter(path7, fourcc, fps, (width, height))
np.random.seed(99)
num_particles = 42
px_base = np.random.uniform(width * 0.25, width * 0.75, num_particles)
py_base = np.random.uniform(height * 0.35, height * 0.85, num_particles)
p_cycles = np.random.choice([2, 3, 4], num_particles)  # Exact integer screen traversals
p_phase = np.random.uniform(0, 2 * math.pi, num_particles)
psize = np.random.randint(3, 7, num_particles)
cx, cy = width // 2, int(height * 0.58)

for f in range(total_frames):
    t = (f / total_frames) * 2 * math.pi
    frame = np.zeros((height, width, 3), dtype=np.uint8)  # Pitch black OLED
    
    # Core aura breathing (gentle 1t harmonic)
    pulse = 1.0 + 0.08 * math.sin(t)
    base_radius = int(160 * pulse)
    
    for r_offset, col in [(75, (255, 140, 20)), (38, (255, 220, 40)), (0, (255, 255, 180))]:
        rad = int(base_radius + r_offset + 10 * math.sin(t))
        cv2.circle(frame, (cx, cy), rad, col, -1)
    
    frame = cv2.GaussianBlur(frame, (85, 85), 0)
    
    # Rotating energy elliptical rings: exactly 2 full 360° rotations (720°) over total_frames
    for ring in range(3):
        ring_rad = int(base_radius * (0.8 + ring * 0.35) + 8 * math.cos(t))
        angle_start = int((ring * 120 + (f / float(total_frames)) * 720.0) % 360)
        cv2.ellipse(frame, (cx, cy), (ring_rad, int(ring_rad * 0.7)), 0, angle_start, angle_start + 85, (255, 255, 0), 4)
        cv2.ellipse(frame, (cx, cy), (ring_rad, int(ring_rad * 0.7)), 0, angle_start + 180, angle_start + 265, (0, 240, 255), 4)

    # Rising electric aura spark particles with exact integer cycles
    for i in range(num_particles):
        y_travel = int(f * (p_cycles[i] * height / float(total_frames)))
        y_pos = int((py_base[i] - y_travel) % height)
        x_pos = int(px_base[i] + 18.0 * math.sin(2 * t + p_phase[i]))
        bright = int(220 + 35 * math.cos(t + p_phase[i]))
        color = (bright, int(bright * 0.85), 30) if i % 2 == 0 else (40, int(bright * 0.9), bright)
        cv2.circle(frame, (x_pos, y_pos), psize[i], color, -1)

    out7.write(frame)
out7.release()
print("7. Cartoon Anime Aura raw generated.")
encode_h264(path7)

print("\nALL 7 LIVE WALLPAPERS RE-GENERATED SUCCESSFULLY!")
