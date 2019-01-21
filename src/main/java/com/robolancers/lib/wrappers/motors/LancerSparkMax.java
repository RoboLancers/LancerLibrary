package com.robolancers.lib.wrappers.motors;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import org.ghrobotics.lib.mathematics.units.SIUnit;
import org.ghrobotics.lib.mathematics.units.derivedunits.Velocity;
import org.ghrobotics.lib.mathematics.units.derivedunits.VelocityKt;
import org.ghrobotics.lib.mathematics.units.derivedunits.Volt;
import org.ghrobotics.lib.mathematics.units.derivedunits.VoltKt;
import org.ghrobotics.lib.mathematics.units.nativeunits.*;
import org.ghrobotics.lib.wrappers.FalconMotor;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
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
}
