import os
from PIL import Image, ImageDraw
import resvg_py

res_dir = r"C:\Users\pc\.gemini\antigravity\scratch\aura-wallpapers\android\app\src\main\res"
svg_path = r"C:\Users\pc\.gemini\antigravity\scratch\aura-wallpapers\design_uiux\app_icon.svg"

with open(svg_path, "r", encoding="utf-8") as f:
    svg_data = f.read()

# Update badge to Lark Wall
svg_data = svg_data.replace("HON CHAY E", "LARK WALL")
# Save updated svg
with open(svg_path, "w", encoding="utf-8") as f:
    f.write(svg_data)

# Render master 512x512 PNG using resvg
png_bytes = resvg_py.svg_to_bytes(svg_data)
master_img = Image.open(io_bytes := __import__("io").BytesIO(png_bytes)).convert("RGBA")

# Ensure drawable directory has ic_launcher.png (512x512)
drawable_dir = os.path.join(res_dir, "drawable")
os.makedirs(drawable_dir, exist_ok=True)
master_img.save(os.path.join(drawable_dir, "ic_launcher.png"), "PNG")
master_img.save(os.path.join(r"C:\Users\pc\.gemini\antigravity\scratch\aura-wallpapers\design_uiux", "app_icon.png"), "PNG")

densities = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

for folder, size in densities.items():
    target_dir = os.path.join(res_dir, folder)
    os.makedirs(target_dir, exist_ok=True)
    
    # 1. Square / rounded squircle icon
    sq_img = master_img.resize((size, size), Image.Resampling.LANCZOS)
    sq_img.save(os.path.join(target_dir, "ic_launcher.png"), "PNG")
    
    # 2. Round icon (masked circle)
    round_img = sq_img.copy()
    mask = Image.new("L", (size, size), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, size - 1, size - 1), fill=255)
    round_img.putalpha(Image.composite(round_img.getchannel("A"), mask, mask))
    round_img.save(os.path.join(target_dir, "ic_launcher_round.png"), "PNG")

print("All mipmap icons generated successfully!")
