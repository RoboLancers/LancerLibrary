package com.robolancers.lib.wrappers.hid;

import com.robolancers.lib.Utilities;
import edu.wpi.first.wpilibj.command.Command;

@SuppressWarnings("unused")
public class XboxController extends BaseController {
    public enum Axis {
        LEFT_X(0),
        LEFT_Y(1, true),
        LEFT_TRIGGER(2),
        RIGHT_TRIGGER(3),
        RIGHT_X(4),
        RIGHT_Y(5, true);

        int port;
        int inverted;

        Axis(int port){
            this(port, false);
        }

        Axis(int port, boolean inverted){
            this.port = port;
            this.inverted = inverted ? -1 : 1;
        }
    }

    public enum Button {
        A(1),
        B(2),
        X(3),
        Y(4),
        LEFT_BUMPER(5),
        RIGHT_BUMPER(6),
        SELECT(7),
        START(8),
        LEFT_JOYSTICK_BUTTON(9),
        RIGHT_JOYSTICK_BUTTON(10);

        int port;

        Button(int port){
            this.port = port;
        }
    }

    public XboxController(int port) {
        super(port);
    }

    public double getAxisValue(Axis axis){
        return Utilities.applyDeadband(axis.inverted * joystick.getRawAxis(axis.port), DEADZONE);
    }

    public boolean getButtonState(Button button){
        return joystick.getRawButton(button.port);
    }

    public XboxController whileHeld(Button button, Command command){
        buttons[button.port].whileHeld(command);
        return this;
    }

    public XboxController whenPressed(Button button, Command command){
        buttons[button.port].whenPressed(command);
        return this;
    }

    public XboxController whenReleased(Button button, Command command){
        buttons[button.port].whenReleased(command);
        return this;
    }

    public XboxController toggleWhenPressed(Button button, Command command){
        buttons[button.port].toggleWhenPressed(command);
        return this;
    }

    public XboxController cancelWhenPressed(Button button, Command command){
        buttons[button.port].cancelWhenPressed(command);
        return this;
    }
}