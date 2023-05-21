package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.energy.DeluxeEnergyStorage;
import com.hbm.forgefluid.FFUtils;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.IConsumer;
import com.hbm.interfaces.ISource;
import com.hbm.interfaces.ITankPacketAcceptor;
import com.hbm.interfaces.ReferenceImplementation;
import com.hbm.lib.Library;
import com.hbm.packet.FluidTankPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.TileEntityMachineBase;

import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityMachineDiesel extends TileEntityMachineBase implements ITickable, IFluidHandler, ITankPacketAcceptor {

@ReferenceImplementation("How to implement energy based machines")

	public int soundCycle = 0;
    //first and foremost create a new instance of DeluxeEnergyStorage, which is a NTM-fied version of the normal RF one
	//this fella handles 90% of TE-side energy
	public DeluxeEnergyStorage energyStorage;
	//these getters are merely for safety, DO NOT USE THE NORMAL ONES, ONLY USE THE "REAL" ONES
	public long power = energyStorage.getRealEnergy();
	public long maxPower = energyStorage.getRealMaxEnergy();
	//this is just diesel gen weirdness, remind me to throw this out the window asap
	long powerCap = maxPower;


	public FluidTank tank;
	public Fluid tankType = ModForgeFluids.diesel;
	public boolean needsUpdate;

	private static final int[] slots_top = new int[] { 0 };
	private static final int[] slots_bottom = new int[] { 1, 2 };
	private static final int[] slots_side = new int[] { 2 };

	public TileEntityMachineDiesel() {
		super(3);
		tank = new FluidTank(16000);
		energyStorage = new DeluxeEnergyStorage(50000,0, 0,0);
	}
	
	@Override
	public String getName() {
		return "container.machineDiesel";
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setLong("energy", energyStorage.energy);
		tank.writeToNBT(compound);
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		energyStorage.energy = compound.getLong("energy");
		tank.readFromNBT(compound);
		super.readFromNBT(compound);
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e) {
		int p_94128_1_ = e.ordinal();
		return p_94128_1_ == 0 ? slots_bottom : (p_94128_1_ == 1 ? slots_top : slots_side);
	}
	
	@Override
	public void update() {
		if(tank.getFluid() != null)
			tankType = tank.getFluid().getFluid();
		if (!world.isRemote) {
			if (needsUpdate) {
				needsUpdate = false;
			}

			//Tank Management
			if(this.inputValidForTank(-1, 0))
				//70k: yeah I aint sure about the quality of those FFUtils functions, if they start to misbehave yell at me
				if(FFUtils.fillFromFluidContainer(inventory, tank, 0, 1))
					needsUpdate = true;

			Fluid type = tank.getFluid() == null ? null : tank.getFluid().getFluid();

			//TODO: remove this when proper fuel system exists
			if(type != null && type == ModForgeFluids.nitan)
				powerCap = maxPower * 10;
			else
				powerCap = maxPower;
			
			// Battery Item
			power = Library.chargeItemsFromTE(inventory, 2, power, powerCap);

			generate();

			NBTTagCompound data = new NBTTagCompound();
			data.setInteger("power", (int) power);
			data.setInteger("powerCap", (int) powerCap);
			this.networkPack(data, 50);
			
			PacketDispatcher.wrapper.sendToAllAround(new FluidTankPacket(pos, tank), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 10));
		}
	}
	
	@Override
	public void networkUnpack(NBTTagCompound data) {
		power = data.getInteger("power");
		powerCap = data.getInteger("powerCap");
	}
	
	public boolean hasAcceptableFuel() {
		return getHEFromFuel() > 0;
	}

	//TODO: port 1.7 fuel system instead of this horrible misery
	public int getHEFromFuel() {
		Fluid type = tankType;
		if(type == null)
			return 0;
		if(type == ModForgeFluids.diesel)
			return 500;
		if(type == ModForgeFluids.petroil)
			return 300;
		if(type == ModForgeFluids.biofuel)
			return 400;
		if(type == ModForgeFluids.nitan)
			return 5000;
		return 0;
	}

	public void generate() {
		if (hasAcceptableFuel()) {
			if (tank.getFluidAmount() > 0) {
				if (soundCycle == 0) {
					this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_FIREWORK_BLAST, SoundCategory.BLOCKS, 1.0F, 0.5F);
				}
				soundCycle++;

				if (soundCycle >= 3)
					soundCycle = 0;

				tank.drain(10, true);
				needsUpdate = true;
				if (power + getHEFromFuel() <= powerCap) {
					power += getHEFromFuel();
				} else {
					power = powerCap;
				}
			}
		}
	}
	protected boolean inputValidForTank(int tank, int slot){
		if(!inventory.getStackInSlot(slot).isEmpty()){
            return isValidFluid(FluidUtil.getFluidContained(inventory.getStackInSlot(slot)));
		}
		return false;
	}
	
	private boolean isValidFluid(FluidStack stack) {
		if(stack == null)
			return false;
		//TODO: EWURHGUGRHUUHRHUEGHIUEGRHIBGER
		return stack.getFluid() == ModForgeFluids.diesel || stack.getFluid() == ModForgeFluids.nitan || stack.getFluid() == ModForgeFluids.petroil || stack.getFluid() == ModForgeFluids.biofuel;
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return new IFluidTankProperties[]{tank.getTankProperties()[0]};
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if (isValidFluid(resource)) {
			return tank.fill(resource, doFill);
		}
		return 0;
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public void recievePacket(NBTTagCompound[] tags) {
		if(tags.length != 1){
        } else {
			tank.readFromNBT(tags[0]);
		}
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ||  capability == CapabilityEnergy.ENERGY){
			return true;
		} else {
			return super.hasCapability(capability, facing);
		}
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
		} else if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this);
		} else if(capability == CapabilityEnergy.ENERGY){
			return CapabilityEnergy.ENERGY.cast(energyStorage);
		} else {
			return super.getCapability(capability, facing);
		}

	}

}
