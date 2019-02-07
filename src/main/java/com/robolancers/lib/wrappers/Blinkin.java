package com.robolancers.lib.wrappers;

import edu.wpi.first.wpilibj.Spark;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Blinkin {
    private Spark blinkin;

    public Blinkin(int channel){
        blinkin = new Spark(channel);
    }

    public void setPattern(double sparkValue){
        blinkin.set(sparkValue);
    }

    public void setPattern(PatternType pattern){
        setPattern(pattern.value);
    }

    public enum PatternType {
        // TODO Finish putting all possible values here
        RAINBOW_RAINBOW_PALETTE(-0.99),
        RAINBOW_PARTY_PALETTE(-0.97);

        double value;

        PatternType(double value){
            this.value = value;
        }
    }
}
