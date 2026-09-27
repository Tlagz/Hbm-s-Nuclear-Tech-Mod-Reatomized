"""
Translates the registerDefaults of the original's generic recipe sets (AssemblyMachineRecipes,
ChemicalPlantRecipes...) into com.hbm.inventory.recipes.gen.Gen<Set>, using the expression translation of
gen_recipes.py and the AStack translation of gen_anvil.py. Recipes referencing anything that isn't ported yet
are skipped, the summary lists the most common blockers.

Expensive mode and 528 mode are off (their inputItemsEx/inputFluidsEx/setPools528 calls are dropped),
loops (e.g. the assembler's fluid packages) and mod compat recipes are left out.

  python tools/gen_generic.py
"""
import os, re, sys
from collections import Counter

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_recipes as g
import gen_anvil as a
from gen_recipes import Skip, split_top, match_close

# recipe set class in the original (inventory/recipes) -> its recipe class, generated into Gen<set>
TARGETS = {'AssemblyMachineRecipes': 'GenericRecipe', 'ChemicalPlantRecipes': 'GenericRecipe', 'BlastFurnaceRecipesNT': 'GenericRecipeNoPower', 'PUREXRecipes': 'GenericRecipe', 'RockMillRecipes': 'GenericRecipe'}
# recipe subclasses that only differ in NEI display, they become plain GenericRecipes
CLASS_ALIAS = {'PUREXRecipe': 'GenericRecipe'}
OUT_DIR = os.path.join(g.PORT, 'inventory', 'recipes', 'gen')
FLUIDS = os.path.join(g.PORT, 'inventory', 'fluid', 'Fluids.java')

DROPPED = {'inputItemsEx', 'inputFluidsEx', 'setPools528'}

def load_fluids():
	return set(re.findall(r'public static FluidType (\w+)', g.read(FLUIDS)))

def resolve(e):
	"""config ternaries (GeneralConfig.enable528PressurizedRecipes ? a : b) -> the branch of the default config"""
	parts = split_top(e, '?')
	if len(parts) != 2: return e.strip()
	cond = parts[0].strip()
	branches = split_top(parts[1], ':')
	val = g.eval_condition(cond)
	if val is None or len(branches) != 2: raise Skip('condition:' + cond[:40])
	return resolve(branches[0] if val else branches[1])

def fluid_ref(e, fluids):
	fm = re.fullmatch(r'Fluids\.(\w+)', resolve(e))
	if not fm: raise Skip('fluid expr:' + e.strip()[:30])
	if fm.group(1) not in fluids: raise Skip('fluid:' + fm.group(1))
	return 'Fluids.' + fm.group(1)

def fluid_stack(e, fluids):
	m = re.fullmatch(r'new\s+FluidStack\s*\((.*)\)', e.strip(), re.S)
	if not m: raise Skip('fluidstack:' + e.strip()[:30])
	args = split_top(m.group(1))
	ref = fluid_ref(args[0], fluids)
	nums = [resolve(x) for x in args[1:]]
	if len(nums) == 2 and nums[1] == '0': nums = nums[:1]  # no pressure
	for n in nums:
		if not re.fullmatch(r'[\d_]+', n): raise Skip('fluid amount:' + n)
	return 'new FluidStack(%s, %s)' % (ref, ', '.join(nums))

def chance_output(e, sym):
	"""new ChanceOutput(stack[, chance][, weight])"""
	m = re.fullmatch(r'new\s+ChanceOutput\s*\((.*)\)', e.strip(), re.S)
	if not m: raise Skip('output:' + e.strip()[:30])
	args = split_top(m.group(1))
	rest = [x.strip() for x in args[1:]]
	for r in rest:
		if not re.fullmatch(r'[\d.]+F?', r): raise Skip('chance arg:' + r)
	return 'new ChanceOutput(%s)' % ', '.join([g.translate(args[0], 'result', sym)] + rest)

def output_item(e, sym):
	e = e.strip()
	m = re.fullmatch(r'new\s+ChanceOutputMulti\s*\((.*)\)', e, re.S)
	if m: return 'new ChanceOutputMulti(%s)' % ', '.join(chance_output(x, sym) for x in split_top(m.group(1)))
	if re.match(r'new\s+ChanceOutput\s*\(', e): return chance_output(e, sym)
	return 'new ChanceOutput(%s)' % g.translate(e, 'result', sym)

