package com.hbm.energy;


import net.minecraftforge.energy.EnergyStorage;

public class DeluxeEnergyStorage extends EnergyStorage {
    public long capacity;

    public long energy;
    public int maxReceive;
    public int maxExtract = maxReceive;
	public DeluxeEnergyStorage(int capacity) {
		super(capacity);
	
	}
	 public DeluxeEnergyStorage(int capacity, int maxTransfer)
	    {
	        this(capacity, maxTransfer, maxTransfer, 0);
	    }

	    public DeluxeEnergyStorage(int capacity, int maxReceive, int maxExtract)
	    {
	        this(capacity, maxReceive, maxExtract, 0);
	    }

	    public DeluxeEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy)
	    {
	        super(capacity, maxReceive, maxExtract, 0);
	    }
	    @Override
	    public int receiveEnergy(int maxReceive, boolean simulate)
	    {
	        if (!canReceive())
	            return 0;

	        int energyReceived = (int) Math.min(getRealMaxEnergy() - getRealEnergy(), Math.min(this.maxReceive, maxReceive));
	        if (!simulate)
	            energy += energyReceived;
	        return energyReceived;
	    }
	    @Override
	    public int extractEnergy(int maxExtract, boolean simulate)
	    {
	        if (!canExtract())
	            return 0;

	        int energyExtracted = (int) Math.min(energy, Math.min(this.maxExtract, maxExtract));
	        
	        if (!simulate)
	            energy -= energyExtracted;
	        
	        return energyExtracted;
	    }
	    @Override
	    public int getEnergyStored()
	    {
	    	if ((energy > Integer.MAX_VALUE))
	        return Integer.MAX_VALUE;
	    	else
	    	return(int)energy;
	    }

	    @Override
	    public int getMaxEnergyStored()
	    {
	    	if ((capacity > Integer.MAX_VALUE))
		    return Integer.MAX_VALUE;
		    else
		    return(int)capacity;
	    }
	    public long getRealEnergy(){
	    	return energy;
	    }
	    public long getRealMaxEnergy(){
	    	return capacity;
	    }
	   

        
}
