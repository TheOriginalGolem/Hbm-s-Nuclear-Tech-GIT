package com.hbm.entity.logic;

import java.util.ArrayList;
import java.util.List;
import com.hbm.entity.logic.IChunkLoader;
import com.hbm.main.MainRegistry;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraft.util.math.ChunkPos;

import org.apache.logging.log4j.Level;

import com.hbm.config.BombConfig;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.effect.EntityFalloutRain;
import com.hbm.explosion.ExplosionFleija;
import com.hbm.explosion.ExplosionHurtUtil;
import com.hbm.explosion.ExplosionNukeAdvanced;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.explosion.ExplosionSolinium;
import com.hbm.interfaces.Spaghetti;
import com.hbm.main.MainRegistry;

import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

@Spaghetti("why???")
public class EntityNukeExplosionMK3 extends Entity implements IChunkLoader {
	
	public int age = 0;
	public int destructionRange = 0;
	public ExplosionNukeAdvanced exp;
	public ExplosionNukeAdvanced wst;
	public ExplosionNukeAdvanced vap;
	public ExplosionFleija expl;
	public ExplosionSolinium sol;
	public int speed = 1;
	public float coefficient = 1;
	public float coefficient2 = 1;
	public boolean did = false;
	public boolean did2 = false;
	public boolean waste = true;
	//Extended Type
	public int extType = 0;
	private Ticket loaderTicket;

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		age = nbt.getInteger("age");
		destructionRange = nbt.getInteger("destructionRange");
		speed = nbt.getInteger("speed");
		coefficient = nbt.getFloat("coefficient");
		coefficient2 = nbt.getFloat("coefficient2");
		did = nbt.getBoolean("did");
		did2 = nbt.getBoolean("did2");
		waste = nbt.getBoolean("waste");
		extType = nbt.getInteger("extType");
		
		long time = nbt.getLong("milliTime");
		
		if(BombConfig.limitExplosionLifespan > 0 && System.currentTimeMillis() - time > BombConfig.limitExplosionLifespan * 1000)
			this.setDead();
		
    	if(this.waste)
    	{
        	exp = new ExplosionNukeAdvanced((int)this.posX, (int)this.posY, (int)this.posZ, this.world, this.destructionRange, this.coefficient, 0);
			exp.readFromNbt(nbt, "exp_");
    		wst = new ExplosionNukeAdvanced((int)this.posX, (int)this.posY, (int)this.posZ, this.world, (int)(this.destructionRange * 1.8), this.coefficient, 2);
			wst.readFromNbt(nbt, "wst_");
    		vap = new ExplosionNukeAdvanced((int)this.posX, (int)this.posY, (int)this.posZ, this.world, (int)(this.destructionRange * 2.5), this.coefficient, 1);
			vap.readFromNbt(nbt, "vap_");
    	} else {

    		if(extType == 0) {
    			expl = new ExplosionFleija((int)this.posX, (int)this.posY, (int)this.posZ, this.world, this.destructionRange, this.coefficient, this.coefficient2);
				expl.readFromNbt(nbt, "expl_");
    		}
    		if(extType == 1) {
    			sol = new ExplosionSolinium((int)this.posX, (int)this.posY, (int)this.posZ, this.world, this.destructionRange, this.coefficient, this.coefficient2);
    			sol.readFromNbt(nbt, "sol_");
    		}
    	}
    	
    	this.did = true;
		
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("age", age);
		nbt.setInteger("destructionRange", destructionRange);
		nbt.setInteger("speed", speed);
		nbt.setFloat("coefficient", coefficient);
		nbt.setFloat("coefficient2", coefficient2);
		nbt.setBoolean("did", did);
		nbt.setBoolean("did2", did2);
		nbt.setBoolean("waste", waste);
		nbt.setInteger("extType", extType);
		
		nbt.setLong("milliTime", System.currentTimeMillis());
    	
