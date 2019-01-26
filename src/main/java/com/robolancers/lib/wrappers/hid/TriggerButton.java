package com.robolancers.lib.wrappers.hid;

import com.robolancers.lib.Utilities;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.buttons.Trigger;

public class TriggerButton extends Trigger {
    private Joystick joystick;
    private int port;

    public TriggerButton(Joystick joystick, int port){
        this.joystick = joystick;
        this.port = port;
    }

    @Override
    public boolean get() {
        return Utilities.applyDeadband(joystick.getRawAxis(port), 0.1) > 0;
    }
}
