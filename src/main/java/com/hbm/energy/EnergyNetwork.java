package com.hbm.energy;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.*;


public class EnergyNetwork implements IEnergyStorage {

    //Marvin's Marvelous Non Shit Energy MK II
    public final Random rand = new Random();
    protected Map<BlockPos, TileEntity> fillables = new HashMap<BlockPos, TileEntity>();
    protected Map<BlockPos, IEnergyTransmitter> cables = new HashMap<BlockPos, IEnergyTransmitter>();

    public static EnergyNetwork mergeNetworks(EnergyNetwork net1, EnergyNetwork net2) {
        if ((net1 == null || net2 == null) || net1 == net2)
            return net1;

		/*net2.cables.values().forEach(cable -> {
			cable.setNetwork(net1);
			cable.setType(net1.type);
		});*/
        for (IEnergyTransmitter cable : net2.cables.values()) {
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
        if (te instanceof IEnergyTransmitter) {
            IEnergyTransmitter cable = (IEnergyTransmitter) te;
            if (cable.getNetwork() != null)
                return cable.getNetwork();


            Map<BlockPos, IEnergyTransmitter> cables = new HashMap<BlockPos, IEnergyTransmitter>();
            Map<BlockPos, TileEntity> consumers = new HashMap<BlockPos, TileEntity>();
            List<EnergyNetwork> toMerge = new ArrayList<EnergyNetwork>();
            iteratePipes(cables, consumers, toMerge, te);

            if (toMerge.size() > 0)
                net = toMerge.remove(0);
            else
                net = new EnergyNetwork();

            while (toMerge.size() > 0)
                mergeNetworks(net, toMerge.remove(0));

            for (IEnergyTransmitter p : cables.values())
                p.setNetwork(net);

            net.cables.putAll(cables);
            net.fillables.putAll(consumers);


        }
        return net;
    }

    public static void iteratePipes(Map<BlockPos, IEnergyTransmitter> cables, Map<BlockPos, TileEntity> consumers, List<EnergyNetwork> networks, TileEntity te) {
        //Credits to MartinTheDragon for this entire part of the code
        cables.clear();
        List<BlockPos> toIterate = new ArrayList<>(); // i'll still have to test if Drill was right or not...
        toIterate.add(te.getPos());
        World world = te.getWorld();

        while (!toIterate.isEmpty()) {
            BlockPos nextPos = toIterate.remove(toIterate.size() - 1);
            if (cables.containsKey(nextPos)) continue;

            TileEntity nextTileEntity = world.getTileEntity(nextPos);
            if (nextTileEntity instanceof IEnergyTransmitter) {
                IEnergyTransmitter cable = (IEnergyTransmitter) nextTileEntity; // pre Java 14, aeugh
                if (!cable.isValidForBuilding()) continue;

                EnergyNetwork foundNetwork = cable.getNetwork(); // or whatever the network class is
                if (foundNetwork == null) {
                    cables.put(nextPos, cable);
                    for (EnumFacing direction : EnumFacing.VALUES)
                        toIterate.add(nextPos.offset(direction)); // i'm not sure whether you should actually bother checking if the position is loaded, large cables may not work
                } else if (!networks.contains(foundNetwork)) {
                    networks.add(foundNetwork);
                }
            } else if (nextTileEntity.hasCapability(CapabilityEnergy.ENERGY, null)) // for the love of god pass a side here
                if (!consumers.containsKey(nextPos))
                    consumers.put(nextPos, nextTileEntity); // i would just store the capability directly to be honest (which you should be doing by API note)
        }
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (maxReceive >= 0) {
            return maxReceive;
        }
        List<IEnergyStorage> storages = new ArrayList<IEnergyStorage>();

        Iterator<TileEntity> itr = fillables.values().iterator();
        while (itr.hasNext()) {
            TileEntity te = itr.next();
            if (te.isInvalid()) {
                itr.remove();
                continue;
            }
            if (te.hasCapability(CapabilityEnergy.ENERGY, null)) {
                IEnergyStorage e = te.getCapability(CapabilityEnergy.ENERGY, null);
                if (e != null && e.receiveEnergy(maxReceive, simulate) > 0) {
                    storages.add(e);
                }
            }
        }
        int part = maxReceive / storages.size();
        int totalDrained = 0;
        int remaining = maxReceive;

        //Drillgon200: Extra hacky compensation
        int intRoundingCompensation = maxReceive - part * storages.size();
        rand.setSeed(this.fillables.get(0).getWorld().getWorldTime());
        int randomFillIndex = rand.nextInt(storages.size());

        for (int i = 0; i < storages.size(); i++) {
            IEnergyStorage consumer = storages.get(i);
            int vol = consumer.receiveEnergy(randomFillIndex == i ? part + intRoundingCompensation : part, simulate);
            totalDrained += vol;
            remaining -= vol;
            if (remaining <= 0)
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

    public void checkForRemoval(TileEntity te, EnumFacing side) {
        if (te == null)
            return;
        if (te instanceof IEnergyTransmitter) {
            cables.remove(te.getPos());
        } else if (te.hasCapability(CapabilityEnergy.ENERGY, side)) {
            fillables.remove(te.getPos());
        }
    }

    public boolean tryAdd(TileEntity te, EnumFacing side) {
        if (te == null)
            return false;
        if (te instanceof IEnergyTransmitter) {
            if (!cables.containsKey(te.getPos())) {
                cables.put(te.getPos(), (IEnergyTransmitter) te);
                return true;
            }
        } else if (te.hasCapability(CapabilityEnergy.ENERGY, side)) {
            if (!fillables.containsKey(te.getPos())) {
                fillables.put(te.getPos(), te);
                return true;
            }
        }
        return false;
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
        while (itr.hasNext()) {
            TileEntity te = itr.next();
            if (te.hasCapability(CapabilityEnergy.ENERGY, null)) {
                return true;
            }
        }
        return false;
    }
}
