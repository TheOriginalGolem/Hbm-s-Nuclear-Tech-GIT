package com.hbm.render.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public interface IHbmBlockRenderHandler {

	void renderInventoryBlock(IBlockState block, EnumFacing side, long rand);
	
	void renderWorldBlock(World world, int x, int y, int z, IBlockState block);
	
	boolean shouldClearCache(boolean inventory, EnumFacing side);
}
