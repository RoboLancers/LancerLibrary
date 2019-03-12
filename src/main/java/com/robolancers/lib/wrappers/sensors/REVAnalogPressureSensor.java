package com.robolancers.lib.wrappers.sensors;

import edu.wpi.first.wpilibj.AnalogInput;

@SuppressWarnings("unused")
public class REVAnalogPressureSensor {
    private AnalogInput analogInput;

    public REVAnalogPressureSensor(int port){
        analogInput = new AnalogInput(port);
    }

    public double getPressure(){
        return (250 * (analogInput.getVoltage() / 5.0)) - 25;
    }
}
