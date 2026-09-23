package com.hbm.items.tool;

import java.util.ArrayList;
import java.util.List;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.main.ModSounds;
import com.hbm.util.ContaminationUtil;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemDosimeter extends Item {

	public ItemDosimeter(Properties properties) {
		super(properties);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {

		if(!(entity instanceof LivingEntity living) || world.isClientSide)
			return;

		float x = HbmLivingProps.getRadBuf(living);

		if(world.getGameTime() % 5 == 0) {

			if(x > 1E-5) {
				List<Integer> list = new ArrayList<>();

				if(x < 0.5) list.add(0);
				if(x < 1) list.add(1);
				if(x >= 0.5 && x < 2) list.add(2);
				if(x >= 1 && x >= 2) list.add(3);

				int r = list.get(world.random.nextInt(list.size()));

				if(r > 0)
					world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.get("item.geiger" + r), SoundSource.PLAYERS, 1.0F, 1.0F);

			} else if(world.random.nextInt(100) == 0) {
				world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.get("item.geiger1"), SoundSource.PLAYERS, 1.0F, 1.0F);
			}
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {

		if(!world.isClientSide) {
			world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.get("item.techBoop"), SoundSource.PLAYERS, 1.0F, 1.0F);
			ContaminationUtil.printDosimeterData(player);
		}

		return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), world.isClientSide);
	}
}
