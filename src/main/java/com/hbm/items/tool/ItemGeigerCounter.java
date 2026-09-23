package com.hbm.items.tool;

import java.util.ArrayList;
import java.util.List;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.main.ModSounds;
import com.hbm.util.ContaminationUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemGeigerCounter extends Item {

	public ItemGeigerCounter(Properties properties) {
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

				if(x < 1) list.add(0);
				if(x < 5) list.add(0);
				if(x < 10) list.add(1);
				if(x > 5 && x < 15) list.add(2);
				if(x > 10 && x < 20) list.add(3);
				if(x > 15 && x < 25) list.add(4);
				if(x > 20 && x < 30) list.add(5);
				if(x > 25) list.add(6);

				int r = list.get(world.random.nextInt(list.size()));

				if(r > 0)
					world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.get("item.geiger" + r), SoundSource.PLAYERS, 1.0F, 1.0F);
			} else if(world.random.nextInt(50) == 0) {
				world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.get("item.geiger1"), SoundSource.PLAYERS, 1.0F, 1.0F);
			}
		}
	}

	public static int check(Level world, BlockPos pos) {
		return (int) Math.ceil(ChunkRadiationManager.proxy.getRadiation(world, pos));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {

		if(!world.isClientSide) {
			world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.get("item.techBoop"), SoundSource.PLAYERS, 1.0F, 1.0F);
			ContaminationUtil.printGeigerData(player);
		}

		return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), world.isClientSide);
	}
}
