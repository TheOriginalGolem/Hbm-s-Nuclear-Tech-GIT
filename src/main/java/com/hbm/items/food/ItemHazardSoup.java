package com.hbm.items.food;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.entity.effect.EntityNukeCloudSmall;
import com.hbm.entity.logic.EntityBalefire;
import com.hbm.interfaces.IItemHazard;
import com.hbm.modules.ItemHazardModule;
import com.hbm.potion.HbmPotion;

import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.item.ItemSoup;

public class ItemHazardSoup extends ItemSoup implements IItemHazard {

	ItemHazardModule module;

	public ItemHazardSoup(int i, String s) {
		super(i);
		this.setUnlocalizedName(s);
		this.setRegistryName(s);
		this.module = new ItemHazardModule();
		
		ModItems.ALL_ITEMS.add(this);
	}
	

	@Override
	public ItemHazardModule getModule() {
		return this.module;
	}
	
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entity, int itemSlot, boolean isSelected){
		if(entity instanceof EntityLivingBase)
			this.module.applyEffects((EntityLivingBase) entity, stack.getCount(), itemSlot, isSelected, ((EntityLivingBase)entity).getHeldItem(EnumHand.MAIN_HAND) == stack ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
	}
	
	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn){
		this.module.addInformation(stack, list, flagIn);
		super.addInformation(stack, world, list, flagIn);
	}
	
	@Override
	public boolean onEntityItemUpdate(EntityItem item){
		super.onEntityItemUpdate(item);
		return super.onEntityItemUpdate(item);
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
		if(stack.getItem() == ModItems.glowing_stew){
			player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 20, 0));
			player.addPotionEffect(new PotionEffect(HbmPotion.radaway, 10 * 20, 1));
		}
		if(stack.getItem() == ModItems.balefire_scrambled){
			player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 15 * 60 * 20, 10));
			player.addPotionEffect(new PotionEffect(HbmPotion.radaway, 15 * 60 * 20, 4));

			EntityBalefire bf = new EntityBalefire(worldIn);
			bf.posX = player.posX;
			bf.posY = player.posX;
			bf.posZ = player.posZ;
			bf.destructionRange = (int) 20;
			worldIn.spawnEntity(bf);
			worldIn.spawnEntity(EntityNukeCloudSmall.statFacBale(worldIn, player.posX, player.posY, player.posZ, 20 * 1.5F, 1000));
		}
		if(stack.getItem() == ModItems.balefire_and_ham){
			player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 60 * 60 * 20, 10));
			player.addPotionEffect(new PotionEffect(HbmPotion.radaway, 60 * 60 * 20, 16));

			EntityBalefire bf = new EntityBalefire(worldIn);
			bf.posX = player.posX;
			bf.posY = player.posX;
			bf.posZ = player.posZ;
			bf.destructionRange = (int) 40;
			worldIn.spawnEntity(bf);
			worldIn.spawnEntity(EntityNukeCloudSmall.statFacBale(worldIn, player.posX, player.posY, player.posZ, 40 * 1.5F, 1000));
		}
	}
}
