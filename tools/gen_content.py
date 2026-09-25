"""
Generates registrations for the "simple" items and blocks of the original mod straight from its
ModItems.java / ModBlocks.java declarations, in the original registration order (= creative tab order).

Simple means: the declaration only uses one of the known classes below and only setters this script
understands. Everything else is reported as skipped and gets ported by hand.

The output replaces the region between the "BEGIN GENERATED" / "END GENERATED" markers in the port's
ModItems.java and ModBlocks.java. Hand-written registrations live outside the markers; names that already
exist there are skipped by the generator.

Usage: python tools/gen_content.py <path to original repo root> [--report]
"""
import os
import re
import sys

ORIG = sys.argv[1]
REPORT = "--report" in sys.argv
PORT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAVA = os.path.join(PORT, "src", "main", "java", "com", "hbm")
TEXTURES = os.path.join(PORT, "src", "main", "resources", "assets", "hbm", "textures")

TABS = {
    "MainRegistry.partsTab": "NtmTab.PARTS",
    "MainRegistry.controlTab": "NtmTab.CONTROL",
    "MainRegistry.templateTab": "NtmTab.TEMPLATE",
    "MainRegistry.blockTab": "NtmTab.BLOCKS",
    "MainRegistry.machineTab": "NtmTab.MACHINE",
    "MainRegistry.nukeTab": "NtmTab.NUKE",
    "MainRegistry.missileTab": "NtmTab.MISSILE",
    "MainRegistry.weaponTab": "NtmTab.WEAPON",
    "MainRegistry.consumableTab": "NtmTab.CONSUMABLE",
    "null": "null",
}

RARITY = {"common": "COMMON", "uncommon": "UNCOMMON", "rare": "RARE", "epic": "EPIC"}


def read(path):
    with open(path, encoding="utf-8", errors="replace") as f:
        return f.read()


def statements(src, method):
    """All ';'-terminated statements inside the body of the given static method"""
    i = src.index(f"static void {method}()")
    j = src.index("{", i)
    depth, k = 0, j
    while True:
        if src[k] == "{": depth += 1
        elif src[k] == "}":
            depth -= 1
            if depth == 0: break
        k += 1
    body = src[j + 1:k]
    body = re.sub(r"//[^\n]*", "", body)
    body = re.sub(r"/\*.*?\*/", "", body, flags=re.S)
    return [" ".join(s.split()) for s in body.split(";") if s.strip()]


def split_chain(expr):
    """'new X(a).b(c).d()' -> ('X', 'a', [('b','c'), ('d','')])"""
    m = re.match(r"new (\w+)\((.*?)\)((?:\.\w+\(.*?\))*)$", expr)
    if not m:
        return None
    cls, args, rest = m.group(1), m.group(2), m.group(3)
    calls = re.findall(r"\.(\w+)\(((?:[^()]|\([^()]*\))*)\)", rest)
    return cls, args, calls


def texture_path(folder, tex_expr):
    m = re.match(r'RefStrings\.MODID \+ ":(.+)"$', tex_expr.strip())
    if not m:
        return None
    return f"{folder}/{m.group(1).lower()}"


def texture_exists(tex):
    return os.path.exists(os.path.join(TEXTURES, tex + ".png"))


def java_str(s):
    return '"' + s + '"'


def existing_names(path, start_marker):
    src = read(path)
    head = src[:src.index(start_marker)] + src[src.index("// END GENERATED"):]
    return set(re.findall(r"public static final [\w.]+<[^>]+> (\w+) =", head))


# ---------------------------------------------------------------------------------------------- items

def ported_enums():
    """Enums in the port's ItemEnums: name -> constant names"""
    src = read(os.path.join(JAVA, "items", "ItemEnums.java"))
    enums = {}
    for m in re.finditer(r"enum (\w+) \{([^}]*)\}", src):
        body = re.sub(r"//[^\n]*", "", m.group(2)).split(";")[0]
        enums[m.group(1)] = [c.strip().split("(")[0] for c in body.split(",") if c.strip()]
    return enums


PORTED_ENUMS = ported_enums()


