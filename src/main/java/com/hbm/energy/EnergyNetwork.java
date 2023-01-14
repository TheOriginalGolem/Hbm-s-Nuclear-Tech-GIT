package com.hbm.energy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;



public class EnergyNetwork implements IEnergyStorage{
	
  //Marvin's Marvelous Non Shit Energy MK II
    public final Random rand = new Random();
	protected Map<BlockPos, TileEntity> fillables = new HashMap<BlockPos, TileEntity>();
	protected Map<BlockPos, IEnergyTransmitter> cables = new HashMap<BlockPos, IEnergyTransmitter>();
	
	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		List<IEnergyStorage> storages = new ArrayList<IEnergyStorage>();
		
		Iterator<TileEntity> itr = fillables.values().iterator();
		while(itr.hasNext()){
			TileEntity te = itr.next();
			if(te.isInvalid()){
				itr.remove();
				continue;
			}
			if(te.hasCapability(CapabilityEnergy.ENERGY, null)){
				IEnergyStorage e = te.getCapability(CapabilityEnergy.ENERGY, null);
				if(e != null && e.receiveEnergy(maxReceive, false) > 0){
					storages.add(e);
				}
			}
		}
		int part = maxReceive/storages.size();
		int totalDrained = 0;
		int remaining = maxReceive;
		
		//Drillgon200: Extra hacky compensation
		int intRoundingCompensation = maxReceive-part*storages.size();
		rand.setSeed(((TileEntity)this.fillables.values().iterator().next()).getWorld().getWorldTime());
		int randomFillIndex = rand.nextInt(storages.size());
		for(int i = 0; i < storages.size(); i++){
			IEnergyStorage consumer = storages.get(i);
			int vol = consumer.receiveEnergy(randomFillIndex == i ? part + intRoundingCompensation : part, false);
			totalDrained += vol;
			remaining -= vol;
			if(remaining <= 0)
				return totalDrained;
		}
		
		return totalDrained;
	}
	
	
	public void destroy() {
		cables.values().forEach(cable -> cable.setNetwork(null));
		cables.clear();
		fillables.clear();
	}	
	
	
	public int size() {
		return fillables.size() + cables.size();
	}
	
	public void checkForRemoval(TileEntity te) {
		if(te == null)
			return;
		if(te instanceof IEnergyTransmitter) {
			cables.remove(te.getPos());
		} else if(te.hasCapability(CapabilityEnergy.ENERGY, null)) {
			fillables.remove(te.getPos());
		}
	}
	
	public boolean tryAdd(TileEntity te) {
		if(te == null)
			return false;
		if(te instanceof IEnergyTransmitter) {
			if(!cables.containsKey(te.getPos())){
				cables.put(te.getPos(), (IEnergyTransmitter) te);
				return true;
			}
		} else if(te.hasCapability(CapabilityEnergy.ENERGY, null)) {
			if(!fillables.containsKey(te.getPos())) {
				fillables.put(te.getPos(), te);
				return true;
			}
		}
		return false;
	}
	public static EnergyNetwork mergeNetworks(EnergyNetwork net1, EnergyNetwork net2) {
		if((net1 == null || net2 == null) || net1 == net2)
			return net1;

		/*net2.cables.values().forEach(cable -> {
			cable.setNetwork(net1);
			cable.setType(net1.type);
		});*/
		for(IEnergyTransmitter cable : net2.cables.values()){
			cable.setNetwork(net1);
		}

		net1.fillables.putAll(net2.fillables);
		net1.cables.putAll(net2.cables);

		net2.fillables.clear();
		net2.cables.clear();

		return net1;
		
	}
		public static EnergyNetwork buildNetwork(TileEntity te) {
			EnergyNetwork net = null;
			if(te instanceof IEnergyTransmitter) {
				IEnergyTransmitter cable = (IEnergyTransmitter) te;
				if(cable.getNetwork() != null)
					return cable.getNetwork();
			

				Map<BlockPos, IEnergyTransmitter> cables = new HashMap<BlockPos, IEnergyTransmitter>();
				Map<BlockPos, TileEntity> consumers = new HashMap<BlockPos, TileEntity>();
				List<EnergyNetwork> toMerge = new ArrayList<EnergyNetwork>();
				iteratePipes(cables, consumers, toMerge, te);

				if(toMerge.size() > 0)
					net = toMerge.remove(0);
				else
					net = new EnergyNetwork();
				
				while(toMerge.size() > 0)
					mergeNetworks(net, toMerge.remove(0));
				
				for(IEnergyTransmitter p : cables.values())
					p.setNetwork(net);
					
				net.cables.putAll(cables);
				net.fillables.putAll(consumers);
				
				
			}
			return net;
		}

		public static void iteratePipes(Map<BlockPos, IEnergyTransmitter> cables, Map<BlockPos, TileEntity> consumers, List<EnergyNetwork> networks, TileEntity te) {
			if(te == null)
				return;

			if(te instanceof IEnergyTransmitter) {
				IEnergyTransmitter cable = (IEnergyTransmitter) te;
				if(cable.isValidForBuilding()) {
					if(cable.getNetwork() == null) {
						if(!cables.containsKey(te.getPos())) {
							cables.put(te.getPos(), cable);
							for(EnumFacing e : EnumFacing.VALUES){
								BlockPos pos = te.getPos().offset(e);
								if(te.getWorld().isBlockLoaded(pos))
									iteratePipes(cables, consumers, networks, te.getWorld().getTileEntity(pos));
							}
							
						}
					} else if(!networks.contains(cable.getNetwork())) {
						networks.add(cable.getNetwork());
					}
				}
			} else if(te.hasCapability(CapabilityEnergy.ENERGY, null)) {
				if(!consumers.containsKey(te.getPos()))
					consumers.put(te.getPos(), te);
			}
		}
	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		return 0;
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
		return false;
	}
	@Override
	public boolean canReceive() {
		Iterator<TileEntity> itr = fillables.values().iterator();
		while(itr.hasNext()){
			TileEntity te = itr.next();
			if(te.hasCapability(CapabilityEnergy.ENERGY, null)){
				return true;
			}
	      }
		return false;
	}
}
