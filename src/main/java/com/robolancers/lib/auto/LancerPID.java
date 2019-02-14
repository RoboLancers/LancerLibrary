package com.robolancers.lib.auto;

@SuppressWarnings({"unused", "UnusedAssignment"})
public class LancerPID {
    private double kP = 0, kI = 0, kD = 0,  kF = 0;
    private double output = 0, kPoutput = 0, kIoutput = 0, kDoutput = 0, kFoutput = 0;

    private double time = 0.02;
    private double target = 0;
    private double error;

    private boolean firstRun;
    private double lastActual = 0;

    public LancerPID(double p, double i, double d) {
        kP = p;
        kI = i;
        kD = d;

        firstRun = true;
    }

    public LancerPID(double p, double i, double d, double f) {
        this(p, i, d);
        kF = f;
    }

    public double setTarget(double target) {
        return this.target = target;
    }

    public double getOutput(double actual) {
        error = target - actual;

        if(firstRun) {
            lastActual = actual;
            firstRun = false;
        }

        kFoutput = kF * target;
        kPoutput = kP * error;
        kIoutput += error * time;
        kDoutput = kD * ((actual - lastActual)/ time);

        output = kFoutput + kPoutput + kIoutput + kDoutput;

        lastActual = actual;
        return output;
    }

    public double getError(){
        return error;
    }

    public void reset() {
        kIoutput = 0;
        lastActual = 0;
        firstRun = true;
    }
}
