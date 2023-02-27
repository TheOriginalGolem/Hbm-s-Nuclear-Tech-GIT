package com.hbm.calc;

import com.hbm.interfaces.IEnergyHandler;

public class UnionOfTileEntitiesAndBooleans {

	public IEnergyHandler source;
	public boolean ticked = false;
	
	public UnionOfTileEntitiesAndBooleans(IEnergyHandler tileentity, boolean bool)
	{
		source = tileentity;
		ticked = bool;
	}
}
