package api.hbm.energy;

import net.minecraft.item.ItemStack;

public interface IBatteryItem {

    void chargeBattery(ItemStack stack, long i);

    void setCharge(ItemStack stack, long i);

    void dischargeBattery(ItemStack stack, long i);

    long getCharge(ItemStack stack);

    long getMaxCharge();

    long getChargeRate();

    long getDischargeRate();
}
