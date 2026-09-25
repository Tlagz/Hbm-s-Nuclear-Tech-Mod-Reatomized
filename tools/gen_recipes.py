"""
Translates the original's crafting recipe code (CraftingManager + com.hbm.crafting.*Recipes) into Java classes
that run during datagen (com.hbm.crafting.gen), see com.hbm.crafting.RecipeBase / RecipeSink.

Statements are copied one by one, expressions translated from 1.7.10 to the port:
 - vanilla Items/Blocks renamed (metadata variants like dyes or wool colors mapped, wildcards to tags)
 - DictFrame.fromOne(ModItems.x, EnumY.Z, n) -> ModItems.x.stack(EnumY.Z, n) (ItemEnumMulti variants)
 - config conditions evaluated with their default values, loops skipped
Statements referencing anything that isn't ported (items, blocks, enums, material system...) are skipped, the
summary lists the missing symbols that block the most recipes.

  python tools/gen_recipes.py            generate
  python tools/gen_recipes.py --fixup L  comment out generated lines that failed to compile (javac log L)
"""
import os, re, sys
from collections import Counter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ORIG = os.path.join(ROOT, '..', 'src', 'Hbm-s-Nuclear-Tech-GIT-1.0.27_X5808', 'src', 'main', 'java', 'com', 'hbm')
PORT = os.path.join(ROOT, 'src', 'main', 'java', 'com', 'hbm')
NF = os.path.join(ROOT, '..', 'nf-src', 'net', 'minecraft')
OUT = os.path.join(PORT, 'crafting', 'gen')

SOURCES = [
	('CraftingManager', os.path.join(ORIG, 'main', 'CraftingManager.java'), ['AddCraftingRec', 'reg2']),
	('MineralRecipes', os.path.join(ORIG, 'crafting', 'MineralRecipes.java'), ['register']),
	('RodRecipes', os.path.join(ORIG, 'crafting', 'RodRecipes.java'), ['register']),
	('ToolRecipes', os.path.join(ORIG, 'crafting', 'ToolRecipes.java'), ['register']),
	('ArmorRecipes', os.path.join(ORIG, 'crafting', 'ArmorRecipes.java'), ['register']),
	('WeaponRecipes', os.path.join(ORIG, 'crafting', 'WeaponRecipes.java'), ['register']),
	('ConsumableRecipes', os.path.join(ORIG, 'crafting', 'ConsumableRecipes.java'), ['register']),
	('PowderRecipes', os.path.join(ORIG, 'crafting', 'PowderRecipes.java'), ['register']),
	('SmeltingRecipes', os.path.join(ORIG, 'crafting', 'SmeltingRecipes.java'), ['AddSmeltingRec']),
]

# recipe functions of RecipeBase: name -> how many leading args are item-like "results"
RECIPE_FUNCS = {
	'addRecipeAuto', 'addShapelessAuto',
	'add1To9Pair', 'add1To9', 'add9To1', 'addMineralSet', 'addBillet', 'addBilletFragment', 'addBilletToIngot',
	'addHelmet', 'addChest', 'addLegs', 'addBoots', 'addSword', 'addPickaxe', 'addAxe', 'addShovel', 'addHoe',
	'addFuelRodBillet', 'addDualFuelRodBillet', 'addQuadFuelRodBillet',
	'addRodBilletUnload', 'addDualRodBilletUnload', 'addQuadRodBilletUnload', 'addSmelting',
}

# config flags and their defaults
CONFIG_DEFAULTS = {
	'GeneralConfig.enable528': False,
	'GeneralConfig.enableLBSM': False,
	'GeneralConfig.enableLBSMSimpleCrafting': True,
	'GeneralConfig.enableBabyMode': False,
}

DICTFRAME_METHODS = {'any', 'nugget', 'tiny', 'bolt', 'ingot', 'dustTiny', 'dust', 'gem', 'crystal', 'plate', 'plateCast', 'plateWelded',
	'wireFine', 'wireDense', 'shell', 'pipe', 'billet', 'block', 'ore', 'fragment', 'lightBarrel', 'heavyBarrel', 'lightReceiver',
	'heavyReceiver', 'mechanism', 'stock', 'grip'}

