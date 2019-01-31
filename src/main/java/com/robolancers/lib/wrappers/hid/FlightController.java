package com.robolancers.lib.wrappers.hid;

import com.robolancers.lib.Utilities;
import edu.wpi.first.wpilibj.command.Command;

@SuppressWarnings("unused")
public class FlightController extends BaseController{
    private static final int NUMBER_OF_AXIS = Axis.values().length;

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

        OUTER_TOP(7),
        INNER_TOP(8),
        OUTER_MIDDLE(9),
        INNER_MIDDLE(10),
        OUTER_BOTTOM(11),
        INNER_BOTTOM(12);

        int port;

        Button(int port){
            this.port = port;
        }
    }

    public enum Trigger {
        X_FORWARD(Axis.X.port),
        X_BACKWARD(Axis.X.port, true),

        Y_FORWARD(Axis.Y.port),
        Y_BACKWARD(Axis.Y.port, true),

        TWIST_RIGHT(Axis.TWIST.port),
        TWIST_LEFT(Axis.TWIST.port, true),

        RUDDER_FORWARD(Axis.RUDDER.port),
        RUDDER_BACKWARD(Axis.RUDDER.port, true);

        int port;
        boolean negative;

        Trigger(int port){
            this(port, false);
        }

        Trigger(int port, boolean negative){
            this.port = port;
            this.negative = negative;
        }
    }

    public FlightController(int port) {
        super(port);

        triggerButtons = new TriggerButton[NUMBER_OF_AXIS];

        for(int i = 0; i < triggerButtons.length; i++){
            triggerButtons[i] = new TriggerButton(joystick, i, Trigger.values()[i].negative);
        }
    }

    public double getAxisValue(Axis axis){
        return Utilities.applyDeadband(axis.inverted * joystick.getRawAxis(axis.port), DEADZONE);
    }

    public boolean getButtonState(Button button){
        return joystick.getRawButton(button.port);
    }

    public FlightController whileHeld(Trigger trigger, Command command){
        triggerButtons[trigger.port].whileActive(command);
        return this;
    }

    public FlightController whileHeld(Button button, Command command){
        buttons[button.port].whileHeld(command);
        return this;
    }

    public FlightController whenPressed(Trigger trigger, Command command){
        triggerButtons[trigger.port].whenActive(command);
        return this;
    }

    public FlightController whenPressed(Button button, Command command){
        buttons[button.port].whenPressed(command);
        return this;
    }

    public FlightController whenReleased(Trigger trigger, Command command){
        triggerButtons[trigger.port].whenInactive(command);
        return this;
    }

    public FlightController whenReleased(Button button, Command command){
        buttons[button.port].whenReleased(command);
        return this;
    }

    public FlightController toggleWhenPressed(Trigger trigger, Command command){
        triggerButtons[trigger.port].toggleWhenActive(command);
        return this;
    }

    public FlightController toggleWhenPressed(Button button, Command command){
        buttons[button.port].toggleWhenPressed(command);
        return this;
    }

    public FlightController cancelWhenPressed(Trigger trigger, Command command){
        triggerButtons[trigger.port].cancelWhenActive(command);
        return this;
    }

    public FlightController cancelWhenPressed(Button button, Command command){
        buttons[button.port].cancelWhenPressed(command);
        return this;
    }
}