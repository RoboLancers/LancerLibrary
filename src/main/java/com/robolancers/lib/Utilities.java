package com.robolancers.lib;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Utilities {
    public static double applyDeadband(double value, double deadbandValue){
        return Math.abs(value) > deadbandValue ? value : 0;
    }

    public static double range(double value, double max){
        return range(value, -max, max);
    }

    public static double range(double value, double min, double max){
        return Math.min(max, Math.max(min, value));
    }
}