COLORS = ['WHITE', 'ORANGE', 'MAGENTA', 'LIGHT_BLUE', 'YELLOW', 'LIME', 'PINK', 'GRAY', 'LIGHT_GRAY', 'CYAN', 'PURPLE', 'BLUE', 'BROWN', 'GREEN', 'RED', 'BLACK']
DYES = ['INK_SAC', 'RED_DYE', 'GREEN_DYE', 'COCOA_BEANS', 'LAPIS_LAZULI', 'PURPLE_DYE', 'CYAN_DYE', 'LIGHT_GRAY_DYE', 'GRAY_DYE', 'PINK_DYE',
	'LIME_DYE', 'YELLOW_DYE', 'LIGHT_BLUE_DYE', 'MAGENTA_DYE', 'ORANGE_DYE', 'BONE_MEAL']

# 1.7.10 name -> 1.21 Items field, or ('tag', ItemTags field) for blocks that had variants
VANILLA_RENAMES = {
	'reeds': 'SUGAR_CANE', 'speckled_melon': 'GLISTERING_MELON_SLICE', 'melon': 'MELON_SLICE', 'netherbrick': 'NETHER_BRICK',
	'fish': 'COD', 'cooked_fished': 'COOKED_COD', 'wooden_door': 'OAK_DOOR', 'boat': 'OAK_BOAT', 'sign': 'OAK_SIGN', 'bed': 'RED_BED',
	'fireworks': 'FIREWORK_ROCKET', 'firework_charge': 'FIREWORK_STAR', 'potionitem': 'POTION', 'skull': 'SKELETON_SKULL',
	'stonebrick': 'STONE_BRICKS', 'brick_block': 'BRICKS', 'nether_brick': 'NETHER_BRICKS', 'web': 'COBWEB', 'stone_slab': 'SMOOTH_STONE_SLAB',
	'lit_pumpkin': 'JACK_O_LANTERN', 'snow': 'SNOW_BLOCK', 'snow_layer': 'SNOW', 'hardened_clay': 'TERRACOTTA', 'noteblock': 'NOTE_BLOCK',
	'wooden_pressure_plate': 'OAK_PRESSURE_PLATE', 'wooden_button': 'OAK_BUTTON', 'fence': 'OAK_FENCE', 'fence_gate': 'OAK_FENCE_GATE',
	'trapdoor': 'OAK_TRAPDOOR', 'red_flower': 'POPPY', 'yellow_flower': 'DANDELION', 'melon_block': 'MELON', 'waterlily': 'LILY_PAD',
	'golden_rail': 'POWERED_RAIL', 'grass': 'GRASS_BLOCK', 'slime': 'SLIME_BLOCK', 'pumpkin': 'CARVED_PUMPKIN', 'tallgrass': 'SHORT_GRASS',
	'deadbush': 'DEAD_BUSH', 'mob_spawner': 'SPAWNER', 'quartz_ore': 'NETHER_QUARTZ_ORE', 'crafting_table': 'CRAFTING_TABLE',
	'chest_minecart': 'CHEST_MINECART', 'furnace_minecart': 'FURNACE_MINECART', 'tnt_minecart': 'TNT_MINECART', 'hopper_minecart': 'HOPPER_MINECART',
	'ender_eye': 'ENDER_EYE', 'carrot_on_a_stick': 'CARROT_ON_A_STICK', 'golden_carrot': 'GOLDEN_CARROT', 'experience_bottle': 'EXPERIENCE_BOTTLE',
	'redstone_torch': 'REDSTONE_TORCH', 'stone_button': 'STONE_BUTTON', 'light_weighted_pressure_plate': 'LIGHT_WEIGHTED_PRESSURE_PLATE',
	'heavy_weighted_pressure_plate': 'HEAVY_WEIGHTED_PRESSURE_PLATE', 'wooden_slab': ('tag', 'WOODEN_SLABS'), 'planks': ('tag', 'PLANKS'),
	'log': ('tag', 'LOGS'), 'log2': ('tag', 'LOGS'), 'leaves': ('tag', 'LEAVES'), 'leaves2': ('tag', 'LEAVES'), 'sapling': ('tag', 'SAPLINGS'),
	'wool': ('tag', 'WOOL'), 'carpet': ('tag', 'WOOL_CARPETS'), 'stained_glass': 'WHITE_STAINED_GLASS', 'stained_glass_pane': 'WHITE_STAINED_GLASS_PANE',
	'stained_hardened_clay': 'WHITE_TERRACOTTA', 'double_plant': 'SUNFLOWER', 'cobblestone_wall': 'COBBLESTONE_WALL', 'anvil': 'ANVIL',
}
# metadata variants: 1.7.10 name -> list indexed by meta (1.21 Items fields)
META_VARIANTS = {
	'dye': DYES, 'coal': ['COAL', 'CHARCOAL'], 'golden_apple': ['GOLDEN_APPLE', 'ENCHANTED_GOLDEN_APPLE'],
	'wool': [c + '_WOOL' for c in COLORS], 'carpet': [c + '_CARPET' for c in COLORS],
	'stained_glass': [c + '_STAINED_GLASS' for c in COLORS], 'stained_glass_pane': [c + '_STAINED_GLASS_PANE' for c in COLORS],
	'stained_hardened_clay': [c + '_TERRACOTTA' for c in COLORS],
	'planks': ['OAK_PLANKS', 'SPRUCE_PLANKS', 'BIRCH_PLANKS', 'JUNGLE_PLANKS', 'ACACIA_PLANKS', 'DARK_OAK_PLANKS'],
	'log': ['OAK_LOG', 'SPRUCE_LOG', 'BIRCH_LOG', 'JUNGLE_LOG'], 'sand': ['SAND', 'RED_SAND'], 'fish': ['COD', 'SALMON', 'TROPICAL_FISH', 'PUFFERFISH'],
	'stonebrick': ['STONE_BRICKS', 'MOSSY_STONE_BRICKS', 'CRACKED_STONE_BRICKS', 'CHISELED_STONE_BRICKS'], 'sandstone': ['SANDSTONE', 'CHISELED_SANDSTONE', 'SMOOTH_SANDSTONE'],
	'quartz_block': ['QUARTZ_BLOCK', 'CHISELED_QUARTZ_BLOCK', 'QUARTZ_PILLAR'], 'skull': ['SKELETON_SKULL', 'WITHER_SKELETON_SKULL', 'ZOMBIE_HEAD', 'PLAYER_HEAD', 'CREEPER_HEAD'],
}

