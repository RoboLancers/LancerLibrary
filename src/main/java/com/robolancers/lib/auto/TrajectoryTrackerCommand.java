package com.robolancers.lib.auto;

import com.robolancers.lib.subsystems.drivetrain.TankDriveSubsystem;
import com.team254.lib.physics.DifferentialDrive;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.ghrobotics.lib.debug.LiveDashboard;
import org.ghrobotics.lib.localization.Localization;
import org.ghrobotics.lib.mathematics.twodim.control.TrajectoryTracker;
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d;
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2dWithCurvature;
import org.ghrobotics.lib.mathematics.twodim.trajectory.types.TimedEntry;
import org.ghrobotics.lib.mathematics.twodim.trajectory.types.TimedTrajectory;
import org.ghrobotics.lib.mathematics.twodim.trajectory.types.TrajectorySamplePoint;
import org.ghrobotics.lib.mathematics.units.LengthKt;
import org.ghrobotics.lib.mathematics.units.TimeUnitsKt;
import org.ghrobotics.lib.mathematics.units.derivedunits.VelocityKt;
import org.ghrobotics.lib.subsystems.drive.TrajectoryTrackerDriveBase;
import org.ghrobotics.lib.subsystems.drive.TrajectoryTrackerOutput;

import java.util.function.Supplier;

@SuppressWarnings({"unused"})
public class TrajectoryTrackerCommand extends Command {
    private TrajectoryTracker trajectoryTracker;
    private Supplier<TimedTrajectory<Pose2dWithCurvature>> trajectorySource;
    private TrajectoryTrackerDriveBase driveBase;
    private TankDriveSubsystem tankDriveSubsystem;
    private Localization localization;
    private boolean reset;

    public TrajectoryTrackerCommand(TankDriveSubsystem tankDriveSubsystem, TrajectoryTrackerDriveBase driveBase, Supplier<TimedTrajectory<Pose2dWithCurvature>> trajectorySource){
        this(tankDriveSubsystem, driveBase, trajectorySource, false);
    }

    public TrajectoryTrackerCommand(TankDriveSubsystem tankDriveSubsystem, TrajectoryTrackerDriveBase driveBase, Supplier<TimedTrajectory<Pose2dWithCurvature>> trajectorySource, boolean reset){
        requires(tankDriveSubsystem);

        this.trajectoryTracker = driveBase.getTrajectoryTracker();
        this.trajectorySource = trajectorySource;
        this.driveBase = driveBase;
        this.tankDriveSubsystem = tankDriveSubsystem;
        this.localization = tankDriveSubsystem.getLocalization();
        this.reset = reset;
    }

    @Override
    protected void initialize(){
        trajectoryTracker.reset(trajectorySource.get());

        if(reset) {
            localization.reset(trajectorySource.get().getFirstState().component1().getPose());
        }

        LiveDashboard.INSTANCE.setFollowingPath(true);
    }

    @Override
    protected void execute(){
        TrajectoryTrackerOutput nextState = trajectoryTracker.nextState(driveBase.getRobotPosition(), TimeUnitsKt.getMillisecond(System.currentTimeMillis()));
        DifferentialDrive.DriveDynamics driveDynamics = tankDriveSubsystem.getDifferentialDrive().solveInverseDynamics(nextState.getDifferentialDriveVelocity(), nextState.getDifferentialDriveAcceleration());
        double leftVelocity = VelocityKt.getFeetPerSecond(VelocityKt.getVelocity(LengthKt.getMeter(driveDynamics.getWheelVelocity().getLeft() * tankDriveSubsystem.getDifferentialDrive().getWheelRadius())));
        double rightVelocity = VelocityKt.getFeetPerSecond(VelocityKt.getVelocity(LengthKt.getMeter(driveDynamics.getWheelVelocity().getRight() * tankDriveSubsystem.getDifferentialDrive().getWheelRadius())));

        driveBase.setOutput(nextState);

        TrajectorySamplePoint<TimedEntry<Pose2dWithCurvature>> referencePoint = trajectoryTracker.getReferencePoint();
        if(referencePoint != null){
            Pose2d referencePose = referencePoint.getState().getState().getPose();

            LiveDashboard.INSTANCE.setPathX(referencePose.getTranslation().getX().getFeet());
            LiveDashboard.INSTANCE.setPathY(referencePose.getTranslation().getY().getFeet());
            LiveDashboard.INSTANCE.setPathHeading(referencePose.getRotation().getRadian());

            SmartDashboard.putNumber("Left Path Velocity", leftVelocity);
            SmartDashboard.putNumber("Right Path Velocity", rightVelocity);
        }
    }

    @Override
    protected void end(){
        driveBase.zeroOutputs();
        LiveDashboard.INSTANCE.setFollowingPath(false);
    }

    @Override
    protected boolean isFinished() {
        return trajectoryTracker.isFinished();
    }
}