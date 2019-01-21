package com.robolancers.lib.commands.subsystems.misc;

import com.robolancers.lib.subsystem.misc.Pneumatic;
import edu.wpi.first.wpilibj.command.Command;

public class RegulateCompressor extends Command {
    public RegulateCompressor() {
        requires(Pneumatic.getInstance());
        setInterruptible(false);
    }

    @Override
    protected void execute() {
        Pneumatic.getInstance().regulateCompressor();
    }

    @Override
    protected void end(){
        Pneumatic.getInstance().stopCompressor();
    }

    @Override
    protected boolean isFinished() {
        return false;
    }
}