# mod blocks whose metadata variants are separate blocks: name -> blocks indexed by meta, and the dictionary key
# that stands in for the plain block as an ingredient (1.7.10 matched a plain block with any metadata)
BLOCK_META_VARIANTS = {
	'steel_scaffold': ['steel_scaffold', 'steel_scaffold_red', 'steel_scaffold_white', 'steel_scaffold_yellow'],
}
BLOCK_ANY_KEYS = {
	'steel_scaffold': '"steelScaffolds"',
}

def read(path):
	with open(path, encoding='utf-8') as f: return f.read()

def strip_comments(s):
	out, i, n = [], 0, len(s)
	while i < n:
		c = s[i]
		if c == '"' or c == "'":
			j = i + 1
			while j < n and s[j] != c:
				j += 2 if s[j] == '\\' else 1
			out.append(s[i:j + 1]); i = j + 1
		elif s.startswith('//', i):
			j = s.find('\n', i); i = n if j < 0 else j
		elif s.startswith('/*', i):
			j = s.find('*/', i); i = n if j < 0 else j + 2
		else:
			out.append(c); i += 1
	return ''.join(out)

def match_close(s, i, open_c, close_c):
	"""index of the bracket closing the one at s[i]"""
	depth, j, n = 0, i, len(s)
	while j < n:
		c = s[j]
		if c == '"' or c == "'":
			k = j + 1
			while k < n and s[k] != c: k += 2 if s[k] == '\\' else 1
			j = k
		elif c == open_c: depth += 1
		elif c == close_c:
			depth -= 1
			if depth == 0: return j
		j += 1
	raise ValueError('unbalanced')

def split_top(s, sep=','):
	parts, depth, cur, i, n = [], 0, [], 0, len(s)
	while i < n:
		c = s[i]
		if c == '"' or c == "'":
			k = i + 1
			while k < n and s[k] != c: k += 2 if s[k] == '\\' else 1
			cur.append(s[i:k + 1]); i = k + 1; continue
		if c in '([{': depth += 1
		elif c in ')]}': depth -= 1
		if c == sep and depth == 0:
			parts.append(''.join(cur).strip()); cur = []
		else: cur.append(c)
		i += 1
	if ''.join(cur).strip(): parts.append(''.join(cur).strip())
	return parts

