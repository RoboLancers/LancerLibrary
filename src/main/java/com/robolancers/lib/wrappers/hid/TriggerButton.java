package com.robolancers.lib.wrappers.hid;

import com.robolancers.lib.Utilities;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.buttons.Trigger;

public class TriggerButton extends Trigger {
    private Joystick joystick;
    private int port;
    private boolean negative;

    TriggerButton(Joystick joystick, int port, boolean negative){
        this.joystick = joystick;
        this.port = port;
        this.negative = negative;
    }

    @Override
    public boolean get() {
        return negative ? (Utilities.applyDeadband(joystick.getRawAxis(port), 0.1) < 0) : (Utilities.applyDeadband(joystick.getRawAxis(port), 0.1) > 0);
    }
}
