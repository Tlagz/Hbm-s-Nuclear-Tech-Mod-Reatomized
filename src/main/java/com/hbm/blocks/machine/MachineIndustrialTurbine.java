package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Coolable;
import com.hbm.main.ModSounds;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineIndustrialTurbine;
import com.hbm.tileentity.machine.TileEntityTurbineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Industrial steam turbine, 3x7x3; the lever on the front cycles the steam grade when the turbine is idle */
public class MachineIndustrialTurbine extends BlockDummyable implements ITooltipProvider, ILookOverlay {

	public MachineIndustrialTurbine(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineIndustrialTurbine(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).fluid().power();
		return null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {

		if(!player.isShiftKeyDown()) {

			BlockPos core = this.findCore(world, pos);
			if(core == null) return InteractionResult.SUCCESS;

			if(world.getBlockEntity(core) instanceof TileEntityTurbineBase entity) {

				Direction dir = getRotation(world.getBlockState(core));

				if(pos.equals(core.relative(dir, 3).above())) {
					if(!world.isClientSide) {
						if(!entity.operational) {
							world.playSound(null, pos, ModSounds.get("block.chungusLever"), SoundSource.BLOCKS, 1.5F, 1.0F);
							entity.onLeverPull();
						} else {
							player.sendSystemMessage(Component.literal("Cannot change compressor setting while operational!").withStyle(ChatFormatting.RED));
						}
					}
					return InteractionResult.sidedSuccess(world.isClientSide);
				}
			}
		}

		return InteractionResult.PASS;
	}

	@Override public int[] getDimensions() { return new int[] { 2, 0, 3, 3, 1, 1 }; }
	@Override public int getOffset() { return 3; }

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		BlockPos core = pos.relative(dir, o);
		Direction rot = dir.getClockWise();

		this.makeExtra(world, core.relative(dir, 3).relative(rot));
		this.makeExtra(world, core.relative(dir, 3).relative(rot, -1));
		this.makeExtra(world, core.relative(dir, -1).relative(rot));
		this.makeExtra(world, core.relative(dir, -1).relative(rot, -1));
		this.makeExtra(world, core.relative(dir, 3).above(2));
		this.makeExtra(world, core.relative(dir, -1).above(2));
		this.makeExtra(world, core.relative(dir, -3).above());
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		this.addStandardInfo(list);
	}

	private static String[] blocks = new String[] {"▖ ", "▘ ", " ▘", " ▖"}; // right hand side quarter blocks break the renderer so we cheat a little

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityMachineIndustrialTurbine chungus)) return;

		List<String> text = new ArrayList<>();

		FluidTank tankInput = chungus.tanks[0];
		FluidTank tankOutput = chungus.tanks[1];

		FluidType inputType = tankInput.getTankType();
		FluidType outputType = Fluids.NONE;

		if(inputType.hasTrait(FT_Coolable.class)) {
			outputType = inputType.getTrait(FT_Coolable.class).coolsTo;
		}

		int color = ((int) (0xFF - 0xFF * chungus.spin)) << 16 | ((int) (0xFF * chungus.spin) << 8);
		int time = (int) ((world.getGameTime() / 4) % 4);

		text.add(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET + inputType.getLocalizedName() + ": " + String.format(Locale.US, "%,d", tankInput.getFill()) + "/" + String.format(Locale.US, "%,d", tankInput.getMaxFill()) + "mB");
		text.add(ChatFormatting.RED + "<- " + ChatFormatting.RESET + outputType.getLocalizedName() + ": " + String.format(Locale.US, "%,d", tankOutput.getFill()) + "/" + String.format(Locale.US, "%,d", tankOutput.getMaxFill()) + "mB");
		text.add("&[" + color + "&]" + ChatFormatting.RED + "<- " + ChatFormatting.WHITE + BobMathUtil.getShortNumber(chungus.powerBuffer) + "HE (" +
				ChatFormatting.RESET + blocks[chungus.powerBuffer <= 0 ? 0 : time] + (int) Math.round(chungus.spin * 100) + "%" + ChatFormatting.WHITE + ")");

		ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
	}
}
