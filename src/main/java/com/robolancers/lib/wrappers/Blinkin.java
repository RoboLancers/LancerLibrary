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
        //Fixed Palette Pattern
        RAINBOW_RAINBOW_PALETTE(-0.99),
        RAINBOW_PARTY_PALETTE(-0.97),
        RAINBOW_OCEAN_PALETTE(-0.95),
        RAINBOW_LAVA_PALETTE(-0.93),
        FOREST_PALETTE(-0.91),
        RAINBOW_GLITTER(-0.89),

        CONFETTI(-0.87),

        SHOT_RED(-0.85),
        SHOT_BLUE(-0.83),
        SHOT_WHITE(-0.81),

        SINELON_RAINBOW_PALETTE(-0.79),
        SINELON_PARTY_PALETTE(-0.77),
        SINELON_OCEAN_PALETTE(-0.75),
        SINELON_LAVA_PALETTE(-0.73),
        SINELON_FOREST_PALETTE(-0.71),

        BPM_RAINBOW_PALETTE(-0.69),
        BPM_PARTY_PALETTE(-0.67),
        BPM_OCEAN_PALETTE(-0.65),
        BPM_LAVA_PALETTE(-0.63),
        BPM_FOREST_PALETTE(-0.61),

        FIRE_MEDIUM(-0.59),
        FIRE_LARGE(-0.57),

        TWINKLES_RAINBOW_PALETTE(-0.55),
        TWINKLES_PARTY_PALETTE(-0.53),
        TWINKLES_OCEAN_PALETTE(-0.51),
        TWINKLES_LAVA_PALETTE(-0.49),
        TWINKLES_FOREST_PALETTE(-0.47),

        COLORWAVES_RAINBOW_PALETTE(-0.45),
        COLORWAVES_PARTY_PALETTE(-0.43),
        COLORWAVES_OCEAN_PALETTE(-0.41),
        COLORWAVES_LAVA_PALETTE(-0.39),
        COLORWAVES_FOREST_PALETTE(-0.37),

        LARSON_SCANNER_RED(-0.35),
        LARSON_SCANNER_GREY(-0.33),

        LIGHT_CHASE_RED(-0.31),
        LIGHT_CHASE_BLUE(-0.29),
        LIGHT_CHASE_GREY(-0.27),

        HEARTBEAT_RED(-0.25),
        HEARTBEAT_BLUE(-0.23),
        HEARTBEAT_WHITE(-0.21),
        HEARTBEAT_GREY(-0.19),

        BREATH_RED(-0.17),
        BREATH_BLUE(-0.15),
        BREATH_GREY(-0.13),

        STROBE_RED(-0.11),
        STROBE_BLUE(-0.09),
        STROBE_GOLD(-0.07),
        STROBE_WHITE(-0.05),

        END_TO_END_BLEND_TO_BLACK1(-0.03),

        LARSON_SCANNER1(-0.01),

        LIGHT_CHASE1(0.01),

        HEARTBEAT_SLOW1(0.03),
        HEARTBEAT_MEDIUM1(0.05),
        HEARTBEAT_FASTER1(0.07),

        BREATH_SLOW1(0.09),
        BREATH_FAST1(0.11),

        SHOT1(0.13),

        STROBE1(0.15),

        END_TO_END_BLEND_TO_BLACK2(0.17),

        LARSON_SCANNER2(0.19),

        LIGHT_CHASE2(0.21),

        HEARTBEAT_SLOW2(0.23),
        HEARTBEAT_MEDIUM2(0.25),
        HEARTBEAT_FASTER2(0.27),

        BREATH_SLOW2(0.29),
        BREATH_FAST2(0.31),

        SHOT2(0.33),

        STROBE2(0.35),

        SPARKLE_COLOR1_COLOR2(0.37),
        SPARKLE_COLOR2_COLOR1(0.39),

        COLOR_GRADIENT_COLOR102(0.41),

        BPM_COLOR102(0.43),

        END_TO_END_BLEND_COLOR1_AND2(0.45),
        END_TO_END_BLEND(0.47),

        COLOR1_AND_COLOR2_NO_BLENDING(0.49),

        TWINKLES_COLOR1_AND_COLOR2(0.51),

        COLOR_WAVE_COLOR1_COLOR2(0.53),

        SINELONE_COLOR1_AND_COLOR2(0.55),

        HOTPINK(0.57),
        DARKRED(0.59),
        RED(0.61),
        RED_ORANGE(0.63),
        ORANGE(0.65),
        GOLD(0.67),
        YELLOW(0.69),
        LAWN_GREEN(0.71),
        LIME(0.73),
        DARK_GREEN(0.75),
        GREEN(0.77),
        BLUE_GREEN(0.79),
        AQUA(0.81),
        SKY_BLUE(0.83),
        DARK_BLUE(0.85),
        BLUE(0.87),
        BLUE_VIOLET(0.89),
        VIOLET(0.91),
        WHITE(0.93),
        GREY(0.95),
        DARK_GREY(0.97),
        BLACK(0.99);

        double value;

        PatternType(double value){
            this.value = value;
        }
    }
}