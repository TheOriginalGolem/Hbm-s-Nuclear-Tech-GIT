package com.hbm.tileentity.machine;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hbm.blocks.ModBlocks;
import com.hbm.forgefluid.FFUtils;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.IConsumer;
import com.hbm.interfaces.ITankPacketAcceptor;
import com.hbm.inventory.CentrifugeRecipes;
import com.hbm.inventory.CrystallizerRecipes;
import com.hbm.inventory.ShredderRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.packet.FluidTankPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.InventoryUtil;

import api.hbm.block.IDrillInteraction;
import api.hbm.block.IMiningDrill;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class TileEntityMachineMiningLaser extends TileEntityMachineBase implements ITickable, IConsumer, IFluidHandler, ITankPacketAcceptor, IMiningDrill {

	public long power;
	public int age = 0;
	public static final long maxPower = 100000000;
	public static final int consumption = 10000;
	public FluidTank tank;

	public boolean isOn;
	public int targetX;
	public int targetY;
	public int targetZ;
	public int lastTargetX;
	public int lastTargetY;
	public int lastTargetZ;
	public boolean beam;
	boolean lock = false;
	double breakProgress;

	public TileEntityMachineMiningLaser() {
		super(0);
		//slot 0: battery
		//slots 1 - 8: upgrades
		//slots 9 - 29: output
		inventory = new ItemStackHandler(30){
			@Override
			protected void onContentsChanged(int slot) {
				super.onContentsChanged(slot);
				markDirty();
			}
			@Override
			public void setStackInSlot(int slot, ItemStack stack) {
				super.setStackInSlot(slot, stack);
				if(stack != null && slot >= 1 && slot <= 8 && stack.getItem() instanceof ItemMachineUpgrade)
					world.playSound(null, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, HBMSoundHandler.upgradePlug, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
		};
		tank = new FluidTank(64000);
	}

	@Override
	public String getName() {
		return "container.miningLaser";
	}

	@Override
	public void update() {
		if(!world.isRemote) {

			age++;
			if (age >= 20) {
				age = 0;
			}

			if (age == 9 || age == 19)
				fillFluidInit();
			
			power = Library.chargeTEFromItems(inventory, 0, power, maxPower);
			PacketDispatcher.wrapper.sendToAllAround(new FluidTankPacket(pos, new FluidTank[]{tank}), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 10));

			//reset progress if the position changes
			if(lastTargetX != targetX ||
					lastTargetY != targetY ||
					lastTargetZ != targetZ)
				breakProgress = 0;

			//set last positions for interpolation and the like
			lastTargetX = targetX;
			lastTargetY = targetY;
			lastTargetZ = targetZ;

			double clientBreakProgress = 0;

			if(isOn) {
				
				int cycles = getOverdrive();
				int speed = getSpeed();
				int range = getRange();
				int fortune = getFortune();
				int consumption = getConsumption() * speed;

				for(int i = 0; i < cycles; i++) {

					if(power < consumption) {
						beam = false;
						break;
					}

					power -= consumption;

					if(targetY <= 0)
						targetY = pos.getY() - 2;

					scan(range);

					IBlockState block = world.getBlockState(new BlockPos(targetX, targetY, targetZ));

					if(block.getMaterial().isLiquid()) {
						world.setBlockToAir(new BlockPos(targetX, targetY, targetZ));
						buildDam();
						continue;
					}

					if(beam && canBreak(block, targetX, targetY, targetZ)) {

						breakProgress += getBreakSpeed(speed);
						clientBreakProgress = Math.min(breakProgress, 1);

						if(breakProgress < 1) {
							world.sendBlockBreakProgress(-1, new BlockPos(targetX, targetY, targetZ), (int) Math.floor(breakProgress * 10));
						} else {
							breakBlock(fortune);
							buildDam();
						}
					}
				}
			} else {
				targetY = pos.getY() - 2;
				beam = false;
			}
			
			this.tryFillContainer(pos.getX() + 2, pos.getY(), pos.getZ());
			this.tryFillContainer(pos.getX() - 2, pos.getY(), pos.getZ());
			this.tryFillContainer(pos.getX(), pos.getY(), pos.getZ() + 2);
			this.tryFillContainer(pos.getX(), pos.getY(), pos.getZ() - 2);

			NBTTagCompound data = new NBTTagCompound();
			data.setLong("power", power);
			data.setInteger("lastX", lastTargetX);
			data.setInteger("lastY", lastTargetY);
			data.setInteger("lastZ", lastTargetZ);
			data.setInteger("x", targetX);
			data.setInteger("y", targetY);
			data.setInteger("z", targetZ);
			data.setBoolean("beam", beam);
			data.setBoolean("isOn", isOn);
			data.setDouble("progress", clientBreakProgress);

			this.networkPack(data, 250);
		}
	}

	@Override
	public void networkUnpack(NBTTagCompound data) {
		this.power = data.getLong("power");
		this.lastTargetX = data.getInteger("lastX");
		this.lastTargetY = data.getInteger("lastY");
		this.lastTargetZ = data.getInteger("lastZ");
		this.targetX = data.getInteger("x");
		this.targetY = data.getInteger("y");
		this.targetZ = data.getInteger("z");
		this.beam = data.getBoolean("beam");
		this.isOn = data.getBoolean("isOn");
		this.breakProgress = data.getDouble("progress");
	}
	
	private void buildDam() {

		if(world.getBlockState(new BlockPos(targetX + 1, targetY, targetZ)).getMaterial().isLiquid())
			world.setBlockState(new BlockPos(targetX + 1, targetY, targetZ), ModBlocks.barricade.getDefaultState());
		if(world.getBlockState(new BlockPos(targetX - 1, targetY, targetZ)).getMaterial().isLiquid())
			world.setBlockState(new BlockPos(targetX - 1, targetY, targetZ), ModBlocks.barricade.getDefaultState());
		if(world.getBlockState(new BlockPos(targetX, targetY, targetZ + 1)).getMaterial().isLiquid())
			world.setBlockState(new BlockPos(targetX, targetY, targetZ + 1), ModBlocks.barricade.getDefaultState());
		if(world.getBlockState(new BlockPos(targetX, targetY, targetZ - 1)).getMaterial().isLiquid())
			world.setBlockState(new BlockPos(targetX, targetY, targetZ - 1), ModBlocks.barricade.getDefaultState());
	}
	
	private void tryFillContainer(int x, int y, int z) {

		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		if(te == null || !te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
			return;
		IItemHandler h = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		if(!(h instanceof IItemHandlerModifiable))
			return;
		
		IItemHandlerModifiable inv = (IItemHandlerModifiable)h;
		
		for(int i = 9; i <= 29; i++) {

			if(!inventory.getStackInSlot(i).isEmpty()) {
				int prev = inventory.getStackInSlot(i).getCount();
				inventory.setStackInSlot(i, InventoryUtil.tryAddItemToInventory(inv, 0, inv.getSlots() - 1, inventory.getStackInSlot(i)));

				if(inventory.getStackInSlot(i).isEmpty() || inventory.getStackInSlot(i).getCount() < prev)
					return;
			}
		}
	}

	private void breakBlock(int fortune) {
		IBlockState state = world.getBlockState(new BlockPos(targetX, targetY, targetZ));
		Block b = state.getBlock();
		boolean normal = true;
		boolean doesBreak = true;

		if(b == Blocks.LIT_REDSTONE_ORE){
			b = Blocks.REDSTONE_ORE;
			state = Blocks.REDSTONE_ORE.getDefaultState();
		}

		ItemStack stack = new ItemStack(b, 1, b.getMetaFromState(state));

		if(stack != null && stack.getItem() != null) {
			if(hasCrystallizer()) {

				ItemStack result = CrystallizerRecipes.getOutput(stack);
				if(result != null && result.getItem() != ModItems.scrap) {
					world.spawnEntity(new EntityItem(world, targetX + 0.5, targetY + 0.5, targetZ + 0.5, result.copy()));
					normal = false;
				}

			} else if(hasCentrifuge()) {

				ItemStack[] result = CentrifugeRecipes.getOutput(stack);
				if(result != null) {
					for(ItemStack sta : result) {

						if(sta != null) {
							world.spawnEntity(new EntityItem(world, targetX + 0.5, targetY + 0.5, targetZ + 0.5, sta.copy()));
							normal = false;
						}
					}
				}

			} else if(hasShredder()) {

				ItemStack result = ShredderRecipes.getShredderResult(stack);
				if(result != null && result.getItem() != ModItems.scrap) {
					world.spawnEntity(new EntityItem(world, targetX + 0.5, targetY + 0.5, targetZ + 0.5, result.copy()));
					normal = false;
				}

			} else if(hasSmelter()) {

				ItemStack result = FurnaceRecipes.instance().getSmeltingResult(stack);
				if(result != null) {
					world.spawnEntity(new EntityItem(world, targetX + 0.5, targetY + 0.5, targetZ + 0.5, result.copy()));
					normal = false;
				}
			}
		}
		
		if(normal && b instanceof IDrillInteraction) {
			IDrillInteraction in = (IDrillInteraction) b;
			ItemStack drop = in.extractResource(world, targetX, targetY, targetZ, state, this);
			
			if(drop != null) {
				world.spawnEntity(new EntityItem(world, targetX + 0.5, targetY + 0.5, targetZ + 0.5, drop.copy()));
			}
			
			doesBreak = in.canBreak(world, targetX, targetY, targetZ, state, this);
		}

		if(doesBreak){
			if(normal)
				b.dropBlockAsItem(world, new BlockPos(targetX, targetY, targetZ), state, fortune);
			world.destroyBlock(new BlockPos(targetX, targetY, targetZ), false);
		}
		
		suckDrops();

		if(doesScream()) {
			world.playSound(null, targetX + 0.5, targetY + 0.5, targetZ + 0.5, HBMSoundHandler.screm, SoundCategory.BLOCKS, 2000.0F, 1.0F);
		}

		breakProgress = 0;
	}

	private static final Set<Item> bad = Sets.newHashSet(new Item[] {
			Item.getItemFromBlock(Blocks.DIRT),
			Item.getItemFromBlock(Blocks.STONE),
			Item.getItemFromBlock(Blocks.COBBLESTONE),
			Item.getItemFromBlock(Blocks.SAND),
			Item.getItemFromBlock(Blocks.SANDSTONE),
			Item.getItemFromBlock(Blocks.GRAVEL),
			Item.getItemFromBlock(ModBlocks.stone_gneiss),
			Items.FLINT,
			Items.SNOWBALL,
			Items.WHEAT_SEEDS
			});

	//hahahahahahahaha he said "suck"
	private void suckDrops() {

		int rangeHor = 3;
		int rangeVer = 1;
		boolean nullifier = hasNullifier();

		List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(
				targetX + 0.5 - rangeHor,
				targetY + 0.5 - rangeVer,
				targetZ + 0.5 - rangeHor,
				targetX + 0.5 + rangeHor,
				targetY + 0.5 + rangeVer,
				targetZ + 0.5 + rangeHor
				));

		for(EntityItem item : items) {

			if(nullifier && bad.contains(item.getItem().getItem())) {
				item.setDead();
				continue;
			}
			
			if(item.getItem().getItem() == Item.getItemFromBlock(ModBlocks.ore_oil)) {

				tank.fill(new FluidStack(ModForgeFluids.oil, 500), true);

				item.setDead();
				continue;
			}

			ItemStack stack = InventoryUtil.tryAddItemToInventory(inventory, 9, 29, item.getItem().copy());

			if(stack == null)
				item.setDead();
			else
				item.setItem(stack.copy()); //copy is not necessary but i'm paranoid due to the kerfuffle of the old drill
		}

		List<EntityLivingBase> mobs = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(
				targetX + 0.5 - 1,
				targetY + 0.5 - 1,
				targetZ + 0.5 - 1,
				targetX + 0.5 + 1,
				targetY + 0.5 + 1,
				targetZ + 0.5 + 1
				));

		for(EntityLivingBase mob : mobs) {
			mob.setFire(5);
		}
	}

	public double getBreakSpeed(int speed) {

		float hardness = world.getBlockState(new BlockPos(targetX, targetY, targetZ)).getBlockHardness(world, new BlockPos(targetX, targetY, targetZ)) * 15 / speed;
		if(hardness == 0)
			return 1;

		return 1 / hardness;
	}

	public void scan(int range) {

		for(int x = -range; x <= range; x++) {
			for(int z = -range; z <= range; z++) {

				if(world.getBlockState(new BlockPos(x + pos.getX(), targetY, z + pos.getZ())).getMaterial().isLiquid()) {
					/*targetX = x + xCoord;
					targetZ = z + zCoord;
					world.func_147480_a(x + xCoord, targetY, z + zCoord, false);
					beam = true;*/

					continue;
				}

				if(canBreak(world.getBlockState(new BlockPos(x + pos.getX(), targetY, z + pos.getZ())), x + pos.getX(), targetY, z + pos.getZ())) {
					targetX = x + pos.getX();
					targetZ = z + pos.getZ();
					beam = true;
					return;
				}
			}
		}

		beam = false;
		targetY--;
	}

	private boolean canBreak(IBlockState block, int x, int y, int z) {
		return block.getBlock() != Blocks.AIR && block.getBlockHardness(world, new BlockPos(x, y, z)) >= 0 && !block.getMaterial().isLiquid() && block.getBlock() != Blocks.BEDROCK;
	}

	public int getOverdrive() {

		int speed = 1;		
		for(int i = 1; i < 9; i++) {
			
			if(!inventory.getStackInSlot(i).isEmpty()) {

				if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_overdrive_1)
					speed += 1;
				else if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_overdrive_2)
					speed += 2;
				else if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_overdrive_3)
					speed += 3;
			}
		}

		return Math.min(speed, 4);
	}

	public int getSpeed() {

		int speed = 1;
		for(int i = 1; i < 9; i++) {

			if(!inventory.getStackInSlot(i).isEmpty()) {

				if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_speed_1)
					speed += 2;
				else if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_speed_2)
					speed += 4;
				else if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_speed_3)
					speed += 6;
			}
		}

		return Math.min(speed, 13);
	}

	public int getRange() {

		int range = 1;

		for(int i = 1; i < 9; i++) {

			if(!inventory.getStackInSlot(i).isEmpty()) {
				
				if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_effect_1)
					range += 2;
				else if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_effect_2)
					range += 4;
				else if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_effect_3)
					range += 6;
			}
		}

		return Math.min(range, 25);
	}
	
	public int getFortune() {

		int fortune = 0;

		for(int i = 1; i < 9; i++) {

			if(!inventory.getStackInSlot(i).isEmpty()) {

				if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_fortune_1)
					fortune += 1;
				else if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_fortune_2)
					fortune += 2;
				else if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_fortune_3)
					fortune += 3;
			}
		}

		return Math.min(fortune, 3);
	}

	public boolean hasNullifier() {

		for(int i = 1; i < 9; i++) {

			if(!inventory.getStackInSlot(i).isEmpty()) {

				if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_nullifier)
					return true;
			}
		}

		return false;
	}

	public boolean hasSmelter() {

		for(int i = 1; i < 9; i++) {

			if(!inventory.getStackInSlot(i).isEmpty()) {

				if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_smelter)
					return true;
			}
		}

		return false;
	}

	public boolean hasShredder() {

		for(int i = 1; i < 9; i++) {

			if(!inventory.getStackInSlot(i).isEmpty()) {

				if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_shredder)
					return true;
			}
		}

		return false;
	}

	public boolean hasCentrifuge() {

		for(int i = 1; i < 9; i++) {

			if(!inventory.getStackInSlot(i).isEmpty()) {

				if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_centrifuge)
					return true;
			}
		}

		return false;
	}

	public boolean hasCrystallizer() {

		for(int i = 1; i < 9; i++) {

			if(!inventory.getStackInSlot(i).isEmpty()) {

				if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_crystallizer)
					return true;
			}
		}

		return false;
	}

	public boolean doesScream() {

		for(int i = 1; i < 9; i++) {

			if(!inventory.getStackInSlot(i).isEmpty()) {

				if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_screm)
					return true;
			}
		}

		return false;
	}

	public int getConsumption() {

		int consumption = TileEntityMachineMiningLaser.consumption;

		return consumption;
	}
	
	public int getWidth() {

		return 1 + getRange() * 2;
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
	
	public int getPowerScaled(int i) {
		return (int)((power * i) / maxPower);
	}

	public int getProgressScaled(int i) {
		return (int) (breakProgress * i);
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, int j) {
		return this.isItemValidForSlot(i, itemStack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int j) {
		return i >= 9 && i <= 29;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing slot) {

		int[] slots = new int[21];

		for(int i = 0; i < 21; i++) {
			slots[i] = i + 9;
		}

		return slots;
	}

	@Override
	public void setPower(long i) {
		power = i;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}
	
	public void fillFluidInit() {
		fillFluid(pos.getX() + 2, pos.getY(), pos.getZ(), this.tank);
		fillFluid(pos.getX() - 2, pos.getY(), pos.getZ(), this.tank);
		fillFluid(pos.getX(), pos.getY(), pos.getZ() + 2, this.tank);
		fillFluid(pos.getX(), pos.getY(), pos.getZ() - 2, this.tank);
	}

	public void fillFluid(int x, int y, int z, FluidTank tank) {
		FFUtils.fillFluid(this, tank, world, new BlockPos(x, y, z), 5000);
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return tank.getTankProperties();
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		return 0;
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		if(resource != null && resource.getFluid() == ModForgeFluids.oil)
			return tank.drain(resource, doDrain);
		return null;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		return tank.drain(maxDrain, doDrain);
	}

	@Override
	public void recievePacket(NBTTagCompound[] tags) {
		if(tags.length == 1)
			tank.readFromNBT(tags[0]);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		tank.readFromNBT(compound.getCompoundTag("tank"));
		isOn = compound.getBoolean("isOn");
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
		compound.setBoolean("isOn", isOn);
		return super.writeToNBT(compound);
	}

	@Override
	public DrillType getDrillTier(){
		return DrillType.HITECH;
	}

	@Override
	public int getDrillRating(){
		return 65;
	}

}