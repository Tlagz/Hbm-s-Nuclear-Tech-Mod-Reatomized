package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Coolable;
import com.hbm.main.ModSounds;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityChungus;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Leviathan steam turbine, 5 wide and 15 long: the steam goes in through the front connector on top, the spent steam
 * out through the side connectors, the power out the back. The lever on the side cycles the steam grade.
 */
public class MachineChungus extends BlockDummyable implements ITooltipProvider, ILookOverlay {

	public MachineChungus(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityChungus(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).power().fluid();
		return null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {

		if(!player.isShiftKeyDown()) {

			BlockPos core = this.findCore(world, pos);
			if(core == null) return InteractionResult.SUCCESS;

			if(world.getBlockEntity(core) instanceof TileEntityChungus entity) {

				Direction dir = getRotation(world.getBlockState(core));
				Direction turn = dir.getCounterClockWise();

				// the lever, 2 to the side and 1-2 blocks forward, the bottom two layers
				BlockPos lever1 = core.relative(dir).relative(turn, 2);
				BlockPos lever2 = core.relative(dir, 2).relative(turn, 2);
				boolean onLever = (pos.getX() == lever1.getX() || pos.getX() == lever2.getX()) && (pos.getZ() == lever1.getZ() || pos.getZ() == lever2.getZ()) && pos.getY() < core.getY() + 2;

				if(onLever) {
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

	@Override
	public int[] getDimensions() {
		return new int[] { 3, 0, 0, 3, 2, 2 };
	}

	@Override
	public int getOffset() {
		return 3;
	}

	private static final int[][] EXTRA_DIMS = new int[][] {
		new int[] { 4, -4, 0, 3, 1, 1 },
		new int[] { 3, 0, 6, -1, 1, 1 },
		new int[] { 2, 0, 10, -7, 1, 1 },
	};

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		BlockPos core = pos.relative(dir, o);
		for(int[] dim : EXTRA_DIMS) MultiblockHandlerXR.fillSpace(world, core, dim, this, dir);

		// the front steam connector sticks out on top, a dummy pointing back into the machine
		BlockPos front = pos.relative(dir).above(2);
		world.setBlock(front, this.defaultBlockState().setValue(META, dir.get3DDataValue()), Block.UPDATE_ALL);

		this.makeExtra(world, front); //front connector
		this.makeExtra(world, pos.relative(dir, o - 10)); //back connector
		Direction side = dir.getClockWise();
		this.makeExtra(world, core.relative(side, 2)); //side connectors
		this.makeExtra(world, core.relative(side, -2));
	}

	@Override
	protected boolean checkRequirement(Level world, BlockPos core, BlockPos placed, Direction dir) {
		if(!MultiblockHandlerXR.checkSpace(world, core, getDimensions(), placed, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, core, EXTRA_DIMS[1], placed, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, core, EXTRA_DIMS[2], placed, dir)) return false;
		return world.getBlockState(placed.relative(dir).above(2)).canBeReplaced();
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		this.addStandardInfo(list);
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityChungus chungus)) return;

		List<String> text = new ArrayList<>();

		FluidTank tankInput = chungus.tanks[0];
		FluidTank tankOutput = chungus.tanks[1];

		FluidType inputType = tankInput.getTankType();
		FluidType outputType = Fluids.NONE;

		if(inputType.hasTrait(FT_Coolable.class)) {
			outputType = inputType.getTrait(FT_Coolable.class).coolsTo;
		}

		text.add(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET + inputType.getLocalizedName() + ": " + String.format(Locale.US, "%,d", tankInput.getFill()) + "/" + String.format(Locale.US, "%,d", tankInput.getMaxFill()) + "mB");
		text.add(ChatFormatting.RED + "<- " + ChatFormatting.RESET + outputType.getLocalizedName() + ": " + String.format(Locale.US, "%,d", tankOutput.getFill()) + "/" + String.format(Locale.US, "%,d", tankOutput.getMaxFill()) + "mB");
		text.add(ChatFormatting.RED + "<- " + ChatFormatting.RESET + BobMathUtil.getShortNumber(chungus.powerBuffer) + "HE");

		ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
	}
}
