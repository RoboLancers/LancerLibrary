package com.robolancers.lib.subsystems.misc;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.command.InstantCommand;
import edu.wpi.first.wpilibj.command.Subsystem;

@SuppressWarnings("unused")
public class ToggleSolenoid extends InstantCommand {
    private Solenoid solenoid;

    public ToggleSolenoid(Subsystem subsystem, Solenoid solenoid){
        requires(subsystem);

        this.solenoid = solenoid;
    }

    @Override
    protected void initialize(){
        solenoid.set(!solenoid.get());
    }
}