def icon(args, sym, fluids):
	"""setIcon(stack) / setIcon(item) / setIcon(item, meta), a fluid ID as meta means a fluid container stack"""
	first = args[0].strip()
	if len(args) == 1:
		if first.startswith('new ItemStack') or first.startswith('DictFrame'): return g.translate(first, 'result', sym)
		return 'new ItemStack(%s)' % g.item_ref(first, 'result', sym)[1]
	if len(args) == 2:
		fm = re.fullmatch(r'(Fluids\.\w+)\.getID\(\)', args[1].strip())
		if fm:
			kind, ref = g.item_ref(first, 'result', sym)
			return 'ItemFluidContainerBase.withFluid(%s, %s)' % (ref, fluid_ref(fm.group(1), fluids))
		kind, ref = g.item_ref(first, 'result', sym, a.enum_meta(args[1]))
		return 'new ItemStack(%s)' % ref
	raise Skip('setIcon args')

def pool(e, local):
	"""GenericRecipes.POOL_PREFIX_ALT + "plates" -> same expression, with local string variables inlined"""
	parts = []
	for p in split_top(e, '+'):
		p = p.strip()
		pm = re.fullmatch(r'(?:GenericRecipes\.)?(POOL_PREFIX_\w+)', p)
		if pm: parts.append('GenericRecipes.' + pm.group(1)); continue
		if re.fullmatch(r'"[^"]*"', p): parts.append(p); continue
		if p in local: parts.append(local[p]); continue
		raise Skip('pool:' + p[:30])
	return ' + '.join(parts)

def translate_register(stmt, sym, fluids, local):
	m = re.fullmatch(r'this\.register\s*\((?:\(\s*\w+\s*\)\s*)?(.*)\)', stmt.strip(), re.S)
	if not m: raise Skip('statement')
	e = m.group(1).strip()
	nm = re.match(r'new\s+(GenericRecipe\w*|PUREXRecipe)\s*\(\s*("(?:[^"\\]|\\.)*")\s*\)', e)
	if not nm: raise Skip('recipe name')
	cls = CLASS_ALIAS.get(nm.group(1), nm.group(1))
	# the chain returns GenericRecipe, subclasses need the cast back like in the original
	out = 'set.register(%snew %s(%s)' % ('' if cls == 'GenericRecipe' else '(%s) ' % cls, cls, nm.group(2))
	i = nm.end()
	while i < len(e):
		while i < len(e) and e[i] in ' \t\r\n': i += 1
		if i >= len(e): break
		cm = re.match(r'\.(\w+)\s*\(', e[i:])
		if not cm: raise Skip('chain syntax')
		name = cm.group(1)
		p = i + cm.end() - 1
		q = match_close(e, p, '(', ')')
		args = [x for x in split_top(e[p + 1:q]) if x.strip()]
		i = q + 1
		if name in DROPPED: continue
		if name in ('setup', 'setupNamed', 'setDuration', 'setPower'):
			# local number variables (e.g. the PUREX's power levels) are inlined
			args = [local.get(x.strip(), x.strip()) for x in args]
			for x in args:
				if not re.fullmatch(r'[\d_]+L?', x): raise Skip('number:' + x)
			out += '.%s(%s)' % (name, ', '.join(args))
		elif name in ('setNamed', 'setIconToFirstIngredient') and not args:
			out += '.%s()' % name
		elif name == 'setNameWrapper':
			out += '.setNameWrapper(%s)' % g.translate(args[0], 'result', sym)
		elif name == 'setIcon':
			out += '.setIcon(%s)' % icon(args, sym, fluids)
		elif name == 'outputItems':
			out += '.outputItems(%s)' % ', '.join(output_item(x, sym) for x in args)
		elif name == 'inputItems':
			out += '.inputItems(%s)' % ', '.join(a.astack(x, sym) for x in args)
		elif name in ('inputFluids', 'outputFluids'):
			out += '.%s(%s)' % (name, ', '.join(fluid_stack(x, fluids) for x in args))
		elif name == 'setPools':
			out += '.setPools(%s)' % ', '.join(pool(x, local) for x in args)
		elif name == 'setGroup':
			group = args[0].strip()
			if group in local: group = local[group]
			if not re.fullmatch(r'"[^"]*"', group): raise Skip('group:' + group)
			out += '.setGroup(%s, set)' % group
		else:
			raise Skip('chain:' + name)
	return out + ');'

