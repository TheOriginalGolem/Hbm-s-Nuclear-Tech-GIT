package com.hbm.tileentity;

import net.minecraft.nbt.NBTTagCompound;

public interface INBTPacketReceiver {
    void networkUnpack(NBTTagCompound nbt);
}
