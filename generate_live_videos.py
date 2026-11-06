import os
import cv2
import numpy as np
import math

output_dir = r"C:\Users\pc\.gemini\antigravity\scratch\aura-wallpapers\android\app\src\main\assets\live_videos"
os.makedirs(output_dir, exist_ok=True)

width, height = 720, 1280 # Clean 720x1280 mobile aspect ratio for smooth 60fps performance and small APK size
fps = 30
duration_sec = 4
total_frames = fps * duration_sec
fourcc = cv2.VideoWriter_fourcc(*'mp4v')

print(f"Generating 6 seamless live wallpaper video loops ({width}x{height} @ {fps}fps, {total_frames} frames)...")

# 1. Liquid Chrome Loop (Molten mercury rippling waves)
path1 = os.path.join(output_dir, "liquid_chrome_loop.mp4")
out1 = cv2.VideoWriter(path1, fourcc, fps, (width, height))
for f in range(total_frames):
    t = (f / total_frames) * 2 * math.pi
    y, x = np.ogrid[:height, :width]
    # Undulating wave interference
    wave1 = np.sin(x * 0.015 + t) * 40
    wave2 = np.cos(y * 0.012 + t * 2) * 35
    wave3 = np.sin((x + y) * 0.01 + t) * 30
    combined = wave1 + wave2 + wave3
    
    # Silver mercury gradient with specular light sweep
    base = 140 + 70 * np.sin((y + combined) * 0.008)
    specular = np.clip(255 * np.power(np.sin((x * 0.02 - y * 0.01 + t)), 8), 0, 100)
    
    b = np.clip(base + specular + 30 * np.sin(t), 0, 255).astype(np.uint8)
    g = np.clip(base * 0.95 + specular + 20 * np.cos(t), 0, 255).astype(np.uint8)
    r = np.clip(base * 0.92 + specular + 40 * np.sin(t + 1), 0, 255).astype(np.uint8)
    
    frame = np.stack([b, g, r], axis=2)
    out1.write(frame)
out1.release()
print(f"Created: {path1}")