def method_body(src, name):
	m = re.search(r'(?:public|private|protected)?\s*(?:static\s+)?void\s+' + name + r'\s*\(\s*\)\s*\{', src)
	if not m: raise ValueError('no method ' + name)
	start = m.end() - 1
	return src[start + 1:match_close(src, start, '{', '}')]

# ---------------------------------------------------------------- symbols

def load_symbols():
	items = read(os.path.join(PORT, 'items', 'ModItems.java'))
	sym = {'items': {}, 'blocks': set(), 'frames': set(), 'keys': set(), 'enums': {}, 'mc_items': set(), 'mc_tags': set()}
	for m in re.finditer(r'DeferredItem<[^>]*>\s+(\w+)\s*=', items): sym['items'][m.group(1)] = 'item'
	for m in re.finditer(r'ItemEnumMulti\.Variants<(\w+)>\s+(\w+)\s*=', items): sym['items'][m.group(2)] = 'variants:' + m.group(1)
	for m in re.finditer(r'DeferredBlock<[^>]*>\s+(\w+)\s*=', read(os.path.join(PORT, 'blocks', 'ModBlocks.java'))): sym['blocks'].add(m.group(1))
	odm = read(os.path.join(PORT, 'inventory', 'OreDictManager.java'))
	for m in re.finditer(r'public static final Dict(?:Frame|Group) (\w+)\s*=', odm): sym['frames'].add(m.group(1))
	for m in re.finditer(r'public static final String (\w+)\s*=', odm): sym['keys'].add(m.group(1))
	enums = read(os.path.join(PORT, 'items', 'ItemEnums.java'))
	for m in re.finditer(r'enum (\w+)\s*(?:implements [\w, ]+)?\{([^}]*)\}', enums):
		body = m.group(2).split(';')[0]
		sym['enums'][m.group(1)] = set(re.findall(r'^\s*([A-Z0-9_]+)\s*(?:\(|,|$)', body, re.M))
	# enums declared in other item classes (e.g. ItemCircuit.EnumCircuitType), generated files import them
	sym['enum_imports'] = {}
	for dirpath, _, files in os.walk(os.path.join(PORT, 'items')):
		for f in files:
			if not f.endswith('.java') or f == 'ItemEnums.java': continue
			src = read(os.path.join(dirpath, f))
			pkg = re.search(r'package ([\w.]+);', src).group(1)
			for m in re.finditer(r'enum (\w+)\s*(?:implements [\w, ]+)?\{([^}]*)\}', src):
				if m.group(1) in sym['enums']: continue
				body = m.group(2).split(';')[0]
				sym['enums'][m.group(1)] = set(re.findall(r'^\s*([A-Z0-9_]+)\s*(?:\(|,|$)', body, re.M))
				sym['enum_imports'][m.group(1)] = '%s.%s.%s' % (pkg, f[:-5], m.group(1))
	# material autogen items (ModItems.wire_fine...) and the shapes each material has
	for m in re.finditer(r'AutogenItems\s+(\w+)\s*=\s*autogen\("\w+",\s*"\w+",\s*MaterialShapes\.(\w+)', items): sym['items'][m.group(1)] = 'autogen:' + m.group(2)
	sym['mat_shapes'] = {}
	for m in re.finditer(r'public static final NTMMaterial (MAT_\w+)\s*=([^;]*);', read(os.path.join(PORT, 'inventory', 'material', 'Mats.java'))):
		auto = re.search(r'\.setAutogen\(([^)]*)\)', m.group(2))
		sym['mat_shapes'][m.group(1)] = set(x.strip() for x in auto.group(1).split(',')) if auto else set()
	sym['mc_items'] = set(re.findall(r'public static final Item (\w+) =', read(os.path.join(NF, 'world', 'item', 'Items.java'))))
	sym['mc_tags'] = set(re.findall(r'public static final TagKey<Item> (\w+) =', read(os.path.join(NF, 'tags', 'ItemTags.java'))))
	return sym

# ---------------------------------------------------------------- translation

class Skip(Exception):
	def __init__(self, reason): self.reason = reason

