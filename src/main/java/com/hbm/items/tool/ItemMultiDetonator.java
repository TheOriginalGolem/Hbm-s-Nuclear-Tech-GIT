package com.hbm.items.tool;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Level;

import com.hbm.config.GeneralConfig;
import com.hbm.interfaces.IBomb;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class ItemMultiDetonator extends Item {

	public ItemMultiDetonator(String s) {
		this.setUnlocalizedName(s);
		this.setRegistryName(s);
		this.setCreativeTab(MainRegistry.controlTab);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		list.add("Shift right-click block to add position,");
		list.add("right-click to detonate!");
		list.add("Shift right-click in the air to clear postitions.");
		
		if(stack.getTagCompound() == null || getLocations(stack) == null)
		{
			list.add("No position set");
		} else {
			
			int[][] locs = getLocations(stack);
			
			for(int i = 0; i < locs[0].length; i++) {

				list.add(locs[0][i] + " / " + locs[1][i] + " / " + locs[2][i]);
			}
		}
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		if(stack.getTagCompound() == null)
		{
			stack.setTagCompound(new NBTTagCompound());
		}
		
		if(player.isSneaking())
		{
			addLocation(stack, pos.getX(), pos.getY(), pos.getZ());
			
			if(world.isRemote)
			{
				player.sendMessage(new TextComponentTranslation("§a[Position added]§r"));
			}
			
	        world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBoop, SoundCategory.AMBIENT, 2.0F, 1.0F);
        	
			return EnumActionResult.SUCCESS;
		}
		
		return EnumActionResult.PASS;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if(stack.getTagCompound() == null || getLocations(stack) == null)
		{
			if(world.isRemote)
				player.sendMessage(new TextComponentTranslation("§c[Error]: Position not set.§r"));
			
		} else {
			
			if(!player.isSneaking()) {
				int[][] locs = getLocations(stack);
				
				int succ = 0;
				
				for (int i = 0; i < locs[0].length; i++) {
	
					int x = locs[0][i];
					int y = locs[1][i];
					int z = locs[2][i];
					BlockPos pos = new BlockPos(x, y, z);
					if (world.getBlockState(pos).getBlock() instanceof IBomb) {
						world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBleep, SoundCategory.AMBIENT, 1.0F, 1.0F);
						if (!world.isRemote) {
							((IBomb) world.getBlockState(pos).getBlock()).explode(world, pos);

				    		if(GeneralConfig.enableExtendedLogging)
				    			MainRegistry.logger.log(Level.INFO, "[DET] Tried to detonate block at " + x + " / " + y + " / " + z + " by " + player.getDisplayName() + "!");
						}
						
						succ++;
					}
				}
				
				if (world.isRemote) {
					player.sendMessage(new TextComponentTranslation("§2[Detonated] (" + succ + "/" + locs[0].length + ")§r"));
				}
			} else {

				stack.getTagCompound().setIntArray("xValues", new int[0]);
				stack.getTagCompound().setIntArray("yValues", new int[0]);
				stack.getTagCompound().setIntArray("zValues", new int[0]);
				
		        world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBoop, SoundCategory.AMBIENT, 2.0F, 1.0F);
				
				if(world.isRemote)
				{
					player.sendMessage(new TextComponentTranslation("§eAll positions removed.§r"));
				}
			}
		}
		
		return super.onItemRightClick(world, player, hand);
	}
	
	private static void addLocation(ItemStack stack, int x, int y, int z){
		if(stack.getTagCompound() == null)
		{
			stack.setTagCompound(new NBTTagCompound());
		}

		int[] xs = stack.getTagCompound().getIntArray("xValues");
		int[] ys = stack.getTagCompound().getIntArray("yValues");
		int[] zs = stack.getTagCompound().getIntArray("zValues");
		
		stack.getTagCompound().setIntArray("xValues", ArrayUtils.add(xs, x));
		stack.getTagCompound().setIntArray("yValues", ArrayUtils.add(ys, y));
		stack.getTagCompound().setIntArray("zValues", ArrayUtils.add(zs, z));
	}
	
	public static int[][] getLocations(ItemStack stack){
		if(!stack.hasTagCompound())
			return null;
		int[] xs = stack.getTagCompound().getIntArray("xValues");
		int[] ys = stack.getTagCompound().getIntArray("yValues");
		int[] zs = stack.getTagCompound().getIntArray("zValues");

		if(xs == null || ys == null || zs == null || xs.length == 0 || ys.length == 0 || zs.length == 0) {
			return null;
		}
		
		return new int[][] { xs, ys, zs };
	}
}
