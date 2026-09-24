"""
Translates the original's AssemblyMachineRecipes.registerDefaults into
com.hbm.inventory.recipes.gen.GenAssemblyMachineRecipes, using the expression translation of gen_recipes.py
and the AStack translation of gen_anvil.py. Recipes referencing anything that isn't ported yet are skipped,
the summary lists the most common blockers.

Expensive mode and 528 mode are off (their inputItemsEx/inputFluidsEx/setPools528 calls are dropped),
the fluid package loop and the Mekanism recipes are left out.

  python tools/gen_assembly.py
"""
import os, re, sys
from collections import Counter

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_recipes as g
import gen_anvil as a
from gen_recipes import Skip, split_top, match_close

SRC = os.path.join(g.ORIG, 'inventory', 'recipes', 'AssemblyMachineRecipes.java')
OUT_DIR = os.path.join(g.PORT, 'inventory', 'recipes', 'gen')
FLUIDS = os.path.join(g.PORT, 'inventory', 'fluid', 'Fluids.java')

DROPPED = {'inputItemsEx', 'inputFluidsEx', 'setPools528'}

def load_fluids():
	return set(re.findall(r'public static FluidType (\w+)', g.read(FLUIDS)))

def fluid_stack(e, fluids):
	m = re.fullmatch(r'new\s+FluidStack\s*\((.*)\)', e.strip(), re.S)
	if not m: raise Skip('fluidstack:' + e.strip()[:30])
	args = split_top(m.group(1))
	fm = re.fullmatch(r'Fluids\.(\w+)', args[0].strip())
	if not fm: raise Skip('fluid expr:' + args[0].strip()[:30])
	if fm.group(1) not in fluids: raise Skip('fluid:' + fm.group(1))
	nums = [x.strip() for x in args[1:]]
	for n in nums:
		if not re.fullmatch(r'[\d_]+', n): raise Skip('fluid amount:' + n)
	return 'new FluidStack(Fluids.%s, %s)' % (fm.group(1), ', '.join(nums))

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
			if len(args) == 1 and not args[0].strip().startswith('new ItemStack') and not args[0].strip().startswith('DictFrame'):
				out += '.setIcon(new ItemStack(%s))' % g.item_ref(args[0].strip(), 'result', sym)[1]
			elif len(args) == 1:
				out += '.setIcon(%s)' % g.translate(args[0], 'result', sym)
			else: raise Skip('setIcon args')
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
%s
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * GENERATED by tools/gen_assembly.py from the original's AssemblyMachineRecipes, do not edit.
 * %d recipes translated, %d skipped (not ported yet or not translatable).
 */
@SuppressWarnings("unused")
public class GenAssemblyMachineRecipes {

	public static void register(GenericRecipes<GenericRecipe> set) {
%s
	}
}
'''

def generate():
	sym = g.load_symbols()
	fluids = load_fluids()
	src = g.strip_comments(g.read(SRC))
	lines, skipped, local = [], Counter(), {}
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

	g.CONFIG_DEFAULTS['no528'] = True  # local copy of !GeneralConfig.enable528
	g.walk(g.method_body(src, 'registerDefaults'), emit, stats)
	skipped.update(stats['skipped'])

	body = '\n'.join(lines)
	os.makedirs(OUT_DIR, exist_ok=True)
	with open(os.path.join(OUT_DIR, 'GenAssemblyMachineRecipes.java'), 'w', encoding='utf-8', newline='\n') as f:
		f.write(HEADER % (g.enum_import_lines(sym, body), len(lines), sum(skipped.values()), body))
	print('assembly recipes %d, skipped %d' % (len(lines), sum(skipped.values())))
	blockers = Counter()
	for r, n in skipped.items():
		blockers[r if r.startswith(('item:', 'block:', 'fluid:')) else r.split(':')[0]] += n
	for r, n in blockers.most_common(40): print('  %4d  %s' % (n, r))

if __name__ == '__main__':
	generate()
