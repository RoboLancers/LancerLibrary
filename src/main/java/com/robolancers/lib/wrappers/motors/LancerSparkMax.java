package com.robolancers.lib.wrappers.motors;

import com.revrobotics.*;
import com.robolancers.lib.enums.GainType;
import edu.wpi.first.wpilibj.DriverStation;
import org.ghrobotics.lib.mathematics.units.SIUnit;
import org.ghrobotics.lib.mathematics.units.derivedunits.Velocity;
import org.ghrobotics.lib.mathematics.units.derivedunits.VelocityKt;
import org.ghrobotics.lib.mathematics.units.derivedunits.Volt;
import org.ghrobotics.lib.mathematics.units.derivedunits.VoltKt;
import org.ghrobotics.lib.mathematics.units.nativeunits.*;
import org.ghrobotics.lib.wrappers.FalconMotor;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "WeakerAccess"})
public class LancerSparkMax<T extends SIUnit<T>> extends CANSparkMax implements FalconMotor<T> {
    private CANPIDController canPIDController;
    private CANEncoder canEncoder;

    //Native unit model should have gear ratio as sensorUnitPerRevolution and Length as wheel radius
    private NativeUnitModel<T> nativeUnitModel;

    public LancerSparkMax(int deviceID, MotorType type, NativeUnitModel<T> nativeUnitModel) {
        super(deviceID, type);

        restoreFactoryDefaults();

        canPIDController = new CANPIDController(this);
        canEncoder = new CANEncoder(this);

        this.nativeUnitModel = nativeUnitModel;
    }

    public CANPIDController getCanPIDController(){
        return canPIDController;
    }

    public CANEncoder getCanEncoder(){
        return canEncoder;
    }

    @Override
    public double getPercentOutput() {
        return get();
    }

    @Override
    public void setPercentOutput(double percentOutput) {
        set(percentOutput);
    }

    @NotNull
    @Override
    public Volt getVoltageOutput() {
        return VoltKt.getVolt(getAppliedOutput());
    }

    @NotNull
    @Override
    public Velocity<T> getVelocity() {
        return nativeUnitModel.fromNativeUnitVelocity(
                VelocityKt.getVelocity(NativeUnitKt.getNativeUnits(canEncoder.getVelocity() / 60))
        );
    }

    @Override
    public void setVelocity(@NotNull Velocity<T> velocity) {
        canPIDController.setReference(
                nativeUnitModel.toNativeUnitVelocity(velocity).getValue() * 60,
                ControlType.kVelocity
        );
    }

    @Override
    public void setVelocityAndArbitraryFeedForward(@NotNull Velocity<T> velocity, double arbitraryFeedForward) {
        canPIDController.setReference(
                nativeUnitModel.toNativeUnitVelocity(velocity).getValue() * 60,
                ControlType.kVelocity,
                0,
                arbitraryFeedForward * getBusVoltage()
        );
    }

    public T getSensorPosition(){
        return nativeUnitModel.fromNativeUnitPosition(NativeUnitKt.getNativeUnits(canEncoder.getPosition()));
    }

    public static void checkCANError(LancerSparkMax lancerSparkMax, CANError canError, String methodName){
        if(canError != CANError.kOK){
            DriverStation.reportError("(SparkMax " + lancerSparkMax.getDeviceId()  + ") " + canError.name() + " in " + methodName, false);
        }
    }

    public void setGain(GainType type, double gain){
        CANError canError = CANError.kError;

        switch (type){
            case FF:
                canError = canPIDController.setFF(gain);
                break;
            case P:
                canError = canPIDController.setP(gain);
                break;
            case I:
                canError = canPIDController.setI(gain);
                break;
            case D:
                canError = canPIDController.setD(gain);
                break;
        }

        checkCANError(this, canError, "setGain");
    }
}
