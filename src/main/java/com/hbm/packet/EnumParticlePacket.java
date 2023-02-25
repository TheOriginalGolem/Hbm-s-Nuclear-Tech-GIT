package com.hbm.packet;

import com.hbm.particle.EnumHbmParticles;
import com.hbm.particle.ParticleManager;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

public class EnumParticlePacket implements IMessage {

	EnumHbmParticles particle;
	double x, y, z, strength;
	int count;
	
	public EnumParticlePacket() {
	}
	
	public EnumParticlePacket(EnumHbmParticles particle, double x, double y, double z, int count, double strength) {
		this.particle = particle;
		this.count = count;
		this.strength = strength;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public EnumParticlePacket(EnumHbmParticles particle, double x, double y, double z, int count) {
		this(particle, x, y, z, count, 0);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		particle = EnumHbmParticles.values()[buf.readInt()];
		x = buf.readDouble();
		y = buf.readDouble();
		z = buf.readDouble();
		strength = buf.readDouble();
		count = buf.readInt();
		
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(particle.ordinal());
		buf.writeDouble(x);
		buf.writeDouble(y);
		buf.writeDouble(z);
		buf.writeDouble(strength);
		buf.writeInt(count);
		
	}
	
	public static class Handler implements IMessageHandler<EnumParticlePacket, IMessage> {

		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(EnumParticlePacket message, MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
                if (Objects.requireNonNull(message.particle) == EnumHbmParticles.PARTICLES) {
                    ParticleManager.spawnParticles(message.x, message.y, message.z, message.count);
                }
			});
			return null;
		}
		
	}

}
