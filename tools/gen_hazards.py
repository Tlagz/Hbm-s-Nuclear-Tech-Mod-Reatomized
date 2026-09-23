"""
Generates the plain item hazard registrations (HazardSystem.register(item, makeData(...))) of the original's
HazardRegistry.registerItems() into the region between the markers in the port's HazardRegistry.
Only items/blocks that exist in the port are included; registrations that need metadata stacks, hazard
modifiers or the fuel helper methods (RBMK, PWR, RTG...) are left for later. Rerun after porting more items.

Usage: python tools/gen_hazards.py <path to original repo root>
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


src = read(os.path.join(ORIG, "src/main/java/com/hbm/hazard/HazardRegistry.java"))
items = set(re.findall(r"public static final \w+<[^>]+> (\w+) =", read(os.path.join(JAVA, "items/ModItems.java"))))
blocks = set(re.findall(r"public static final \w+<[^>]+> (\w+) =", read(os.path.join(JAVA, "blocks/ModBlocks.java"))))

body = src[src.index("public static void registerItems()"):src.index("public static void registerTrafos()")]
body = body[body.index("{") + 1:]
body = re.sub(r"//[^\n]*", "", body)

lines, skipped = [], 0
for st in body.split(";"):
    st = " ".join(st.split())
    m = re.fullmatch(r"HazardSystem\.register\((.+?), (makeData\(.*\))\)", st)
    if not m:
        if st.startswith(("HazardSystem", "register")): skipped += 1
        continue
    target, data = m.group(1), m.group(2)
    if "addMod" in data or "new " in data:
        skipped += 1
        continue
    t = re.fullmatch(r"(?:(ModItems|ModBlocks)\.)?(\w+)", target)
    v = re.fullmatch(r"(Items|Blocks)\.(\w+)", target)
    s = re.fullmatch(r'"(\w+)"', target)
    if t and t.group(2) in items and t.group(1) != "ModBlocks":
        target = "ModItems." + t.group(2)
    elif t and t.group(2) in blocks:
        target = "ModBlocks." + t.group(2)
    elif v:
        target = f"{v.group(1)}.{v.group(2).upper()}"
    elif s:
        target = f'OreDictManager.tag("{s.group(1)}")'
    else:
        skipped += 1
        continue
    lines.append(f"\t\tHazardSystem.register({target}, {data});")

path = os.path.join(JAVA, "hazard", "HazardRegistry.java")
port = read(path)
start = port.index("\n", port.index("// BEGIN GENERATED")) + 1
end = port.rindex("\n", 0, port.index("// END GENERATED")) + 1
port = port[:start] + "\n".join(lines) + "\n" + port[end:]
with open(path, "w", encoding="utf-8", newline="\n") as f:
    f.write(port)
print(f"{len(lines)} hazard registrations, {skipped} skipped (unported items, modifiers, metadata)")
