package com.hbm.lib;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockStorageCrate;
import com.hbm.blocks.machine.PinkCloudBroadcaster;
import com.hbm.blocks.machine.SoyuzCapsule;
import com.hbm.config.GeneralConfig;
import com.hbm.config.WorldConfig;
import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntitySafe;
import com.hbm.tileentity.machine.TileEntitySoyuzCapsule;
import com.hbm.world.Antenna;
import com.hbm.world.Barrel;
import com.hbm.world.Bunker;
import com.hbm.world.CrashedVertibird;
import com.hbm.world.DesertAtom001;
import com.hbm.world.Dud;
import com.hbm.world.Factory;
import com.hbm.world.Geyser;
import com.hbm.world.GeyserLarge;
import com.hbm.world.LibraryDungeon;
import com.hbm.world.OilBubble;
import com.hbm.world.OilSandBubble;
import com.hbm.world.Radio01;
import com.hbm.world.Relay;
import com.hbm.world.Satellite;
import com.hbm.world.Sellafield;
import com.hbm.world.Silo;
import com.hbm.world.Spaceship;
import com.hbm.world.Vertibird;
import com.hbm.world.dungeon.AncientTomb;
import com.hbm.world.dungeon.ArcticVault;
import com.hbm.world.feature.DepthDeposit;
import com.hbm.world.feature.OilSpot;
import com.hbm.world.generator.CellularDungeonFactory;
import com.hbm.world.generator.DungeonToolbox;

import net.minecraft.block.Block;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.IWorldGenerator;

public class HbmWorldGen implements IWorldGenerator {

	@Override
	public void generate(Random rand, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		switch(world.provider.getDimension()) {
		case -1:
			generateNether(world, rand, chunkX * 16, chunkZ * 16);
			break;
		case 0:
			generateSurface(world, rand, chunkX * 16, chunkZ * 16);
			break;
		case 1:
			generateEnd(world, rand, chunkX * 16, chunkZ * 16);
			break;
		default:
			if(GeneralConfig.enableMDOres)
				generateSurface(world, rand, chunkX * 16, chunkZ * 16);
			break;
		}
	}
	
	/**
	 * Fake noise generator "unruh" ("unrest", the motion of a clockwork), using a bunch of layered, scaaled and offset
	 * sine functions to simulate a simple noise generator that runs somewhat efficiently
	 * @param long the random function seed used for this operation
	 * @param x the exact x-coord of the height you want
	 * @param z the exact z-coord of the height you want
	 * @param scale how much the x/z coords should be amplified
	 * @param depth the resolution of the operation, higher numbers call more sine functions
	 * @return the height value
	 */
	private double generateUnruh(long seed, int x, int z, double scale, int depth) {

		scale = 1/scale;

		double result = 1;

		Random rand = new Random(seed);

		for(int i = 0; i < depth; i++) {

			double offsetX = rand.nextDouble() * Math.PI * 2;
			double offsetZ = rand.nextDouble() * Math.PI * 2;

			result += Math.sin(x / Math.pow(2, depth) * scale + offsetX) * Math.sin(z / Math.pow(2, depth) * scale + offsetZ);
		}

		return result / depth;
	}