def vanilla(name, meta, role, sym):
	"""1.7.10 Items.x / Blocks.x (+ meta) -> Java expression"""
	if meta is not None and meta != '0':
		if meta in ('OreDictionary.WILDCARD_VALUE', '32767') and name in VANILLA_RENAMES and isinstance(VANILLA_RENAMES[name], tuple):
			if role == 'result': raise Skip('vanilla wildcard result')
			return 'net.minecraft.tags.ItemTags.' + VANILLA_RENAMES[name][1]
		if name in META_VARIANTS and meta.isdigit() and int(meta) < len(META_VARIANTS[name]):
			return 'Items.' + META_VARIANTS[name][int(meta)]
		raise Skip('vanilla meta ' + name + ':' + meta)
	if name in META_VARIANTS and name not in VANILLA_RENAMES:
		field = META_VARIANTS[name][0]
		return 'Items.' + field
	r = VANILLA_RENAMES.get(name)
	if isinstance(r, tuple):
		if role == 'result': raise Skip('vanilla tag result ' + name)
		return 'net.minecraft.tags.ItemTags.' + r[1]
	field = r or name.upper()
	if field in sym['mc_items']: return 'Items.' + field
	raise Skip('vanilla ' + name)

def autogen_ref(name, kind, meta, sym):
	"""ModItems.wire_fine + Mats.MAT_COPPER.id -> ModItems.wire_fine.get(Mats.MAT_COPPER), if the material has that shape"""
	mm = re.fullmatch(r'(?:Mats\.)?(MAT_\w+)\.id', (meta or '').strip())
	if not mm: raise Skip('autogen without material:' + name)
	if kind.split(':')[1] not in sym['mat_shapes'].get(mm.group(1), ()): raise Skip('autogen shape:' + name + '/' + mm.group(1))
	return 'ModItems.%s.get(Mats.%s)' % (name, mm.group(1))

def item_ref(e, role, sym, meta=None):
	"""reference to an item or block (without count)"""
	m = re.fullmatch(r'Item\.getItemFromBlock\s*\((.*)\)', e, re.S)
	if m: e = m.group(1).strip()
	m = re.fullmatch(r'(?:com\.hbm\.items\.)?ModItems\.(\w+)', e)
	if m:
		kind = sym['items'].get(m.group(1))
		if kind is None: raise Skip('item:' + m.group(1))
		if kind.startswith('variants:'):
			enum = kind.split(':')[1]
			if meta is not None:
				mm = re.fullmatch(r'(?:ItemEnums\.)?(\w+)\.(\w+)\.ordinal\(\)', meta)
				if mm and mm.group(1) == enum and mm.group(2) in sym['enums'].get(enum, ()):
					return ('variant', 'ModItems.%s.get(%s.%s)' % (m.group(1), enum, mm.group(2)))
			raise Skip('variants without enum:' + m.group(1))
		if kind.startswith('autogen:'):
			return ('variant', autogen_ref(m.group(1), kind, meta, sym))
		if meta is not None and meta != '0': raise Skip('item meta:' + m.group(1))
		return ('item', 'ModItems.' + m.group(1))
	m = re.fullmatch(r'(?:com\.hbm\.blocks\.)?ModBlocks\.(\w+)', e)
	if m:
		if m.group(1) not in sym['blocks']: raise Skip('block:' + m.group(1))
		if m.group(1) in BLOCK_META_VARIANTS:
			if meta is None and role == 'ingredient' and m.group(1) in BLOCK_ANY_KEYS: return ('tag', BLOCK_ANY_KEYS[m.group(1)])
			variants = BLOCK_META_VARIANTS[m.group(1)]
			if meta is None: meta = '0'
			if not meta.isdigit() or int(meta) >= len(variants): raise Skip('block meta:' + m.group(1))
			return ('item', 'ModBlocks.' + variants[int(meta)])
		if meta is not None and meta != '0': raise Skip('block meta:' + m.group(1))
		return ('item', 'ModBlocks.' + m.group(1))
	m = re.fullmatch(r'(?:net\.minecraft\.init\.)?(Items|Blocks)\.(\w+)', e)
	if m:
		v = vanilla(m.group(2), meta, role, sym)
		return ('tag' if 'ItemTags' in v else 'item', v)
	raise Skip('expr:' + e[:40])

