package com.robolancers.lib.wrappers.motors;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Gearbox {
    private double voltageNominal, freeSpeed, freeCurrent, stallCurrent, stallTorque;

    public Gearbox(double voltageNominal, double freeSpeed, double freeCurrent, double stallCurrent, double stallTorque, double reduction, double numberOfMotors){
        this.voltageNominal = voltageNominal;
        this.freeSpeed = freeSpeed / reduction;
        this.freeCurrent = freeCurrent * numberOfMotors;
        this.stallCurrent = stallCurrent * numberOfMotors;
        this.stallTorque = stallTorque * reduction * numberOfMotors;
    }

    public double R(){
        return voltageNominal / stallCurrent;
    }

    public double kw(){
        return (voltageNominal - freeCurrent * R()) / freeSpeed;
    }

    public double kt(){
        return stallCurrent / stallTorque;
    }

    public double getCurrent(double voltage, double angularVelocity){
        return (voltage - kw() * angularVelocity) / R();
    }

    public double getTorque(double current){
        return current / kt();
    }
}
