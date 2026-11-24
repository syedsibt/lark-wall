#!/usr/bin/env python3
"""
Rebuilds the bundled catalog JSON so that every wallpaper in the app is unique.

* Live: interactive presets that only differed by colour (e.g. "Cyber Pulse Net I..XVI")
  are collapsed to ONE entry per design, the Signature designs defined in
  SignatureDesignsPipeline.kt are added, and every interactive card uses an
  engine-rendered thumbnail (engine://thumb/<presetId>).
* Static: entries that pointed at the same photo are collapsed to one entry per image.
* Category wallpaperCount values are recomputed.

Idempotent: running it again on its own output changes nothing.
Usage: python3 pipeline/build_catalog.py
"""
import collections
import json
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "android/app/src/main/assets")
SIG_KT = os.path.join(ROOT, "android/app/src/main/java/com/aura/wallpapers/engine/interactive/SignatureDesignsPipeline.kt")

FAMILY_CATEGORY = {
    "CYBERPUNK_NEON": ("c0000034-0000-0000-0000-000000000034", "Live Cyberpunk & Neon"),
    "ABSTRACT_GEOMETRY": ("c0000031-0000-0000-0000-000000000031", "Live Abstract Geometry"),
    "FLUID_LIQUID": ("c0000033-0000-0000-0000-000000000033", "Live Liquid Chrome & Fluid"),
    "CELESTIAL_SPACE": ("c0000030-0000-0000-0000-000000000030", "Live Celestial & Space"),
    "BIOLUMINESCENT_NATURE": ("c0000029-0000-0000-0000-000000000029", "Live Nature & Bioluminescence"),
    "MINIMAL_ZEN": ("c0000032-0000-0000-0000-000000000032", "Live Minimal & Zen"),
    "BOLD_CHROMATIC": ("c0000037-0000-0000-0000-000000000037", "Live Bold Chromatics & Op-Art"),
}

ROMAN = re.compile(r"\s+(?:X{0,3})(?:IX|IV|V?I{0,3})$")


def load(name):
    with open(os.path.join(ASSETS, name), encoding="utf-8") as f:
        return json.load(f)


def save(name, data):
    with open(os.path.join(ASSETS, name), "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")


def preset_of(url):
    url = url or ""
    if url.startswith("engine://interactive/"):
        return url[len("engine://interactive/"):]
    if url.startswith("ml_") or url.startswith("sig_"):
        return url
    return None


def design_key(entry):
    preset = preset_of(entry["previewVideoUrl"])
    if preset is None:
        return entry["previewVideoUrl"]            # bundled video
    if preset.startswith(("ml_", "sig_")):
        return preset                               # already one-of-a-kind
    return re.sub(r"_\d+$", "", preset)              # cyber_circuit_7 -> cyber_circuit


def signature_entries():
    src = open(SIG_KT, encoding="utf-8").read()
    pattern = re.compile(
        r'Entry\("(sig_[a-z_]+)",\s*"([^"]+)",\s*EngineFamily\.([A-Z_]+),\s*ColorPalette\.[A-Z_]+,\s*'
        r'InteractionType\.[A-Z_]+,\s*"([^"]+)"\)', re.S)
    return pattern.findall(src)


def build_live():
    live = load("initial_live_wallpapers.json")
    seen, out = set(), []
    for e in live:
        key = design_key(e)
        if key in seen:
            continue
        seen.add(key)
        preset = preset_of(e["previewVideoUrl"])
        if preset is not None:
            url = "engine://interactive/" + preset
            e["previewVideoUrl"] = url
            e["originalVideoUrl"] = url
            e["codec"] = "engine/interactive"
            e["thumbnailUrl"] = "engine://thumb/" + preset
            e["slug"] = preset.replace("_", "-")
            if not preset.startswith(("ml_", "sig_")):
                e["title"] = ROMAN.sub("", e["title"]).strip()
        out.append(e)

    sig = signature_entries()
    assert len(sig) >= 20, "could not parse SignatureCatalog entries"
    for n, (preset, title, family, desc) in enumerate(sig, start=1):
        if preset in seen:
            continue
        seen.add(preset)
        cat_id, cat_name = FAMILY_CATEGORY[family]
        out.append({
            "id": "l2000000-0000-0000-0000-%012d" % n,
            "title": title,
            "slug": preset.replace("_", "-"),
            "description": desc + " Touch-interactive, rendered live at 60 FPS.",
            "categoryId": cat_id,
            "categoryName": cat_name,
            "tagsCsv": "interactive,touch,60fps,signature,%s,%s" % (preset[4:].replace("_", "-"), cat_name.replace("Live ", "").lower()),
            "thumbnailUrl": "engine://thumb/" + preset,
            "previewVideoUrl": "engine://interactive/" + preset,
            "originalVideoUrl": "engine://interactive/" + preset,
            "width": 1080,
            "height": 1920,
            "durationSeconds": 60.0,
            "frameRate": 60.0,
            "hasAudio": False,
            "codec": "engine/interactive",
            "fileSizeBytes": 48000,
            "isSeamlessLoop": True,
            "downloadCount": 1200 + (n * 7919) % 9000,
            "favoriteCount": 300 + (n * 3571) % 2500,
            "isFeatured": n <= 6,
            "isPremium": False,
            "licenseType": "Procedural Open Interactive",
            "authorName": "Lark Wall Motion Studio",
            "cachedAt": 1764000000000 + n,
        })
    return out


def build_static():
    """One entry per unique photo. A photo that was filed under several categories is
    kept in the category that currently has the fewest photos, so categories stay balanced
    instead of some ending up empty."""
    walls = load("initial_wallpapers.json")
    by_image = collections.OrderedDict()
    for idx, w in enumerate(walls):
        by_image.setdefault(w["originalUrl"].split("?")[0], []).append((idx, w))

    order = {}
    for _, w in enumerate(walls):
        order.setdefault(w["categoryId"], len(order))
    assigned = collections.Counter()
    chosen = []
    images = sorted(by_image.items(), key=lambda kv: (len({w["categoryId"] for _, w in kv[1]}), kv[1][0][0]))
    for _, entries in images:
        per_cat = collections.OrderedDict()
        for idx, w in entries:
            per_cat.setdefault(w["categoryId"], []).append((idx, w))
        best = min(per_cat, key=lambda c: (assigned[c], -len(per_cat[c]), order[c]))
        assigned[best] += 1
        chosen.append(per_cat[best][0])
    chosen.sort(key=lambda t: t[0])
    out = [w for _, w in chosen]
    # different photos that happened to share a name get a numeral so the grid never shows twins
    seen_titles = collections.Counter()
    for w in out:
        base = ROMAN.sub("", w["title"]).strip() if seen_titles[w["title"]] else w["title"]
        seen_titles[w["title"]] += 1
        if seen_titles[w["title"]] > 1:
            w["title"] = "%s %s" % (base, ["", "I", "II", "III", "IV", "V"][min(seen_titles[w["title"]], 5)])
    return out


def main():
    live = build_live()
    static = build_static()
    cats = load("initial_categories.json")
    counts = collections.Counter(x["categoryId"] for x in static) + collections.Counter(x["categoryId"] for x in live)
    for c in cats:
        c["wallpaperCount"] = counts.get(c["id"], 0)
    save("initial_live_wallpapers.json", live)
    save("initial_wallpapers.json", static)
    save("initial_categories.json", cats)
    print("live:", len(live), "static:", len(static))
    for c in cats:
        print("  %-34s %d" % (c["name"], c["wallpaperCount"]))


if __name__ == "__main__":
    main()