# 2. Cyberpunk Neon Loop (OLED true black with glowing neon grids and particles)
path2 = os.path.join(output_dir, "cyberpunk_neon_loop.mp4")
out2 = cv2.VideoWriter(path2, fourcc, fps, (width, height))
for f in range(total_frames):
    t = (f / total_frames) * 2 * math.pi
    frame = np.zeros((height, width, 3), dtype=np.uint8)
    
    # Perspective grid lines moving forward
    grid_y_offset = int((f / total_frames) * 40)
    for gy in range(height // 2, height, 40):
        y_pos = gy + grid_y_offset
        if y_pos < height:
            alpha = (y_pos - height // 2) / (height // 2)
            color = (int(240 * alpha), int(40 * alpha), int(200 * alpha)) # Neon Magenta / Cyan
            cv2.line(frame, (0, y_pos), (width, y_pos), color, 2)
            
    # Perspective vertical vanishing lines
    center_x, center_y = width // 2, height // 2
    for vx in range(0, width + 60, 60):
        cv2.line(frame, (center_x, center_y), (vx, height), (160, 30, 220), 1)
        
    # Glowing neon horizon sun
    cv2.circle(frame, (center_x, center_y), 90, (230, 70, 160), -1)
    cv2.circle(frame, (center_x, center_y), 60, (255, 140, 200), -1)
    cv2.circle(frame, (center_x, center_y), 30, (255, 230, 250), -1)
    
    # Ambient top gradient
    top_glow = (np.sin(t) * 15 + 25)
    frame[:center_y, :, 0] = np.clip(frame[:center_y, :, 0] + int(top_glow * 1.5), 0, 255)
    frame[:center_y, :, 2] = np.clip(frame[:center_y, :, 2] + int(top_glow), 0, 255)

    out2.write(frame)
out2.release()
print(f"Created: {path2}")

# 3. Aurora Space Loop (Emerald & violet ethereal ribbons across starfield)
path3 = os.path.join(output_dir, "aurora_space_loop.mp4")
out3 = cv2.VideoWriter(path3, fourcc, fps, (width, height))
np.random.seed(42)
stars_x = np.random.randint(0, width, 120)
stars_y = np.random.randint(0, height, 120)
stars_bright = np.random.randint(120, 255, 120)

for f in range(total_frames):
    t = (f / total_frames) * 2 * math.pi
    frame = np.zeros((height, width, 3), dtype=np.uint8)
    
    # Deep midnight cosmic background
    y_idx = np.linspace(0, 1, height)[:, None]
    frame[:, :, 0] = (25 * y_idx).astype(np.uint8) # Dark blue
    frame[:, :, 2] = (15 * y_idx).astype(np.uint8) # Dark purple
    
    # Stars
    for sx, sy, sb in zip(stars_x, stars_y, stars_bright):
        twinkle = int(sb * (0.7 + 0.3 * math.sin(t * 3 + sx)))
        frame[sy, sx] = (twinkle, twinkle, twinkle)
        
    # Aurora ribbons
    y, x = np.ogrid[:height, :width]
    ribbon1 = np.exp(-((y - (height * 0.45 + np.sin(x * 0.01 + t) * 60)) ** 2) / (2 * 45 ** 2))
    ribbon2 = np.exp(-((y - (height * 0.55 + np.cos(x * 0.008 + t * 1.5) * 50)) ** 2) / (2 * 55 ** 2))
    
    frame[:, :, 1] = np.clip(frame[:, :, 1] + (ribbon1 * 220 + ribbon2 * 140).astype(np.uint8), 0, 255) # Emerald Green
    frame[:, :, 0] = np.clip(frame[:, :, 0] + (ribbon1 * 130 + ribbon2 * 190).astype(np.uint8), 0, 255) # Cyan / Blue
    frame[:, :, 2] = np.clip(frame[:, :, 2] + (ribbon2 * 180).astype(np.uint8), 0, 255) # Violet
    
    out3.write(frame)
out3.release()
print(f"Created: {path3}")

# 4. Tokyo Rain Loop (Midnight rain drops with glowing amber/cyan bokeh)
path4 = os.path.join(output_dir, "tokyo_rain_loop.mp4")
out4 = cv2.VideoWriter(path4, fourcc, fps, (width, height))
np.random.seed(101)
bokeh_count = 25
bx = np.random.randint(40, width - 40, bokeh_count)
by = np.random.randint(60, height - 60, bokeh_count)
br = np.random.randint(25, 60, bokeh_count)
b_colors = [
    (240, 180, 50),   # Cyan
    (50, 140, 255),   # Amber gold
    (220, 80, 200),   # Neon pink
    (180, 220, 70),   # Mint
    (80, 90, 240),    # Soft red
]
b_col = [b_colors[i % len(b_colors)] for i in range(bokeh_count)]

for f in range(total_frames):
    t = (f / total_frames) * 2 * math.pi
    frame = np.zeros((height, width, 3), dtype=np.uint8)
    frame[:, :] = (15, 10, 20) # Moody dark slate
    
    # Soft glowing bokeh orbs
    for x_pos, y_pos, radius, col in zip(bx, by, br, b_col):
        pulse = 0.8 + 0.2 * math.sin(t * 2 + x_pos)
        r_actual = int(radius * pulse)
        overlay = frame.copy()
        cv2.circle(overlay, (x_pos, y_pos), r_actual, col, -1)
        frame = cv2.addWeighted(overlay, 0.35, frame, 0.65, 0)
        
    # Vertical rain streaks
    rain_offset = int((f / total_frames) * 200)
    for rx in range(10, width, 30):
        ry_start = (rx * 17 + rain_offset * 3) % height
        cv2.line(frame, (rx, ry_start), (rx + 2, min(ry_start + 45, height)), (180, 190, 210), 1)
        
    out4.write(frame)
out4.release()
print(f"Created: {path4}")

# 5. Dark Academia Ember Loop (Warm flickering candlelight & floating golden dust)
path5 = os.path.join(output_dir, "dark_academia_flame_loop.mp4")
out5 = cv2.VideoWriter(path5, fourcc, fps, (width, height))
np.random.seed(777)
ember_count = 40
ex = np.random.randint(50, width - 50, ember_count)
ey_base = np.random.randint(height // 2, height, ember_count)
espeed = np.random.uniform(1.0, 3.0, ember_count)

for f in range(total_frames):
    t = (f / total_frames) * 2 * math.pi
    frame = np.zeros((height, width, 3), dtype=np.uint8)
    
    # Deep warm espresso gradient
    y_idx = np.linspace(0, 1, height)[:, None]
    frame[:, :, 0] = (8 * y_idx).astype(np.uint8)  # Blue
    frame[:, :, 1] = (22 * y_idx).astype(np.uint8) # Green
    frame[:, :, 2] = (45 * y_idx).astype(np.uint8) # Rich warm brown
    
    # Warm radial candle hearth at bottom center
    center_glow = int(50 + 15 * math.sin(t * 4))
    cv2.circle(frame, (width // 2, int(height * 0.85)), 220, (20, 60 + center_glow // 2, 120 + center_glow), -1)
    frame = cv2.GaussianBlur(frame, (65, 65), 0)
    
    # Floating golden ember particles drifting up
    for i in range(ember_count):
        y_pos = int((ey_base[i] - f * espeed[i] * 10) % height)
        x_pos = int(ex[i] + math.sin(t * 2 + i) * 15)
        bright = int(180 + 75 * math.sin(t * 3 + i))
        cv2.circle(frame, (x_pos, y_pos), 3, (40, int(bright * 0.7), bright), -1)
        
    out5.write(frame)
out5.release()
print(f"Created: {path5}")

# 6. Minimal Gradient Flow Loop (Silky smooth violet, peach, and indigo aura wave)
path6 = os.path.join(output_dir, "minimal_gradient_flow_loop.mp4")
out6 = cv2.VideoWriter(path6, fourcc, fps, (width, height))
for f in range(total_frames):
    t = (f / total_frames) * 2 * math.pi
    y, x = np.ogrid[:height, :width]
    
    drift1 = np.sin(x * 0.005 + t) * 0.5 + 0.5
    drift2 = np.cos(y * 0.004 + t) * 0.5 + 0.5
    
    # Elegant mauve / violet / apricot aura
    b = (180 * drift1 + 70 * (1 - drift2)).astype(np.uint8)
    g = (70 * drift2 + 40 * drift1).astype(np.uint8)
    r = (210 * drift2 + 120 * (1 - drift1)).astype(np.uint8)
    
    frame = np.stack([b, g, r], axis=2)
    out6.write(frame)
out6.release()
print(f"Created: {path6}")

print("\nSUCCESS: All 6 offline live wallpaper video loops generated!")
