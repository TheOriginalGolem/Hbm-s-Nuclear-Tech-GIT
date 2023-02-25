package com.hbm.tileentity.conductor;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import com.hbm.energy.EnergyNetwork;
import com.hbm.energy.IEnergyTransmitter;
import com.hbm.packet.PacketDispatcher;


public class TileEntityCableBase extends TileEntity implements IEnergyTransmitter, IEnergyStorage {
	public EnumFacing[] connections = new EnumFacing[6];

	protected EnergyNetwork network = null;
	public TileEntity[] tileentityCache = new TileEntity[6];
	public boolean isBeingDestroyed = false;
	@Override
	public void onLoad() {
		if(!world.isRemote){
			world.getMinecraftServer().addScheduledTask(() -> {
				joinOrMakeNetwork();
				onNeighborChange();
			});
		} else {
			joinOrMakeNetwork();
			onNeighborChange();
		}
	}
		

	public void onNeighborChange() {
		rebuildCache();
		
	}

	@Override
	public void onChunkUnload() {
		if(network == null)
			return;
		for(TileEntity te : tileentityCache) {
			if(te != null) {
				if(te instanceof IEnergyTransmitter)
					continue;
				if(!world.isBlockLoaded(te.getPos())) {
					network.checkForRemoval(te, null);
					continue;
				}
				boolean flag = true;
				for(EnumFacing e : EnumFacing.VALUES) {
					BlockPos pos = te.getPos().offset(e);
					if(world.isBlockLoaded(pos)) {
						TileEntity ent = world.getTileEntity(pos);
						if(ent instanceof IEnergyTransmitter && ((IEnergyTransmitter) ent).getNetwork() == network) {
							flag = false;
							break;
						}
					}
				}
				if(flag)
					network.checkForRemoval(te, null);
			}
		}
		network.checkForRemoval(this, null);

		// Not sure if I need to do this, but I'll be safe
		for(int i = 0; i < tileentityCache.length; i++)
			tileentityCache[i] = null;

		this.network = null;
	}

	@Override
	public void invalidate() {
		super.invalidate();
	}

	// Drillgon200: Has to be static because breakBlock doesn't get called on
	// client, and the tile entity is gone before a packet can reach it.
	public static void breakBlock(World world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityFFDuctBaseMk2) {
			((TileEntityFFDuctBaseMk2) te).isBeingDestroyed = true;
		}
		rebuildNetworks(world, pos);
	}

	public static void rebuildNetworks(World world, BlockPos pos) {
		TileEntity center = world.getTileEntity(pos);
		for(EnumFacing e : EnumFacing.VALUES) {
			TileEntity te = world.getTileEntity(pos.offset(e));
			if(te instanceof IEnergyTransmitter) {
				IEnergyTransmitter pipe = (IEnergyTransmitter) te;
				if(pipe.getNetwork() != null)
					pipe.getNetwork().destroy();
			}
		}
		if(center instanceof IEnergyTransmitter && ((IEnergyTransmitter) center).getNetwork() != null)
			((IEnergyTransmitter) center).getNetwork().destroy();

		for(EnumFacing e : EnumFacing.VALUES)
			EnergyNetwork.buildNetwork(world.getTileEntity(pos.offset(e)));
		EnergyNetwork.buildNetwork(center);
	}

	@Override
	public void joinOrMakeNetwork() {
		List<EnergyNetwork> otherNetworks = new ArrayList<EnergyNetwork>();
		for(EnumFacing e : EnumFacing.VALUES) {
			
			BlockPos offset = pos.offset(e);
			TileEntity te = world.getTileEntity(offset);
			if(te instanceof IEnergyTransmitter) {
				IEnergyTransmitter pipe = (IEnergyTransmitter) te;
				if(pipe.getNetwork() != null && !otherNetworks.contains(pipe.getNetwork())) {
					otherNetworks.add(pipe.getNetwork());
				}
				connections[e.getIndex()] = e;
			}
		}
		if(otherNetworks.isEmpty()) {
			network = new EnergyNetwork();
			network.tryAdd(this, null);
			return;
		} else {
			EnergyNetwork net = otherNetworks.remove(0);
			while(otherNetworks.size() > 0)
				net = EnergyNetwork.mergeNetworks(net, otherNetworks.remove(0));
			network = net;
			net.tryAdd(this, null);
		}
	}

	protected boolean rebuildCache() {
		boolean changed = false;
		for(EnumFacing e : EnumFacing.VALUES) {
			TileEntity te = world.getTileEntity(pos.offset(e));
			if(tileentityCache[e.getIndex()] == null) {
				if(te != null) {
					if(network != null)
					     if(network.tryAdd(te,e)){
					    	 connections[e.getIndex()] = e;
					     }
					tileentityCache[e.getIndex()] = te;
					changed = true;
				}
			} else {
				if(te == null) {
					if(network != null)
						network.checkForRemoval(tileentityCache[e.getIndex()], e);
					tileentityCache[e.getIndex()] = null;
					changed = true;
				} else if(te != tileentityCache[e.getIndex()]) {
					if(network != null) {
						network.checkForRemoval(tileentityCache[e.getIndex()], e);
						network.tryAdd(te, e);
					}
					tileentityCache[e.getIndex()] = te;
					changed = true;
				}
			}
		}
		return changed;
	}


	@Override
	public EnergyNetwork getNetwork() {
		return network;
	}

	@Override
	public void setNetwork(EnergyNetwork net) {
		network = net;
	}

	@Override
	public boolean isValidForBuilding() {
		return !isBeingDestroyed;
	}

	

	@Override
	public int receiveEnergy(int energy, boolean doFill) {
		return network != null ? network.receiveEnergy(energy, doFill) : 0;
	}

	@Override
	public int extractEnergy(int energy, boolean doDrain) {
		return network != null ? network.extractEnergy(energy, doDrain) : null;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityEnergy.ENERGY|| super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == CapabilityEnergy.ENERGY ? CapabilityEnergy.ENERGY.cast(this) : super.getCapability(capability, facing);
	}


	@Override
	public int getEnergyStored() {

		return 0;
	}


	@Override
	public int getMaxEnergyStored() {

		return 0;
	}


	@Override
	public boolean canExtract() {
		return true;
	}


	@Override
	public boolean canReceive() {
		return true;
	}
}


