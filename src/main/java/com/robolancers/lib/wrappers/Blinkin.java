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
        RAINBOW_PARTY_PALETTE(-0.97),
        HEARTBEAT_FAST(0.07),
        STROBE_GOLD(-0.07),
        STROBE_RED(-0.11),
        OCEAN_TWINK(-0.51),
        HEARTBEAT_RED(-0.25),
        WAVE_PARTY(-0.43),
        CONFETTI(-0.87),
        THE_END(-0.03);

        double value;

        PatternType(double value){
            this.value = value;
        }
    }
}