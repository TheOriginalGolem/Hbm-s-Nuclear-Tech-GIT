package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemForgeFluidIdentifier;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.oil.TileEntityMachineFractionTower;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

public class MachineFractionTower extends BlockDummyable {

	public MachineFractionTower(Material mat, String s) {
		super(mat, s);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		
		if(meta >= 12)
			return new TileEntityMachineFractionTower();
		if(meta >= extra)
			return new TileEntityProxyCombo(false, false, true);
		
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {2, 0, 1, 1, 1, 1};
	}

	@Override
	public int getOffset() {
		return 1;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos1, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
		if(!world.isRemote && !player.isSneaking()) {
			
			if(player.getHeldItem(hand).isEmpty() || player.getHeldItem(hand).getItem() == ModItems.forge_fluid_identifier) {
				int[] pos = this.findCore(world, pos1.getX(), pos1.getY(), pos1.getZ());
					
				if(pos == null)
					return false;
				
				TileEntity te = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
				
				if(!(te instanceof TileEntityMachineFractionTower))
					return false;
				
				TileEntityMachineFractionTower frac = (TileEntityMachineFractionTower) te;
				
				if(player.getHeldItem(hand).isEmpty()) {
					
					player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "=== FRACTIONING TOWER Y:" + pos[1] + " ==="));

					for(int i = 0; i < frac.tanks.length; i++)
						player.sendMessage(new TextComponentTranslation(frac.types[i].getUnlocalizedName()).appendSibling(new TextComponentString(": " + frac.tanks[i].getFluidAmount() + "/" + frac.tanks[i].getCapacity() + "mB")));
				} else {
					
					if(world.getTileEntity(new BlockPos(pos[0], pos[1] - 3, pos[2])) instanceof TileEntityMachineFractionTower) {
						player.sendMessage(new TextComponentString(TextFormatting.RED + "You can only change the type in the bottom segment!"));
					} else {
						Fluid type = ItemForgeFluidIdentifier.getType(player.getHeldItem(hand));
						frac.setTankType(0, type);
						frac.markDirty();
						player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Changed type to " + I18n.format(type.getUnlocalizedName()) + "!"));
					}
				}
				
				return true;
			}
			return false;
			
		} else {
			return true;
		}
	}
	
	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);
		
		x = x + dir.offsetX * o;
		z = z + dir.offsetZ * o;

		this.makeExtra(world, x + 1, y, z);
		this.makeExtra(world, x - 1, y, z);
		this.makeExtra(world, x, y, z + 1);
		this.makeExtra(world, x, y, z - 1);
	}
}