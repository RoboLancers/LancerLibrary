package com.robolancers.lib;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RobotMathTest {
    private static final double EPSILON = 1E-9;

    @Test
    public void sqrtKeepSignTest(){
        assertEquals(0, RobotMath.sqrtKeepSign(0), EPSILON);
        assertEquals(-5, RobotMath.sqrtKeepSign(-25), EPSILON);
        assertEquals(5, RobotMath.sqrtKeepSign(25), EPSILON);
    }

    @Test
    public void squareKeepSignTest(){
        assertEquals(0, RobotMath.squareKeepSign(0), EPSILON);
        assertEquals(-25, RobotMath.squareKeepSign(-5), EPSILON);
        assertEquals(25, RobotMath.squareKeepSign(5), EPSILON);
    }
}
