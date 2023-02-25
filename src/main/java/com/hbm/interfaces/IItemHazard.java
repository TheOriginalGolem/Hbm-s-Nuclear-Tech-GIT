package com.hbm.interfaces;

import com.hbm.modules.ItemHazardModule;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

public interface IItemHazard {

    ItemHazardModule getModule();

    default IItemHazard addRadiation(float radiation) {
        this.getModule().addRadiation(radiation);
        return this;
    }

    default IItemHazard addDigamma(float digamma) {
        this.getModule().addDigamma(digamma);
        return this;
    }

    default IItemHazard addFire(int fire) {
        this.getModule().addFire(fire);
        return this;
    }

    default IItemHazard addAsbestos(int asbestos) {
        this.getModule().addAsbestos(asbestos);
        return this;
    }

    default IItemHazard addCoal(int coal) {
        this.getModule().addCoal(coal);
        return this;
    }

    default IItemHazard addBlinding() {
        this.getModule().addBlinding();
        return this;
    }

    default IItemHazard addHydroReactivity() {
        this.getModule().addHydroReactivity();
        return this;
    }

    default IItemHazard addExplosive(float bang) {
        this.getModule().addExplosive(bang);
        return this;
    }

    //the only ugly part of this entire system is the manual casting so that the rest of the daisychained setters work
    default Item toItem() {
        return (Item) this;
    }

    default Block toBlock() {
        return (Block) this;
    }
}