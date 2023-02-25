package com.hbm.energy;

public interface IEnergyTransmitter {
	
	public EnergyNetwork getNetwork();
	public void setNetwork(EnergyNetwork net);
	public void joinOrMakeNetwork();
	public boolean isValidForBuilding();
	
}
