"""
Generates com/hbm/inventory/OreDictManager.java from the original's OreDictManager.

The material declarations (DictFrames, DictGroups, keys) are copied as they are. The registerOres() body is
filtered: arguments referring to items/blocks that don't exist in the port yet (or metadata stacks) are
dropped, a call without arguments left is dropped entirely. Rerun after porting more items to pick them up.

Usage: python tools/gen_oredict.py <path to original repo root>
"""
import os
import re
import sys

ORIG = sys.argv[1]
PORT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAVA = os.path.join(PORT, "src", "main", "java", "com", "hbm")


def read(path):
    with open(path, encoding="utf-8", errors="replace") as f:
        return f.read()


src = read(os.path.join(ORIG, "src/main/java/com/hbm/inventory/OreDictManager.java"))
multis = set(re.findall(r"public static final ItemEnumMulti\.Variants<\w+> (\w+) =", read(os.path.join(JAVA, "items/ModItems.java"))))
items = set(re.findall(r"public static final [\w.]+<[^>]+> (\w+) =", read(os.path.join(JAVA, "items/ModItems.java"))))
blocks = set(re.findall(r"public static final [\w.]+<[^>]+> (\w+) =", read(os.path.join(JAVA, "blocks/ModBlocks.java"))))

consts = src[src.index("\tpublic static final String KEY_STICK"):src.index("\tpublic static void registerOres()")]
consts = consts.replace('Compat.isModLoaded(Compat.MOD_GT6) ? "Uraninite" : "Uranium"', '"Uranium"')
frames = set(re.findall(r"public static final Dict(?:Frame|Group) (\w+) =", consts))

VANILLA_RENAMES = {"quartz_ore": "NETHER_QUARTZ_ORE", "quartz": "QUARTZ"}


def convert_arg(arg):
    arg = arg.strip()
    m = re.fullmatch(r"(?:DictFrame\.)?fromOne\((?:ModItems\.)?(\w+), (?:ItemEnums\.)?(\w+\.\w+)\)", arg)
    if m:
        return f"ModItems.{m.group(1)}.get({m.group(2)})" if m.group(1) in multis else None
    m = re.fullmatch(r"(?:DictFrame\.)?fromAll\((?:ModItems\.)?(\w+), (?:ItemEnums\.)?\w+\.class\)", arg)
    if m:
        return f"ModItems.{m.group(1)}.all()" if m.group(1) in multis else None
    m = re.fullmatch(r"(?:ModItems\.)?(\w+)", arg)
    if m and m.group(1) in multis:
        return f"ModItems.{m.group(1)}.all()"
    if m and m.group(1) in items:
        return "ModItems." + m.group(1)
    m = re.fullmatch(r"(?:ModBlocks\.)?(\w+)", arg)
    if m and m.group(1) in blocks:
        return "ModBlocks." + m.group(1)
    m = re.fullmatch(r"(Items|Blocks)\.(\w+)", arg)
    if m:
        return f"{m.group(1)}.{VANILLA_RENAMES.get(m.group(2), m.group(2).upper())}"
    if re.fullmatch(r"HazardRegistry\.\w+|[\d.]+F?", arg):
        return arg
    return None  # metadata stacks, material items, unported items


def split_args(s):
    out, depth, cur = [], 0, ""
    for ch in s:
        if ch == "(" : depth += 1
        if ch == ")" : depth -= 1
        if ch == "," and depth == 0:
            out.append(cur)
            cur = ""
        else:
            cur += ch
    if cur.strip():
        out.append(cur)
    return out


body = src[src.index("\tpublic static void registerOres()"):src.index("\tpublic static void registerGroups()")]
body = body[body.index("{") + 1:body.rindex("}")]
body = re.sub(r"//[^\n]*", "", body)
body = re.sub(r"/\*.*?\*/", "", body, flags=re.S)

lines, dropped = [], 0
for st in body.split(";"):
    st = " ".join(st.split())
    if not st:
        continue
    m = re.match(r"(\w+)\s*(\..*)$", st)
    if m and m.group(1) in frames:
        calls = re.findall(r"\.(\w+)\(((?:[^()]|\((?:[^()]|\([^()]*\))*\))*)\)", m.group(2))
        parts = []
        for method, args in calls:
            if method in ("rad", "hot", "blinding", "asbestos", "hydro"):
                parts.append(f".{method}({args})")
                continue
            if method == "hazIngot":
                parts.append(".hazIngot()")
                continue
            conv = [convert_arg(a) for a in split_args(args)]
            kept = [c for c in conv if c]
            dropped += len(conv) - len(kept)
            if kept:
                parts.append(f".{method}({', '.join(kept)})")
        if any(not p.startswith((".rad", ".hot", ".blinding", ".asbestos", ".hydro")) for p in parts):
            lines.append(f"\t\t{m.group(1)}{''.join(parts)};")
        continue
    m = re.fullmatch(r'OreDictionary\.registerOre\((KEY_\w+|"\w+"), (.+)\)', st)
    if m:
        conv = convert_arg(m.group(2))
        if conv:
            lines.append(f"\t\tregisterExtra({m.group(1)}, {conv});")
            continue
    dropped += 1

TEMPLATE = read(os.path.join(PORT, "tools", "OreDictManager.java.template"))
out = TEMPLATE.replace("\t//@CONSTANTS@\n", consts).replace("\t\t//@REGISTER@\n", "\n".join(lines) + "\n")
with open(os.path.join(JAVA, "inventory", "OreDictManager.java"), "w", encoding="utf-8", newline="\n") as f:
    f.write(out)
print(f"{len(lines)} registration statements, {dropped} unported arguments/statements dropped")
