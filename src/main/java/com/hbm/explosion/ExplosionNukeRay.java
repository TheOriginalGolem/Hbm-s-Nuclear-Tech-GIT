package com.hbm.explosion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.lib.Library;
import com.hbm.render.amlfrom1710.Vec3;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class ExplosionNukeRay {
	private int maxSamples;
	private float phi;
	public boolean isContained=true;
	
	World world;
	int posX;
	int posY;
	int posZ;
	int radius;
	int processed;
	int currentSample;

	private List<FloatTriplet> affectedBlocks = new ArrayList<FloatTriplet>();
	public boolean isAusf3Complete = false;
	public Random rand = new Random();
	
	public ExplosionNukeRay(World world, int x, int y, int z, int radius) {
		this.world = world;
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.radius = radius;
		//Ausf3, must be double
		//this.startY = strength;
		//Mk 4.5, must be int32
		this.currentSample = 0;
		this.maxSamples = (int) (20 * 4 * Math.PI * Math.pow(radius, 2));
		this.phi = (float)(Math.PI * (3 - Math.sqrt(5)));
	}

	//currently used by mk4
	@SuppressWarnings("deprecation")
	public void collectTipMk6(int count) {
		MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int raysProcessed = 0;		
		
		FloatTriplet lastPos = new FloatTriplet(posX, posY, posZ);
		for(int s = currentSample; s < this.maxSamples; s++) {
			FloatTriplet direction = getNormalFibVec(s);
			float rayEnergy = (float)radius*0.6F;
			for(int l = 0; l < this.radius+1; l++){
				float x0 = (float) (posX + direction.xCoord * l);
				float y0 = (float) (posY + direction.yCoord * l);
				float z0 = (float) (posZ + direction.zCoord * l);
				if(y0 < 1 || y0 > 256){
					if(affectedBlocks.size() < Integer.MAX_VALUE - 100) {
						affectedBlocks.add(new FloatTriplet(lastPos.xCoord, lastPos.yCoord, lastPos.zCoord));
					}
					break;
				}

				pos.setPos(x0, y0, z0);
				rayEnergy -= Math.pow(getNukeResistance(pos), 7F * (l/this.radius)+0.5F);

				if(rayEnergy > 0 && world.getBlockState(pos).getBlock() != Blocks.AIR){
					lastPos = new FloatTriplet(x0, y0, z0);
				}
				
				if(rayEnergy <= 0 || l == this.radius) {
					if(isContained==true){
						if(l == this.radius){
							isContained = false;
						}
					}
					if(affectedBlocks.size() < Integer.MAX_VALUE - 100) {
						affectedBlocks.add(new FloatTriplet(lastPos.xCoord, lastPos.yCoord, lastPos.zCoord));
					}
					break;
				}
			}
			raysProcessed++;
			if(raysProcessed >= count) {
				currentSample = s;
				return;
			}
		}
		isAusf3Complete = true;
	}

	private float getNukeResistance(MutableBlockPos pos){
		if(world.getBlockState(pos).getMaterial().isLiquid())
			return 0F;
		else
			return world.getBlockState(pos).getBlock().getExplosionResistance(null);
	}

	private FloatTriplet getNormalFibVec(int sample){
		double fy = (2D * sample / (this.maxSamples - 1D)) - 1D;  // y goes from 1 to -1
        double fr = Math.sqrt(1D - fy * fy);  // radius at y

        double theta = phi * sample;  // golden angle increment
        return new FloatTriplet((float)(Math.cos(theta) * fr), (float)fy, (float)(Math.sin(theta) * fr));
	}
	
	// //currently used by mk4
	// @SuppressWarnings("deprecation")
	// public void collectTipMk4_5(int count) {
	// 	MutableBlockPos pos = new BlockPos.MutableBlockPos();
	// 	int raysProcessed = 0;
		
	// 	double bow = Math.PI * this.strength;
	// 	double bowCount = Math.ceil(bow);
		
	// 	//Axial
	// 	//StartY starts at this.length
	// 	for(int v = startY; v <= bowCount; v++) {
			
	// 		float part = (float) (Math.PI/bow);
	// 		float rot = part * -v;
			
	// 		Vec3 heightVec = Vec3.createVectorHelper(0, -strength, 0);
	// 		heightVec.rotateAroundZ(rot);
			
	// 		double y = heightVec.yCoord;
			
	// 		double sectionRad = Math.sqrt(Math.pow(strength, 2) - Math.pow(y, 2));
	// 		double circumference = 2 * Math.PI * sectionRad;
			
	// 		//Radial
	// 		//StartCir starts at circumference
	// 		for(int r = startCir; r < circumference; r ++) {
				
	// 			Vec3 vec = Vec3.createVectorHelper(sectionRad, y, 0);
	// 			vec = vec.normalize();
	// 			/*if(y > 0)
	// 				vec.rotateAroundZ((float) (y / sectionRad) * 0.15F);*/
	// 			/*if(y < 0)
	// 				vec.rotateAroundZ((float) (y / sectionRad) * 0.15F);*/
	// 			vec.rotateAroundY((float) (360 / circumference * r));
				
	// 			int length = (int)Math.ceil(strength);
				
	// 			float res = strength;
				
	// 			FloatTriplet lastPos = null;
				
	// 			for(int i = 0; i < length; i ++) {
					
	// 				if(i > this.length)
	// 					break;
					
	// 				float x0 = (float) (posX + (vec.xCoord * i));
	// 				float y0 = (float) (posY + (vec.yCoord * i));
	// 				float z0 = (float) (posZ + (vec.zCoord * i));
	// 				pos.setPos(x0, y0, z0);
	// 				double fac = 100 - ((double) i) / ((double) length) * 100;
	// 				fac *= 0.07D;
					
	// 				if(!world.getBlockState(pos).getMaterial().isLiquid())
	// 					res -= Math.pow(world.getBlockState(pos).getBlock().getExplosionResistance(null), 7.5D - fac);
	// 				else
	// 					res -= Math.pow(Blocks.AIR.getExplosionResistance(null), 7.5D - fac);
	
	// 				if(res > 0 && world.getBlockState(pos).getBlock() != Blocks.AIR) {
	// 					lastPos = new FloatTriplet(x0, y0, z0);
	// 				}
					
	// 				if(res <= 0 || i + 1 >= this.length) {
	// 					if(affectedBlocks.size() < Integer.MAX_VALUE - 100 && lastPos != null) {
	// 						affectedBlocks.add(new FloatTriplet(lastPos.xCoord, lastPos.yCoord, lastPos.zCoord));
	// 					}
	// 					break;
	// 				}
	// 			}
				
	// 			raysProcessed++;
				
	// 			if(raysProcessed >= count) {
	// 				startY = v;
	// 				startCir = startCir + 1;
	// 				return;
	// 			}
	// 		}
	// 	}
		
	// 	isAusf3Complete = true;
	// }

	public void processTip(int count) {
		MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int processedBlocks = 0;
		int braker = 0;
		
		for(int l = 0; l < Integer.MAX_VALUE; l++) {

			if(processedBlocks >= count)
				return;
			
			if(braker >= count * 50)
				return;

            if(l > affectedBlocks.size() - 1)
            	break;
            
            if(affectedBlocks.isEmpty())
            	return;
            
            int in = affectedBlocks.size() - 1;
            
			float x = affectedBlocks.get(in).xCoord;
			float y = affectedBlocks.get(in).yCoord;
			float z = affectedBlocks.get(in).zCoord;
			pos.setPos(x, y, z);
			world.setBlockToAir(pos);
			
			Vec3 vec = Vec3.createVectorHelper(x - this.posX, y - this.posY, z - this.posZ);
			double pX = vec.xCoord / vec.lengthVector();
			double pY = vec.yCoord / vec.lengthVector();
			double pZ = vec.zCoord / vec.lengthVector();
			
			for(int i = 0; i < vec.lengthVector(); i ++) {
				int x0 = (int)(posX + pX * i);
				int y0 = (int)(posY + pY * i);
				int z0 = (int)(posZ + pZ * i);
				pos.setPos(x0, y0, z0);
				if(!world.isAirBlock(pos)) {
					world.setBlockToAir(pos);
					processedBlocks++;
				}
				
				braker++;
			}
			affectedBlocks.remove(in);
		}
		processed += count;
	}
	

	public void collectTip(int count) {
		MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for(int k = 0; k < count; k++) {
			double phi = rand.nextDouble() * (Math.PI * 2);
			double costheta = rand.nextDouble() * 2 - 1;
			double theta = Math.acos(costheta);
			double x = Math.sin(theta) * Math.cos(phi);
			double y = Math.sin(theta) * Math.sin(phi);
			double z = Math.cos(theta);
			
			Vec3 vec = Vec3.createVectorHelper(x, y, z);
			int length = (int)Math.ceil(this.radius);
			
			float res = this.radius;
			
			FloatTriplet lastPos = null;
			Explosion dummy = Library.explosionDummy(world, x, y, z);
			
			for(int i = 0; i < length; i ++) {
				
				if(i > this.radius)
					break;
				
				float x0 = (float) (posX + (vec.xCoord * i));
				float y0 = (float) (posY + (vec.yCoord * i));
				float z0 = (float) (posZ + (vec.zCoord * i));
				pos.setPos(x0, y0, z0);
				if(!world.getBlockState(pos).getMaterial().isLiquid())
					res -= Math.pow(world.getBlockState(pos).getBlock().getExplosionResistance(world, pos, null, dummy), 1.25);
				else
					res -= Math.pow(Blocks.AIR.getExplosionResistance(world, pos, null, dummy), 1.25);

				if(res > 0 && world.getBlockState(pos).getBlock() != Blocks.AIR) {
					lastPos = new FloatTriplet(x0, y0, z0);
				}
				
				if(res <= 0 || i + 1 >= this.radius) {
					if(affectedBlocks.size() < Integer.MAX_VALUE - 100 && lastPos != null)
						affectedBlocks.add(new FloatTriplet(lastPos.xCoord, lastPos.yCoord, lastPos.zCoord));
					break;
				}
			}
		}
	}

	
	@SuppressWarnings("deprecation")
	public void collectTipExperimental(int count) {
		MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for(int k = 0; k < count; k++) {
			double phi = rand.nextDouble() * (Math.PI * 2);
			double costheta = rand.nextDouble() * 2 - 1;
			double theta = Math.acos(costheta);
			double x = Math.sin(theta) * Math.cos(phi);
			double y = Math.sin(theta) * Math.sin(phi);
			double z = Math.cos(theta);
			
			Vec3 vec = Vec3.createVectorHelper(x, y, z);
			int length = (int)Math.ceil(this.radius);
			
			float res = this.radius;
			
			FloatTriplet lastPos = null;
			
			for(int i = 0; i < length; i ++) {
				
				if(i > this.radius)
					break;
				
				float x0 = (float) (posX + (vec.xCoord * i));
				float y0 = (float) (posY + (vec.yCoord * i));
				float z0 = (float) (posZ + (vec.zCoord * i));
				pos.setPos(x0, y0, z0);
				double fac = 100 - ((double) i) / ((double) length) * 100;
				fac *= 0.07D;
				
				if(!world.getBlockState(pos).getMaterial().isLiquid())
					res -= Math.pow(world.getBlockState(pos).getBlock().getExplosionResistance(null), 7.5D - fac);
				else
					res -= Math.pow(Blocks.AIR.getExplosionResistance(null), 7.5D - fac);

				if(res > 0 && world.getBlockState(pos).getBlock() != Blocks.AIR) {
					lastPos = new FloatTriplet(x0, y0, z0);
				}
				
				if(res <= 0 || i + 1 >= this.radius) {
					if(affectedBlocks.size() < Integer.MAX_VALUE - 100 && lastPos != null)
						affectedBlocks.add(new FloatTriplet(lastPos.xCoord, lastPos.yCoord, lastPos.zCoord));
					break;
				}
			}
		}
	}
	
	
	
	
	public void deleteStorage() {
		this.affectedBlocks.clear();
	}
	
	public int getStoredSize() {
		return this.affectedBlocks.size();
	}
	
	public int getProgress() {
		return this.processed;
	}
	
	public class FloatTriplet {
		public float xCoord;
		public float yCoord;
		public float zCoord;
		
		public FloatTriplet(float x, float y, float z) {
			xCoord = x;
			yCoord = y;
			zCoord = z;
		}
	}
}
