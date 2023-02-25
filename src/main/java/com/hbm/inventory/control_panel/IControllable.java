package com.hbm.inventory.control_panel;

import java.util.Collections;
import java.util.List;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IControllable {

	default List<String> getInEvents(){return Collections.emptyList();}
	default List<String> getOutEvents(){return Collections.emptyList();}
	
	void receiveEvent(BlockPos from, ControlEvent e);
	
	BlockPos getControlPos();
	World getControlWorld();
}