HEADER = '''package com.hbm.inventory.recipes.gen;

import static com.hbm.crafting.RecipeBase.stack;
import static com.hbm.inventory.OreDictManager.*;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.GenericRecipeNoPower;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipes.ChanceOutput;
import com.hbm.inventory.recipes.loader.GenericRecipes.ChanceOutputMulti;
import com.hbm.items.ItemEnums.*;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidContainerBase;
%s
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * GENERATED by tools/gen_generic.py from the original's %s, do not edit.
 * %d recipes translated, %d skipped (not ported yet or not translatable).
 */
@SuppressWarnings("unused")
public class Gen%s {

	public static void register(GenericRecipes<%s> set) {
%s
	}
}
'''

def generate_set(name, sym, fluids):
	src = g.strip_comments(g.read(os.path.join(g.ORIG, 'inventory', 'recipes', name + '.java')))
	lines, local = [], {}
	stats = {'skipped': Counter()}

	def emit(stmt):
		s = stmt.strip()
		lm = re.fullmatch(r'String\s+(\w+)\s*=\s*("(?:[^"\\]|\\.)*")', s)
		if lm: local[lm.group(1)] = lm.group(2); return
		nl = re.fullmatch(r'(?:long|int)\s+(\w+)\s*=\s*([\d_]+L?)', s)
		if nl: local[nl.group(1)] = nl.group(2); return
		if not s.startswith('this.register'): return
		try:
			lines.append('\t\t' + translate_register(s, sym, fluids, local))
		except Skip as ex:
			stats['skipped'].update([ex.reason])

	g.walk(g.method_body(src, 'registerDefaults'), emit, stats)

	body = '\n'.join(lines)
	skipped = sum(stats['skipped'].values())
	os.makedirs(OUT_DIR, exist_ok=True)
	with open(os.path.join(OUT_DIR, 'Gen%s.java' % name), 'w', encoding='utf-8', newline='\n') as f:
		f.write(HEADER % (g.enum_import_lines(sym, body), name, len(lines), skipped, name, TARGETS[name], body))
	print('%-24s %4d recipes, %4d skipped' % (name, len(lines), skipped))
	return stats['skipped']

# ---------------------------------------------------------------- list based recipe classes

def translate_arc_welder(args, sym, fluids):
	"""new ArcWelderRecipe(output, duration, consumption[, fluid], inputs...)"""
	out = [g.translate(args[0], 'result', sym)]
	for n in args[1:3]:
		if not re.fullmatch(r'[\d_]+L?', n.strip()): raise Skip('number:' + n.strip())
		out.append(n.strip())
	rest = args[3:]
	if rest and rest[0].strip().startswith('new FluidStack'):
		out.append(fluid_stack(rest[0], fluids))
		rest = rest[1:]
	out += [a.astack(x, sym) for x in rest]
	return out

def translate_soldering(args, sym, fluids):
	"""new SolderingRecipe(output, duration, consumption[, fluid], toppings[], pcb[], solder[])"""
	out = [g.translate(args[0], 'result', sym)]
	for n in args[1:3]:
		if not re.fullmatch(r'[\d_]+L?', n.strip()): raise Skip('number:' + n.strip())
		out.append(n.strip())
	rest = args[3:]
	if len(rest) == 4:
		out.append(fluid_stack(rest[0], fluids))
		rest = rest[1:]
	else:
		out.append('null')
	if len(rest) != 3: raise Skip('args')
	out += [a.astack_list(x, sym) for x in rest]
	return out

# ---------------------------------------------------------------- map based recipe classes (Class.setRecipe(in, out))

def translate_in_out(args, sym, fluids):
	"""setRecipe(input, output): both concrete items (the input is a map key, not a tag)"""
	if len(args) != 2: raise Skip('args')
	return [g.translate(args[0], 'result', sym), g.translate(args[1], 'result', sym)]

# recipe set class -> (method called in registerDefaults, argument translation)
CALL_TARGETS = {
	'ShredderRecipes': ('ShredderRecipes.setRecipe', translate_in_out),
}

