package com.robolancers.lib.subsystems.misc;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.command.InstantCommand;
import edu.wpi.first.wpilibj.command.Subsystem;

@SuppressWarnings("unused")
public class ChangeDoubleSolenoidState extends InstantCommand {
    private DoubleSolenoid doubleSolenoid;
    private DoubleSolenoid.Value value;

    public ChangeDoubleSolenoidState(Subsystem subsystem, DoubleSolenoid doubleSolenoid, DoubleSolenoid.Value value){
        requires(subsystem);

        this.doubleSolenoid = doubleSolenoid;
        this.value = value;
    }

    @Override
    protected void initialize(){
        doubleSolenoid.set(value);
    }
}