def gen_items():
    src = read(os.path.join(ORIG, "src/main/java/com/hbm/items/ModItems.java"))
    decls = {}
    for st in statements(src, "initializeItem") + statements(src, "initializeItem2"):
        m = re.match(r"(\w+) = (new .*)$", st)
        if m:
            decls[m.group(1)] = m.group(2)
    order = re.findall(r"GameRegistry\.registerItem\((\w+), \1\.getUnlocalizedName\(\)\)", src)

    port_file = os.path.join(JAVA, "items", "ModItems.java")
    manual = existing_names(port_file, "// BEGIN GENERATED")
    out, skipped, missing_tex = [], [], []

    for var in order:
        if var in manual or var not in decls:
            continue
        chain = split_chain(decls[var])
        if not chain:
            skipped.append((var, "unparsable"))
            continue
        cls, args, calls = chain
        multi = None
        if cls == "ItemEnumMulti":
            m = re.fullmatch(r"(?:ItemEnums\.)?(\w+)\.class, (true|false), (true|false)", args)
            if not m or m.group(1) not in PORTED_ENUMS:
                skipped.append((var, "ItemEnumMulti " + args))
                continue
            multi = (m.group(1), m.group(2), m.group(3))
        elif cls not in ("Item", "ItemCustomLore") or args:
            skipped.append((var, cls))
            continue

        name = tab = tex = None
        stack = None
        rarity = None
        glint = False
        ok = True
        for method, arg in calls:
            if method == "setUnlocalizedName": name = arg.strip('"')
            elif method == "setCreativeTab":
                if arg not in TABS:
                    tab = "null"  # vanilla tabs
                else:
                    tab = TABS[arg]
            elif method == "setTextureName": tex = texture_path("items", arg)
            elif method == "setMaxStackSize": stack = arg
            elif method == "setRarity": rarity = RARITY.get(arg.split(".")[-1])
            elif method == "setEffect": glint = True
            elif method in ("setFull3D", "setContainerItem"): pass  # TODO crafting remainders come with recipes
            else:
                ok = False
                skipped.append((var, "setter " + method))
                break
        if not ok:
            continue
        if cls in ("ItemCustomLore", "ItemEnumMulti") and tex is None:
            tex = "items/" + name.lower()
        if name is None or tex is None:
            skipped.append((var, "no name/texture"))
            continue
        if multi:
            values = PORTED_ENUMS[multi[0]]
            textures = [tex + "." + v.lower() for v in values] if multi[2] == "true" else [tex]
            missing = [t for t in textures if not texture_exists(t)]
            if missing:
                missing_tex.append((var, missing[0]))
                continue
        elif not texture_exists(tex):
            missing_tex.append((var, tex))
            continue

        props = "new Item.Properties()"
        if stack: props += f".stacksTo({stack})"
        if rarity and rarity != "COMMON": props += f".rarity(Rarity.{rarity})"
        if glint: props += ".component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)"
        tab = tab or "null"

        if multi:
            out.append(f'	public static final ItemEnumMulti.Variants<{multi[0]}> {var} = multi({java_str(name.lower())}, {java_str(name)}, {multi[0]}.class, {multi[1]}, {multi[2]}, {tab}, {props});')
        elif cls == "Item":
            out.append(f'\tpublic static final DeferredItem<Item> {var} = simple({java_str(name.lower())}, {tab}, {java_str(tex)}, {props});')
        else:
            out.append(f'\tpublic static final DeferredItem<ItemCustomLore> {var} = lore({java_str(name.lower())}, {java_str(name)}, {tab}, {java_str(tex)}, {props});')

    write_region(port_file, out)
    return len(out), skipped, missing_tex


# --------------------------------------------------------------------------------------------- blocks

BLOCK_CLASSES = {
    "BlockGeneric": "Block::new",
    "BlockBeaconable": "Block::new",
    "BlockHazard": "BlockHazard::new",
    "BlockOre": "BlockOre::new",
    "BlockOutgas": "BlockOutgas::new",
    "BlockFalling": "BlockFallingNT::new",
    "BlockNoSpawn": "BlockNoSpawn::new",
    "BlockDecoCT": "BlockOre::new",        # TODO connected textures
    "BlockCluster": "Block::new",          # drops come from the loot table
    "BlockPillar": "Block::new",
    "BlockRotatablePillar": "RotatedPillarBlock::new",
    "BlockNTMGlassCT": None,               # special, see below
    "BlockGenericStairs": None,            # special, see below
}