CALL_HEADER = '''package com.hbm.inventory.recipes.gen;

import static com.hbm.crafting.RecipeBase.stack;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.recipes.%s;
import com.hbm.items.ItemEnums.*;
import com.hbm.items.ModItems;
%s
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * GENERATED by tools/gen_generic.py from the original's %s, do not edit.
 * %d recipes translated, %d skipped (not ported yet or not translatable).
 */
@SuppressWarnings("unused")
public class Gen%s {

	public static void register() {
%s
	}
}
'''

def generate_calls(name, sym, fluids):
	method, translate_args = CALL_TARGETS[name]
	src = g.strip_comments(g.read(os.path.join(g.ORIG, 'inventory', 'recipes', name + '.java')))
	lines = []
	stats = {'skipped': Counter()}

	def emit(stmt):
		m = re.fullmatch(re.escape(method) + r'\s*\((.*)\)', stmt.strip(), re.S)
		if not m: return
		try:
			lines.append('\t\t%s(%s);' % (method, ', '.join(translate_args(split_top(m.group(1)), sym, fluids))))
		except Skip as ex:
			stats['skipped'].update([ex.reason])

	g.walk(g.method_body(src, 'registerDefaults'), emit, stats)

	body = '\n'.join(lines)
	skipped = sum(stats['skipped'].values())
	with open(os.path.join(OUT_DIR, 'Gen%s.java' % name), 'w', encoding='utf-8', newline='\n') as f:
		f.write(CALL_HEADER % (name, g.enum_import_lines(sym, body), name, len(lines), skipped, name, body))
	print('%-24s %4d recipes, %4d skipped' % (name, len(lines), skipped))
	return stats['skipped']

# recipe set class -> (recipe class, argument translation, imports)
LIST_TARGETS = {
	'ArcWelderRecipes': ('ArcWelderRecipe', translate_arc_welder, 'import com.hbm.inventory.recipes.ArcWelderRecipes.ArcWelderRecipe;'),
	'SolderingRecipes': ('SolderingRecipe', translate_soldering, 'import com.hbm.inventory.RecipesCommon.AStack;\nimport com.hbm.inventory.recipes.SolderingRecipes.SolderingRecipe;'),
}

LIST_HEADER = '''package com.hbm.inventory.recipes.gen;

import static com.hbm.crafting.RecipeBase.stack;
import static com.hbm.inventory.OreDictManager.*;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ItemEnums.*;
import com.hbm.items.ModItems;
%s
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * GENERATED by tools/gen_generic.py from the original's %s, do not edit.
 * %d recipes translated, %d skipped (not ported yet or not translatable).
 */
@SuppressWarnings("unused")
public class Gen%s {

	public static void register(List<%s> recipes) {
%s
	}
}
'''

def generate_list(name, sym, fluids):
	cls, translate_args, imports = LIST_TARGETS[name]
	src = g.strip_comments(g.read(os.path.join(g.ORIG, 'inventory', 'recipes', name + '.java')))
	lines = []
	stats = {'skipped': Counter()}

	def emit(stmt):
		m = re.fullmatch(r'recipes\.add\s*\(\s*new\s+' + cls + r'\s*\((.*)\)\s*\)', stmt.strip(), re.S)
		if not m: return
		try:
			lines.append('\t\trecipes.add(new %s(%s));' % (cls, ', '.join(translate_args(split_top(m.group(1)), sym, fluids))))
		except Skip as ex:
			stats['skipped'].update([ex.reason])

	g.walk(g.method_body(src, 'registerDefaults'), emit, stats)

	body = '\n'.join(lines)
	skipped = sum(stats['skipped'].values())
	with open(os.path.join(OUT_DIR, 'Gen%s.java' % name), 'w', encoding='utf-8', newline='\n') as f:
		f.write(LIST_HEADER % (imports + '\n' + g.enum_import_lines(sym, body), name, len(lines), skipped, name, cls, body))
	print('%-24s %4d recipes, %4d skipped' % (name, len(lines), skipped))
	return stats['skipped']

# ---------------------------------------------------------------- map based recipe classes (recipes.put(AStack, ItemStack[]))

def translate_stack_array(e, sym):
	e = a.unwrap_ternary(e.strip())
	m = re.fullmatch(r'new\s+ItemStack\s*\[\s*\]\s*\{(.*)\}', e, re.S)
	if not m: raise Skip('outputs:' + e[:40])
	return 'new ItemStack[] {' + ', '.join(g.translate(a.unwrap_ternary(x), 'result', sym) for x in split_top(m.group(1))) + '}'

