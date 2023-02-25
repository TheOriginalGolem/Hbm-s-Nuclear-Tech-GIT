package com.hbm.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IFactory {
	
	boolean isStructureValid(World world);
	
	long getPowerScaled(long i);
	
	int getProgressScaled(int i);
	
	boolean isProcessable(ItemStack item);
}