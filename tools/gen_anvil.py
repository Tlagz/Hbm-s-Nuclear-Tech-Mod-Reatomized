"""
Translates the original's AnvilRecipes (smithing and construction recipes) into
com.hbm.inventory.recipes.anvil.gen.GenAnvilRecipes, using the expression translation of gen_recipes.py.
Recipes referencing anything that isn't ported yet are skipped, the summary lists the most common blockers.

  python tools/gen_anvil.py
"""
import os, re, sys
from collections import Counter

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_recipes as g
from gen_recipes import Skip, split_top, match_close

SRC = os.path.join(g.ORIG, 'inventory', 'recipes', 'anvil', 'AnvilRecipes.java')
OUT_DIR = os.path.join(g.PORT, 'inventory', 'recipes', 'anvil', 'gen')
METHODS = ['registerSmithing', 'registerConstruction', 'registerConstructionRecipes', 'registerConstructionStamps',
	'registerConstructionAmmo', 'registerConstructionSirens', 'registerConstructionRecycling']
FLAGS = {'exp': False}  # GeneralConfig.enableExpensiveMode

def unwrap_ternary(e):
	parts = split_top(e, '?')
	if len(parts) == 2:
		cond = parts[0].strip()
		branches = split_top(parts[1], ':')
		if cond in FLAGS and len(branches) == 2:
			return (branches[0] if FLAGS[cond] else branches[1]).strip()
		raise Skip('ternary:' + cond)
	return e

def enum_meta(meta):
	"""Enum constant as metadata -> the .ordinal() form item_ref understands"""
	if meta is None: return None
	meta = meta.strip()
	if re.fullmatch(r'(?:ItemEnums\.)?\w+\.\w+', meta) and not meta.endswith('.ordinal()'):
		return meta + '.ordinal()'
	return meta

def astack(e, sym):
	e = unwrap_ternary(e.strip())
	m = re.fullmatch(r'new\s+OreDictStack\s*\((.*)\)', e, re.S)
	if m:
		args = split_top(m.group(1))
		key = g.translate(args[0], 'ingredient', sym)
		if len(args) > 1:
			if not re.fullmatch(r'\d+', args[1]): raise Skip('count:' + args[1])
			return 'new OreDictStack(%s, %s)' % (key, args[1])
		return 'new OreDictStack(%s)' % key
	m = re.fullmatch(r'new\s+ComparableStack\s*\((.*)\)', e, re.S)
	if m:
		args = split_top(m.group(1))
		first = args[0].strip()
		if first.startswith('DictFrame.fromOne') or first.startswith('new ItemStack') or first.startswith('OreDictManager.DictFrame.fromOne'):
			if len(args) > 1: raise Skip('ComparableStack(stack, ...)')
			return 'new ComparableStack(%s)' % g.translate(first, 'ingredient', sym)
		count = args[1].strip() if len(args) > 1 else '1'
		if not re.fullmatch(r'\d+', count): raise Skip('count:' + count)
		kind, ref = g.item_ref(first, 'result', sym, enum_meta(args[2]) if len(args) > 2 else None)
		if kind == 'tag': raise Skip('ComparableStack of a tag')
		return 'new ComparableStack(%s, %s)' % (ref, count)
	raise Skip('astack:' + e[:40])

def astack_list(e, sym):
	m = re.fullmatch(r'new\s+AStack\s*\[\s*\]\s*\{(.*)\}', e.strip(), re.S)
	if m: return 'new AStack[] {' + ', '.join(astack(x, sym) for x in split_top(m.group(1))) + '}'
	return astack(e, sym)

def output(e, sym):
	m = re.fullmatch(r'new\s+AnvilOutput\s*\((.*)\)', e.strip(), re.S)
	if not m: raise Skip('output:' + e[:40])
	args = split_top(m.group(1))
	stack = g.translate(args[0], 'result', sym)
	if len(args) > 1:
		chance = args[1].strip()
		if not re.fullmatch(r'[\d.]+F?', chance): raise Skip('chance:' + chance)
		return 'new AnvilOutput(%s, %s)' % (stack, chance)
	return 'new AnvilOutput(%s)' % stack

def output_list(e, sym):
	m = re.fullmatch(r'new\s+AnvilOutput\s*\[\s*\]\s*\{(.*)\}', e.strip(), re.S)
	if m: return 'new AnvilOutput[] {' + ', '.join(output(x, sym) for x in split_top(m.group(1))) + '}'
	return output(e, sym)

