package com.hbm.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;
import org.lwjgl.util.vector.Vector4f;

import com.hbm.animloader.AnimationWrapper;
import com.hbm.animloader.AnimationWrapper.EndResult;
import com.hbm.animloader.AnimationWrapper.EndType;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.packet.AuxButtonPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.particle.ParticleLightningHandGlow;
import com.hbm.particle.ParticleLightningStrip;
import com.hbm.util.BobMathUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = RefStrings.MODID)
public class WorldSpaceFPRender {

	public static int ticksActive = -1;
	private static long renderTime;
	public static AnimationWrapper wrapper;
	public static List<ParticleLightningStrip> lightning_strips = new ArrayList<>();
	public static List<Particle> particles = new ArrayList<>(); 
	
	@SubscribeEvent
	public static void renderHand(RenderHandEvent e) {
		if(true || ticksActive < 0)
			return;
		e.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void doDepthRender(CameraSetup e){
		if(true || Minecraft.getMinecraft().gameSettings.thirdPersonView != 0 || ticksActive < 0)
			return;
			
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		Project.gluPerspective(70, (float) Minecraft.getMinecraft().displayWidth / (float) Minecraft.getMinecraft().displayHeight, 0.05F, Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16F * 2.0F);
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glPushMatrix();
			
		GL11.glTranslated(-0.3, 0, -2.25);
		GL11.glRotated(90, 0, 1, 0);
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.skin);
		ResourceManager.lightning_fp.controller.setAnim(wrapper);
		GlStateManager.colorMask(false, false, false, false);
		ResourceManager.maxdepth.use();
		ResourceManager.lightning_fp.renderAnimated(renderTime = System.currentTimeMillis());
		HbmShaderManager2.releaseShader();
		GlStateManager.colorMask(true, true, true, true);
		GL11.glPopMatrix();
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
	}

	public static void doHandRendering(RenderWorldLastEvent e) {
		if(true || Minecraft.getMinecraft().gameSettings.thirdPersonView != 0)
			return;
		
		GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		Project.gluPerspective(70, (float) Minecraft.getMinecraft().displayWidth / (float) Minecraft.getMinecraft().displayHeight, 0.05F, Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16F * 2.0F);
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glPushMatrix();
		
		if(ticksActive >= 0){
			GL11.glPushMatrix();
			GL11.glTranslated(-0.3, 0, -2.25);
			GL11.glRotated(90, 0, 1, 0);
	
			RenderHelper.enableStandardItemLighting();
			Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.skin);
	        ResourceManager.lightning_fp.controller.setAnim(wrapper);
	        ResourceManager.lightning_fp.renderAnimated(renderTime, (last, first, name) -> {
	        	if(name.equals("lower")){
	        		if(ticksActive < 55)
	        		for(ParticleLightningStrip p : lightning_strips){
	        			p.setNewPoint(BobMathUtil.glWorldFromLocalNoView(new Vector4f(0.156664F, -0.60966F, -0.252432F, 1))[0]);
	        		}
	        		for(Particle p : particles){
	        			p.renderParticle(Tessellator.getInstance().getBuffer(), Minecraft.getMinecraft().getRenderViewEntity(), e.getPartialTicks(), 0, 0, 0, 0, 0);
	        		}
	        		
	        		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.skin);
	        	}
	        	return false;
	        });
	        GL11.glPopMatrix();
		}
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.turbofan_blades_tex);
        for(ParticleLightningStrip p : lightning_strips){
        	if(p != null)
        		p.renderParticle(Tessellator.getInstance().getBuffer(), Minecraft.getMinecraft().getRenderViewEntity(), e.getPartialTicks(), 0, 0, 0, 0, 0);
        }
        
		GL11.glPopMatrix();
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
	}
	
	@SubscribeEvent
	public static void worldTick(TickEvent.ClientTickEvent e){
		if(true || e.phase == Phase.END || Minecraft.getMinecraft().world == null)
			return;
		Random rand = Minecraft.getMinecraft().world.rand;
		if(ticksActive >= 0){
			ticksActive ++;
			if(ticksActive >= 84){
				ticksActive = -1;
			}
			particles.add(new ParticleLightningHandGlow(Minecraft.getMinecraft().world, 0.156664F, -0.60966F, -0.252432F, 2+rand.nextFloat()*0.5F, 3+rand.nextInt(3)).color(0.8F, 0.9F, 1F, 1F));
		} else if(Keyboard.isKeyDown(Keyboard.KEY_I)) {
			ticksActive = 0;
			PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(0, 0, 0, 1000, 0));
			wrapper = new AnimationWrapper(System.currentTimeMillis(), ResourceManager.lightning_fp_anim).onEnd(new EndResult(EndType.END, null));
			lightning_strips.clear();
			lightning_strips.add(new ParticleLightningStrip(Minecraft.getMinecraft().world, 0, 0, 0));
			lightning_strips.add(new ParticleLightningStrip(Minecraft.getMinecraft().world, 0, 0, 0));
		}
		Iterator<ParticleLightningStrip> iter = lightning_strips.iterator();
		while(iter.hasNext()){
			Particle p = iter.next();
			p.onUpdate();
			if(!p.isAlive())
				iter.remove();
		}
		Iterator<Particle> iter2 = particles.iterator();
		while(iter2.hasNext()){
			Particle p = iter2.next();
			p.onUpdate();
			if(!p.isAlive())
				iter2.remove();
		}
	}

}
