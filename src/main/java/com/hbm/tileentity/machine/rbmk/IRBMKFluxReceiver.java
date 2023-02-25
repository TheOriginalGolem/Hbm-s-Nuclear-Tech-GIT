package com.hbm.tileentity.machine.rbmk;

public interface IRBMKFluxReceiver {
    void receiveFlux(NType type, double flux);

    enum NType {
        FAST("trait.rbmk.neutron.fast"),
        SLOW("trait.rbmk.neutron.slow"),
        ANY("trait.rbmk.neutron.any");    //not to be used for reactor flux calculation, only for the fuel designation

        public String unlocalized;

        NType(String loc) {
            this.unlocalized = loc;
        }
    }
}