def translate(e, role, sym):
	e = e.strip()
	if re.fullmatch(r'"(?:[^"\\]|\\.)*"', e): return e
	if re.fullmatch(r'-?\d+\.\d*[Ff]?|-?\d+[Ff]', e): return e if e[-1] in 'Ff' else e + 'F'
	if re.fullmatch(r"'(?:[^'\\]|\\.)'", e): return e
	if re.fullmatch(r'-?\d+', e): return e
	m = re.fullmatch(r'new\s+String\s*\[\s*\]\s*\{(.*)\}', e, re.S)
	if m: return 'new String[] {' + ', '.join(translate(x, role, sym) for x in split_top(m.group(1))) + '}'
	m = re.fullmatch(r'new\s+ItemStack\s*\((.*)\)', e, re.S)
	if m:
		args = split_top(m.group(1))
		count = args[1] if len(args) > 1 else '1'
		if not re.fullmatch(r'\d+', count): raise Skip('count:' + count)
		kind, ref = item_ref(args[0], role, sym, args[2] if len(args) > 2 else None)
		if kind == 'tag': return ref
		if kind == 'variant': return 'stack(%s, %s)' % (ref, count)
		return 'stack(%s, %s)' % (ref, count)
	m = re.fullmatch(r'(?:OreDictManager\.)?DictFrame\.fromOne\s*\((.*)\)', e, re.S)
	if m:
		args = split_top(m.group(1))
		im = re.fullmatch(r'ModItems\.(\w+)', args[0])
		if not im: raise Skip('fromOne:' + args[0])
		kind = sym['items'].get(im.group(1))
		if kind is None: raise Skip('item:' + im.group(1))
		if not kind.startswith('variants:'): raise Skip('fromOne non-variants:' + im.group(1))
		enum = kind.split(':')[1]
		em = re.fullmatch(r'(?:ItemEnums\.)?(\w+)\.(\w+)', args[1])
		if not em or em.group(1) != enum or em.group(2) not in sym['enums'].get(enum, ()): raise Skip('enum:' + args[1])
		count = args[2] if len(args) > 2 else '1'
		if not re.fullmatch(r'\d+', count): raise Skip('count:' + count)
		return 'ModItems.%s.stack(%s.%s, %s)' % (im.group(1), enum, em.group(2), count)
	m = re.fullmatch(r'(?:OreDictManager\.)?([A-Z][A-Z0-9_]*)\.(\w+)\(\)', e)
	if m:
		if m.group(1) not in sym['frames']: raise Skip('dictframe:' + m.group(1))
		if m.group(2) not in DICTFRAME_METHODS: raise Skip('dictframe method:' + m.group(2))
		return '%s.%s()' % (m.group(1), m.group(2))
	m = re.fullmatch(r'(?:OreDictManager\.)?([A-Z][A-Z0-9_]*)', e)
	if m:
		if m.group(1) in sym['keys']: return m.group(1)
		if m.group(1) in sym['frames']: return m.group(1)  # DictFrame passed to a helper
		raise Skip('constant:' + m.group(1))
	m = re.fullmatch(r'Item\.getItemFromBlock\s*\((.*)\)', e, re.S)
	if m: return item_ref(m.group(1).strip(), role, sym)[1]
	m = re.fullmatch(r'(?:Mats\.)?(MAT_\w+)\.make\s*\((.*)\)', e, re.S)
	if m:
		args = split_top(m.group(2))
		im = re.fullmatch(r'ModItems\.(\w+)', args[0].strip())
		kind = sym['items'].get(im.group(1)) if im else None
		if not kind or not kind.startswith('autogen:'): raise Skip('make:' + args[0].strip()[:30])
		count = args[1].strip() if len(args) > 1 else '1'
		if not re.fullmatch(r'\d+', count): raise Skip('count:' + count)
		return 'stack(%s, %s)' % (autogen_ref(im.group(1), kind, m.group(1) + '.id', sym), count)
	m = re.fullmatch(r'ModItems\.(\w+)\.stackFromEnum\s*\((.*)\)', e, re.S)
	if m:
		args = split_top(m.group(2))
		count, enum_arg = (args[0], args[1]) if len(args) == 2 else ('1', args[0])
		kind = sym['items'].get(m.group(1))
		if kind is None: raise Skip('item:' + m.group(1))
		if not kind.startswith('variants:'): raise Skip('stackFromEnum non-variants:' + m.group(1))
		enum = kind.split(':')[1]
		em = re.fullmatch(r'(?:ItemEnums\.)?(\w+)\.(\w+)', enum_arg)
		if not em or em.group(1) != enum or em.group(2) not in sym['enums'].get(enum, ()) or not re.fullmatch(r'\d+', count): raise Skip('enum:' + enum_arg)
		return 'ModItems.%s.stack(%s.%s, %s)' % (m.group(1), enum, em.group(2), count)
	kind, ref = item_ref(e, role, sym)
	return ref