		if(exp != null)
			exp.saveToNbt(nbt, "exp_");
		if(wst != null)
			wst.saveToNbt(nbt, "wst_");
		if(vap != null)
			vap.saveToNbt(nbt, "vap_");
		if(expl != null)
			expl.saveToNbt(nbt, "expl_");
		if(sol != null)
			sol.saveToNbt(nbt, "sol_");
		
	}

	public EntityNukeExplosionMK3(World p_i1582_1_) {
		super(p_i1582_1_);
	}

    @Override
	public void onUpdate() {
        super.onUpdate();
        	
        if(world.isRemote)
        	return;
        if(!this.did)
        {
    		if(GeneralConfig.enableExtendedLogging && !world.isRemote)
    			MainRegistry.logger.log(Level.INFO, "[NUKE] Initialized mk3 explosion at " + posX + " / " + posY + " / " + posZ + " with strength " + destructionRange + "!");
    		
        	if(this.waste)
        	{
            	exp = new ExplosionNukeAdvanced((int)this.posX, (int)this.posY, (int)this.posZ, this.world, this.destructionRange, this.coefficient, 0);
        		wst = new ExplosionNukeAdvanced((int)this.posX, (int)this.posY, (int)this.posZ, this.world, (int)(this.destructionRange * 1.8), this.coefficient, 2);
        		vap = new ExplosionNukeAdvanced((int)this.posX, (int)this.posY, (int)this.posZ, this.world, (int)(this.destructionRange * 2.5), this.coefficient, 1);
        	} else {
        		if(extType == 0)
        			expl = new ExplosionFleija((int)this.posX, (int)this.posY, (int)this.posZ, this.world, this.destructionRange, this.coefficient, this.coefficient2);
        		if(extType == 1)
        			sol = new ExplosionSolinium((int)this.posX, (int)this.posY, (int)this.posZ, this.world, this.destructionRange, this.coefficient, this.coefficient2);
        	}
        	
        	this.did = true;
        }
        
        speed += 1;	//increase speed to keep up with expansion
        
        boolean flag = false;
        boolean flag3 = false;
        
        for(int i = 0; i < this.speed; i++)
        {
        	if(waste) {
        		flag = exp.update();
        		flag3 = vap.update();
        		
        		if(flag3) {
        			this.setDead();
        		}
        	} else {
        		if(extType == 0)
        			if(expl.update())
        				this.setDead();
        		if(extType == 1)
        			if(sol.update())
        				this.setDead();
        	}
        }
        	
        if(!flag)
        {
        	this.world.playSound(this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.AMBIENT, 10000.0F, 0.8F + this.rand.nextFloat() * 0.2F, true);
        	if(waste || extType != 1) {
        		ExplosionNukeGeneric.dealDamage(this.world, this.posX, this.posY, this.posZ, this.destructionRange * 2);
        	} else {
        		ExplosionHurtUtil.doRadiation(world, posX, posY, posZ, 15000, 250000, this.destructionRange);
        	}
        } else {
			if (!did2 && waste) {
				EntityFalloutRain fallout = new EntityFalloutRain(this.world, (int)(this.destructionRange * 1.8) * 10);
				fallout.posX = this.posX;
				fallout.posY = this.posY;
				fallout.posZ = this.posZ;
				fallout.setScale((int)(this.destructionRange * 1.8));

				this.world.spawnEntity(fallout);
				//this.world.getWorldInfo().setRaining(true);
				
				did2 = true;
        	}
        }
        
        age++;
    }

	@Override
	protected void entityInit() {
		init(ForgeChunkManager.requestTicket(MainRegistry.instance, world, Type.ENTITY));
	}

	@Override
	public void init(Ticket ticket) {
		if(!world.isRemote) {
			
            if(ticket != null) {
            	
                if(loaderTicket == null) {
                	
                	loaderTicket = ticket;
                	loaderTicket.bindEntity(this);
                	loaderTicket.getModData();
                }

                ForgeChunkManager.forceChunk(loaderTicket, new ChunkPos(chunkCoordX, chunkCoordZ));
            }
        }
	}

	List<ChunkPos> loadedChunks = new ArrayList<ChunkPos>();
	@Override
	public void loadNeighboringChunks(int newChunkX, int newChunkZ) {
		if(!world.isRemote && loaderTicket != null)
        {
            for(ChunkPos chunk : loadedChunks)
            {
                ForgeChunkManager.unforceChunk(loaderTicket, chunk);
            }

            loadedChunks.clear();
            loadedChunks.add(new ChunkPos(newChunkX, newChunkZ));
            loadedChunks.add(new ChunkPos(newChunkX + 1, newChunkZ + 1));
            loadedChunks.add(new ChunkPos(newChunkX - 1, newChunkZ - 1));
            loadedChunks.add(new ChunkPos(newChunkX + 1, newChunkZ - 1));
            loadedChunks.add(new ChunkPos(newChunkX - 1, newChunkZ + 1));
            loadedChunks.add(new ChunkPos(newChunkX + 1, newChunkZ));
            loadedChunks.add(new ChunkPos(newChunkX, newChunkZ + 1));
            loadedChunks.add(new ChunkPos(newChunkX - 1, newChunkZ));
            loadedChunks.add(new ChunkPos(newChunkX, newChunkZ - 1));

            for(ChunkPos chunk : loadedChunks)
            {
                ForgeChunkManager.forceChunk(loaderTicket, chunk);
            }
        }
	}
}
