package com.robolancers.lib.wrappers.motors;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import org.ghrobotics.lib.mathematics.units.derivedunits.Velocity;
import org.ghrobotics.lib.mathematics.units.derivedunits.VelocityKt;
import org.ghrobotics.lib.mathematics.units.derivedunits.Volt;
import org.ghrobotics.lib.mathematics.units.derivedunits.VoltKt;
import org.ghrobotics.lib.mathematics.units.nativeunits.NativeUnitKt;
import org.ghrobotics.lib.wrappers.FalconMotor;
import org.jetbrains.annotations.NotNull;
import org.ghrobotics.lib.mathematics.units.nativeunits.NativeUnit;

@SuppressWarnings("unused")
public class LancerSparkMax extends CANSparkMax implements FalconMotor<NativeUnit> {
    private CANPIDController canpidController;
    private CANEncoder canEncoder;

    public LancerSparkMax(int deviceID, MotorType type) {
        super(deviceID, type);

        canpidController = new CANPIDController(this);
        canEncoder = new CANEncoder(this);
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
    public Velocity<NativeUnit> getVelocity() {
        return VelocityKt.getVelocity(NativeUnitKt.getSTU(canEncoder.getVelocity()));
    }

    @NotNull
    @Override
    public Volt getVoltageOutput() {
        return VoltKt.getVolt(getAppliedOutput());
    }

    @Override
    public void setVelocity(@NotNull Velocity<NativeUnit> velocity) {
        canpidController.setReference(velocity.getValue(), ControlType.kVelocity);
    }

    @Override
    public void setVelocityAndArbitraryFeedForward(@NotNull Velocity<NativeUnit> velocity, double arbitraryFeedForward) {
        canpidController.setReference(velocity.getValue(), ControlType.kVelocity, 0, arbitraryFeedForward * getBusVoltage());
    }
}
