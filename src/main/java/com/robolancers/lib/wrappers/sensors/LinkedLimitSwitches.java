package com.robolancers.lib.wrappers.sensors;

import edu.wpi.first.wpilibj.DigitalInput;

import java.util.stream.Stream;

@SuppressWarnings("unused")
public class LinkedLimitSwitches {
    private DigitalInput[] limitSwitches;
    private boolean invert;

    public LinkedLimitSwitches(int... ports){
        limitSwitches = new DigitalInput[ports.length];

        for(int i = 0; i < limitSwitches.length; i++){
            limitSwitches[i] = new DigitalInput(ports[i]);
        }
    }

    public void invert(boolean invert){
        this.invert = invert;
    }

    public boolean get(){
        return Stream.of(limitSwitches).anyMatch(digitalInput -> invert != digitalInput.get());
    }

    public boolean get(int index) {
        return limitSwitches[index].get();
    }
}