# recipe set class -> (map the generated code puts into, local boolean flags of the original's registerDefaults)
MAP_TARGETS = {
	'CentrifugeRecipes': ('CentrifugeRecipes.recipes', {'lbs': False}),
}

MAP_HEADER = '''package com.hbm.inventory.recipes.gen;

import static com.hbm.crafting.RecipeBase.stack;
import static com.hbm.inventory.OreDictManager.*;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.recipes.%s;
import com.hbm.items.ItemEnums.*;
import com.hbm.items.ModItems;
%s
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * GENERATED by tools/gen_generic.py from the original's %s, do not edit.
 * %d recipes translated, %d skipped (not ported yet or not translatable).
 */
@SuppressWarnings("unused")
public class Gen%s {

	public static void register() {
%s
	}
}
'''

def generate_map(name, sym, fluids):
	target, flags = MAP_TARGETS[name]
	src = g.strip_comments(g.read(os.path.join(g.ORIG, 'inventory', 'recipes', name + '.java')))
	lines = []
	stats = {'skipped': Counter()}
	old_flags = dict(a.FLAGS)
	a.FLAGS.update(flags)

	def emit(stmt):
		m = re.fullmatch(r'recipes\.put\s*\((.*)\)', stmt.strip(), re.S)
		if not m: return
		try:
			args = split_top(m.group(1))
			if len(args) != 2: raise Skip('args')
			lines.append('\t\t%s.put(%s, %s);' % (target, a.astack(args[0], sym), translate_stack_array(args[1], sym)))
		except Skip as ex:
			stats['skipped'].update([ex.reason])

	try:
		g.walk(g.method_body(src, 'registerDefaults'), emit, stats)
	finally:
		a.FLAGS.clear()
		a.FLAGS.update(old_flags)

	body = '\n'.join(lines)
	skipped = sum(stats['skipped'].values())
	with open(os.path.join(OUT_DIR, 'Gen%s.java' % name), 'w', encoding='utf-8', newline='\n') as f:
		f.write(MAP_HEADER % (name, g.enum_import_lines(sym, body), name, len(lines), skipped, name, body))
	print('%-24s %4d recipes, %4d skipped' % (name, len(lines), skipped))
	return stats['skipped']

# ---------------------------------------------------------------- SILEX (recipes.put(key, new SILEXRecipe(...).addOut(...)), item/dict translations)

SILEX_HEADER = """package com.hbm.inventory.recipes.gen;

import static com.hbm.crafting.RecipeBase.stack;
import static com.hbm.inventory.OreDictManager.*;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.SILEXRecipes;
import com.hbm.inventory.recipes.SILEXRecipes.SILEXRecipe;
import com.hbm.items.ItemEnums.*;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.items.machine.ItemFluidContainerBase;
%s
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * GENERATED by tools/gen_generic.py from the original's SILEXRecipes, do not edit.
 * %d recipes translated, %d skipped (not ported yet or not translatable).
 */
@SuppressWarnings("unused")
public class GenSILEXRecipes {

	public static void register() {
%s
	}
}
"""

SILEX_FLUIDS = None

def silex_fluid_icon(e):
	"""new ComparableStack(ModItems.fluid_icon, 1, Fluids.X.getID()) -> the port's fluid icon stack, None if it's something else"""
	m = re.fullmatch(r'new\s+ComparableStack\s*\(\s*ModItems\.fluid_icon\s*,\s*1\s*,\s*Fluids\.(\w+)\.getID\(\)\s*\)', e.strip())
	if not m: return None
	if SILEX_FLUIDS is not None and m.group(1) not in SILEX_FLUIDS: raise Skip('fluid:' + m.group(1))
	return 'new ComparableStack(com.hbm.items.machine.ItemFluidIcon.make(Fluids.%s, 1))' % m.group(1)

def silex_stack(e, sym):
	icon = silex_fluid_icon(e)
	return icon if icon else a.astack(e, sym)

def silex_key(e, sym):
	e = e.strip()
	icon = silex_fluid_icon(e)
	if icon: return icon
	# ore dictionary keys are plain strings (e.g. U.ingot())
	if not e.startswith('new '):
		return 'new OreDictStack(%s)' % g.translate(e, 'dict', sym)
	return a.astack(e, sym)