# BlockEnumMulti and its subclasses, one block per enum value (see ModBlocks.multi):
#   mat: material of classes without a material argument, enum: enum of classes that fix it,
#   factory: VariantFactory, tex: texture rule ({tex} base texture, {v} lowercase value), desc: translation key prefix,
#   top: a [texture].top texture is used for top and bottom when it exists, glass: see-through model
BLOCK_MULTI = {
    "BlockEnumMulti": {},
    "BlockEnumMultiCT": {},  # TODO connected textures
    "BlockLightstone": {"top": True},
    "BlockCM": {"tex": "{tex}_{v}"},
    "BlockCMPort": {"tex": "{tex}_{v}"},  # TODO the proxy tile entity of custom machines
    "BlockCMGlass": {"tex": "{tex}_{v}", "factory": "com.hbm.blocks.machine.BlockCMGlass::new", "glass": True},
    "BlockConcreteColoredExt": {"enum": "EnumConcreteType", "factory": "com.hbm.blocks.generic.BlockConcreteColoredExt::new"},
    "BlockMeteorOre": {"mat": "Mat.ROCK", "enum": "EnumMeteorType"},
    "BlockResourceStone": {"mat": "Mat.ROCK", "enum": "EnumStoneType"},  # TODO asbestos gas when mined
    "BlockCoke": {"mat": "Mat.IRON", "enum": "EnumCokeType", "factory": "com.hbm.blocks.generic.BlockCoke::new"},
    "BlockNTMSand": {"enum": "EnumSandType", "factory": "com.hbm.blocks.generic.BlockNTMSand::new", "tex": "blocks/sand_{v}", "desc": "block.hbm.sand_"},
}


def block_enums():
    """Enums the multi blocks can use: name -> constant names (BlockEnums, ItemEnums and the block classes' own)"""
    enums = {}
    for rel in ("blocks/BlockEnums.java", "items/ItemEnums.java", "blocks/generic/BlockConcreteColoredExt.java", "blocks/generic/BlockNTMSand.java"):
        src = read(os.path.join(JAVA, rel))
        for m in re.finditer(r"enum (\w+)\s*(?:implements [\w, ]+)?\{([^}]*)\}", src):
            body = re.sub(r"//[^\n]*", "", m.group(2)).split(";")[0]
            enums[m.group(1)] = [c.strip().split("(")[0].strip() for c in body.split(",") if c.strip()]
    return enums


BLOCK_ENUMS = block_enums()

MATERIALS = {
    "Material.rock": "Mat.ROCK", "Material.iron": "Mat.IRON", "Material.ground": "Mat.GROUND",
    "Material.sand": "Mat.SAND", "Material.wood": "Mat.WOOD", "Material.cloth": "Mat.CLOTH",
    "Material.craftedSnow": "Mat.SNOW", "Material.glass": "Mat.GLASS", "Material.packedIce": "Mat.GLASS",
}

SOUNDS = {
    "soundTypeMetal": "SoundType.METAL", "soundTypeStone": "SoundType.STONE", "soundTypeGlass": "SoundType.GLASS",
    "soundTypeGravel": "SoundType.GRAVEL", "soundTypeSand": "SoundType.SAND", "soundTypeWood": "SoundType.WOOD",
    "soundTypeCloth": "SoundType.WOOL", "soundTypeSnow": "SoundType.SNOW", "soundTypePiston": "SoundType.STONE",
    "soundTypeGrass": "SoundType.GRASS",
}


