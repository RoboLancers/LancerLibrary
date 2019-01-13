package com.robolancers.lib;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilitiesTest {
    private static final double EPSILON = 1E-9;
    private static final double DEADBAND_VALUE = 0.1;
    private static final double MAX_VALUE = 1;

    @Test
    public void applyDeadbandTest(){
        assertEquals(0.5, Utilities.applyDeadband(0.5, DEADBAND_VALUE), EPSILON);
        assertEquals(-0.5, Utilities.applyDeadband(-0.5, DEADBAND_VALUE), EPSILON);
        assertEquals(0, Utilities.applyDeadband(0.05, DEADBAND_VALUE), EPSILON);
        assertEquals(0, Utilities.applyDeadband(-0.05, DEADBAND_VALUE), EPSILON);
        assertEquals(0, Utilities.applyDeadband(0, DEADBAND_VALUE), EPSILON);
    }

    @Test
    public void rangeTest(){
        assertEquals(0.5, Utilities.range(0.5, MAX_VALUE), EPSILON);
        assertEquals(-0.5, Utilities.range(-0.5, MAX_VALUE), EPSILON);
        assertEquals(1, Utilities.range(1.5, MAX_VALUE), EPSILON);
        assertEquals(-1, Utilities.range(-1.5, MAX_VALUE), EPSILON);
        assertEquals(0, Utilities.range(0, MAX_VALUE), EPSILON);
    }

    @Test
    public void sqrtKeepSignTest(){
        assertEquals(0, Utilities.sqrtKeepSign(0), EPSILON);
        assertEquals(-5, Utilities.sqrtKeepSign(-25), EPSILON);
        assertEquals(5, Utilities.sqrtKeepSign(25), EPSILON);
    }

    @Test
    public void squareKeepSignTest(){
        assertEquals(0, Utilities.squareKeepSign(0), EPSILON);
        assertEquals(-25, Utilities.squareKeepSign(-5), EPSILON);
        assertEquals(25, Utilities.squareKeepSign(5), EPSILON);
    }
}
