package com.robolancers.lib.subsystems.misc;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.command.InstantCommand;
import edu.wpi.first.wpilibj.command.Subsystem;

@SuppressWarnings("unused")
public class SetSolenoid extends InstantCommand {
    private Solenoid solenoid;
    private boolean value;

    public SetSolenoid(Subsystem subsystem, Solenoid solenoid, boolean value){
        requires(subsystem);

        this.solenoid = solenoid;
        this.value = value;
    }

    @Override
    protected void initialize(){
        solenoid.set(value);
    }
}
