package com.hbm.tileentity;

import net.minecraft.core.BlockPos;

/**
 * Cores that want some of their proxies to act differently (e.g. coolant ports only seeing the coolant tanks) hand out
 * a delegate for those positions. Returning null falls back to the core itself.
 */
public interface IProxyDelegateProvider {

	public Object getDelegateForPosition(BlockPos pos);
}
