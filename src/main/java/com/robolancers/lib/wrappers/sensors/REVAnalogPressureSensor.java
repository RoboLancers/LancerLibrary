package com.robolancers.lib.wrappers.sensors;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.RobotController;

@SuppressWarnings("unused")
public class REVAnalogPressureSensor {
    private AnalogInput analogInput;

    public REVAnalogPressureSensor(int port){
        analogInput = new AnalogInput(port);
    }

    public double getPressure(){
        return (250 * (analogInput.getVoltage() / RobotController.getVoltage5V())) - 25;
    }
}
