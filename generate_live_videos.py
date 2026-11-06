import os
import cv2
import numpy as np
import math

output_dir = r"C:\Users\pc\.gemini\antigravity\scratch\aura-wallpapers\android\app\src\main\assets\live_videos"
os.makedirs(output_dir, exist_ok=True)

width, height = 720, 1280
fps = 30
duration_sec = 4
total_frames = fps * duration_sec
fourcc = cv2.VideoWriter_fourcc(*'mp4v')

print("Generating 6 Battery-Smart, High-Contrast, Color-Enhanced Live Wallpaper Loops...")

# 1. Liquid Chrome Loop: High-contrast molten chrome with iridescent rainbow sheen & deep shadows
path1 = os.path.join(output_dir, "liquid_chrome_loop.mp4")
out1 = cv2.VideoWriter(path1, fourcc, fps, (width, height))
for f in range(total_frames):
    t = (f / total_frames) * 2 * math.pi
    y, x = np.ogrid[:height, :width]
    
    # Fluid interference
    wave1 = np.sin(x * 0.016 + t) * 45
    wave2 = np.cos(y * 0.014 + t * 2) * 40
    wave3 = np.sin((x + y) * 0.012 + t * 1.5) * 35
    comb = wave1 + wave2 + wave3
    
    # Deep contrast: Deep dark silver shadows to dazzling specular highlights
    grad = np.sin((y + comb) * 0.009)
    base = np.clip(130 + 115 * grad, 0, 255)
    
    # Specular glint
    spec = np.clip(255 * np.power(np.sin(x * 0.022 - y * 0.012 + t * 2), 12), 0, 255)
    
    # Iridescent color accents (Cyan & Magenta fringe on chrome edges)
    chroma_b = np.sin(t + x * 0.01) * 35
    chroma_r = np.cos(t + y * 0.01) * 45
    
    b = np.clip(base * 0.95 + spec * 0.8 + chroma_b, 10, 255).astype(np.uint8)
    g = np.clip(base * 0.92 + spec * 0.8, 10, 255).astype(np.uint8)
    r = np.clip(base * 0.98 + spec * 0.9 + chroma_r, 10, 255).astype(np.uint8)
    
    frame = np.stack([b, g, r], axis=2)
    out1.write(frame)
out1.release()
print(f"1. Liquid Chrome: {path1}")

# 2. Cyberpunk Neon Loop: True OLED Black (#000000) Canvas (Turns OLED LEDs OFF = Max Battery Savings!) with intense electric neon
path2 = os.path.join(output_dir, "cyberpunk_neon_loop.mp4")
out2 = cv2.VideoWriter(path2, fourcc, fps, (width, height))
for f in range(total_frames):
    t = (f / total_frames) * 2 * math.pi
    frame = np.zeros((height, width, 3), dtype=np.uint8) # Pitch Black OLED base
    
    center_x, center_y = width // 2, int(height * 0.48)
    
    # Moving perspective horizon grid
    grid_offset = int((f / total_frames) * 45)
    for gy in range(center_y, height, 45):
        y_pos = gy + grid_offset
        if y_pos < height:
            depth_ratio = (y_pos - center_y) / (height - center_y)
            # Vivid Electric Cyan to Neon Magenta
            b_val = int(255 * depth_ratio)
            g_val = int(40 * depth_ratio)
            r_val = int(240 * depth_ratio)
            cv2.line(frame, (0, y_pos), (width, y_pos), (b_val, g_val, r_val), 2)
            
    # Vanishing rays
    for vx in range(-120, width + 180, 70):
        cv2.line(frame, (center_x, center_y), (vx, height), (220, 20, 180), 1)
        
    # Vibrant Neon Horizon Core with bloom
    glow_pulse = int(15 * math.sin(t * 3))
    cv2.circle(frame, (center_x, center_y), 95 + glow_pulse, (240, 50, 180), -1)
    cv2.circle(frame, (center_x, center_y), 65 + glow_pulse, (255, 120, 230), -1)
    cv2.circle(frame, (center_x, center_y), 35, (255, 230, 255), -1) # Brilliant white-hot center

    out2.write(frame)
out2.release()
print(f"2. Cyberpunk Neon: {path2}")

# 3. Aurora Space Loop: Pure OLED Black with intensely saturated Emerald & Royal Violet ribbons
path3 = os.path.join(output_dir, "aurora_space_loop.mp4")
out3 = cv2.VideoWriter(path3, fourcc, fps, (width, height))
np.random.seed(99)
stars_x = np.random.randint(0, width, 90)
stars_y = np.random.randint(0, height, 90)
stars_b = np.random.randint(140, 255, 90)

for f in range(total_frames):
    t = (f / total_frames) * 2 * math.pi
    frame = np.zeros((height, width, 3), dtype=np.uint8) # Pitch black space for OLED efficiency
    
    # Twinkling stars
    for sx, sy, sb in zip(stars_x, stars_y, stars_b):
        tw = int(sb * (0.6 + 0.4 * math.sin(t * 3 + sx)))
        frame[sy, sx] = (tw, tw, tw)
        
    # Saturated Emerald & Electric Violet Aurora Wave
    y, x = np.ogrid[:height, :width]
    rib1 = np.exp(-((y - (height * 0.42 + np.sin(x * 0.012 + t) * 65)) ** 2) / (2 * 40 ** 2))
    rib2 = np.exp(-((y - (height * 0.52 + np.cos(x * 0.01 + t * 1.5) * 55)) ** 2) / (2 * 48 ** 2))
    
    frame[:, :, 1] = np.clip(frame[:, :, 1] + (rib1 * 255 + rib2 * 110).astype(np.uint8), 0, 255) # Electric Green
    frame[:, :, 0] = np.clip(frame[:, :, 0] + (rib1 * 180 + rib2 * 240).astype(np.uint8), 0, 255) # Intense Cyan / Blue
    frame[:, :, 2] = np.clip(frame[:, :, 2] + (rib2 * 235).astype(np.uint8), 0, 255) # Rich Violet
    
    out3.write(frame)
