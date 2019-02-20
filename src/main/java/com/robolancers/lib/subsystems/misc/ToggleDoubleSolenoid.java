package com.robolancers.lib.subsystems.misc;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.command.InstantCommand;
import edu.wpi.first.wpilibj.command.Subsystem;

@SuppressWarnings("unused")
public class ToggleDoubleSolenoid extends InstantCommand {
    private DoubleSolenoid doubleSolenoid;

    public ToggleDoubleSolenoid(Subsystem subsystem, DoubleSolenoid doubleSolenoid){
        requires(subsystem);

        this.doubleSolenoid = doubleSolenoid;
    }

    @Override
    protected void initialize(){
        doubleSolenoid.set(doubleSolenoid.get() == DoubleSolenoid.Value.kForward ? DoubleSolenoid.Value.kReverse : DoubleSolenoid.Value.kForward);
    }
}
