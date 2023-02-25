package com.hbm.interfaces;

import com.hbm.forgefluid.FFPipeNetwork;
import net.minecraftforge.fluids.Fluid;

public interface IFluidPipe {


    FFPipeNetwork getNetwork();

    void setNetwork(FFPipeNetwork net);

    FFPipeNetwork getNetworkTrue();

    Fluid getType();

    void setType(Fluid fluid);

    boolean getIsValidForForming();

    void breakBlock();

    void setTypeTrue(Fluid fluid);
}
