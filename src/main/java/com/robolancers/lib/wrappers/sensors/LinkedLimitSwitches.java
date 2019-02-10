package com.robolancers.lib.wrappers.sensors;

import edu.wpi.first.wpilibj.DigitalInput;

import java.util.stream.Stream;

@SuppressWarnings("unused")
public class LinkedLimitSwitches {
    private DigitalInput[] limitSwitches;

    public LinkedLimitSwitches(int... ports){
        limitSwitches = new DigitalInput[ports.length];

        for(int i = 0; i < limitSwitches.length; i++){
            limitSwitches[i] = new DigitalInput(ports[i]);
        }
    }

    public boolean get(){
        return Stream.of(limitSwitches).anyMatch(DigitalInput::get);
    }

    public boolean get(int index) {
        return limitSwitches[index].get();
    }
}
