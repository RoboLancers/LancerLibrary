package com.robolancers.lib.wrappers.sensors;

import edu.wpi.first.wpilibj.AnalogInput;

@SuppressWarnings("unused")
public class MB1013 {
    private AnalogInput input;

    private double vcc = 5;
    private double scaleFactor = (vcc/1024) / 5;

    public MB1013(int channel){
        input = new AnalogInput(channel);
    }

    public double getDistanceInFeet(){
        return input.getVoltage() / scaleFactor;
    }
}
