package com.robolancers.lib.auto;

import org.ghrobotics.lib.mathematics.units.TimeUnitsKt;
import org.ghrobotics.lib.utils.DeltaTime;

@SuppressWarnings({"unused", "WeakerAccess"})
public class LancerPID {
    private double kP, kI, kD,  kF = 0;
    private double errorSum = 0;

    private double target = 0;
    private double error;

    private boolean firstRun;
    private double lastActual = 0;

    private DeltaTime deltaTimeController;

    public LancerPID(double p, double i, double d) {
        kP = p;
        kI = i;
        kD = d;

        firstRun = true;

        deltaTimeController = new DeltaTime(TimeUnitsKt.getMillisecond(System.currentTimeMillis()));
    }

    public LancerPID(double p, double i, double d, double f) {
        this(p, i, d);

        kF = f;
    }

    public void setTarget(double target) {
        this.target = target;
        deltaTimeController.reset();
    }

    public double getOutput(double actual) {
        double deltaTime = deltaTimeController.updateTime(TimeUnitsKt.getMillisecond(System.currentTimeMillis())).getSecond();

        error = target - actual;
        errorSum += error * deltaTime;

        if(firstRun) {
            lastActual = actual;
            firstRun = false;
        }

        double fOutput = kF * target;
        double pOutput = kP * error;
        double iOutput = kI * errorSum;
        double dOutput = kD * ((actual - lastActual) / deltaTime);

        double output = fOutput + pOutput + errorSum + dOutput;

        lastActual = actual;

        return output;
    }

    public double getError(){
        return error;
    }

    public void reset() {
        errorSum = 0;
        lastActual = 0;
        deltaTimeController.reset();
        firstRun = true;
    }
}
