package com.robolancers.lib;

@SuppressWarnings("unused")
public class RobotMath {
    public static double squareKeepSign(double value){
        return Math.signum(value) * Math.pow(value, 2);
    }

    public static double sqrtKeepSign(double value){
        return Math.signum(value) * Math.sqrt(Math.abs(value));
    }
}