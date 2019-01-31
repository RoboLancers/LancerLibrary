package com.robolancers.lib.wrappers.hid;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.buttons.Button;
import edu.wpi.first.wpilibj.buttons.JoystickButton;

abstract class BaseController {
    private static final int NUMBER_OF_BUTTONS = 13;

    static final double DEADZONE = 0.1;

    Joystick joystick;
    Button[] buttons;
    TriggerButton[] triggerButtons;

    BaseController(int port) {
        joystick = new Joystick(port);
        buttons = new JoystickButton[NUMBER_OF_BUTTONS];

        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new JoystickButton(joystick, i);
        }
    }
}