package com.hbm.items.machine;

import java.util.List;

import com.hbm.interfaces.IItemHazard;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.modules.ItemHazardModule;
import com.hbm.tileentity.machine.rbmk.IRBMKFluxReceiver.NType;
import com.hbm.tileentity.machine.rbmk.RBMKDials;
import com.hbm.util.I18nUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemRBMKRod extends Item implements IItemHazard {
	
	public ItemRBMKPellet pellet;
	public String fullName = "";			//full name of the fuel rod
	public double reactivity;					//endpoint of the function
	public double selfRate;					//self-inflicted flux from self-igniting fuels
	public EnumBurnFunc function = EnumBurnFunc.LOG_TEN;
	public double xGen = 0.5D;				//multiplier for xenon production
	public double xBurn = 50D;				//divider for xenon burnup
	public double heat = 1D;				//heat produced per outFlux
	public double yield;					//total potential inFlux the rod can take in its lifetime
	public double meltingPoint = 1000D;		//the maximum heat of the rod's hull before shit hits the fan. the core can be as hot as it wants to be
	public double diffusion = 0.02D;		//the speed at which the core heats the hull
	public NType nType = NType.SLOW;		//neutronType, the most efficient neutron type for fission
	public NType rType = NType.FAST;		//releaseType, the type of neutrons released by this fuel
	
	/*   _____
	 * ,I I I I,
	 * |'-----'|
	 * |       |
	 *  '-----'
	 *  I I I I
	 *  I I I I
	 *  I I I I
	 *  I I I I
	 *  I I I I
	 *  I I I I
	 * ,I I I I,
	 * |'-----'|
	 * |       |
	 *  '-----'
	 *  I I I I
	 *  
	 *  i drew a fuel rod yay
	 */

	// Lol had quite some freetime

	public ItemRBMKRod(ItemRBMKPellet pellet, String s) {
		this(pellet.fullName, s);
		this.pellet = pellet;
	}

	public ItemRBMKRod(String fullName, String s) {
		this.setUnlocalizedName(s);
		this.setRegistryName(s);
		this.module = new ItemHazardModule();
		
		this.fullName = fullName;
		
		this.setContainerItem(ModItems.rbmk_fuel_empty);
		this.setMaxStackSize(1);
		this.setCreativeTab(MainRegistry.controlTab);
		
		ModItems.ALL_ITEMS.add(this);
	}

	public ItemRBMKRod setYield(double yield) {
		this.yield = yield;
		return this;
	}

	public ItemRBMKRod setStats(double funcEnd) {
		return setStats(funcEnd, 0);
	}

	public ItemRBMKRod setStats(double funcEnd, double selfRate) {
		this.reactivity = funcEnd;
		this.selfRate = selfRate;
		return this;
	}

	public ItemRBMKRod setFunction(EnumBurnFunc func) {
		this.function = func;
		return this;
	}

	public ItemRBMKRod setHeat(double heat) {
		this.heat = heat;
		return this;
	}

	public ItemRBMKRod setDiffusion(double diffusion) {
		this.diffusion = diffusion;
		return this;
	}

	public ItemRBMKRod setMeltingPoint(double meltingPoint) {
		this.meltingPoint = meltingPoint;
		return this;
	}

	public ItemRBMKRod setNeutronTypes(NType nType, NType rType) {
		this.nType = nType;
		this.rType = rType;
		return this;
	}
	
	/**
	 * Adjusts the input flux using the poison level
	 * Generates, then burns poison
	 * Calculates the outflux based on influx, enrichment and poison
	 * Depletes the yield, then returns the outflux
	 * @param stack
	 * @param inFlux
	 * @return outFlux
	 */
	public double burn(World world, ItemStack stack, double inFlux) {
		
		inFlux += selfRate;
		
		double xenon = getPoison(stack);
		xenon -= xenonBurnFunc(inFlux);
		
		inFlux *= (1D - getPoisonLevel(stack));

		xenon += xenonGenFunc(inFlux);
		
		if(xenon < 0D) xenon = 0D;
		if(xenon > 100D) xenon = 100D;
		
		setPoison(stack, xenon);
		
		double outFlux = reactivityFunc(inFlux, getEnrichment(stack)) * RBMKDials.getReactivityMod(world);
		
		double y = getYield(stack);
		y -= inFlux;
		
		if(y < 0D) y = 0D;
		
		setYield(stack, y);
		
		double coreHeat = getCoreHeat(stack);
		coreHeat += outFlux * heat;
		setCoreHeat(stack, coreHeat);
		
		return outFlux;
	}
	
	/**
	 * Heat up the core based on the outFlux, then move some heat to the hull
	 * @param stack
	 */
	public void updateHeat(World world, ItemStack stack, double mod) {
		
		double coreHeat = getCoreHeat(stack);
		double hullHeat = getHullHeat(stack);
		
		if(coreHeat > hullHeat) {
			
			double mid = (coreHeat - hullHeat) / 2D;
			
			coreHeat -= mid * this.diffusion * RBMKDials.getFuelDiffusionMod(world) * mod;
			hullHeat += mid * this.diffusion * RBMKDials.getFuelDiffusionMod(world) * mod;
			
			setCoreHeat(stack, coreHeat);
			setHullHeat(stack, hullHeat);
		}
	}
	
	/**
	 * return one tick's worth of heat and cool the hull of the fuel rod, this heat goes into the fuel rod assembly block
	 * @param stack
	 * @return
	 */
	public double provideHeat(World world, ItemStack stack, double heat, double mod) {
		
		double hullHeat = getHullHeat(stack);
		
		//metldown! the hull melts so the entire structure stops making sense
		//hull and core heats are instantly equalized into 33% of their sum each,
		//the rest is sent to the component which is always fatal
		if(hullHeat > this.meltingPoint) {
			double coreHeat = getCoreHeat(stack);
			double avg = (heat + hullHeat + coreHeat) / 3D;
			setCoreHeat(stack, avg);
			setHullHeat(stack, avg);
			return avg;
		}
		
		if(hullHeat <= heat)
			return 0;
		
		double ret = (hullHeat - heat) / 2;
		
		ret *= RBMKDials.getFuelHeatProvision(world) * mod;
		
		hullHeat -= ret;
		setHullHeat(stack, hullHeat);
		
		return ret;
	}
	
	public static enum EnumBurnFunc {
		PASSIVE(TextFormatting.DARK_GREEN + "SAFE / PASSIVE"),			//const, no reactivity
		LOG_TEN(TextFormatting.YELLOW + "MEDIUM / LOGARITHMIC"),		//log10(x + 1) * reactivity * 50
		PLATEU(TextFormatting.GREEN + "SAFE / EULER"),					//(1 - e^(-x/25)) * reactivity * 100
		ARCH(TextFormatting.YELLOW + "MEDIUM / NEGATIVE-QUADRATIC"),	//x-(x²/1000) * reactivity
		SIGMOID(TextFormatting.GREEN + "SAFE / SIGMOID"),				//100 / (1 + e^(-(x - 50) / 10)) <- tiny amount of reactivity at x=0 !
		SQUARE_ROOT(TextFormatting.YELLOW + "MEDIUM / SQUARE ROOT"),	//sqrt(x) * 10 * reactivity
		LINEAR(TextFormatting.RED + "DANGEROUS / LINEAR"),				//x * reactivity
		QUADRATIC(TextFormatting.RED + "DANGEROUS / QUADRATIC");		//x^2 / 100 * reactivity
		
		public String title = "";
		
		private EnumBurnFunc(String title) {
			this.title = title;
		}
	}
	
	/**
	 * @param flux [0;100] ...or at least those are sane levels
	 * @return the amount of reactivity yielded, unmodified by xenon
	 */
	public double reactivityFunc(double in, double enrichment) {
		
		double flux = in * enrichment;
		
		switch(this.function) {
		case PASSIVE: return selfRate * enrichment;
		case LOG_TEN: return Math.log10(flux + 1) * 0.5D * reactivity;
		case PLATEU: return (1 - Math.pow(Math.E, -flux / 25D)) * reactivity;
		case ARCH: return Math.max(flux - (flux * flux / 100000D) / 100D * reactivity, 0D);
		case SIGMOID: return reactivity / (1 + Math.pow(Math.E, -(flux - 50D) / 10D));
		case SQUARE_ROOT: return Math.sqrt(flux) * reactivity / 10D;
		case LINEAR: return flux / 100D * reactivity;
		case QUADRATIC: return flux * flux / 10000D * reactivity;
		}
		
		return 0;
	}
	
	public String getFuncDescription() {
		
		String x = "x";
		
		if(selfRate > 0)
			x = "(x" + TextFormatting.RED + " + " + selfRate + "" + TextFormatting.WHITE + ")";
		
		switch(this.function) {
		case PASSIVE: return TextFormatting.RED + "" + selfRate;
		case LOG_TEN: return "log10(x + 1" + (selfRate > 0 ? (TextFormatting.RED + " + " + selfRate) : "") + TextFormatting.WHITE + ") * 0.5 * " + reactivity;
		case PLATEU: return "(1 - e^-" + x + " / 25)) * " + reactivity;
		case ARCH: return "(" + x + " - " + x + "² / 100000) / 100 * " + reactivity + " [0;∞]";
		case SIGMOID: return reactivity + " / (1 + e^(-(" + x + " - 50) / 10)";
		case SQUARE_ROOT: return "sqrt(" + x + ") * " + reactivity + " / 10";
		case LINEAR: return x + " / 100 * " + reactivity;
		case QUADRATIC: return x + "² / 10000 * " + reactivity;
		}
		
		return "ERROR";
	}
	
	/**
	 * Xenon generated per tick, linear function
	 * @param flux
	 * @return
	 */
	public double xenonGenFunc(double flux) {
		return flux * xGen;
	}
	
	/**
	 * Xenon burned away per tick, quadratic function
	 * @param flux
	 * @return
	 */
	public double xenonBurnFunc(double flux) {
		return (flux * flux) / xBurn;
	}
	
	/**
	 * @param stack
	 * @return enrichment [0;1]
	 */
	public static double getEnrichment(ItemStack stack) {
		return getYield(stack) / ((ItemRBMKRod) stack.getItem()).yield;
	}
	
	/**
	 * @param stack
	 * @return poison [0;1]
	 */
	public static double getPoisonLevel(ItemStack stack) {
		return getPoison(stack) / 100D;
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flag) {
		
		list.add(TextFormatting.ITALIC + this.fullName);
		
		if(this == ModItems.rbmk_fuel_drx) {
			
			if(selfRate > 0 || this.function == EnumBurnFunc.SIGMOID) {
				list.add(TextFormatting.RED + I18nUtil.resolveKey("trait.rbmx.source"));
			}
			
			list.add(TextFormatting.GREEN + I18nUtil.resolveKey("trait.rbmx.depletion", ((int)(((yield - getYield(stack)) / yield) * 100000)) / 1000D + "%"));
			list.add(TextFormatting.DARK_PURPLE + I18nUtil.resolveKey("trait.rbmx.xenon", ((int)(getPoison(stack) * 1000D) / 1000D) + "%"));
			list.add(TextFormatting.BLUE + I18nUtil.resolveKey("trait.rbmx.splitsWith", I18nUtil.resolveKey(nType.unlocalized + ".x")));
			list.add(TextFormatting.BLUE + I18nUtil.resolveKey("trait.rbmx.splitsInto", I18nUtil.resolveKey(rType.unlocalized + ".x")));
			list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("trait.rbmx.fluxFunc", TextFormatting.WHITE + getFuncDescription()));
			list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("trait.rbmx.funcType", this.function.title));
			list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("trait.rbmx.xenonGen", TextFormatting.WHITE + "x * " + xGen));
			list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("trait.rbmx.xenonBurn", TextFormatting.WHITE + "x² * " + xBurn));
			list.add(TextFormatting.GOLD + I18nUtil.resolveKey("trait.rbmx.heat", heat + "°C"));
			list.add(TextFormatting.GOLD + I18nUtil.resolveKey("trait.rbmx.diffusion", diffusion + "¹/²"));
			list.add(TextFormatting.RED + I18nUtil.resolveKey("trait.rbmx.skinTemp", ((int)(getHullHeat(stack) * 10D) / 10D) + "m"));
			list.add(TextFormatting.RED + I18nUtil.resolveKey("trait.rbmx.coreTemp", ((int)(getCoreHeat(stack) * 10D) / 10D) + "m"));
			list.add(TextFormatting.DARK_RED + I18nUtil.resolveKey("trait.rbmx.melt", meltingPoint + "m"));
			
		} else {

			if(selfRate > 0 || this.function == EnumBurnFunc.SIGMOID) {
				list.add(TextFormatting.RED + I18nUtil.resolveKey("trait.rbmk.source"));
			}
			
			list.add(TextFormatting.GREEN + I18nUtil.resolveKey("trait.rbmk.depletion", ((int)(((yield - getYield(stack)) / yield) * 100000D)) / 1000D + "%"));
			list.add(TextFormatting.DARK_PURPLE + I18nUtil.resolveKey("trait.rbmk.xenon", ((int)(getPoison(stack) * 1000D) / 1000D) + "%"));
			list.add(TextFormatting.BLUE + I18nUtil.resolveKey("trait.rbmk.splitsWith", I18nUtil.resolveKey(nType.unlocalized)));
			list.add(TextFormatting.BLUE + I18nUtil.resolveKey("trait.rbmk.splitsInto", I18nUtil.resolveKey(rType.unlocalized)));
			list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("trait.rbmk.fluxFunc", TextFormatting.WHITE + getFuncDescription()));
			list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("trait.rbmk.funcType", this.function.title));
			list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("trait.rbmk.xenonGen", TextFormatting.WHITE + "x * " + xGen));
			list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("trait.rbmk.xenonBurn", TextFormatting.WHITE + "x² * " + xBurn));
			list.add(TextFormatting.GOLD + I18nUtil.resolveKey("trait.rbmk.heat", heat + "°C"));
			list.add(TextFormatting.GOLD + I18nUtil.resolveKey("trait.rbmk.diffusion", diffusion + "¹/²"));
			list.add(TextFormatting.RED + I18nUtil.resolveKey("trait.rbmk.skinTemp", ((int)(getHullHeat(stack) * 10D) / 10D) + "°C"));
			list.add(TextFormatting.RED + I18nUtil.resolveKey("trait.rbmk.coreTemp", ((int)(getCoreHeat(stack) * 10D) / 10D) + "°C"));
			list.add(TextFormatting.DARK_RED + I18nUtil.resolveKey("trait.rbmk.melt", meltingPoint + "°C"));
		}

		/*list.add(TextFormatting.GREEN + "Depletion: " + ((int)(((yield - getYield(stack)) / yield) * 10000)) / 10000D + "%");
		list.add(TextFormatting.DARK_PURPLE + "Xenon poison: " + ((getPoison(stack) * 100D) / 100D) + "%");
		list.add(TextFormatting.BLUE + "Splits with: " + nType.unlocalized);
		list.add(TextFormatting.BLUE + "Splits into: " + rType.unlocalized);
		list.add(TextFormatting.YELLOW + "Flux function: " + TextFormatting.WHITE + "" + funcEnd + " * x" + (selfRate > 0 ? (TextFormatting.RED + " + " + selfRate) : ""));
		list.add(TextFormatting.YELLOW + "Xenon gen function: " + TextFormatting.WHITE + "x * " + xGen);
		list.add(TextFormatting.YELLOW + "Xenon burn function: " + TextFormatting.WHITE + "x² * " + xBurn);
		list.add(TextFormatting.GOLD + "Heat per tick at full power: " + heat + "°C");
		list.add(TextFormatting.GOLD + "Diffusion: " + diffusion + "°C/t");
		list.add(TextFormatting.RED + "Skin temp: " + ((int)(getHullHeat(stack) * 10D) / 10D) + "°C");
		list.add(TextFormatting.RED + "Core temp: " + ((int)(getCoreHeat(stack) * 10D) / 10D) + "°C");
		list.add(TextFormatting.DARK_RED + "Melting point: " + meltingPoint + "°C");*/
		
		super.addInformation(stack, worldIn, list, flag);
		updateModule(stack);
		this.module.addInformation(stack, list, flag);
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int i, boolean b) {
		
		if(entity instanceof EntityLivingBase) {
			updateModule(stack);
			this.module.applyEffects((EntityLivingBase) entity, stack.getCount(), i, b, ((EntityLivingBase)entity).getHeldItemMainhand() == stack ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
		}
	}
	
	@Override
	public boolean onEntityItemUpdate(EntityItem item) {
		
		super.onEntityItemUpdate(item);
		updateModule(item.getItem());
		return this.module.onEntityItemUpdate(item);
	}
	
	ItemHazardModule module;

	@Override
	public ItemHazardModule getModule() {
		return this.module;
	}
	
	private void updateModule(ItemStack stack) {
		
		float mod = (float)(1 + (1 - getEnrichment(stack)) * 24 + getPoisonLevel(stack) * 100);
		this.module.setMod(mod);
	}
	
	/*  __    __   ____     ________
	 * |  \  |  | |  __ \  |__    __|
	 * |   \ |  | | |__| |    |  |
	 * |  |\\|  | |  __ <     |  |
	 * |  | \   | | |__| |    |  |
	 * |__|  \__| |_____/     |__|
	 */
	
	public static void setYield(ItemStack stack, double yield) {
		setDouble(stack, "yield", yield);
	}
	
	public static double getYield(ItemStack stack) {
		
		if(stack.getItem() instanceof ItemRBMKRod) {
			return getDouble(stack, "yield");
		}
		
		return 0;
	}
	
	public static void setPoison(ItemStack stack, double xenon) {
		setDouble(stack, "xenon", xenon);
	}
	
	public static double getPoison(ItemStack stack) {
		return getDouble(stack, "xenon");
	}
	
	public static void setCoreHeat(ItemStack stack, double heat) {
		setDouble(stack, "core", heat);
	}
	
	public static double getCoreHeat(ItemStack stack) {
		return getDouble(stack, "core");
	}
	
	public static void setHullHeat(ItemStack stack, double heat) {
		setDouble(stack, "hull", heat);
	}
	
	public static double getHullHeat(ItemStack stack) {
		return getDouble(stack, "hull");
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return getDurabilityForDisplay(stack) > 0D;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return 1D - getEnrichment(stack);
	}
	
	public static void setDouble(ItemStack stack, String key, double yield) {
		
		if(!stack.hasTagCompound())
			setNBTDefaults(stack);
		
		stack.getTagCompound().setDouble(key, yield);
	}
	
	public static double getDouble(ItemStack stack, String key) {
		
		if(!stack.hasTagCompound())
			setNBTDefaults(stack);

		return stack.getTagCompound().getDouble(key);
	}
	
	/**
	 * Sets up the default values for all NBT data because doing it one-by-one will only correctly set the first called value and the rest stays 0 which is very not good
	 * @param stack
	 */
	private static void setNBTDefaults(ItemStack stack) {
		stack.setTagCompound(new NBTTagCompound());
		setYield(stack, ((ItemRBMKRod)stack.getItem()).yield);
		setCoreHeat(stack, 20.0D);
		setHullHeat(stack, 20.0D);
	}
}