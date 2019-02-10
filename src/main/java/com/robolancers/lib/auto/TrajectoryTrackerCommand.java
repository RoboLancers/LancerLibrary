package com.robolancers.lib.auto;

import com.robolancers.lib.subsystems.drivetrain.TankDriveSubsystem;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Subsystem;
import org.ghrobotics.lib.debug.LiveDashboard;
import org.ghrobotics.lib.localization.Localization;
import org.ghrobotics.lib.mathematics.twodim.control.TrajectoryTracker;
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d;
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2dWithCurvature;
import org.ghrobotics.lib.mathematics.twodim.trajectory.types.TimedEntry;
import org.ghrobotics.lib.mathematics.twodim.trajectory.types.TimedTrajectory;
import org.ghrobotics.lib.mathematics.twodim.trajectory.types.TrajectorySamplePoint;
import org.ghrobotics.lib.mathematics.units.TimeUnitsKt;
import org.ghrobotics.lib.subsystems.drive.TrajectoryTrackerDriveBase;
import java.util.function.Supplier;

@SuppressWarnings({"unused"})
public class TrajectoryTrackerCommand extends Command {
    private TrajectoryTracker trajectoryTracker;
    private Supplier<TimedTrajectory<Pose2dWithCurvature>> trajectorySource;
    private TrajectoryTrackerDriveBase driveBase;
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
        driveBase.setOutput(trajectoryTracker.nextState(driveBase.getRobotPosition(), TimeUnitsKt.getMillisecond(System.currentTimeMillis())));

        TrajectorySamplePoint<TimedEntry<Pose2dWithCurvature>> referencePoint = trajectoryTracker.getReferencePoint();
        if(referencePoint != null){
            Pose2d referencePose = referencePoint.getState().getState().getPose();

            LiveDashboard.INSTANCE.setPathX(referencePose.getTranslation().getX().getFeet());
            LiveDashboard.INSTANCE.setPathY(referencePose.getTranslation().getY().getFeet());
            LiveDashboard.INSTANCE.setPathHeading(referencePose.getRotation().getRadian());
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