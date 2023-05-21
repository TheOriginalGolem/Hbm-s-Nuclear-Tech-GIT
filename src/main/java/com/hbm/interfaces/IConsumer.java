package com.hbm.interfaces;

public interface IConsumer {
    //had to re add the wretched fungus because I realized porting all machines to the new energy system to then test is a shitty idea
    void setPower(long i);

    long getPower();

    long getMaxPower();
}
