package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;

public class BlockNTMGlass extends BlockBreakable {

	BlockRenderLayer layer;
	
	public BlockNTMGlass(Material materialIn, BlockRenderLayer layer, String s) {
		super(materialIn, false);
		this.setUnlocalizedName(s);
		this.setRegistryName(s);
		this.layer = layer;
		
		ModBlocks.ALL_BLOCKS.add(this);
	}
	
	@Override
	public Block setSoundType(SoundType sound) {
		return super.setSoundType(sound);
	}
	
	@Override
	public int quantityDropped(IBlockState state, int fortune, Random random) {
		return 0;
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return layer;
	}
	
	@Override
	protected boolean canSilkHarvest() {
		return false;
	}

}