def translate_silex_recipe(e, sym):
	e = e.strip()
	m = re.match(r'new\s+SILEXRecipe\s*\(', e)
	if not m: raise Skip('silex recipe')
	q = match_close(e, m.end() - 1, '(', ')')
	args = [x.strip() for x in split_top(e[m.end():q])]
	if len(args) != 3: raise Skip('silex args')
	for x in args[:2]:
		if not re.fullmatch(r'[\d_]+', x): raise Skip('silex number:' + x)
	strength = args[2]
	if re.fullmatch(r'\d+', strength): strength = 'EnumWavelengths.values()[%s]' % strength
	elif not re.fullmatch(r'EnumWavelengths\.\w+', strength): raise Skip('silex strength:' + strength)
	out = 'new SILEXRecipe(%s, %s, %s)' % (args[0], args[1], strength)
	i = q + 1
	while i < len(e):
		while i < len(e) and e[i] in ' \t\r\n': i += 1
		if i >= len(e): break
		cm = re.match(r'\.(\w+)\s*\(', e[i:])
		if not cm or cm.group(1) != 'addOut': raise Skip('silex chain')
		p = i + cm.end() - 1
		q = match_close(e, p, '(', ')')
		oargs = [x.strip() for x in split_top(e[p + 1:q])]
		if len(oargs) != 2 or not re.fullmatch(r'\d+', oargs[1]): raise Skip('silex out')
		out += '.addOut(%s, %s)' % (g.translate(oargs[0], 'result', sym), oargs[1])
		i = q + 1
	return out

def generate_silex(sym, fluids):
	global SILEX_FLUIDS
	SILEX_FLUIDS = fluids
	src = g.strip_comments(g.read(os.path.join(g.ORIG, 'inventory', 'recipes', 'SILEXRecipes.java')))
	lines = []
	stats = {'skipped': Counter()}

	def emit(stmt):
		s = stmt.strip()
		try:
			m = re.fullmatch(r'recipes\.put\s*\((.*)\)', s, re.S)
			if m:
				args = split_top(m.group(1))
				if len(args) != 2: raise Skip('args')
				lines.append('\t\tSILEXRecipes.put(%s, %s);' % (silex_key(args[0], sym), translate_silex_recipe(args[1], sym)))
				return
			m = re.fullmatch(r'itemTranslation\.put\s*\((.*)\)', s, re.S)
			if m:
				args = split_top(m.group(1))
				lines.append('\t\tSILEXRecipes.translateItem(%s, %s);' % (silex_stack(args[0], sym), silex_stack(args[1], sym)))
				return
			m = re.fullmatch(r'dictTranslation\.put\s*\((.*)\)', s, re.S)
			if m:
				args = split_top(m.group(1))
				lines.append('\t\tSILEXRecipes.translateDict(%s, %s);' % (g.translate(args[0], 'dict', sym), g.translate(args[1], 'dict', sym)))
		except Skip as ex:
			stats['skipped'].update([ex.reason])

	g.walk(g.method_body(src, 'register'), emit, stats)

	body = '\n'.join(lines)
	skipped = sum(stats['skipped'].values())
	with open(os.path.join(OUT_DIR, 'GenSILEXRecipes.java'), 'w', encoding='utf-8', newline='\n') as f:
		f.write(SILEX_HEADER % (g.enum_import_lines(sym, body), len(lines), skipped, body))
	print('%-24s %4d recipes, %4d skipped' % ('SILEXRecipes', len(lines), skipped))
	return stats['skipped']

# ---------------------------------------------------------------- crystallizer (registerRecipe(AStack, new CrystallizerRecipe(...).prod().setReq()[, FluidStack]))

CRYSTALLIZER_LOCALS = {'baseTime': '600', 'utilityTime': '100', 'mixingTime': '20'}
CRYSTALLIZER_FLUIDS = {'sulfur': 'new FluidStack(Fluids.SULFURIC_ACID, 500)'}

def crystallizer_output(e, sym):
	e = e.strip()
	# a bare item/block reference is a single item
	if re.fullmatch(r'(?:ModItems|ModBlocks|Items|Blocks)\.\w+', e): e = 'new ItemStack(%s)' % e
	return g.translate(e, 'result', sym)

