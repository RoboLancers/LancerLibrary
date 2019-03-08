package com.robolancers.lib.wrappers.hid;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.buttons.Button;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.buttons.POVButton;

abstract class BaseController {
    double deadzone = 0;

    Joystick joystick;
    Button[] buttons;
    TriggerButton[] triggerButtons;
    POVButton[] povButtons;

    BaseController(int port) {
        joystick = new Joystick(port);
    }
}