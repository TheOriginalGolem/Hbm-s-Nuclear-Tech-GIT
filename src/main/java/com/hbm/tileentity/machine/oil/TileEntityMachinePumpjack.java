package com.hbm.tileentity.machine.oil;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachinePumpjack;
import com.hbm.entity.particle.EntityGasFX;
import com.hbm.forgefluid.FFUtils;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.packet.AuxElectricityPacket;
import com.hbm.packet.FluidTankPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.TEPumpjackPacket;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityMachinePumpjack extends TileEntityOilDrillBase {

	protected static final int delay = 25;
	protected static final int consumption = 200;

	protected static final int oilPerDeposit = 650;


	public boolean isProgressing;
	public float rotation;
	public float prevRotation;

	
	//private static final int[] slots_top = new int[] {1};
	//private static final int[] slots_bottom = new int[] {2, 0};
	//private static final int[] slots_side = new int[] {0};


	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.getCustomName() : "container.pumpjack";
	}


	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.rotation = compound.getFloat("rotation");

	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		compound = super.writeToNBT(compound);
		compound.setFloat("rotation", rotation);

		return compound;
	}



	
	@SuppressWarnings("deprecation")
	@Override
	public void update() {
		int timer = delay;
		prevRotation = rotation;
		age++;
		age2++;
		if(age >= timer)
			age -= timer;
		if(age2 >= 20)
			age2 -= 20;
		if(!world.isRemote) {
			int tank0Amount = tanks[0].getFluidAmount();
			int tank1Amount = tanks[1].getFluidAmount();
			if(age2 == 9 || age2 == 19) {
				fillFluidInit(tanks[0]);
				fillFluidInit(tanks[1]);
			}
		
		
			if(FFUtils.fillFluidContainer(inventory, tanks[0], 1, 2))
				needsUpdate = true;
			if(FFUtils.fillFluidContainer(inventory, tanks[1], 3, 4))
				needsUpdate = true;
			
			if(needsUpdate){
				needsUpdate = false;
			}

			power = Library.chargeTEFromItems(inventory, 0, power, maxPower);
			
			if(power >= consumption && !(tank0Amount >= tanks[0].getCapacity() || tank1Amount >= tanks[1].getCapacity())) {
				
				//operation start
				
				if(age == timer - 1) {
					warning = 0;
					
					//warning 0, green: derrick is operational
					//warning 1, red: derrick is full, has no power or the drill is jammed
					//warning 2, yellow: drill has reached max depth
					
					for(int i = pos.getY() - 1; i > pos.getY() - 1 - 100; i--) {
						
						if(i <= 5) {
							//Code 2: The drilling ended
							warning = 2;
							break;
						}
						BlockPos checkPos = new BlockPos(pos.getX(), i, pos.getZ());
						IBlockState state = world.getBlockState(checkPos);
						Block b = state.getBlock();
						if(b == ModBlocks.oil_pipe)
							continue;
						if((b.isReplaceable(world, checkPos) || b.getExplosionResistance(null) < 100) && !(b == ModBlocks.ore_oil || b == ModBlocks.ore_oil_empty)) {
							world.setBlockState(checkPos, ModBlocks.oil_pipe.getDefaultState());
							
							//Code 2: The drilling ended
							if(i == pos.getY() - 100)
								warning = 2;
							break;
							
						} else if(this.tanks[0].getFluidAmount() < this.tanks[0].getCapacity() && this.tanks[1].getFluidAmount() < this.tanks[1].getCapacity()) {
							if(succ(pos.getX(), i, pos.getZ()) == 1) {
								
								this.tanks[0].fill(new FluidStack(tankTypes[0], oilPerDeposit), true);
								
								this.tanks[1].fill(new FluidStack(tankTypes[1], gasPerDepositMin + rand.nextInt(extraGasPerDepositMax)), true);
								needsUpdate = true;
								
								break;
							} else {
								world.setBlockState(checkPos, ModBlocks.oil_pipe.getDefaultState());
								break;
							}
							
						} else {
							//Code 1: Drill jammed
							warning = 1;
							break;
						}
					}
				}
				
				//operation end
				
				power -= consumption;
			} else {
				warning = 1;
			}

			warning2 = 0;
			if(tanks[1].getFluidAmount() > 0) {
				if(inventory.getStackInSlot(5).getItem() == ModItems.fuse || inventory.getStackInSlot(5).getItem() == ModItems.screwdriver) {
					warning2 = 2;
					tanks[1].drain(50, true);
					needsUpdate = true;
		    		world.spawnEntity(new EntityGasFX(world, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 0.0, 0.0, 0.0));
				} else {
					warning2 = 1;
				}
			}
			
			isProgressing = warning == 0;
			rotation += (warning == 0 ? 5 : 0);

			PacketDispatcher.wrapper.sendToAllAround(new TEPumpjackPacket(pos.getX(), pos.getY(), pos.getZ(), rotation, isProgressing), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 100));
			PacketDispatcher.wrapper.sendToAllAround(new AuxElectricityPacket(pos, power), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 10));
			PacketDispatcher.wrapper.sendToAllAround(new FluidTankPacket(pos, new FluidTank[]{tanks[0], tanks[1]}), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 10));
			if(tank0Amount != tanks[0].getFluidAmount() || tank1Amount != tanks[1].getFluidAmount()){
				markDirty();
			}
		}
	}

	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}

	public void fillFluidInit(FluidTank tank) {
		
		EnumFacing e = world.getBlockState(pos).getValue(MachinePumpjack.FACING);
		e = e.rotateY();

		if(e == EnumFacing.EAST) {
			needsUpdate = FFUtils.fillFluid(this, tank, world, pos.add(-2, 0, 2), 2000) || needsUpdate;
			needsUpdate = FFUtils.fillFluid(this, tank, world, pos.add(-2, 0, -2), 2000) || needsUpdate;
			needsUpdate = FFUtils.fillFluid(this, tank, world, pos.add(-4, 0, 2), 2000) || needsUpdate;
			needsUpdate = FFUtils.fillFluid(this, tank, world, pos.add(-4, 0, -2), 2000) || needsUpdate;
		}
		if(e == EnumFacing.SOUTH) {
			needsUpdate = FFUtils.fillFluid(this, tank, world, pos.add(2, 0, -2), 2000) || needsUpdate;
			needsUpdate = FFUtils.fillFluid(this, tank, world, pos.add(-2, 0, -2), 2000) || needsUpdate;
			needsUpdate = FFUtils.fillFluid(this, tank, world, pos.add(2, 0, -4), 2000) || needsUpdate;
			needsUpdate = FFUtils.fillFluid(this, tank, world, pos.add(-2, 0, -4), 2000) || needsUpdate;
		}
		if(e == EnumFacing.WEST) {
			needsUpdate = FFUtils.fillFluid(this, tank, world, pos.add(2, 0, 2), 2000) || needsUpdate;
			needsUpdate = FFUtils.fillFluid(this, tank, world, pos.add(2, 0, -2), 2000) || needsUpdate;
			needsUpdate = FFUtils.fillFluid(this, tank, world, pos.add(4, 0, 2), 2000) || needsUpdate;
			needsUpdate = FFUtils.fillFluid(this, tank, world, pos.add(4, 0, -2), 2000) || needsUpdate;
		}
		if(e == EnumFacing.NORTH) {
			needsUpdate = FFUtils.fillFluid(this, tank, world, pos.add(2, 0, 2), 2000) || needsUpdate;
			needsUpdate = FFUtils.fillFluid(this, tank, world, pos.add(-2, 0, 2), 2000) || needsUpdate;
			needsUpdate = FFUtils.fillFluid(this, tank, world, pos.add(2, 0, 4), 2000) || needsUpdate;
			needsUpdate = FFUtils.fillFluid(this, tank, world, pos.add(-2, 0, 4), 2000) || needsUpdate;
		}
	}


	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		if(resource == null){
			return null;
		} else if(resource.getFluid() == tankTypes[0]){
			return tanks[0].drain(resource.amount, doDrain);
		} else if(resource.getFluid() == tankTypes[1]){
			return tanks[1].drain(resource.amount, doDrain);
		} else {
			return null;
		}
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		if(tanks[0].getFluidAmount() > 0){
			return tanks[0].drain(maxDrain, doDrain);
		} else if(tanks[1].getFluidAmount() > 0){
			return tanks[1].drain(maxDrain, doDrain);
		} else {
			return null;
		}
	}
}