def chain(rest):
	"""'.setTier(2).setOverlay(OverlayType.CONSTRUCTION)' -> validated chain"""
	out = ''
	for m in re.finditer(r'\.(\w+)\(([^()]*)\)', rest):
		name, arg = m.group(1), m.group(2).strip()
		if name == 'setTier' and re.fullmatch(r'\d+|NTMAnvil\.TIER_\w+', arg): out += '.setTier(%s)' % tier(arg)
		elif name == 'setTierRange':
			a, b = split_top(arg); out += '.setTierRange(%s, %s)' % (tier(a), tier(b))
		elif name == 'setOverlay' and re.fullmatch(r'(?:AnvilRecipes\.)?OverlayType\.\w+', arg): out += '.setOverlay(OverlayType.%s)' % arg.split('.')[-1]
		elif name == 'makeShapeless' and arg == '': out += '.makeShapeless()'
		else: raise Skip('chain:' + name)
	return out

TIERS = {'TIER_IRON': 1, 'TIER_STEEL': 2, 'TIER_OIL': 3, 'TIER_NUCLEAR': 4, 'TIER_RBMK': 5, 'TIER_FUSION': 6, 'TIER_PARTICLE': 7, 'TIER_GERALD': 8}
def tier(a):
	a = a.strip()
	if a.startswith('NTMAnvil.'): return str(TIERS[a.split('.')[1]])
	return a

def translate_add(stmt, sym):
	m = re.fullmatch(r'(constructionRecipes|smithingRecipes)\.add\s*\((.*)\)', stmt.strip(), re.S)
	if not m: raise Skip('statement')
	target, e = m.group(1), m.group(2).strip()
	cm = re.match(r'new\s+(AnvilConstructionRecipe|AnvilSmithingRecipe)\s*\(', e)
	if not cm: raise Skip('recipe class:' + e[:30])
	open_i = e.index('(', cm.start())
	close_i = match_close(e, open_i, '(', ')')
	args = split_top(e[open_i + 1:close_i])
	rest = chain(e[close_i + 1:])
	if cm.group(1) == 'AnvilConstructionRecipe':
		if len(args) != 2: raise Skip('construction args')
		return '%s.add(new AnvilConstructionRecipe(%s, %s)%s);' % (target, astack_list(args[0], sym), output_list(args[1], sym), rest)
	if len(args) != 4: raise Skip('smithing args')
	return '%s.add(new AnvilSmithingRecipe(%s, %s, %s, %s)%s);' % (target, tier(args[0]), g.translate(args[1], 'result', sym), astack(args[2], sym), astack(args[3], sym), rest)

HEADER = '''package com.hbm.inventory.recipes.anvil.gen;

import static com.hbm.crafting.RecipeBase.stack;
import static com.hbm.inventory.OreDictManager.*;
import static com.hbm.inventory.recipes.anvil.AnvilRecipes.constructionRecipes;
import static com.hbm.inventory.recipes.anvil.AnvilRecipes.smithingRecipes;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.recipes.anvil.AnvilRecipes.AnvilConstructionRecipe;
import com.hbm.inventory.recipes.anvil.AnvilRecipes.AnvilOutput;
import com.hbm.inventory.recipes.anvil.AnvilRecipes.OverlayType;
import com.hbm.inventory.recipes.anvil.AnvilSmithingRecipe;
import com.hbm.items.ItemEnums.*;
import com.hbm.items.ModItems;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * GENERATED by tools/gen_anvil.py from the original's AnvilRecipes, do not edit.
 * %d smithing and %d construction recipes translated, %d skipped (not ported yet or not translatable).
 */
@SuppressWarnings("unused")
public class GenAnvilRecipes {

	public static void registerSmithing() {
%s
	}

	public static void registerConstruction() {
%s
	}
}
'''

def generate():
	sym = g.load_symbols()
	src = g.strip_comments(g.read(SRC))
	smithing, construction, skipped = [], [], Counter()

	for method in METHODS:
		stats = {'skipped': Counter()}
		def emit(stmt):
			s = stmt.strip()
			if not re.match(r'(constructionRecipes|smithingRecipes)\.add', s): return
			try:
				line = '\t\t' + translate_add(s, sym)
				(smithing if s.startswith('smithing') else construction).append(line)
			except Skip as ex:
				stats['skipped'].update([ex.reason])
		g.walk(g.method_body(src, method), emit, stats)
		skipped.update(stats['skipped'])

	os.makedirs(OUT_DIR, exist_ok=True)
	with open(os.path.join(OUT_DIR, 'GenAnvilRecipes.java'), 'w', encoding='utf-8', newline='\n') as f:
		f.write(HEADER % (len(smithing), len(construction), sum(skipped.values()), '\n'.join(smithing), '\n'.join(construction)))
	print('smithing %d, construction %d, skipped %d' % (len(smithing), len(construction), sum(skipped.values())))
	blockers = Counter()
	for r, n in skipped.items():
		blockers[r if r.startswith(('item:', 'block:')) else r.split(':')[0]] += n
	for r, n in blockers.most_common(30): print('  %4d  %s' % (n, r))

if __name__ == '__main__':
	generate()
