package com.hbm.tileentity.machine;

import com.hbm.blocks.machine.MachineArcFurnace;
import com.hbm.interfaces.IConsumer;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.packet.AuxElectricityPacket;
import com.hbm.packet.AuxGaugePacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.TileEntityMachineBase;

import api.hbm.energy.IBatteryItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class TileEntityMachineArcFurnace extends TileEntityMachineBase implements ITickable, IConsumer {
	
	public int dualCookTime;
	public long power;
	public static final long maxPower = 50000;
	public static final int processingSpeed = 20;
	
	//0: input
	//1: output
	//2: 1 electrod
	//3: 2 electrod
	//4: 3 electrod
	// 5: battery
	private static final int[] slots_top = new int[] { 0 };
	private static final int[] slots_bottom = new int[] { 1 };
	private static final int[] slots_side = new int[] { 2, 3, 4, 5 };

	public TileEntityMachineArcFurnace() {
		super(6);
	}
	
	@Override
	public String getName() {
		return "container.arcFurnace";
	}
	
	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return checkUsableByPlayer(player, 64);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.power = compound.getLong("powerTime");
		this.dualCookTime = compound.getInteger("cookTime");
		if(compound.hasKey("inventory"))
			inventory.deserializeNBT(compound.getCompoundTag("inventory"));
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setLong("powerTime", power);
		compound.setInteger("cookTime", dualCookTime);
		compound.setTag("inventory", inventory.serializeNBT());
		return super.writeToNBT(compound);
	}

@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e) {
		int i = e.ordinal();
		return i == 0 ? slots_bottom : (i == 1 ? slots_top : slots_side);
	}
	
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if (slot == 1) {
			return false;
		}

		if (slot == 2 || slot == 3 || slot == 4) {
			return stack.getItem() == ModItems.arc_electrode || stack.getItem() == ModItems.arc_electrode_desh;
		}

		if (slot == 5 && stack.getItem() instanceof IBatteryItem) {
			return true;
		}

		if (slot == 0) {
			return true;
		}

		return false;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack itemStack, int amount) {
		return isItemValidForSlot(slot, itemStack);
	}
	
	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
		if(slot == 1)
			return true;
		
		if(slot == 5)
			if (itemStack.getItem() instanceof IBatteryItem && ((IBatteryItem)itemStack.getItem()).getCharge(itemStack) == 0)
				return true;
		
		return false;
	}

	
	public int getDiFurnaceProgressScaled(int i) {
		return (dualCookTime * i) / processingSpeed;
	}
	
	public long getPowerRemainingScaled(long i) {
		return (power * i) / maxPower;
	}
	
	public boolean hasPower() {
		return power > 0;
	}
	
	public boolean isProcessing() {
		return this.dualCookTime > 0;
	}
	
	private boolean hasElectrodes() {
		
		if(!inventory.getStackInSlot(2).isEmpty() && !inventory.getStackInSlot(3).isEmpty() && !inventory.getStackInSlot(4).isEmpty()) {
			if((inventory.getStackInSlot(2).getItem() == ModItems.arc_electrode || inventory.getStackInSlot(2).getItem() == ModItems.arc_electrode_desh) &&
					(inventory.getStackInSlot(3).getItem() == ModItems.arc_electrode || inventory.getStackInSlot(3).getItem() == ModItems.arc_electrode_desh) &&
					(inventory.getStackInSlot(4).getItem() == ModItems.arc_electrode || inventory.getStackInSlot(4).getItem() == ModItems.arc_electrode_desh))
				return true;
		}
		
		return false;
	}
	
	public boolean canProcess() {
		
		if(!hasElectrodes())
			return false;
		
		if(inventory.getStackInSlot(0).isEmpty())
		{
			return false;
		}
        ItemStack itemStack = FurnaceRecipes.instance().getSmeltingResult(inventory.getStackInSlot(0));
        
		if(itemStack == null || itemStack.isEmpty())
		{
			return false;
		}
		
		if(inventory.getStackInSlot(1).isEmpty())
		{
			return true;
		}
		
		if(!inventory.getStackInSlot(1).isItemEqual(itemStack)) {
			return false;
		}
		
		if(inventory.getStackInSlot(1).getCount() < inventory.getSlotLimit(1) && inventory.getStackInSlot(1).getCount() < inventory.getStackInSlot(1).getMaxStackSize()) {
			return true;
		}else{
			return inventory.getStackInSlot(1).getCount() < itemStack.getMaxStackSize();
		}
	}
	
	private void processItem() {
		if(canProcess()) {
	        ItemStack itemStack = FurnaceRecipes.instance().getSmeltingResult(inventory.getStackInSlot(0));
			
			if(inventory.getStackInSlot(1).isEmpty())
			{
				inventory.setStackInSlot(1, itemStack.copy());
			}else if(inventory.getStackInSlot(1).isItemEqual(itemStack)) {
				inventory.getStackInSlot(1).grow(itemStack.getCount());
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
			
			for(int i = 2; i < 5; i++) {
				if(inventory.getStackInSlot(i).getItem() == ModItems.arc_electrode) {
					if(inventory.getStackInSlot(i).getItemDamage() < inventory.getStackInSlot(i).getMaxDamage())
						inventory.getStackInSlot(i).setItemDamage(inventory.getStackInSlot(i).getItemDamage() + 1);
					else
						inventory.setStackInSlot(i, new ItemStack(ModItems.arc_electrode_burnt));
				}
			}
		}
	}
	
	@Override
	public void update() {
		boolean flag1 = false;
		
		
		if(!world.isRemote)
		{			
			long prevPower = power;
			
			if(hasPower() && canProcess())
			{
				dualCookTime++;
				
				power -= 250;
				if(power < 0)
					power = 0;
				
				if(this.dualCookTime == processingSpeed)
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
                MachineArcFurnace.updateBlockState(this.dualCookTime > 0, this.world, pos);
            }
			
			if(MachineArcFurnace.updateBlockRods(this.hasElectrodes(), world, pos))
				flag1 = true;
			power = Library.chargeTEFromItems(inventory, 5, power, maxPower);
			
			PacketDispatcher.wrapper.sendToAllAround(new AuxElectricityPacket(pos.getX(), pos.getY(), pos.getZ(), power), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 10));
			PacketDispatcher.wrapper.sendToAllAround(new AuxGaugePacket(pos.getX(), pos.getY(), pos.getZ(), dualCookTime, 0), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 10));
			if(flag1 || prevPower != power)
			{
				this.markDirty();
			}
		}
		
	}

	@Override
	public void setPower(long i) {
		power = i;
		
	}

	@Override
	public long getPower() {
		return power;
		
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}
}