def translate_call(stmt, sym):
	"""one statement -> generated Java line, or Skip"""
	m = re.fullmatch(r'(?:(?:CraftingManager|GameRegistry|MineralRecipes|RodRecipes|ArmorRecipes|ToolRecipes)\.)?(\w+)\s*\((.*)\)', stmt.strip(), re.S)
	if not m: raise Skip('statement:' + stmt.strip()[:40])
	name, args = m.group(1), split_top(m.group(2))
	# GameRegistry.addRecipe(new ShapedOreRecipe(result, ...))
	if name == 'addRecipe' and len(args) == 1:
		wm = re.fullmatch(r'new\s+(ShapedOreRecipe|ShapelessOreRecipe)\s*\((.*)\)', args[0], re.S)
		if not wm: raise Skip('addRecipe:' + args[0][:30])
		name = 'addRecipeAuto' if wm.group(1) == 'ShapedOreRecipe' else 'addShapelessAuto'
		args = split_top(wm.group(2))
	elif name == 'addRecipe': name = 'addRecipeAuto'
	elif name == 'addShapelessRecipe': name = 'addShapelessAuto'
	if name not in RECIPE_FUNCS: raise Skip('function:' + name)

	flat = []
	for a in args:
		om = re.fullmatch(r'new\s+Object\s*\[\s*\]\s*\{(.*)\}', a, re.S)
		if om: flat.extend(split_top(om.group(1)))
		else: flat.append(a)
	# addSmelting(input, result, xp) has the result second
	result_index = 1 if name == 'addSmelting' else 0
	out = [translate(a, 'result' if i == result_index else 'ingredient', sym) for i, a in enumerate(flat)]
	return '%s(%s);' % (name, ', '.join(out))

def eval_condition(cond):
	c = cond.strip()
	neg = False
	while c.startswith('!'): neg = not neg; c = c[1:].strip()
	parts = [p.strip() for p in c.split('&&')]
	if len(parts) > 1 and not neg:
		vals = [eval_condition(p) for p in parts]
		return None if None in vals else all(vals)
	if c in CONFIG_DEFAULTS: return CONFIG_DEFAULTS[c] != neg
	return None

def walk(body, emit, stats):
	i, n = 0, len(body)
	while i < n:
		while i < n and body[i] in ' \t\r\n;': i += 1
		if i >= n: break
		if re.match(r'if\s*\(', body[i:]):
			p = body.index('(', i); q = match_close(body, p, '(', ')')
			cond = body[p + 1:q]
			i, then_part = take_branch(body, q + 1)
			else_part = None
			m = re.match(r'\s*else\b', body[i:])
			if m:
				i, else_part = take_branch(body, i + m.end())
			val = eval_condition(cond)
			if val is True: walk(then_part, emit, stats)
			elif val is False:
				if else_part: walk(else_part, emit, stats)
			else:
				stats['skipped'].update(['condition:' + cond.strip()[:40]])
			continue
		if re.match(r'(for|while)\s*\(', body[i:]):
			p = body.index('(', i); q = match_close(body, p, '(', ')')
			i, part = take_branch(body, q + 1)
			stats['skipped'].update(['loop'] * max(1, part.count('add')))
			continue
		if body[i] == '{':
			j = match_close(body, i, '{', '}')
			walk(body[i + 1:j], emit, stats); i = j + 1; continue
		j = statement_end(body, i)
		emit(body[i:j]); i = j + 1

def statement_end(body, i):
	"""Index of the ';' ending the plain statement at i (skipping nested brackets and string literals)"""
	j, depth, n = i, 0, len(body)
	while j < n:
		c = body[j]
		if c == '"' or c == "'":
			k = j + 1
			while k < n and body[k] != c: k += 2 if body[k] == '\\' else 1
			j = k
		elif c in '([{': depth += 1
		elif c in ')]}': depth -= 1
		elif c == ';' and depth == 0: break
		j += 1
	return j

