package com.hbm.tileentity.base;

import com.hbm.energy.DeluxeEnergyStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import com.hbm.tileentity.TileEntityMachineBase;

public class TileEntityMachineEnergyBase extends TileEntityMachineBase implements IEnergyStorage {
	public long capacity;

	public long energy;
	public int maxReceive;
	public int maxExtract = maxReceive;


	public TileEntityMachineEnergyBase(int scount) {
		super(scount);
	}

	public TileEntityMachineEnergyBase(int scount, int slotlimit) {
		super(scount, slotlimit);
	}

	;

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		if (!canReceive())
			return 0;

		int energyReceived = (int) Math.min(getRealMaxEnergy() - getRealEnergy(), Math.min(this.maxReceive, maxReceive));
		if (!simulate)
			energy += energyReceived;
		return energyReceived;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		if (!canExtract())
			return 0;

		int energyExtracted = (int) Math.min(energy, Math.min(this.maxExtract, maxExtract));

		if (!simulate)
			energy -= energyExtracted;

		return energyExtracted;
	}

	@Override
	public int getEnergyStored() {
		//don't use this please
		if ((energy > Integer.MAX_VALUE))
			return Integer.MAX_VALUE;
		else
			return (int) energy;
	}

	@Override
	public int getMaxEnergyStored() {
		// or this, at least on stuff that uses/stores more energy than an int
		if ((capacity > Integer.MAX_VALUE))
			return Integer.MAX_VALUE;
		else
			return (int) capacity;
	}

	@Override
	public boolean canExtract() {
		return true;
	}

	public long getRealEnergy() {
		return energy;
	}

	//use these for stuff that uses more than an int can carry
	public long getRealMaxEnergy() {
		return capacity;
	}

	@Override
	public boolean canReceive() {
		return true;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityEnergy.ENERGY;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.energy = compound.getLong("energy");
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setLong("energy", energy);
		return super.writeToNBT(compound);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(this);
		} else {
			return super.getCapability(capability, facing);
		}

	}
}
