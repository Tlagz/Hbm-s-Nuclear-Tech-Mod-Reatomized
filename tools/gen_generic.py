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

# recipe set class in the original (inventory/recipes) -> generated class
TARGETS = ['AssemblyMachineRecipes', 'ChemicalPlantRecipes']
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
	m = re.fullmatch(r'this\.register\s*\((.*)\)', stmt.strip(), re.S)
	if not m: raise Skip('statement')
	e = m.group(1).strip()
	nm = re.match(r'new\s+GenericRecipe\s*\(\s*("(?:[^"\\]|\\.)*")\s*\)', e)
	if not nm: raise Skip('recipe name')
	out = 'set.register(new GenericRecipe(%s)' % nm.group(1)
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
			for x in args:
				if not re.fullmatch(r'[\d_]+L?', x.strip()): raise Skip('number:' + x.strip())
			out += '.%s(%s)' % (name, ', '.join(x.strip() for x in args))
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
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipes.ChanceOutput;
import com.hbm.inventory.recipes.loader.GenericRecipes.ChanceOutputMulti;
import com.hbm.items.ItemEnums.*;
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

	public static void register(GenericRecipes<GenericRecipe> set) {
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
		f.write(HEADER % (g.enum_import_lines(sym, body), name, len(lines), skipped, name, body))
	print('%-24s %4d recipes, %4d skipped' % (name, len(lines), skipped))
	return stats['skipped']

def generate():
	sym = g.load_symbols()
	fluids = load_fluids()
	g.CONFIG_DEFAULTS['no528'] = True  # local copy of !GeneralConfig.enable528
	g.CONFIG_DEFAULTS['GeneralConfig.enable528PressurizedRecipes'] = False
	skipped = Counter()
	for name in TARGETS: skipped.update(generate_set(name, sym, fluids))
	blockers = Counter()
	for r, n in skipped.items():
		blockers[r if r.startswith(('item:', 'block:', 'fluid:')) else r.split(':')[0]] += n
	print('\nmost common first blockers:')
	for r, n in blockers.most_common(30): print('  %4d  %s' % (n, r))

if __name__ == '__main__':
	generate()
