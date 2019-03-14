package com.robolancers.lib.wrappers.vision;

import edu.wpi.first.wpilibj.DriverStation;
import io.github.pseudoresonance.pixy2api.Pixy2;
import io.github.pseudoresonance.pixy2api.Pixy2Line;
import io.github.pseudoresonance.pixy2api.links.SPILink;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"WeakerAccess", "unused"})
public class LancerPixy {
    private Pixy2 pixy2;
    private static final int argument = 0;

    private static final int MAX_X = 78;

    private byte pixyStatus;

    public LancerPixy(){
        pixy2 = Pixy2.createInstance(new SPILink());
        pixy2.init(argument);

        setLamp(true);
        pixy2.getLine().setMode(Pixy2Line.LINE_MODE_WHITE_LINE);
    }

    public synchronized void setLamp(boolean on){
        checkPixyError(pixy2.setLamp((byte) (on ? 1 : 0), (byte) 0), "setLamp");
    }

    public synchronized void update(){
        pixyStatus = pixy2.getLine().getFeatures(Pixy2Line.LINE_GET_MAIN_FEATURES, Pixy2Line.LINE_ALL_FEATURES, false);
        checkPixyError(pixyStatus, "update");
    }

    public synchronized boolean hasLine() {
        return pixy2.getLine().getVectors() != null && pixy2.getLine().getVectors().length > 0  && pixyStatus >= 0;
    }

    @Nullable
    public synchronized Pixy2Line.Vector getVector() {
        Pixy2Line.Vector[] vectors = pixy2.getLine().getVectors();

        if(vectors == null || vectors.length == 0 || pixyStatus < 0){
            return null;
        }

        Pixy2Line.Vector maximum = vectors[0];

        for(Pixy2Line.Vector vector : vectors){
            if(vector.getY1() - vector.getY0() > maximum.getY1() - maximum.getY0()){
                maximum = vector;
            }
        }

        return maximum;
    }

    public int getAverageX(Pixy2Line.Vector vector) {
        return (vector.getX1() + vector.getX0()) / 2;
    }

    public int getAverageY(Pixy2Line.Vector vector) {
        return (vector.getY1() + vector.getY0()) / 2;
    }

    public double getError(Pixy2Line.Vector vector){
        return (MAX_X / 2.0) - getAverageX(vector);
    }

    private void checkPixyError(byte code, String methodName){
        switch(code){
            case Pixy2.PIXY_RESULT_ERROR:
                DriverStation.reportError("General Pixy2 Error in " + methodName, false);
                break;
            case Pixy2.PIXY_RESULT_BUSY:
                DriverStation.reportError("Pixy2 busy in " + methodName, false);
                break;
            case Pixy2.PIXY_RESULT_CHECKSUM_ERROR:
                DriverStation.reportError("Pixy2 checksum error in " + methodName, false);
                break;
            case Pixy2.PIXY_RESULT_TIMEOUT:
                DriverStation.reportError("Pixy2 has timed out in " + methodName, false);
                break;
            case Pixy2.PIXY_RESULT_BUTTON_OVERRIDE:
                DriverStation.reportError("Pixy2 can't perform requested action because user is interacting with button in " + methodName, false);
                break;
        }
    }
}