def take_branch(body, i):
	while body[i] in ' \t\r\n': i += 1
	if body[i] == '{':
		j = match_close(body, i, '{', '}')
		return j + 1, body[i + 1:j]
	# a braceless body can itself be a control statement: for(...) if(...) { ... }
	m = re.match(r'(if|for|while)\s*\(', body[i:])
	if m:
		p = body.index('(', i); q = match_close(body, p, '(', ')')
		j, _ = take_branch(body, q + 1)
		if m.group(1) == 'if':
			e = re.match(r'\s*else\b', body[j:])
			if e: j, _ = take_branch(body, j + e.end())
		return j, body[i:j]
	j = statement_end(body, i)
	return j + 1, body[i:j + 1]

HEADER = '''package com.hbm.crafting.gen;

import static com.hbm.crafting.RecipeBase.*;
import static com.hbm.inventory.OreDictManager.*;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ItemEnums.*;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
%s
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * GENERATED by tools/gen_recipes.py from the original's %s, do not edit.
 * %d recipe calls translated, %d skipped (not ported yet or not translatable).
 */
@SuppressWarnings("unused")
public class Gen%s {

	public static void register() {
%s
	}
}
'''

def enum_import_lines(sym, body):
	"""import lines for the non-ItemEnums enums used in a generated body"""
	used = sorted(path for name, path in sym['enum_imports'].items() if re.search(r'\b%s\.' % name, body))
	return ''.join('import %s;\n' % p for p in used)

def generate():
	sym = load_symbols()
	os.makedirs(OUT, exist_ok=True)
	total_ok, all_skipped = 0, Counter()
	for name, path, methods in SOURCES:
		src = strip_comments(read(path))
		lines, stats = [], {'skipped': Counter()}
		def emit(stmt):
			s = stmt.strip()
			if not s: return
			if not re.search(r'\badd\w*\(', s):
				return  # local variables etc.
			try:
				lines.append('\t\t' + translate_call(s, sym))
			except Skip as ex:
				stats['skipped'].update([ex.reason])
		for method in methods:
			walk(method_body(src, method), emit, stats)
		skipped = sum(stats['skipped'].values())
		with open(os.path.join(OUT, 'Gen%s.java' % name), 'w', encoding='utf-8', newline='\n') as f:
			f.write(HEADER % (enum_import_lines(sym, '\n'.join(lines)), name, len(lines), skipped, name, '\n'.join(lines)))
		print('%-18s %4d generated, %4d skipped' % (name, len(lines), skipped))
		total_ok += len(lines); all_skipped.update(stats['skipped'])
	print('total: %d generated, %d skipped' % (total_ok, sum(all_skipped.values())))
	blockers = Counter()
	for reason, n in all_skipped.items():
		if reason.startswith(('item:', 'block:', 'variants', 'fromOne', 'dictframe:')): blockers[reason] += n
	print('\nmost common missing symbols:')
	for reason, n in blockers.most_common(40): print('  %4d  %s' % (n, reason))
	print('\nother skip reasons:')
	other = Counter({r.split(':')[0]: 0 for r in all_skipped})
	for r, n in all_skipped.items(): other[r.split(':')[0]] += n
	for r, n in other.most_common(): print('  %4d  %s' % (n, r))

def fixup(log):
	"""comment out generated lines javac complained about"""
	errors = {}
	for m in re.finditer(r'(\S*crafting[\\/]gen[\\/](Gen\w+\.java)):(\d+): error', read(log)):
		errors.setdefault(m.group(2), set()).add(int(m.group(3)))
	for fname, nums in errors.items():
		p = os.path.join(OUT, fname)
		lines = read(p).split('\n')
		for n in nums:
			if not lines[n - 1].lstrip().startswith('//'): lines[n - 1] = '\t\t// TODO does not compile: ' + lines[n - 1].strip()
		with open(p, 'w', encoding='utf-8', newline='\n') as f: f.write('\n'.join(lines))
		print('%s: %d lines commented out' % (fname, len(nums)))

if __name__ == '__main__':
	if len(sys.argv) > 2 and sys.argv[1] == '--fixup': fixup(sys.argv[2])
	else: generate()
