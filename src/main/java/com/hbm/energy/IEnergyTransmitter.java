package com.hbm.energy;

public interface IEnergyTransmitter {
	
	EnergyNetwork getNetwork();
	void setNetwork(EnergyNetwork net);
	void joinOrMakeNetwork();
	boolean isValidForBuilding();
	
}
