package com.robolancers.lib.wrappers.hid;

import com.robolancers.lib.Utilities;
import edu.wpi.first.wpilibj.command.Command;

@SuppressWarnings("unused")
public class FlightController extends BaseController{
    public enum Axis {
        X(0),
        Y(1, true),
        TWIST(2),
        RUDDER(3);

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
        TRIGGER(1),
        THUMB(2),
        BOTTOM_LEFT(3),
        BOTTOM_RIGHT(4),
        TOP_LEFT(5),
        TOP_RIGHT(6),
        FAR_TOP(7),
        INNER_TOP(8),
        FAR_MIDDLE(9),
        INNER_MIDDLE(10),
        FAR_BOTTOM(11),
        INNER_BOTTOM(12);

        int port;

        Button(int port){
            this.port = port;
        }
    }

    public FlightController(int port) {
        super(port);
    }

    public double getAxisValue(Axis axis){
        return Utilities.applyDeadband(axis.inverted * joystick.getRawAxis(axis.port), DEADZONE);
    }

    public boolean getButtonState(Button button){
        return joystick.getRawButton(button.port);
    }

    public FlightController whileHeld(Button button, Command command){
        buttons[button.port].whileHeld(command);
        return this;
    }

    public FlightController whenPressed(Button button, Command command){
        buttons[button.port].whenPressed(command);
        return this;
    }

    public FlightController whenReleased(Button button, Command command){
        buttons[button.port].whenReleased(command);
        return this;
    }

    public FlightController toggleWhenPressed(Button button, Command command){
        buttons[button.port].toggleWhenPressed(command);
        return this;
    }

    public FlightController cancelWhenPressed(Button button, Command command){
        buttons[button.port].cancelWhenPressed(command);
        return this;
    }
}