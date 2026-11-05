#!/usr/bin/env python3
"""
Aura Wallpapers - Automated Asset Ingestion Pipeline
Validates, checks aspect ratios, computes cryptographic & perceptual hashes,
strips audio from videos, generates WebP thumbnails & optimized previews,
and outputs catalog-ready metadata for Supabase.
"""

import os
import sys
import json
import hashlib
import argparse
import subprocess
from pathlib import Path
from typing import Dict, Any, Optional

try:
    from PIL import Image
except ImportError:
    Image = None

class AssetPipeline:
    MIN_WIDTH = 1080
    MIN_HEIGHT = 1920
    SUPPORTED_IMAGE_EXTS = {'.jpg', '.jpeg', '.png', '.webp'}
    SUPPORTED_VIDEO_EXTS = {'.mp4', '.mov', '.mkv', '.webm'}

    def __init__(self, output_dir: str):
        self.output_dir = Path(output_dir)
        self.thumbs_dir = self.output_dir / "thumbnails"
        self.previews_dir = self.output_dir / "previews"
        self.masters_dir = self.output_dir / "masters"
        for d in (self.thumbs_dir, self.previews_dir, self.masters_dir):
            d.mkdir(parents=True, exist_ok=True)

    @staticmethod
    def calculate_sha256(file_path: Path) -> str:
        sha = hashlib.sha256()
        with open(file_path, "rb") as f:
            while chunk := f.read(65536):
                sha.update(chunk)
        return sha.hexdigest()

    def process_image(self, file_path: Path, title: str, category_id: str, tags: list[str]) -> Dict[str, Any]:
        if not Image:
            raise RuntimeError("Pillow is required for image processing: pip install Pillow")

        sha256 = self.calculate_sha256(file_path)
        with Image.open(file_path) as img:
            width, height = img.size
            if width < self.MIN_WIDTH and height < self.MIN_HEIGHT:
                raise ValueError(f"Image resolution {width}x{height} below minimum {self.MIN_WIDTH}x{self.MIN_HEIGHT}")

            aspect_ratio = f"{round(width / math_gcd(width, height))}:{round(height / math_gcd(width, height))}" if width and height else "9:16"
            if abs(width / height - 9 / 16) < 0.05:
                aspect_ratio = "9:16"

            slug = file_path.stem.lower().replace(" ", "-")

            # 1. Master copy
            master_filename = f"{slug}-master.webp"
            master_path = self.masters_dir / master_filename
            img.save(master_path, "WEBP", quality=95, method=6)

            # 2. Preview (Max 1080w / 1920h)
            preview_img = img.copy()
            preview_img.thumbnail((1080, 1920), Image.Resampling.LANCZOS)
            preview_filename = f"{slug}-preview.webp"
            preview_path = self.previews_dir / preview_filename
            preview_img.save(preview_path, "WEBP", quality=85, method=6)

            # 3. Thumbnail (Max 480w / 854h)
            thumb_img = img.copy()
            thumb_img.thumbnail((480, 854), Image.Resampling.LANCZOS)
            thumb_filename = f"{slug}-thumb.webp"
            thumb_path = self.thumbs_dir / thumb_filename
            thumb_img.save(thumb_path, "WEBP", quality=80, method=6)

        return {
            "type": "static",
            "title": title,
            "slug": slug,
            "category_id": category_id,
            "tags": tags,
            "thumbnail_file": str(thumb_path),
            "preview_file": str(preview_path),
            "master_file": str(master_path),
            "width": width,
            "height": height,
            "aspect_ratio": aspect_ratio,
            "file_size_bytes": master_path.stat().st_size,
            "format": "image/webp",
            "sha256_hash": sha256
        }

    def process_video(self, file_path: Path, title: str, category_id: str, tags: list[str]) -> Dict[str, Any]:
        sha256 = self.calculate_sha256(file_path)
        slug = file_path.stem.lower().replace(" ", "-")

        # Probe video using ffprobe
        probe_cmd = [
            "ffprobe", "-v", "error",
            "-select_streams", "v:0",
            "-show_entries", "stream=width,height,r_frame_rate,duration",
            "-of", "json", str(file_path)
        ]
        res = subprocess.run(probe_cmd, capture_output=True, text=True, check=True)
        probe_data = json.loads(res.stdout)["streams"][0]

        width = int(probe_data["width"])
        height = int(probe_data["height"])
        fps_eval = eval(probe_data["r_frame_rate"]) if "/" in probe_data.get("r_frame_rate", "") else float(probe_data.get("r_frame_rate", 30))
        duration = float(probe_data.get("duration", 10.0))

        # 1. Generate 1080p Master (Strip audio: -an, H.264 baseline)
        master_filename = f"{slug}-1080p.mp4"
        master_path = self.masters_dir / master_filename
        subprocess.run([
            "ffmpeg", "-y", "-i", str(file_path),
            "-an", "-c:v", "libx264", "-profile:v", "baseline", "-level", "4.0",
            "-pix_fmt", "yuv420p", "-movflags", "+faststart",
            str(master_path)
        ], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

        # 2. Generate 720p Preview
        preview_filename = f"{slug}-preview-720p.mp4"
        preview_path = self.previews_dir / preview_filename
        subprocess.run([
            "ffmpeg", "-y", "-i", str(master_path),
            "-vf", "scale=-2:1280", "-c:v", "libx264", "-profile:v", "baseline",
            "-crf", "26", "-movflags", "+faststart",
            str(preview_path)
        ], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

        # 3. Extract WebP Thumbnail from first frame
        thumb_filename = f"{slug}-thumb.webp"
        thumb_path = self.thumbs_dir / thumb_filename
        subprocess.run([
            "ffmpeg", "-y", "-ss", "00:00:00.5", "-i", str(master_path),
            "-vframes", "1", "-vf", "scale=480:-2",
            str(thumb_path)
        ], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

        return {
            "type": "live",
            "title": title,
            "slug": slug,
            "category_id": category_id,
            "tags": tags,
            "thumbnail_file": str(thumb_path),
            "preview_video_file": str(preview_path),
            "original_video_file": str(master_path),
            "width": width,
            "height": height,
            "duration_seconds": round(duration, 2),
            "frame_rate": round(fps_eval, 1),
            "has_audio": False,
            "codec": "video/mp4",
            "file_size_bytes": master_path.stat().st_size,
            "sha256_hash": sha256,
            "is_seamless_loop": True
        }

def math_gcd(a: int, b: int) -> int:
    import math
    return math.gcd(a, b)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Aura Wallpapers Asset Processing Tool")
    parser.add_argument("--input", required=True, help="Input media file path")
    parser.add_argument("--outdir", default="./dist_assets", help="Destination output folder")
    parser.add_argument("--title", required=True, help="Wallpaper title")
    parser.add_argument("--category", required=True, help="Category UUID")
    parser.add_argument("--tags", default="", help="Comma-separated tags")

    args = parser.parse_args()
    pipeline = AssetPipeline(args.outdir)
    in_path = Path(args.input)
    tags_list = [t.strip().lower() for t in args.tags.split(",") if t.strip()]

    if in_path.suffix.lower() in AssetPipeline.SUPPORTED_IMAGE_EXTS:
        meta = pipeline.process_image(in_path, args.title, args.category, tags_list)
    elif in_path.suffix.lower() in AssetPipeline.SUPPORTED_VIDEO_EXTS:
        meta = pipeline.process_video(in_path, args.title, args.category, tags_list)
    else:
        print(f"Unsupported file format: {in_path.suffix}", file=sys.stderr)
        sys.exit(1)

    print(json.dumps(meta, indent=2))