def translate_crystallizer_recipe(e, sym):
	e = e.strip()
	m = re.match(r'new\s+CrystallizerRecipe\s*\(', e)
	if not m: raise Skip('recipe:' + e[:40])
	p = m.end() - 1
	q = match_close(e, p, '(', ')')
	args = split_top(e[p + 1:q])
	if len(args) != 2: raise Skip('recipe args')
	time = CRYSTALLIZER_LOCALS.get(args[1].strip(), args[1].strip())
	if not re.fullmatch(r'\d+', time): raise Skip('time:' + time)
	out = 'new CrystallizerRecipe(%s, %s)' % (crystallizer_output(args[0], sym), time)
	rest = e[q + 1:].strip()
	while rest:
		cm = re.match(r'\.(prod|setReq)\s*\(([^()]*)\)', rest)
		if not cm: raise Skip('chain:' + rest[:30])
		out += '.%s(%s)' % (cm.group(1), cm.group(2).strip())
		rest = rest[cm.end():].strip()
	return out

def translate_crystallizer(args, sym, fluids):
	if len(args) not in (2, 3): raise Skip('args')
	out = [a.astack(args[0], sym), translate_crystallizer_recipe(args[1], sym)]
	if len(args) == 3:
		f = args[2].strip()
		out.append(CRYSTALLIZER_FLUIDS[f] if f in CRYSTALLIZER_FLUIDS else fluid_stack(f, fluids))
	return out

CRYSTALLIZER_HEADER = '''package com.hbm.inventory.recipes.gen;

import static com.hbm.crafting.RecipeBase.stack;
import static com.hbm.inventory.OreDictManager.*;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.recipes.CrystallizerRecipes;
import com.hbm.inventory.recipes.CrystallizerRecipes.CrystallizerRecipe;
import com.hbm.items.ItemEnums.*;
import com.hbm.items.ModItems;
%s
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * GENERATED by tools/gen_generic.py from the original's CrystallizerRecipes, do not edit.
 * %d recipes translated, %d skipped (not ported yet or not translatable).
 */
@SuppressWarnings("unused")
public class GenCrystallizerRecipes {

	public static void register() {
%s
	}
}
'''

def generate_crystallizer(sym, fluids):
	src = g.strip_comments(g.read(os.path.join(g.ORIG, 'inventory', 'recipes', 'CrystallizerRecipes.java')))
	lines = []
	stats = {'skipped': Counter()}

	def emit(stmt):
		m = re.fullmatch(r'registerRecipe\s*\((.*)\)', stmt.strip(), re.S)
		if not m: return
		try:
			lines.append('\t\tCrystallizerRecipes.registerRecipe(%s);' % ', '.join(translate_crystallizer(split_top(m.group(1)), sym, fluids)))
		except Skip as ex:
			stats['skipped'].update([ex.reason])

	g.walk(g.method_body(src, 'registerDefaults'), emit, stats)

	body = '\n'.join(lines)
	skipped = sum(stats['skipped'].values())
	with open(os.path.join(OUT_DIR, 'GenCrystallizerRecipes.java'), 'w', encoding='utf-8', newline='\n') as f:
		f.write(CRYSTALLIZER_HEADER % (g.enum_import_lines(sym, body), len(lines), skipped, body))
	print('%-24s %4d recipes, %4d skipped' % ('CrystallizerRecipes', len(lines), skipped))
	return stats['skipped']

def generate():
	sym = g.load_symbols()
	fluids = load_fluids()
	g.CONFIG_DEFAULTS['no528'] = True  # local copy of !GeneralConfig.enable528
	g.CONFIG_DEFAULTS['GeneralConfig.enable528PressurizedRecipes'] = False
	a.FLAGS['lbsm'] = False  # local copy of GeneralConfig.enableLBSM && enableLBSMSimpleCrafting
	skipped = Counter()
	for name in TARGETS: skipped.update(generate_set(name, sym, fluids))
	for name in LIST_TARGETS: skipped.update(generate_list(name, sym, fluids))
	for name in CALL_TARGETS: skipped.update(generate_calls(name, sym, fluids))
	for name in MAP_TARGETS: skipped.update(generate_map(name, sym, fluids))
	skipped.update(generate_silex(sym, fluids))
	skipped.update(generate_crystallizer(sym, fluids))
	blockers = Counter()
	for r, n in skipped.items():
		blockers[r if r.startswith(('item:', 'block:', 'fluid:')) else r.split(':')[0]] += n
	print('\nmost common first blockers:')
	for r, n in blockers.most_common(30): print('  %4d  %s' % (n, r))

if __name__ == '__main__':
	generate()