	private void generateSurface(World world, Random rand, int i, int j) {
		
		if(WorldConfig.oilcoalSpawn > 0 && rand.nextInt(WorldConfig.oilcoalSpawn) == 0)
			DungeonToolbox.generateOre(world, rand, i, j, 1, 64, 32, 32, ModBlocks.ore_coal_oil);

		if(WorldConfig.gasbubbleSpawn > 0 && rand.nextInt(WorldConfig.gasbubbleSpawn) == 0)
			DungeonToolbox.generateOre(world, rand, i, j, 1, 32, 30, 10, ModBlocks.gas_flammable);

		if(WorldConfig.explosivebubbleSpawn > 0 && rand.nextInt(WorldConfig.explosivebubbleSpawn) == 0)
			DungeonToolbox.generateOre(world, rand, i, j, 1, 32, 30, 10, ModBlocks.gas_explosive);
		
		DepthDeposit.generateConditionOverworld(world, i, 0, 3, j, 5, 0.6D, ModBlocks.cluster_depth_iron, rand, 24);
		DepthDeposit.generateConditionOverworld(world, i, 0, 3, j, 5, 0.6D, ModBlocks.cluster_depth_titanium, rand, 32);
		DepthDeposit.generateConditionOverworld(world, i, 0, 3, j, 5, 0.6D, ModBlocks.cluster_depth_tungsten, rand, 32);
		DepthDeposit.generateConditionOverworld(world, i, 0, 3, j, 5, 0.8D, ModBlocks.ore_depth_cinnebar, rand, 16);
		DepthDeposit.generateConditionOverworld(world, i, 0, 3, j, 5, 0.8D, ModBlocks.ore_depth_zirconium, rand, 16);
		
		DungeonToolbox.generateOre(world, rand, i, j, 25, 6, 30, 10, ModBlocks.ore_gneiss_iron, ModBlocks.stone_gneiss);
		DungeonToolbox.generateOre(world, rand, i, j, 10, 6, 30, 10, ModBlocks.ore_gneiss_gold, ModBlocks.stone_gneiss);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.uraniumSpawn * 3, 6, 30, 10, ModBlocks.ore_gneiss_uranium, ModBlocks.stone_gneiss);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.copperSpawn * 3, 6, 30, 10, ModBlocks.ore_gneiss_copper, ModBlocks.stone_gneiss);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.asbestosSpawn * 3, 6, 30, 10, ModBlocks.ore_gneiss_asbestos, ModBlocks.stone_gneiss);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.lithiumSpawn, 6, 30, 10, ModBlocks.ore_gneiss_lithium, ModBlocks.stone_gneiss);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.rareSpawn, 6, 30, 10, ModBlocks.ore_gneiss_asbestos, ModBlocks.stone_gneiss);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.gassshaleSpawn * 3, 10, 30, 10, ModBlocks.ore_gneiss_gas, ModBlocks.stone_gneiss);

		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.uraniumSpawn, 5, 5, 20, ModBlocks.ore_uranium);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.thoriumSpawn, 5, 5, 25, ModBlocks.ore_thorium);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.titaniumSpawn, 6, 5, 30, ModBlocks.ore_titanium);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.sulfurSpawn, 8, 5, 30, ModBlocks.ore_sulfur);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.aluminiumSpawn, 6, 5, 40, ModBlocks.ore_aluminium);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.copperSpawn, 6, 5, 45, ModBlocks.ore_copper);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.fluoriteSpawn, 4, 5, 45, ModBlocks.ore_fluorite);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.niterSpawn, 6, 5, 30, ModBlocks.ore_niter);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.tungstenSpawn, 8, 5, 30, ModBlocks.ore_tungsten);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.leadSpawn, 9, 5, 30, ModBlocks.ore_lead);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.berylliumSpawn, 4, 5, 30, ModBlocks.ore_beryllium);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.rareSpawn, 5, 5, 20, ModBlocks.ore_rare);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.ligniteSpawn, 24, 35, 25, ModBlocks.ore_lignite);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.asbestosSpawn, 4, 16, 16, ModBlocks.ore_asbestos);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.cinnebarSpawn, 4, 8, 16, ModBlocks.ore_cinnebar);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.cobaltSpawn, 4, 4, 8, ModBlocks.ore_cobalt);
		
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.ironClusterSpawn, 6, 5, 50, ModBlocks.cluster_iron);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.titaniumClusterSpawn, 6, 5, 30, ModBlocks.cluster_titanium);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.aluminiumClusterSpawn, 6, 5, 40, ModBlocks.cluster_aluminium);
		
		if(WorldConfig.oilcoalSpawn > 0 && rand.nextInt(WorldConfig.oilcoalSpawn) == 0)
			DungeonToolbox.generateOre(world, rand, i, j, 1, 64, 32, 32, ModBlocks.ore_coal_oil);
		
		Random colRand = new Random(world.getSeed() + 5);
		int colX = (int) (colRand.nextGaussian() * 1500);
		int colZ = (int) (colRand.nextGaussian() * 1500);
		int colRange = 750;
		
		if((GeneralConfig.enable528BedrockSpawn || GeneralConfig.enable528BedrockDeposit) && rand.nextInt(GeneralConfig.bedrockRate) != 0) {
			int x = i + rand.nextInt(16);
			int z = j + rand.nextInt(16);
			
			if(GeneralConfig.enable528BedrockSpawn || (GeneralConfig.enable528BedrockDeposit && x <= colX + colRange && x >= colX - colRange && z <= colZ + colRange && z >= colZ - colRange)) {
				
				for(int y = 6; y >= 0; y--) {
					if(world.getBlockState(new BlockPos(x, y, z)).getBlock().isReplaceableOreGen(world.getBlockState(new BlockPos(x, y, z)), world, new BlockPos(x, y, z), BlockMatcher.forBlock(Blocks.BEDROCK))) {
						world.setBlockState(new BlockPos(x, y, z), ModBlocks.ore_bedrock_coltan.getDefaultState());
					}
				}
			}
		}

		if(WorldConfig.bedrockOilSpawn > 0 && rand.nextInt(WorldConfig.bedrockOilSpawn) == 0) {
			int randPosX = i + rand.nextInt(16);
			int randPosZ = j + rand.nextInt(16);

			for (int v = 5; v >= -5; v--) {
				for (int w = 5; w >= -5; w--) {
					for (int y = 6; y >= 0; y--) {
						if (world.getBlockState(new BlockPos(randPosX + w, y, randPosZ + v)).getBlock().isReplaceableOreGen(world.getBlockState(new BlockPos(randPosX + w, y, randPosZ + v)), world, new BlockPos(randPosX + w, y, randPosZ + v), BlockMatcher.forBlock(Blocks.BEDROCK))) {
							world.setBlockState(new BlockPos(randPosX + w, y, randPosZ + v), ModBlocks.ore_bedrock_oil.getDefaultState());
						}
					}
				}
			}

			DungeonToolbox.generateOre(world, rand, i, j, 16, 8, 10, 50, ModBlocks.stone_porous);
			OilSpot.generateOilSpot(world, randPosX, randPosZ, 5, 50);
		}
		
		if(GeneralConfig.enable528ColtanDeposit) {
			for (int k = 0; k < 2; k++) {
				
				for(int r = 1; r <= 5; r++) {
					int randPosX = i + rand.nextInt(16);
					int randPosY = rand.nextInt(25) + 15;
					int randPosZ = j + rand.nextInt(16);
					
					int range = colRange / r;
		
					if(randPosX <= colX + range && randPosX >= colX - range && randPosZ <= colZ + range && randPosZ >= colZ - range) {
						(new WorldGenMinable(ModBlocks.ore_coltan.getDefaultState(), 4)).generate(world, rand, new BlockPos(randPosX, randPosY, randPosZ));
					}
				}
			}
		}

		for (int k = 0; k < rand.nextInt(4); k++) {
			int randPosX = i + rand.nextInt(16);
			int randPosY = rand.nextInt(15) + 15;
			int randPosZ = j + rand.nextInt(16);

			if(randPosX <= -350 && randPosX >= -450 && randPosZ <= -350 && randPosZ >= -450)
				(new WorldGenMinable(ModBlocks.ore_australium.getDefaultState(), 50)).generate(world, rand, new BlockPos(randPosX, randPosY, randPosZ));
		}
		
		if(GeneralConfig.enableDungeons) {
			//Drillgon200: Helps with cascading world gen.
			i += 8;
			j += 8;
			Biome biome = world.getBiome(new BlockPos(i, 0, j));
			
			if (biome.getDefaultTemperature() >= 1F ||  biome.getRainfall() < 2F) {
				if (WorldConfig.radioStructure > 0 && rand.nextInt(WorldConfig.radioStructure) == 0) {
					for (int a = 0; a < 1; a++) {
						int x = i + rand.nextInt(16);
						int z = j + rand.nextInt(16);
						int y = world.getHeight(x, z);

						new Radio01().generate(world, rand, new BlockPos(x, y, z));
					}
				}
			}
			if (biome.getDefaultTemperature() <= 1F && biome.getRainfall() > 2F) {
				if (WorldConfig.antennaStructure > 0 && rand.nextInt(WorldConfig.antennaStructure) == 0) {
					for (int a = 0; a < 1; a++) {
						int x = i + rand.nextInt(16);
						int z = j + rand.nextInt(16);
						int y = world.getHeight(x, z);

						new Antenna().generate(world, rand, new BlockPos(x, y, z));
					}
				}
			}
			if (!biome.canRain() && biome.getDefaultTemperature() >= 2F) {
				if (WorldConfig.atomStructure > 0 && rand.nextInt(WorldConfig.atomStructure) == 0) {
					for (int a = 0; a < 1; a++) {
						int x = i + rand.nextInt(16);
						int z = j + rand.nextInt(16);
						int y = world.getHeight(x, z);

						new DesertAtom001().generate(world, rand, new BlockPos(x, y, z));
					}
				}
			}
			if (WorldConfig.bunkerStructure > 0 && rand.nextInt(WorldConfig.bunkerStructure) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);

				new Bunker().generate(world, rand, new BlockPos(x, y, z));
			}
			if (biome.getDefaultTemperature() < 2F || biome.getDefaultTemperature() > 1.0F) {
				if (WorldConfig.relayStructure > 0 && rand.nextInt(WorldConfig.relayStructure) == 0) {
					for (int a = 0; a < 1; a++) {
						int x = i + rand.nextInt(16);
						int z = j + rand.nextInt(16);
						int y = world.getHeight(x, z);

						new Relay().generate(world, rand, new BlockPos(x, y, z));
					}
				}
			}
			if (WorldConfig.siloStructure > 0 && rand.nextInt(WorldConfig.siloStructure) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);

				new Silo().generate(world, rand, new BlockPos(x, y, z));
			}
			if (WorldConfig.factoryStructure > 0 && rand.nextInt(WorldConfig.factoryStructure) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);

				new Factory().generate(world, rand, new BlockPos(x, y, z));
			}
			if (WorldConfig.dudStructure > 0 && rand.nextInt(WorldConfig.dudStructure) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);

				new Dud().generate(world, rand, new BlockPos(x, y, z));
			}
			if (WorldConfig.barrelStructure > 0 && biome.getDefaultTemperature() > 3F && rand.nextInt(WorldConfig.barrelStructure) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);

				new Barrel().generate(world, rand, new BlockPos(x, y, z));
			}
			if (!biome.canRain() && biome.getDefaultTemperature() >= 2F) {
				if (WorldConfig.vertibirdStructure > 0 && rand.nextInt(WorldConfig.vertibirdStructure) == 0) {
					for (int a = 0; a < 1; a++) {
						int x = i + rand.nextInt(16);
						int z = j + rand.nextInt(16);
						int y = world.getHeight(x, z);

						if (rand.nextInt(2) == 0) {
							new Vertibird().generate(world, rand, new BlockPos(x, y, z));
						} else {
							new CrashedVertibird().generate(world, rand, new BlockPos(x, y, z));
						}

					}
				}
			}
			if (biome.getDefaultTemperature() < 1F || biome.getDefaultTemperature() > 3F) {
				if (WorldConfig.satelliteStructure > 0 && rand.nextInt(WorldConfig.satelliteStructure) == 0) {
					for (int a = 0; a < 1; a++) {
						int x = i + rand.nextInt(16);
						int z = j + rand.nextInt(16);
						int y = world.getHeight(x, z);

						new Satellite().generate(world, rand, new BlockPos(x, y, z));
					}
				}
			}
			if (WorldConfig.spaceshipStructure > 0 && rand.nextInt(WorldConfig.spaceshipStructure) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);

				new Spaceship().generate(world, rand, new BlockPos(x, y, z));
			}
			
			if (WorldConfig.radfreq > 0 && GeneralConfig.enableRad && rand.nextInt(WorldConfig.radfreq) == 0 && biome.getDefaultTemperature() >= 4F) {

				for (int a = 0; a < 1; a++) {
					int x = i + rand.nextInt(16);
					int z = j + rand.nextInt(16);

					double r = rand.nextInt(15) + 10;

					if (rand.nextInt(50) == 0)
						r = 50;

					new Sellafield().generate(world, x, z, r, r * 0.35D);

					if (GeneralConfig.enableDebugMode)
						MainRegistry.logger.info("[Debug] Successfully spawned raditation hotspot at " + x + " " + z);
				}
			}

			if (WorldConfig.radHotspotSmall == true && GeneralConfig.enableRad && rand.nextInt((int)(WorldConfig.minefreq/2F)) == 0) {

				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);


				if (world.getBlockState(new BlockPos(x, y-1, z)).isSideSolid(world, new BlockPos(x, y-1, z), EnumFacing.UP)) {
					int radi = rand.nextInt(128);
					if(radi > 64){
						world.setBlockState(new BlockPos(x, y, z), ModBlocks.sellafield_0.getDefaultState());
					}
					else if(radi > 32){
						world.setBlockState(new BlockPos(x, y, z), ModBlocks.sellafield_1.getDefaultState());
					}
					else if(radi > 16){
						world.setBlockState(new BlockPos(x, y, z), ModBlocks.sellafield_2.getDefaultState());
					}
					else if(radi > 8){
						world.setBlockState(new BlockPos(x, y, z), ModBlocks.sellafield_3.getDefaultState());
					}
					else if(radi > 2){
						world.setBlockState(new BlockPos(x, y, z), ModBlocks.sellafield_4.getDefaultState());
					}
					else{
						world.setBlockState(new BlockPos(x, y, z), ModBlocks.sellafield_core.getDefaultState());
					}

					if (GeneralConfig.enableDebugMode)
						MainRegistry.logger.info("[Debug] Successfully spawned small raditation hotspot at " + x + " " + y + " " + z);			
				}
			}
			
			if (WorldConfig.minefreq > 0 && GeneralConfig.enableMines && rand.nextInt(WorldConfig.minefreq) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);

				if (world.getBlockState(new BlockPos(x, y-1, z)).isSideSolid(world, new BlockPos(x, y-1, z), EnumFacing.UP)) {
					world.setBlockState(new BlockPos(x, y, z), ModBlocks.mine_ap.getDefaultState());

					if (GeneralConfig.enableDebugMode)
						MainRegistry.logger.info("[Debug] Successfully spawned landmine at " + x + " " + y + " " + z);
				}
			}
			if (WorldConfig.broadcaster > 0 && rand.nextInt(WorldConfig.broadcaster) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);

				if (world.getBlockState(new BlockPos(x, y-1, z)).isSideSolid(world, new BlockPos(x, y-1, z), EnumFacing.UP)){
					world.setBlockState(new BlockPos(x, y, z), ModBlocks.broadcaster_pc.getDefaultState().withProperty(PinkCloudBroadcaster.FACING, EnumFacing.getFront(rand.nextInt(4) + 2)), 2);

					if (GeneralConfig.enableDebugMode)
						MainRegistry.logger.info("[Debug] Successfully spawned corrupted broadcaster at " + x + " " + (y) + " " + z);
				}
			}
			if (WorldConfig.dungeonStructure > 0 && rand.nextInt(WorldConfig.dungeonStructure) == 0) {
				int x = i + rand.nextInt(16);
				int y = rand.nextInt(256);
				int z = j + rand.nextInt(16);
				new LibraryDungeon().generate(world, rand, new BlockPos(x, y, z));
			}
			
			if (WorldConfig.geyserWater > 0 && biome.getRainfall() > 2F && rand.nextInt(WorldConfig.geyserWater) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);

				if (world.getBlockState(new BlockPos(x, y - 1, z)).getBlock() == Blocks.GRASS)
					new Geyser().generate(world, rand, new BlockPos(x, y, z));
			}
			if (WorldConfig.geyserChlorine > 0 && biome.getDefaultTemperature() > 3F && biome.getRainfall() < 1F && rand.nextInt(WorldConfig.geyserChlorine) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);

				if (world.getBlockState(new BlockPos(x, y - 1, z)).getBlock() == Blocks.SAND)
					new GeyserLarge().generate(world, rand, new BlockPos(x, y, z));
			}
			if (WorldConfig.geyserVapor > 0 && rand.nextInt(WorldConfig.geyserVapor) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);

				if (world.getBlockState(new BlockPos(x, y - 1, z)).getBlock() == Blocks.STONE)
					world.setBlockState(new BlockPos(x, y - 1, z), ModBlocks.geysir_vapor.getDefaultState());
			}
			if (WorldConfig.capsuleStructure > 0 && biome.getDefaultTemperature() <= 1F && rand.nextInt(WorldConfig.capsuleStructure) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z) - 4;
				
				if(world.getBlockState(new BlockPos(x, y + 1, z)).isSideSolid(world, new BlockPos(x, y + 1, z), EnumFacing.UP)) {
					
					world.setBlockState(new BlockPos(x, y, z), ModBlocks.soyuz_capsule.getDefaultState().withProperty(SoyuzCapsule.RUSTY, true), 2);
					
					TileEntitySoyuzCapsule cap = (TileEntitySoyuzCapsule)world.getTileEntity(new BlockPos(x, y, z));
					
					if(cap != null) {
						cap.inventory.setStackInSlot(rand.nextInt(cap.inventory.getSlots()), new ItemStack(ModItems.record_glass));
					}
	
					if(GeneralConfig.enableDebugMode)
						MainRegistry.logger.info("[Debug] Successfully spawned capsule at " + x + " " + z);
				}
			}
			if (rand.nextInt(1000) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				
				boolean done = false;
				for(int k = 0; k < 256; k++) {
					IBlockState state = world.getBlockState(new BlockPos(x, k, z));
					if(state.getBlock() == Blocks.LOG && state.getValue(BlockOldLog.VARIANT) == BlockPlanks.EnumType.OAK){
						world.setBlockState(new BlockPos(x, k, z), ModBlocks.pink_log.getDefaultState());
						done = true;
					}
				}
				if(GeneralConfig.enableDebugMode && done)
					MainRegistry.logger.info("[Debug] Successfully spawned pink tree at " + x + " " + z);
			}
			if (WorldConfig.vaultfreq > 0 && GeneralConfig.enableVaults && rand.nextInt(WorldConfig.vaultfreq) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);

				if (world.getBlockState(new BlockPos(x, y-1, z)).isSideSolid(world, new BlockPos(x, y-1, z), EnumFacing.UP)) {
					boolean set = world.setBlockState(new BlockPos(x, y, z), ModBlocks.safe.getDefaultState().withProperty(BlockStorageCrate.FACING, EnumFacing.getFront(rand.nextInt(4) + 2)), 2);

					if(set){
						switch (rand.nextInt(10)) {
						case 0:
						case 1:
						case 2:
						case 3:
							WeightedRandomChestContentFrom1710.generateChestContents(rand, HbmChestContents.getLoot(10),
									(TileEntitySafe) world.getTileEntity(new BlockPos(x, y, z)), rand.nextInt(4) + 3);
							((TileEntitySafe) world.getTileEntity(new BlockPos(x, y, z))).setPins(rand.nextInt(999) + 1);
							((TileEntitySafe) world.getTileEntity(new BlockPos(x, y, z))).setMod(1);
							((TileEntitySafe) world.getTileEntity(new BlockPos(x, y, z))).lock();
							break;
						case 4:
						case 5:
						case 6:
							WeightedRandomChestContentFrom1710.generateChestContents(rand, HbmChestContents.getLoot(11),
							(TileEntitySafe) world.getTileEntity(new BlockPos(x, y, z)), rand.nextInt(3) + 2);
							((TileEntitySafe) world.getTileEntity(new BlockPos(x, y, z))).setPins(rand.nextInt(999) + 1);
							((TileEntitySafe) world.getTileEntity(new BlockPos(x, y, z))).setMod(0.1);
							((TileEntitySafe) world.getTileEntity(new BlockPos(x, y, z))).lock();
							break;
						case 7:
						case 8:
							WeightedRandomChestContentFrom1710.generateChestContents(rand, HbmChestContents.getLoot(12),
									(TileEntitySafe) world.getTileEntity(new BlockPos(x, y, z)), rand.nextInt(3) + 1);
							((TileEntitySafe) world.getTileEntity(new BlockPos(x, y, z))).setPins(rand.nextInt(999) + 1);
							((TileEntitySafe) world.getTileEntity(new BlockPos(x, y, z))).setMod(0.02);
							((TileEntitySafe) world.getTileEntity(new BlockPos(x, y, z))).lock();
							break;
						case 9:
							WeightedRandomChestContentFrom1710.generateChestContents(rand, HbmChestContents.getLoot(13),
									(TileEntitySafe) world.getTileEntity(new BlockPos(x, y, z)), rand.nextInt(2) + 1);
							((TileEntitySafe) world.getTileEntity(new BlockPos(x, y, z))).setPins(rand.nextInt(999) + 1);
							((TileEntitySafe) world.getTileEntity(new BlockPos(x, y, z))).setMod(0.0);
							((TileEntitySafe) world.getTileEntity(new BlockPos(x, y, z))).lock();
							break;
						}

						if (GeneralConfig.enableDebugMode)
							MainRegistry.logger.info("[Debug] Successfully spawned safe at " + x + " " + (y + 1) + " " + z);
					}
				}
			}
			
			if (WorldConfig.meteorStructure > 0 && rand.nextInt(WorldConfig.meteorStructure) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				
				CellularDungeonFactory.meteor.generate(world, x, 10, z, rand);
				
				if(GeneralConfig.enableDebugMode)
					MainRegistry.logger.info("[Debug] Successfully spawned meteor dungeon at " + x + " 10 " + z);
				
				int y = world.getHeight(x, z);
				
				for(int f = 0; f < 3; f++)
					world.setBlockState(new BlockPos(x, y + f, z), ModBlocks.meteor_pillar.getDefaultState());
				world.setBlockState(new BlockPos(x, y + 3, z), ModBlocks.meteor_brick_chiseled.getDefaultState());
				
				for(int f = 0; f < 10; f++) {

					x = i + rand.nextInt(65) - 32;
					z = j + rand.nextInt(65) - 32;
					y = world.getHeight(x, z);
					
					if(world.getBlockState(new BlockPos(x, y, z)).isSideSolid(world, new BlockPos(x, y, z), EnumFacing.UP)) {
						if(world.setBlockState(new BlockPos(x, y - 1, z), Blocks.SKULL.getDefaultState().withProperty(BlockSkull.FACING, EnumFacing.UP), 2)){
							TileEntitySkull skull = (TileEntitySkull)world.getTileEntity(new BlockPos(x, y - 1, z));
							
							if(skull != null)
								skull.setType(rand.nextInt(16));
						}
					}
				}
			}
			
			if((biome.isHighHumidity() && biome.getTempCategory() == Biome.TempCategory.WARM) &&
					WorldConfig.jungleStructure > 0 && rand.nextInt(WorldConfig.jungleStructure) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);

				CellularDungeonFactory.jungle.generate(world, x, 20, z, world.rand);
				CellularDungeonFactory.jungle.generate(world, x, 24, z, world.rand);
				CellularDungeonFactory.jungle.generate(world, x, 28, z, world.rand);

				if(GeneralConfig.enableDebugMode)
					MainRegistry.logger.info("[Debug] Successfully spawned jungle dungeon at " + x + " 10 " + z);

				int y = world.getHeight(x, z);

				for(int f = 0; f < 3; f++)
					world.setBlockState(new BlockPos(x, y + f, z), ModBlocks.deco_titanium.getDefaultState());
				world.setBlockState(new BlockPos(x, y + 3, z), Blocks.REDSTONE_BLOCK.getDefaultState());
			}
			
			if (WorldConfig.arcticStructure > 0 && biome.getTempCategory() == Biome.TempCategory.COLD && rand.nextInt(WorldConfig.arcticStructure) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = 16 + rand.nextInt(32);
				new ArcticVault().trySpawn(world, x, y, z);
			}
			
			if (WorldConfig.pyramidStructure > 0 && biome.getDefaultTemperature() >= 3.0F && rand.nextInt(WorldConfig.pyramidStructure) == 0) {
				int x = i + rand.nextInt(16);
				int z = j + rand.nextInt(16);
				int y = world.getHeight(x, z);
				
				new AncientTomb().build(world, rand, x, y, z);
			}
			
			if(!biome.canRain() && biome.getDefaultTemperature() >= 3F) {
				if(rand.nextInt(200) == 0) {
					for(int a = 0; a < 1; a++) {
						int x = i + rand.nextInt(16);
						int z = j + rand.nextInt(16);
						int y = world.getHeight(x, z);

						OilSandBubble.spawnOil(world, x, y, z, 15 + rand.nextInt(31));
					}
				}
			}
		}
		
		if(rand.nextInt(25) == 0) {
			int randPosX = i + rand.nextInt(16);
			int randPosY = rand.nextInt(25);
			int randPosZ = j + rand.nextInt(16);

			OilBubble.spawnOil(world, randPosX, randPosY, randPosZ, 7 + rand.nextInt(9));
		}
		if (GeneralConfig.enableNITAN) {

			if (i <= 10000 && i + 16 >= 10000 && j <= 10000 && j + 16 >= 10000) {
				if (world.getBlockState(new BlockPos(10000, 250, 10000)).getBlock() == Blocks.AIR) {
					world.setBlockState(new BlockPos(10000, 250, 10000), Blocks.CHEST.getDefaultState());
					if (world.getBlockState(new BlockPos(10000, 250, 10000)).getBlock() == Blocks.CHEST) {
						WeightedRandomChestContentFrom1710.generateChestContents(rand, HbmChestContents.getLoot(9),
								(TileEntityChest) world.getTileEntity(new BlockPos(10000, 250, 10000)), 29);
					}
				}
			}
			if (i <= 0 && i + 16 >= 0 && j <= 10000 && j + 16 >= 10000) {
				if (world.getBlockState(new BlockPos(0, 250, 10000)).getBlock() == Blocks.AIR) {
					world.setBlockState(new BlockPos(0, 250, 10000), Blocks.CHEST.getDefaultState());
					if (world.getBlockState(new BlockPos(0, 250, 10000)).getBlock() == Blocks.CHEST) {
						WeightedRandomChestContentFrom1710.generateChestContents(rand, HbmChestContents.getLoot(9),
								(TileEntityChest) world.getTileEntity(new BlockPos(0, 250, 10000)), 29);
					}
				}
			}
			if (i <= -10000 && i + 16 >= -10000 && j <= 10000 && j + 16 >= 10000) {
				if (world.getBlockState(new BlockPos(-10000, 250, 10000)).getBlock() == Blocks.AIR) {
					world.setBlockState(new BlockPos(-10000, 250, 10000), Blocks.CHEST.getDefaultState());
					if (world.getBlockState(new BlockPos(-10000, 250, 10000)).getBlock() == Blocks.CHEST) {
						WeightedRandomChestContentFrom1710.generateChestContents(rand, HbmChestContents.getLoot(9),
								(TileEntityChest) world.getTileEntity(new BlockPos(-10000, 250, 10000)), 29);
					}
				}
			}
			if (i <= 10000 && i + 16 >= 10000 && j <= 0 && j + 16 >= 0) {
				if (world.getBlockState(new BlockPos(10000, 250, 0)).getBlock() == Blocks.AIR) {
					world.setBlockState(new BlockPos(10000, 250, 0), Blocks.CHEST.getDefaultState());
					if (world.getBlockState(new BlockPos(10000, 250, 0)).getBlock() == Blocks.CHEST) {
						WeightedRandomChestContentFrom1710.generateChestContents(rand, HbmChestContents.getLoot(9),
								(TileEntityChest) world.getTileEntity(new BlockPos(10000, 250, 0)), 29);
					}
				}
			}
			if (i <= -10000 && i + 16 >= -10000 && j <= 0 && j + 16 >= 0) {
				if (world.getBlockState(new BlockPos(-10000, 250, 0)).getBlock() == Blocks.AIR) {
					world.setBlockState(new BlockPos(-10000, 250, 0), Blocks.CHEST.getDefaultState());
					if (world.getBlockState(new BlockPos(-10000, 250, 0)).getBlock() == Blocks.CHEST) {
						WeightedRandomChestContentFrom1710.generateChestContents(rand, HbmChestContents.getLoot(9),
								(TileEntityChest) world.getTileEntity(new BlockPos(-10000, 250, 0)), 29);
					}
				}
			}
			if (i <= 10000 && i + 16 >= 10000 && j <= -10000 && j + 16 >= -10000) {
				if (world.getBlockState(new BlockPos(10000, 250, -10000)).getBlock() == Blocks.AIR) {
					world.setBlockState(new BlockPos(10000, 250, -10000), Blocks.CHEST.getDefaultState());
					if (world.getBlockState(new BlockPos(10000, 250, -10000)).getBlock() == Blocks.CHEST) {
						WeightedRandomChestContentFrom1710.generateChestContents(rand, HbmChestContents.getLoot(9),
								(TileEntityChest) world.getTileEntity(new BlockPos(10000, 250, -10000)), 29);
					}
				}
			}
			if (i <= 0 && i + 16 >= 0 && j <= -10000 && j + 16 >= -10000) {
				if (world.getBlockState(new BlockPos(0, 250, -10000)).getBlock() == Blocks.AIR) {
					world.setBlockState(new BlockPos(0, 250, -10000), Blocks.CHEST.getDefaultState());
					if (world.getBlockState(new BlockPos(0, 250, -10000)).getBlock() == Blocks.CHEST) {
						WeightedRandomChestContentFrom1710.generateChestContents(rand, HbmChestContents.getLoot(9),
								(TileEntityChest) world.getTileEntity(new BlockPos(0, 250, -10000)), 29);
					}
				}
			}
			if (i <= -10000 && i + 16 >= -10000 && j <= -10000 && j + 16 >= -10000) {
				if (world.getBlockState(new BlockPos(-10000, 250, -10000)).getBlock() == Blocks.AIR) {
					world.setBlockState(new BlockPos(-10000, 250, -10000), Blocks.CHEST.getDefaultState());
					if (world.getBlockState(new BlockPos(-10000, 250, -10000)).getBlock() == Blocks.CHEST) {
						WeightedRandomChestContentFrom1710.generateChestContents(rand, HbmChestContents.getLoot(9),
								(TileEntityChest) world.getTileEntity(new BlockPos(-10000, 250, -10000)), 29);
					}
				}
			}
		}
	}

	private void generateNether(World world, Random rand, int i, int j) {
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.netherUraniumuSpawn, 6, 0, 127, ModBlocks.ore_nether_uranium, Blocks.NETHERRACK);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.netherTungstenSpawn, 10, 0, 127, ModBlocks.ore_nether_tungsten, Blocks.NETHERRACK);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.netherSulfurSpawn, 12, 0, 127, ModBlocks.ore_nether_sulfur, Blocks.NETHERRACK);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.netherPhosphorusSpawn, 6, 0, 127, ModBlocks.ore_nether_fire, Blocks.NETHERRACK);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.netherCoalSpawn, 32, 16, 96, ModBlocks.ore_nether_coal, Blocks.NETHERRACK);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.netherCobaltSpawn, 6, 100, 26, ModBlocks.ore_nether_cobalt, Blocks.NETHERRACK);
		if(GeneralConfig.enablePlutoniumOre)
			DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.netherPlutoniumSpawn, 4, 0, 127, ModBlocks.ore_nether_plutonium, Blocks.NETHERRACK);
		
		DepthDeposit.generateConditionNether(world, i, 0, 3, j, 7, 0.6D, ModBlocks.ore_depth_nether_neodymium, rand, 16);
		DepthDeposit.generateConditionNether(world, i, 125, 3, j, 7, 0.6D, ModBlocks.ore_depth_nether_neodymium, rand, 16);
		
		for(int k = 0; k < 30; k++){
			int x = i + rand.nextInt(16);
			int z = j + rand.nextInt(16);
			int d = 16 + rand.nextInt(96);

			for(int y = d - 5; y <= d; y++)
			if(world.getBlockState(new BlockPos(x, y + 1, z)).getBlock() == Blocks.AIR && world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.NETHERRACK)
				world.setBlockState(new BlockPos(x, y, z), ModBlocks.ore_nether_smoldering.getDefaultState());
		}
		for(int k = 0; k < 1; k++){
			int x = i + rand.nextInt(16);
			int z = j + rand.nextInt(16);
			int d = 16 + rand.nextInt(96);

			for(int y = d - 5; y <= d; y++)
			if(world.getBlockState(new BlockPos(x, y + 1, z)).getBlock() == Blocks.AIR && world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.NETHERRACK)
				world.setBlockState(new BlockPos(x, y, z), ModBlocks.geysir_nether.getDefaultState());
		}
	}

	private void generateEnd(World world, Random rand, int i, int j) {
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.endTikiteSpawn, 6, 0, 127, ModBlocks.ore_tikite, Blocks.END_STONE);
	}

}
