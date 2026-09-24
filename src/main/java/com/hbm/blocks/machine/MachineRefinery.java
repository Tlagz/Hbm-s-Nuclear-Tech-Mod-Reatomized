package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.ModDataComponents;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.oil.TileEntityMachineRefinery;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Oil refinery, 3x3 base, 9 tall. The four base corners are the fluid/power connections.
 *
 * TODO explosions (onBlockExploded, fire), repairing with a blowtorch (IToolable)
 */
public class MachineRefinery extends BlockDummyable implements ILookOverlay {

	public MachineRefinery(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineRefinery(pos, state);
		if(meta >= 6) return new TileEntityProxyCombo(pos, state).fluid().power().inventory();
		return null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		BlockPos core = this.findCore(world, pos);
		if(core != null && world.getBlockEntity(core) instanceof TileEntityMachineRefinery refinery && refinery.hasExploded) return InteractionResult.PASS;
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);

		BlockPos core = pos.relative(dir, o);
		this.makeExtra(world, core.offset(1, 0, 1));
		this.makeExtra(world, core.offset(1, 0, -1));
		this.makeExtra(world, core.offset(-1, 0, 1));
		this.makeExtra(world, core.offset(-1, 0, -1));
	}

	@Override
	public int[] getDimensions() {
		return new int[] {8, 0, 1, 1, 1, 1};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		CustomData persistent = stack.get(ModDataComponents.PERSISTENT.get());
		if(persistent == null) return;
		var nbt = persistent.copyTag();
		for(int i = 0; i < 5; i++) {
			FluidTank tank = new FluidTank(Fluids.NONE, 0);
			tank.readFromNBT(nbt, "" + i);
			list.add(Component.literal(tank.getFill() + "/" + tank.getMaxFill() + "mB ").append(Component.translatable(tank.getTankType().getConditionalName())).withStyle(ChatFormatting.YELLOW));
		}
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {

		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityMachineRefinery refinery)) return;

		List<String> text = new ArrayList<>();
		if(refinery.hasExploded) {
			text.add(ChatFormatting.RED + "Destroyed");
		} else {
			for(int i = 0; i < refinery.tanks.length; i++) {
				FluidTank tank = refinery.tanks[i];
				text.add((i == 0 ? ChatFormatting.GREEN + "-> " : ChatFormatting.RED + "<- ") + ChatFormatting.RESET + tank.getTankType().getLocalizedName() + ": " + String.format(Locale.US, "%,d", tank.getFill()) + " / " + String.format(Locale.US, "%,d", tank.getMaxFill()) + "mB");
			}
		}

		ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
	}
}
