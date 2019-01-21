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
    private CANPIDController canpidController;
    private CANEncoder canEncoder;

    //Native unit model should have gear ratio as sensorUnitPerRevolution and Length as wheel radius
    private NativeUnitModel<T> nativeUnitModel;

    public LancerSparkMax(int deviceID, MotorType type, NativeUnitModel<T> nativeUnitModel) {
        super(deviceID, type);

        canpidController = new CANPIDController(this);
        canEncoder = new CANEncoder(this);

        this.nativeUnitModel = nativeUnitModel;
    }

    public CANPIDController getCanpidController(){
        return canpidController;
    }

    public CANEncoder getCanEncoder(){
        return canEncoder;
    }

    @Override
    public double getPercentOutput() {
        return getAppliedOutput() / getBusVoltage();
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
                VelocityKt.getVelocity(NativeUnitKt.getSTU(canEncoder.getVelocity() / 60))
        );
    }

    @Override
    public void setVelocity(@NotNull Velocity<T> velocity) {
        canpidController.setReference(
                nativeUnitModel.toNativeUnitVelocity(velocity).getValue() * 60,
                ControlType.kVelocity
        );
    }

    @Override
    public void setVelocityAndArbitraryFeedForward(@NotNull Velocity<T> velocity, double arbitraryFeedForward) {
        canpidController.setReference(
                nativeUnitModel.toNativeUnitVelocity(velocity).getValue() * 60,
                ControlType.kVelocity,
                0,
                arbitraryFeedForward * getBusVoltage()
        );
    }

    public static void checkCANError(CANError canError, String methodName){
        if(canError != CANError.kOK){
            DriverStation.reportError("(SparkMax) " + canError.name() + " in " + methodName, false);
        }
    }

    public void setGain(GainType type, double gain){
        CANError canError = CANError.kError;

        switch (type){
            case FF:
                canError = canpidController.setFF(gain);
                break;
            case P:
                canError = canpidController.setP(gain);
                break;
            case I:
                canError = canpidController.setI(gain);
                break;
            case D:
                canError = canpidController.setD(gain);
                break;
        }

        checkCANError(canError, "setGain");
    }
}