out3.release()
print(f"3. Aurora Space: {path3}")

# 4. Tokyo Rain Loop: Deep moody obsidian night with glowing, vivid neon bokeh drops
path4 = os.path.join(output_dir, "tokyo_rain_loop.mp4")
out4 = cv2.VideoWriter(path4, fourcc, fps, (width, height))
np.random.seed(303)
bokeh_count = 28
bx = np.random.randint(40, width - 40, bokeh_count)
by = np.random.randint(60, height - 60, bokeh_count)
br = np.random.randint(28, 65, bokeh_count)
vivid_colors = [
    (255, 190, 20),   # Vibrant Cyan
    (30, 160, 255),   # Intense Golden Amber
    (240, 40, 220),   # Electric Magenta
    (230, 240, 40),   # Electric Lime / Emerald
    (40, 80, 255),    # Crimson Neon
]
b_col = [vivid_colors[i % len(vivid_colors)] for i in range(bokeh_count)]

for f in range(total_frames):
    t = (f / total_frames) * 2 * math.pi
    frame = np.zeros((height, width, 3), dtype=np.uint8)
    frame[:, :] = (8, 6, 12) # Near-black slate for OLED battery savings
    
    for x_pos, y_pos, radius, col in zip(bx, by, br, b_col):
        pulse = 0.85 + 0.25 * math.sin(t * 2 + x_pos)
        r_actual = int(radius * pulse)
        overlay = frame.copy()
        cv2.circle(overlay, (x_pos, y_pos), r_actual, col, -1)
        frame = cv2.addWeighted(overlay, 0.42, frame, 0.58, 0)
        
    # Crisp rain streaks
    rain_offset = int((f / total_frames) * 220)
    for rx in range(12, width, 28):
        ry_start = (rx * 19 + rain_offset * 3) % height
        cv2.line(frame, (rx, ry_start), (rx + 2, min(ry_start + 50, height)), (210, 225, 245), 1)
        
    out4.write(frame)
out4.release()
print(f"4. Tokyo Rain: {path4}")

# 5. Dark Academia Ember Loop: Deep espresso shadows with blazing warm golden embers
path5 = os.path.join(output_dir, "dark_academia_flame_loop.mp4")
out5 = cv2.VideoWriter(path5, fourcc, fps, (width, height))
np.random.seed(888)
ember_count = 45
ex = np.random.randint(40, width - 40, ember_count)
ey_base = np.random.randint(height // 2, height, ember_count)
espeed = np.random.uniform(1.2, 3.2, ember_count)

for f in range(total_frames):
    t = (f / total_frames) * 2 * math.pi
    frame = np.zeros((height, width, 3), dtype=np.uint8)
    
    # Deep shadow gradient
    y_idx = np.linspace(0, 1, height)[:, None]
    frame[:, :, 0] = (5 * y_idx).astype(np.uint8)
    frame[:, :, 1] = (16 * y_idx).astype(np.uint8)
    frame[:, :, 2] = (38 * y_idx).astype(np.uint8)
    
    # Vibrant warm golden fire hearth bloom
    center_glow = int(60 + 20 * math.sin(t * 4))
    cv2.circle(frame, (width // 2, int(height * 0.88)), 240, (10, 70 + center_glow // 2, 160 + center_glow), -1)
    frame = cv2.GaussianBlur(frame, (65, 65), 0)
    
    for i in range(ember_count):
        y_pos = int((ey_base[i] - f * espeed[i] * 10) % height)
        x_pos = int(ex[i] + math.sin(t * 2.5 + i) * 18)
        bright = int(200 + 55 * math.sin(t * 3.5 + i))
        cv2.circle(frame, (x_pos, y_pos), 3, (20, int(bright * 0.75), bright), -1)
        
    out5.write(frame)
out5.release()
print(f"5. Dark Academia: {path5}")

# 6. Minimal Gradient Flow Loop: Vibrant, saturated Ultra-Violet, Neon Sunset Coral & Indigo
path6 = os.path.join(output_dir, "minimal_gradient_flow_loop.mp4")
out6 = cv2.VideoWriter(path6, fourcc, fps, (width, height))
for f in range(total_frames):
    t = (f / total_frames) * 2 * math.pi
    y, x = np.ogrid[:height, :width]
    
    drift1 = np.sin(x * 0.005 + t) * 0.5 + 0.5
    drift2 = np.cos(y * 0.004 + t) * 0.5 + 0.5
    
    # Deep, punchy color palette: Electric Violet + Vivid Sunset Coral
    b = (220 * drift1 + 50 * (1 - drift2)).astype(np.uint8)
    g = (40 * drift2 + 20 * drift1).astype(np.uint8)
    r = (245 * drift2 + 130 * (1 - drift1)).astype(np.uint8)
    
    frame = np.stack([b, g, r], axis=2)
    out6.write(frame)
out6.release()
print(f"6. Minimal Gradient: {path6}")

print("\nSUCCESS: All 6 Color-Enhanced & Battery-Smart Live Wallpaper Loops generated!")
