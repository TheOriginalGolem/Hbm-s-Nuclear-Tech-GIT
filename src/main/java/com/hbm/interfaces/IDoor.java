package com.hbm.interfaces;

public interface IDoor {

	void open();
	void close();
	DoorState getState();
	void toggle();
	default boolean setTexture(String tex){
		return false;
	}
	default void setTextureState(byte tex){}

    enum DoorState {
		CLOSED,
		OPEN,
		CLOSING,
		OPENING
    }
}
