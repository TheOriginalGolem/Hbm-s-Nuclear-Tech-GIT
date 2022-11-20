package com.hbm.entity.particle;

import java.util.Random;

import com.hbm.main.ModEventHandlerClient;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleContrail extends Particle {

	private TextureManager theRenderEngine;
	private int age = 0;
	private int maxAge;

	public ParticleContrail(TextureManager manage, World worldIn, double posXIn, double posYIn, double posZIn) {
		super(worldIn, posXIn, posYIn, posZIn);
		theRenderEngine = manage;
		maxAge = 400 + rand.nextInt(50);

		this.particleRed = this.particleGreen = this.particleBlue = 0;
		this.particleScale = 1F;
	}

	public ParticleContrail(TextureManager p_i1213_1_, World p_i1218_1_, double p_i1218_2_, double p_i1218_4_, double p_i1218_6_, float red, float green, float blue, float scale) {
		super(p_i1218_1_, p_i1218_2_, p_i1218_4_, p_i1218_6_);
		theRenderEngine = p_i1213_1_;
		maxAge = 600 + rand.nextInt(50);

		this.particleRed = red;
		this.particleGreen = green;
		this.particleBlue = blue;

		this.particleScale = scale;
	}


	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		particleAlpha = 1F - (float)Math.pow((float) this.age / (float) this.maxAge, 2);

		this.age++;

		if (this.age == this.maxAge) {
			this.setExpired();
			;
		}
	}

	@Override
	public int getFXLayer() {
		return 1;
	}
	
	@Override
	public boolean shouldDisableDepth() {
		return true;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		
		this.theRenderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		this.particleTexture = ModEventHandlerClient.contrail;
		float f = (float) this.particleTextureIndexX / 16.0F;
		float f1 = f + 0.0624375F;
		float f2 = (float) this.particleTextureIndexY / 16.0F;
		float f3 = f2 + 0.0624375F;
		float f4 = (1-particleAlpha)*3F + 1F + 0.5F * this.particleScale;

		if (this.particleTexture != null) {
			f = this.particleTexture.getMinU();
			f1 = this.particleTexture.getMaxU();
			f2 = this.particleTexture.getMinV();
			f3 = this.particleTexture.getMaxV();
		}

		float mod = particleAlpha * 0.1F;
		Random urandom = new Random(this.hashCode());
		for (int ii = 0; ii < 6; ii++) {
			
			float f5 = (float) ((this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX) + urandom.nextGaussian() * 0.5);
			float f6 = (float) ((this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY) + urandom.nextGaussian() * 0.5);
			float f7 = (float) ((this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ) + urandom.nextGaussian() * 0.5);
			
			int i = this.getBrightnessForRender(partialTicks);
			int j = i >> 16 & 65535;
			int k = i & 65535;
			
			Vec3d[] avec3d = new Vec3d[] { new Vec3d((double) (-rotationX * f4 - rotationXY * f4), (double) (-rotationZ * f4), (double) (-rotationYZ * f4 - rotationXZ * f4)), new Vec3d((double) (-rotationX * f4 + rotationXY * f4), (double) (rotationZ * f4), (double) (-rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double) (rotationX * f4 + rotationXY * f4), (double) (rotationZ * f4), (double) (rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double) (rotationX * f4 - rotationXY * f4), (double) (-rotationZ * f4), (double) (rotationYZ * f4 - rotationXZ * f4)) };

			if (this.particleAngle != 0.0F) {
				float f8 = this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks;
				float f9 = MathHelper.cos(f8 * 0.5F);
				float f10 = MathHelper.sin(f8 * 0.5F) * (float) cameraViewDir.x;
				float f11 = MathHelper.sin(f8 * 0.5F) * (float) cameraViewDir.y;
				float f12 = MathHelper.sin(f8 * 0.5F) * (float) cameraViewDir.z;
				Vec3d vec3d = new Vec3d((double) f10, (double) f11, (double) f12);

				for (int l = 0; l < 4; ++l) {
					avec3d[l] = vec3d.scale(2.0D * avec3d[l].dotProduct(vec3d)).add(avec3d[l].scale((double) (f9 * f9) - vec3d.dotProduct(vec3d))).add(vec3d.crossProduct(avec3d[l]).scale((double) (2.0F * f9)));
				}
			}
			
			buffer.pos((double) f5 + avec3d[0].x, (double) f6 + avec3d[0].y, (double) f7 + avec3d[0].z).tex((double) f1, (double) f3).color(this.particleRed + mod, this.particleGreen + mod, this.particleBlue + mod, this.particleAlpha).lightmap(j, k).endVertex();
			buffer.pos((double) f5 + avec3d[1].x, (double) f6 + avec3d[1].y, (double) f7 + avec3d[1].z).tex((double) f1, (double) f2).color(this.particleRed + mod, this.particleGreen + mod, this.particleBlue + mod, this.particleAlpha).lightmap(j, k).endVertex();
			buffer.pos((double) f5 + avec3d[2].x, (double) f6 + avec3d[2].y, (double) f7 + avec3d[2].z).tex((double) f, (double) f2).color(this.particleRed + mod, this.particleGreen + mod, this.particleBlue + mod, this.particleAlpha).lightmap(j, k).endVertex();
			buffer.pos((double) f5 + avec3d[3].x, (double) f6 + avec3d[3].y, (double) f7 + avec3d[3].z).tex((double) f, (double) f3).color(this.particleRed + mod, this.particleGreen + mod, this.particleBlue + mod, this.particleAlpha).lightmap(j, k).endVertex();
		}
	}

	@Override
	public int getBrightnessForRender(float p_189214_1_) {
		return 240;
	}
}
