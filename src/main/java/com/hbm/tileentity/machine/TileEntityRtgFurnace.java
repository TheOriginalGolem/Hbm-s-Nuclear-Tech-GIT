package com.hbm.tileentity.machine;

import com.hbm.blocks.machine.MachineRtgFurnace;
import com.hbm.tileentity.TileEntityMachineBase;

import com.hbm.util.RTGUtil;
import com.hbm.items.machine.ItemRTGPellet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

public class TileEntityRtgFurnace extends TileEntityMachineBase implements ITickable {

	public int dualCookTime;
	public int heat;
	public static final int processingSpeed = 3000;

	private static final int[] slots_top = new int[] {0};
	private static final int[] slots_bottom = new int[] {4};
	private static final int[] slots_side = new int[] {1, 2, 3};

	public TileEntityRtgFurnace() {
		super(5);
	}

	@Override
	public String getName() {
		return "container.rtgFurnace";
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return checkUsableByPlayer(player, 64);
	}

	public boolean isLoaded() {
		return RTGUtil.hasHeat(inventory);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		dualCookTime = compound.getShort("cookTime");
		if(compound.hasKey("inventory"))
			inventory.deserializeNBT(compound.getCompoundTag("inventory"));
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setShort("cookTime", (short) dualCookTime);
		compound.setTag("inventory", inventory.serializeNBT());
		return super.writeToNBT(compound);
	}
	
	public int getDiFurnaceProgressScaled(int i) {
		return (dualCookTime * i) / processingSpeed;
	}
	
	public boolean canProcess() {
		if(inventory.getStackInSlot(0).isEmpty())
		{
			return false;
		}
        ItemStack itemStack = FurnaceRecipes.instance().getSmeltingResult(inventory.getStackInSlot(0));
		if(itemStack == null || itemStack.isEmpty())
		{
			return false;
		}
		
		if(inventory.getStackInSlot(4).isEmpty())
		{
			return true;
		}
		
		if(!inventory.getStackInSlot(4).isItemEqual(itemStack)) {
			return false;
		}
		
		if(inventory.getStackInSlot(4).getCount() < inventory.getSlotLimit(4) && inventory.getStackInSlot(4).getCount() < inventory.getStackInSlot(4).getMaxStackSize()) {
			return true;
		}else{
			return inventory.getStackInSlot(4).getCount() < itemStack.getMaxStackSize();
		}
	}
	
	private void processItem() {
		if(canProcess()) {
	        ItemStack itemStack = FurnaceRecipes.instance().getSmeltingResult(inventory.getStackInSlot(0));
			
			if(inventory.getStackInSlot(4).isEmpty())
			{
				inventory.setStackInSlot(4, itemStack.copy());
			}else if(inventory.getStackInSlot(4).isItemEqual(itemStack)) {
				inventory.getStackInSlot(4).grow(itemStack.getCount());
			}
			
			for(int i = 0; i < 1; i++)
			{
				if(inventory.getStackInSlot(i).isEmpty())
				{
					inventory.setStackInSlot(i, new ItemStack(inventory.getStackInSlot(i).getItem()));
				}else{
					inventory.getStackInSlot(i).shrink(1);
				}
				if(inventory.getStackInSlot(i).isEmpty())
				{
					inventory.setStackInSlot(i, ItemStack.EMPTY);
				}
			}
		}
	}
	
	public boolean hasPower() {
		return isLoaded();
	}
	
	public boolean isProcessing() {
		return this.dualCookTime > 0;
	}

	@Override
	public void update() {
		boolean flag1 = false;
		
		if(!world.isRemote)
		{	
			heat = RTGUtil.updateRTGs(inventory);
			if(hasPower() && canProcess())
			{
				dualCookTime+=heat;
				
				if(this.dualCookTime >= TileEntityRtgFurnace.processingSpeed)
				{
					this.dualCookTime = 0;
					this.processItem();
					flag1 = true;
				}
			}else{
				dualCookTime = 0;
			}
			boolean trigger = true;
			
			if(hasPower() && canProcess() && this.dualCookTime == 0)
			{
				trigger = false;
			}
			
			if(trigger)
            {
                flag1 = true;
                MachineRtgFurnace.updateBlockState(this.dualCookTime > 0, this.world, pos);
            }
		}
		
		if(flag1)
		{
			this.markDirty();
		}
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e) {
		int slot = e.ordinal();
		return slot == 0 ? slots_bottom : (slot == 1 ? slots_top : slots_side);
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot == 1 || slot == 2 || slot == 3){
			return stack.getItem() instanceof ItemRTGPellet;
		}
		
		if(slot == 4)
			return false;
		
		return true;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int amount) {
		return isItemValidForSlot(slot, stack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int amount) {
		if (slot == 4)
			return true;

		if (slot == 1 || slot == 2 || slot == 3) {
			if(stack == ItemStack.EMPTY){
				return false;
			}

			if(stack.getItem() instanceof ItemRTGPellet){
				ItemRTGPellet pellet = (ItemRTGPellet) stack.getItem();
				if (RTGUtil.getPower(pellet, stack) == 0) {
					return true;
				}
			}
		}

		return false;
	}

}
