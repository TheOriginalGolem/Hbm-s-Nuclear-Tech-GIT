package com.hbm.inventory.fluid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hbm.inventory.FluidTank;
import com.hbm.render.util.EnumSymbol;

import net.minecraft.tileentity.TileEntity;

public class FluidType {

	//The numeric ID of the fluid
	private int id;
	//Approximate HEX Color of the fluid, used for pipe rendering
	private int color;
	//X position of the fluid on the sheet, the "row"
	private int textureX;
	//Y position of the fluid on the sheet, the "column"
	private int textureY;
	//ID of the texture sheet the fluid is on
	private int sheetID;
	//Unlocalized string ID of the fluid
	private String name;
	
	public int poison;
	public int flammability;
	public int reactivity;
	public EnumSymbol symbol;
	public int temperature;
	public List<FluidTrait> traits = new ArrayList();
	
	public FluidType(int color, int x, int y, int sheet, int p, int f, int r, EnumSymbol symbol, String name) {
		this(color, x, y, sheet, p, f, r, symbol, name, 0, new FluidTrait[0]);
	}
	
	public FluidType(int color, int x, int y, int sheet, int p, int f, int r, EnumSymbol symbol, String name, FluidTrait... traits) {
		this(color, x, y, sheet, p, f, r, symbol, name, 0, traits);
	}
	
	public FluidType(int color, int x, int y, int sheet, int p, int f, int r, EnumSymbol symbol, String name, int temperature) {
		this(color, x, y, sheet, p, f, r, symbol, name, temperature, new FluidTrait[0]);
	}
	
	public FluidType(int color, int x, int y, int sheet, int p, int f, int r, EnumSymbol symbol, String name, int temperature, FluidTrait... traits) {
		this.color = color;
		this.textureX = x;
		this.textureY = y;
		this.name = name;
		this.sheetID = sheet;
		this.poison = p;
		this.flammability = f;
		this.reactivity = r;
		this.symbol = symbol;
		this.temperature = temperature;
		Collections.addAll(this.traits, traits);
		
		this.id = Fluids.registerSelf(this);
	}
	
	public int getID() {
		return this.id;
	}

	public int getColor() {
		return this.color;
	}
	public int getMSAColor() {
		return this.color;
	}
	public int textureX() {
		return this.textureX;
	}
	public int textureY() {
		return this.textureY;
	}
	public int getSheetID() {
		return this.sheetID;
	}
	public String getUnlocalizedName() {
		return this.name;
	}
	
	public String name() {
		return getName();
	}
	
	public String getName() {
		return this.toString();
	}
	
	public boolean isHot() {
		return this.temperature >= 100;
	}
	
	public boolean isCorrosive() {
		return this.traits.contains(FluidTrait.CORROSIVE) || this.traits.contains(FluidTrait.CORROSIVE_2);
	}
	
	public boolean isAntimatter() {
		return this.traits.contains(FluidTrait.AMAT);
	}
	
	public boolean hasNoContainer() {
		return this.traits.contains(FluidTrait.NO_CONTAINER);
	}
	
	public boolean hasNoID() {
		return this.traits.contains(FluidTrait.NO_ID);
	}
	
	public boolean needsLeadContainer() {
		return this.traits.contains(FluidTrait.LEAD_CONTAINER);
	}
	
	//shitty wrapper delegates, go!
	@Deprecated //reason: use the fucking registry you dumbass this isn't a fucking enum anymore, we don't sell lists of all our instances here
	public static FluidType[] values() {
		return Fluids.metaOrder.toArray(new FluidType[0]);
	}
	@Deprecated //reason: not an enum, asshole, use the registry
	public static FluidType getEnum(int i) {
		return Fluids.fromID(i);
	}
	@Deprecated //reason: the more time you waste reading this the less time is there for you to use that fucking registry already
	public static com.hbm.inventory.fluid.FluidType getEnumFromName(String s) {
		for(int i = 0; i < FluidType.values().length; i++)
			if(FluidType.values()[i].getName().equals(s))
				return FluidType.values()[i];
		return Fluids.NONE;
	}
	@Deprecated //reason: not an enum, again, fuck you
	public int ordinal() {
		return this.getID();
	}

	public void onTankBroken(TileEntity te, FluidTank tank) { }
	public void onTankUpdate(TileEntity te, FluidTank tank) { }
	
	public static enum FluidTrait {
		AMAT,
		CORROSIVE,
		CORROSIVE_2,
		NO_CONTAINER,
		LEAD_CONTAINER,
		NO_ID;
	}
}