def gen_blocks():
    src = read(os.path.join(ORIG, "src/main/java/com/hbm/blocks/ModBlocks.java"))
    decls = {}
    for st in statements(src, "initializeBlock"):
        m = re.match(r"(\w+) = (new .*)$", st)
        if m:
            decls[m.group(1)] = m.group(2)
    order = []
    for m in re.finditer(r"GameRegistry\.registerBlock\((\w+),|^\s+register\((\w+)[,)]", src, re.M):
        order.append(m.group(1) or m.group(2))

    port_file = os.path.join(JAVA, "blocks", "ModBlocks.java")
    manual = existing_names(port_file, "// BEGIN GENERATED")
    out, skipped, missing_tex = [], [], []
    seen = set()
    generated = {}  # var -> texture, for stairs

    for var in order:
        if var in seen or var in manual or var not in decls:
            continue
        seen.add(var)
        chain = split_chain(decls[var])
        if not chain:
            skipped.append((var, "unparsable"))
            continue
        cls, args, calls = chain
        if cls not in BLOCK_CLASSES and cls not in BLOCK_MULTI:
            skipped.append((var, cls))
            continue

        args_list = [a.strip() for a in args.split(",")] if args else []
        name = tab = tex = sound = None
        hardness = resistance = light = None
        beacon = cls == "BlockBeaconable"
        unbreakable = no_fortune = False
        ok = True
        for method, arg in calls:
            if method == "setBlockName": name = arg.strip('"')
            elif method == "setCreativeTab": tab = TABS.get(arg, "null")
            elif method == "setBlockTextureName": tex = texture_path("blocks", arg)
            elif method == "setHardness": hardness = arg
            elif method == "setResistance": resistance = arg
            elif method == "setStepSound": sound = SOUNDS.get(arg.split(".")[-1])
            elif method == "setLightLevel": light = arg
            elif method == "makeBeaconable": beacon = True
            elif method == "noFortune": no_fortune = True
            elif method == "setBlockUnbreakable": unbreakable = True
            elif method in ("setDisplayEffect", "setLightOpacity", "noMobSpawn"): pass  # TODO particles
            else:
                ok = False
                skipped.append((var, "setter " + method))
                break
        if not ok or name is None:
            if ok: skipped.append((var, "no name"))
            continue

        # one block per enum value
        if cls in BLOCK_MULTI:
            rule = BLOCK_MULTI[cls]
            if "enum" in rule:
                enum = rule["enum"]
                mat = rule.get("mat") or (MATERIALS.get(args_list[0]) if args_list else None)
                multi_name, multi_tex = True, True
            else:
                m = re.fullmatch(r"(Material\.\w+), (?:BlockEnums\.)?(\w+)\.class, (true|false), (true|false)", args)
                if not m:
                    skipped.append((var, cls + " " + args))
                    continue
                mat, enum = MATERIALS.get(m.group(1)), m.group(2)
                multi_name, multi_tex = m.group(3) == "true", m.group(4) == "true"
            if mat is None or enum not in BLOCK_ENUMS:
                skipped.append((var, cls + " enum/material " + args))
                continue
            base = tex or "blocks/" + name.lower()
            models, missing = [], None
            for value in BLOCK_ENUMS[enum]:
                v = value.lower()
                side = rule["tex"].format(tex=base, v=v) if "tex" in rule else (f"{base}.{v}" if multi_tex else base)
                end = None
                if rule.get("top") and texture_exists(side + ".top"): end = side + ".top"
                if cls == "BlockConcreteColoredExt" and value == "MACHINE_STRIPE": end = f"{base}.machine"
                for t in (side, end):
                    if t is not None and not texture_exists(t): missing = missing or t
                if rule.get("glass"): models.append(f"BlockModel.glass({java_str(side)}, false)")
                elif end: models.append(f"BlockModel.column({java_str(side)}, {java_str(end)})")
                else: models.append(f"BlockModel.cube({java_str(side)})")
            if missing:
                missing_tex.append((var, missing))
                continue
            desc = java_str(rule["desc"]) if "desc" in rule else (java_str(f"block.hbm.{name.lower()}.") if multi_name else "null")
            factory = rule.get("factory", "(p, d, v) -> new BlockEnumMulti(p, d)")
            props = f"props({mat}, {hardness or '0.0F'}, {resistance or 'LEGACY_NONE'})"
            if sound: props += f".sound({sound})"
            if light: props += f".lightLevel(s -> (int) ({light} * 15))"
            out.append(f'\tpublic static final BlockEnumMulti.Variants<{enum}> {var} = multi({java_str(name.lower())}, {enum}.class, {desc}, {factory}, () -> {props}, {tab or "null"},\n\t\t\t' + ", ".join(models) + ");")
            continue

        # stairs: made of another generated block
        if cls == "BlockGenericStairs":
            base = args_list[0] if args_list else None
            if base not in generated or len(args_list) != 2 or args_list[1] != "0":
                skipped.append((var, "stairs of " + str(base)))
                continue
            out.append(f'\tpublic static final DeferredBlock<StairBlock> {var} = stairs({java_str(name.lower())}, {base}, {tab or "null"}, {java_str(generated[base])});')
            continue

        # material
        if cls == "BlockNTMGlassCT":
            mat = MATERIALS.get(args_list[2]) if len(args_list) > 2 else None
        elif cls == "BlockHazard" and not args_list:
            mat = "Mat.IRON"
        elif cls == "BlockFalling" and not args_list:
            mat = "Mat.SAND"
        elif args_list:
            mat = MATERIALS.get(args_list[0])
        else:
            mat = None
        if mat is None:
            skipped.append((var, "material " + args))
            continue

        # model
        if cls == "BlockNTMGlassCT":
            tex = texture_path("blocks", args_list[1])
            model_tex = [tex]
            translucent = "true" if args_list[0] == "1" else "false"
            model = f'BlockModel.glass({java_str(tex)}, {translucent})'
        elif cls in ("BlockPillar", "BlockRotatablePillar"):
            top = texture_path("blocks", args_list[1])
            if tex is None or top is None:
                skipped.append((var, "pillar textures"))
                continue
            model_tex = [tex, top]
            kind = "column" if cls == "BlockPillar" else "axis"
            model = f'BlockModel.{kind}({java_str(tex)}, {java_str(top)})'
        else:
            model_tex = [tex] if tex else []
            model = f'BlockModel.cube({java_str(tex)})' if tex else None
        if not model_tex or any(t is None for t in model_tex):
            skipped.append((var, "no texture"))
            continue
        missing = [t for t in model_tex if not texture_exists(t)]
        if missing:
            missing_tex.append((var, missing[0]))
            continue

        # factory
        extra = ""
        if cls == "BlockOre" and len(args_list) == 3:
            extra = f".setRad({args_list[1]})"  # deprecated constructor with block radiation
        if cls == "BlockOutgas" and len(args_list) > 1:
            extra = f".setOutgas({', '.join(args_list[1:])})"
        if cls == "BlockDecoCT":
            no_fortune = True
        if cls == "BlockNTMGlassCT":
            drops = "true" if len(args_list) > 3 and args_list[3] == "true" else "false"
            factory = f"p -> new BlockNTMGlass(p.noOcclusion().isViewBlocking(BlockNTMGlass::never).isSuffocating(BlockNTMGlass::never).isValidSpawn(BlockNTMGlass::never), {drops})"
            block_type = "BlockNTMGlass"
        else:
            factory = BLOCK_CLASSES[cls]
            block_type = {"Block::new": "Block"}.get(factory, factory.split("::")[0])
            if extra or no_fortune:
                nf = ".noFortune()" if no_fortune else ""
                factory = f"p -> new {factory.split('::')[0]}(p){extra}{nf}"

        hardness = hardness or "0.0F"
        res = resistance or "LEGACY_NONE"
        props = f"props({mat}, {hardness}, {res})"
        if sound: props += f".sound({sound})"
        if light: props += f".lightLevel(s -> (int) ({light} * 15))"
        if unbreakable: props += ".strength(-1.0F, 3600000.0F)"
        flags = []
        if beacon: flags.append("BEACON")
        flags_arg = ", " + ", ".join("Gen." + f for f in flags) if flags else ""
        tab_arg = tab or "null"
        out.append(f'\tpublic static final DeferredBlock<{block_type}> {var} = generated({java_str(name.lower())}, {factory}, {props}, {tab_arg}, {model}{flags_arg});')
        if model.startswith("BlockModel.cube"):
            generated[var] = tex

    write_region(port_file, out)
    return len(out), skipped, missing_tex


def write_region(path, lines):
    src = read(path)
    start = src.index("// BEGIN GENERATED")
    start = src.index("\n", start) + 1
    end = src.index("// END GENERATED")
    end = src.rindex("\n", 0, end) + 1
    src = src[:start] + "\n".join(lines) + ("\n" if lines else "") + src[end:]
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(src)


n_items, skipped_items, missing_items = gen_items()
n_blocks, skipped_blocks, missing_blocks = gen_blocks()
print(f"items: {n_items} generated, {len(skipped_items)} skipped, {len(missing_items)} missing textures")
print(f"blocks: {n_blocks} generated, {len(skipped_blocks)} skipped, {len(missing_blocks)} missing textures")
if REPORT:
    from collections import Counter
    print("skipped item reasons:", Counter(r for _, r in skipped_items).most_common(20))
    print("skipped block reasons:", Counter(r for _, r in skipped_blocks).most_common(30))
    print("missing item textures:", missing_items[:30])
    print("missing block textures:", missing_blocks[:30])
