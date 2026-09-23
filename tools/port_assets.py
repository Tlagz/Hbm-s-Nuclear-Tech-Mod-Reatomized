"""
Copies assets from the original 1.7.10 NTM source tree into the NeoForge port.

- Lowercases every resource path (1.13+ ResourceLocations must match [a-z0-9/._-])
- Converts .lang files to 1.13+ JSON lang files, remapping
    tile.<name>.name -> block.hbm.<name>
    item.<name>.name -> item.hbm.<name>
  and keeping every other key unchanged
- Lowercases sound event names and sound file references in sounds.json
- Points every .obj at a shared default material (models/hbm_default.mtl, texture "#texture0"), NeoForge's
  OBJ loader needs one and the originals have none (their renderer binds textures manually)
- Writes tools/asset_rename_map.txt (old path -> new path) for reference while porting code

Usage: python tools/port_assets.py <path to original repo root>
"""
import json
import os
import re
import shutil
import sys

SKIP_DIRS = {"disks"}           # OpenComputers floppy contents, not needed yet
SKIP_FILES = {"test.lang", "STYLEGUIDE.md"}
# typos in original file names that break references
FIXUPS = {"sounds/weapon/grenadebounnce2.ogg": "sounds/weapon/grenadebounce2.ogg"}
OBJ_HEADER = "mtllib hbm:models/hbm_default.mtl\nusemtl hbm_default\n"
DEFAULT_MTL = "newmtl hbm_default\nmap_Kd #texture0\n"


def convert_obj(src, dst):
    with open(src, encoding="utf-8", errors="replace") as f:
        lines = [l for l in f if not l.startswith(("mtllib", "usemtl"))]
    with open(dst, "w", encoding="utf-8", newline="\n") as f:
        f.write(OBJ_HEADER)
        f.writelines(lines)


def convert_lang(src, dst):
    out = {}
    with open(src, encoding="utf-8-sig") as f:
        for line in f:
            line = line.rstrip("\r\n")
            if not line or line.lstrip().startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            key = key.strip()
            m = re.fullmatch(r"(tile|item)\.(.+)\.name", key)
            if m:
                prefix = "block" if m.group(1) == "tile" else "item"
                key = f"{prefix}.hbm.{m.group(2).lower()}"
            out[key] = value
    with open(dst, "w", encoding="utf-8", newline="\n") as f:
        json.dump(out, f, ensure_ascii=False, indent=2)
    return len(out)


def convert_sounds(src, dst):
    with open(src, encoding="utf-8") as f:
        data = json.load(f)
    out = {}
    for event, spec in data.items():
        sounds = []
        for s in spec.get("sounds", []):
            if isinstance(s, str):
                sounds.append("hbm:" + s.lower())
            else:
                s = dict(s)
                s["name"] = "hbm:" + s["name"].lower()
                sounds.append(s)
        new_spec = {k: v for k, v in spec.items() if k not in ("sounds", "category")}
        new_spec["sounds"] = sounds
        out[event.lower()] = new_spec
    with open(dst, "w", encoding="utf-8", newline="\n") as f:
        json.dump(out, f, indent=2)
    return len(out)


def main():
    orig_root = sys.argv[1]
    port_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    src_assets = os.path.join(orig_root, "src", "main", "resources", "assets", "hbm")
    dst_assets = os.path.join(port_root, "src", "main", "resources", "assets", "hbm")

    renames = []
    copied = 0
    for dirpath, dirnames, filenames in os.walk(src_assets):
        rel_dir = os.path.relpath(dirpath, src_assets)
        if rel_dir.split(os.sep)[0] in SKIP_DIRS:
            continue
        for name in filenames:
            if name in SKIP_FILES:
                continue
            rel = os.path.normpath(os.path.join(rel_dir, name)).replace(os.sep, "/")
            if rel == "sounds.json" or rel.startswith("lang/"):
                continue
            new_rel = rel.lower()
            new_rel = FIXUPS.get(new_rel, new_rel)
            if not re.fullmatch(r"[a-z0-9/._-]+", new_rel):
                print("WARNING: still invalid after lowercasing:", rel)
            if new_rel != rel:
                renames.append((rel, new_rel))
            dst = os.path.join(dst_assets, new_rel)
            os.makedirs(os.path.dirname(dst), exist_ok=True)
            if new_rel.endswith(".obj"):
                convert_obj(os.path.join(dirpath, name), dst)
            else:
                shutil.copy2(os.path.join(dirpath, name), dst)
            copied += 1

    with open(os.path.join(dst_assets, "models", "hbm_default.mtl"), "w", newline="\n") as f:
        f.write(DEFAULT_MTL)

    n = convert_sounds(os.path.join(src_assets, "sounds.json"), os.path.join(dst_assets, "sounds.json"))
    print(f"sounds.json: {n} sound events")

    os.makedirs(os.path.join(dst_assets, "lang"), exist_ok=True)
    for name in sorted(os.listdir(os.path.join(src_assets, "lang"))):
        if name in SKIP_FILES or not name.endswith(".lang"):
            continue
        dst_name = name[:-5].lower() + ".json"
        n = convert_lang(os.path.join(src_assets, "lang", name), os.path.join(dst_assets, "lang", dst_name))
        print(f"lang/{dst_name}: {n} keys")

    with open(os.path.join(port_root, "tools", "asset_rename_map.txt"), "w", encoding="utf-8", newline="\n") as f:
        for old, new in renames:
            f.write(f"{old} -> {new}\n")
    print(f"copied {copied} files, renamed {len(renames)}")


if __name__ == "__main__":
    main()
