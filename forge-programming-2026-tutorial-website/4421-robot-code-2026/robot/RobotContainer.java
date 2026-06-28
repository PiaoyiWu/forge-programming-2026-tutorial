// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import static edu.wpi.first.units.Units.*;

import org.opencv.video.TrackerDaSiamRPN;

import com.ctre.phoenix6.swerve.SwerveModule.DriveRequestType;
import com.ctre.phoenix6.swerve.SwerveRequest;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.RobotModeTriggers;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine.Direction;
import frc.robot.commands.IntakeCommand;
import frc.robot.commands.OuttakeCommand;
import frc.robot.commands.PivotDownCommand;
import frc.robot.commands.PivotUpCommand;
import frc.robot.commands.ShootCommand;
import frc.robot.commands.StartShakeCommand;
import frc.robot.commands.StopIntakeCommand;
import frc.robot.commands.StopShakeCommand;
import frc.robot.commands.StopShooterCommand;
import frc.robot.commands.hamburgerCommand;
import frc.robot.commands.autoHoodAngleOffsetCommand;
import frc.robot.generated.TunerConstants;
import frc.robot.subsystems.CommandSwerveDrivetrain;
import frc.robot.subsystems.IndexerSubsystem;
import frc.robot.subsystems.IntakeSubsystem;
// import frc.robot.subsystems.LightSubsystem;
import frc.robot.subsystems.PivotSubsystem;
import frc.robot.subsystems.TurretShooterSubsystem;

public class RobotContainer {
    private double MaxSpeed = 1.0 * TunerConstants.kSpeedAt12Volts.in(MetersPerSecond); // kSpeedAt12Volts desired top speed
    private double MaxAngularRate = RotationsPerSecond.of(0.75).in(RadiansPerSecond); // 3/4 of a rotation per second max angular velocity

    /* Setting up bindings for necessary control of the swerve drive platform */
    private final SwerveRequest.FieldCentric drive = new SwerveRequest.FieldCentric()
            .withDeadband(MaxSpeed * 0.1).withRotationalDeadband(MaxAngularRate * 0.1) // Add a 10% deadband
            .withDriveRequestType(DriveRequestType.OpenLoopVoltage); // Use open-loop control for drive motors
    private final SwerveRequest.SwerveDriveBrake brake = new SwerveRequest.SwerveDriveBrake();
    private final SwerveRequest.PointWheelsAt point = new SwerveRequest.PointWheelsAt();

    private final Telemetry logger = new Telemetry(MaxSpeed);

    private final CommandXboxController joystick = new CommandXboxController(0);
    private final CommandXboxController operator = new CommandXboxController(1);

    public final CommandSwerveDrivetrain drivetrain = TunerConstants.createDrivetrain();

    // ################################################################################################################################################################################################################## //
    
    public final IntakeSubsystem m_IntakeSubsystem = new IntakeSubsystem();
    public final TurretShooterSubsystem m_TurretShooterSubsystem = new TurretShooterSubsystem(() -> drivetrain.getPose2d(), () -> drivetrain.getXVelocity(), () -> drivetrain.getYVelocity(), () -> drivetrain.getAngularVelocityDegrees(), () -> drivetrain.getFieldCentricVelocity());
    public final IndexerSubsystem m_IndexerSubsystem = new IndexerSubsystem(() -> m_TurretShooterSubsystem.isAtShooterSpeed(), () -> m_TurretShooterSubsystem.isAtTurretAngle(), () -> m_TurretShooterSubsystem.isAtHoodAngle());
    public final PivotSubsystem m_PivotSubsystem = new PivotSubsystem();
    // public final LightSubsystem m_LightSubsystem = new LightSubsystem();

    public SendableChooser<Command> autoChooser;

    public double speedMultiplier = 1.0;
    public boolean rotateOverTurret = false;

    public Translation2d turretCenterOfRotation = new Translation2d(Constants.TurretShooterConstants.turretXOffset, Constants.TurretShooterConstants.turretYOffset); // swap or negate values if rotating over wrong corner
    public Translation2d defaultCenterofRotation = new Translation2d(0, 0);
    // ###################### COMMANDS ########################

    public Command intakeCommand;
    public Command stopIntakeCommand;
    public Command shootCommand;
    public Command stopShooterCommand;
    public Command pivotUp;
    public Command pivotDown;
    public Command autoHoodAngleOffsetCommand;
    public Command hamburgerCommand;
    public Command startShakeCommand;
    public Command stopShakeCommand;
    public Command outtakeCommand;

    public RobotContainer() {
        // Named Commands HERE

        configureAutoCommands();

        speedMultiplier = 1.0;
        autoChooser = AutoBuilder.buildAutoChooser();
        SmartDashboard.putData("Auto Chooser", autoChooser);
        configureBindings();
    }

    private void configureAutoCommands() {
        intakeCommand = new IntakeCommand(m_IntakeSubsystem, m_PivotSubsystem);
        stopIntakeCommand = new StopIntakeCommand(m_IntakeSubsystem);
        shootCommand = new ShootCommand(m_TurretShooterSubsystem, drivetrain);
        stopShooterCommand = new StopShooterCommand(m_TurretShooterSubsystem, drivetrain);
        pivotUp = new PivotUpCommand(m_PivotSubsystem);
        pivotDown = new PivotDownCommand(m_PivotSubsystem);
        autoHoodAngleOffsetCommand = new autoHoodAngleOffsetCommand(m_TurretShooterSubsystem);
        hamburgerCommand = new hamburgerCommand(drivetrain);
        startShakeCommand = new StartShakeCommand(m_PivotSubsystem);
        stopShakeCommand = new StopShakeCommand(m_PivotSubsystem);
        outtakeCommand = new OuttakeCommand(m_IntakeSubsystem);


        NamedCommands.registerCommand("intakeCommand", intakeCommand);
        NamedCommands.registerCommand("stopIntakeCommand", stopIntakeCommand);
        NamedCommands.registerCommand("shootCommand", shootCommand);
        NamedCommands.registerCommand("stopShooterCommand", stopShooterCommand);
        NamedCommands.registerCommand("pivotUp", pivotUp);
        NamedCommands.registerCommand("pivotDown", pivotDown);
        NamedCommands.registerCommand("autoHoodAngleOffsetCommand", autoHoodAngleOffsetCommand);
        NamedCommands.registerCommand("hamburgerCommand", hamburgerCommand);
        NamedCommands.registerCommand("startShakeCommand", startShakeCommand);
        NamedCommands.registerCommand("stopShakeCommand", stopShakeCommand);
        NamedCommands.registerCommand("outtakeCommand", outtakeCommand);
    }

    private double speedMultiplierCalculator() {
        return 
                    (joystick.rightTrigger().getAsBoolean() && joystick.button(10).getAsBoolean() && !isInNoMansLand()) ?
                        Constants.CommandSwerveDrivetrainConstants.shootingSpeed
                     : (!joystick.rightTrigger().getAsBoolean() && joystick.button(10).getAsBoolean() && !isInNoMansLand()) ?
                        Constants.CommandSwerveDrivetrainConstants.slowModeSpeed
                    : (joystick.rightTrigger().getAsBoolean() && !joystick.button(10).getAsBoolean() && !isInNoMansLand()) ?
                        Constants.CommandSwerveDrivetrainConstants.shootingSpeed
                    : (joystick.button(10).getAsBoolean() && isInNoMansLand()) ?
                        Constants.CommandSwerveDrivetrainConstants.slowModeSpeed
                    : 1.0;
    }

    private double rotationalRateSlow() {
        return (joystick.rightTrigger().getAsBoolean() && joystick.button(10).getAsBoolean()) ?
                        1.0
                     : (!joystick.rightTrigger().getAsBoolean() && joystick.button(10).getAsBoolean()) ?
                        1.0
                    : (joystick.rightTrigger().getAsBoolean() && !joystick.button(10).getAsBoolean()) ?
                        1.0
                    : 1.0;
    }

    private void configureBindings() {
        // Note that X is defined as forward according to WPILib convention,
        // and Y is defined as to the left according to WPILib convention.

        drivetrain.setDefaultCommand(
            // Drivetrain will execute this command periodically
            drivetrain.applyRequest(() -> 
                drive.withVelocityX(-joystick.getLeftY() * MaxSpeed * speedMultiplierCalculator()) // Drive forward with negative Y (forward)
                    .withVelocityY(-joystick.getLeftX() * MaxSpeed * speedMultiplierCalculator()) // Drive left with negative X (left)
                    .withRotationalRate(-joystick.getRightX() * MaxAngularRate * speedMultiplierCalculator() * 1.35 * rotationalRateSlow()) // Drive counterclockwise with negative X (left)
                    .withCenterOfRotation(rotateOverTurret ? turretCenterOfRotation : defaultCenterofRotation)
            )
        );

        // Idle while the robot is disabled. This ensures the configured
        // neutral mode is applied to the drive motors while disabled.
        final var idle = new SwerveRequest.Idle();
        RobotModeTriggers.disabled().whileTrue(
            drivetrain.applyRequest(() -> idle).ignoringDisable(true)
        );

        joystick.x().whileTrue(drivetrain.applyRequest(() -> brake));
        // joystick.b().whileTrue(drivetrain.applyRequest(() ->
        //     point.withModuleDirection(new Rotation2d(-joystick.getLeftY(), -joystick.getLeftX()))
        // ));

        // Run SysId routines when holding back/start and X/Y.
        // Note that each routine should be run exactly once in a single log.
        joystick.back().and(joystick.y()).whileTrue(drivetrain.sysIdDynamic(Direction.kForward));
        joystick.back().and(joystick.x()).whileTrue(drivetrain.sysIdDynamic(Direction.kReverse));
        joystick.start().and(joystick.y()).whileTrue(drivetrain.sysIdQuasistatic(Direction.kForward));
        joystick.start().and(joystick.x()).whileTrue(drivetrain.sysIdQuasistatic(Direction.kReverse));

        // Reset the field-centric heading on left bumper press.
        // joystick.leftBumper().onTrue(drivetrain.runOnce(drivetrain::seedFieldCentric));

        drivetrain.registerTelemetry(logger::telemeterize);

        // ################################################################################################################################################################################################################## //


        // joystick.x().whileTrue(new InstantCommand(() -> m_IntakeSubsystem.OutTake()))
        //             .whileFalse(new InstantCommand(() -> m_IntakeSubsystem.StopIntake()));


        joystick.rightTrigger(0.20).whileTrue(new InstantCommand(() -> m_TurretShooterSubsystem.runShooter()))
                    .whileFalse(new InstantCommand(() -> m_TurretShooterSubsystem.stopShooter()))
                    .onTrue(new InstantCommand(() -> {drivetrain.seedHeadingWithVision(); drivetrain.seedHeadingWithVision();}))
                    .whileTrue(new InstantCommand(() -> {rotateOverTurret = true;}))
                    .onFalse(new InstantCommand(() -> {rotateOverTurret = false;}));


        joystick.button(8).onTrue(new InstantCommand(() -> drivetrain.seedHeadingWithVision()));

        joystick.povUp().onTrue(new InstantCommand(() -> m_PivotSubsystem.topPivotSetpoint()));
        joystick.povDown().onTrue(new InstantCommand(() -> m_PivotSubsystem.bottomPivotSetpoint()));
        joystick.button(9).onTrue(startShakeCommand)
            .onFalse(stopShakeCommand)
            .onTrue(intakeCommand)
            .onFalse(stopIntakeCommand);;


        // slow mode
        // joystick.button(10).whileTrue(new InstantCommand(() -> {speedMultiplier = 0.25;}))
        //     .whileFalse(new InstantCommand(() -> {speedMultiplier = 1.0;}));

        joystick.leftBumper().whileTrue(new InstantCommand(() -> m_IntakeSubsystem.SetIntakeSpeed(Constants.IntakeConstants.defaultIntakeSpeed)))
            .whileFalse(new InstantCommand(() -> m_IntakeSubsystem.StopIntake()));

        joystick.leftTrigger(0.20).whileTrue(new InstantCommand(() -> m_IntakeSubsystem.SetIntakeSpeed(Constants.IntakeConstants.defaultIntakeSpeed)))
            .whileFalse(new InstantCommand(() -> m_IntakeSubsystem.StopIntake()));

        joystick.y().onTrue(new InstantCommand(() -> m_TurretShooterSubsystem.setHoodAngleOffset()));

        joystick.povLeft().onTrue(new InstantCommand(() -> m_TurretShooterSubsystem.decreaseHoodAngleOffset()));
        joystick.povRight().onTrue(new InstantCommand(() -> m_TurretShooterSubsystem.increaseHoodAngleOffset()));

        // joystick.button(2).whileTrue(new InstantCommand(() -> m_IndexerSubsystem.runBackwards()));


        joystick.a().onTrue(autoHoodAngleOffsetCommand);
        joystick.rightBumper().whileTrue(new InstantCommand(() -> m_IndexerSubsystem.isRunBackwardsF(true)))
            .whileFalse(new InstantCommand(() -> m_IndexerSubsystem.isRunBackwardsF(false)));

        joystick.leftTrigger().whileTrue(new InstantCommand(() -> m_IntakeSubsystem.SetIntakeSpeed(-Constants.IntakeConstants.defaultIntakeSpeed)))
        .whileFalse(new InstantCommand(() -> m_IntakeSubsystem.StopIntake()))
        .onTrue(startShakeCommand)
            .onFalse(stopShakeCommand);


        // ################################################################# OPERATOR ####################################//#endregion

        operator.a().onTrue(autoHoodAngleOffsetCommand).onTrue(stopShooterCommand);

        operator.povUp().onTrue(new InstantCommand(() -> m_TurretShooterSubsystem.increaseBaseRPSOffset()));
        operator.povDown().onTrue(new InstantCommand(() -> m_TurretShooterSubsystem.decreaseBaseRPSOffset()));
        operator.b().onTrue(new InstantCommand(() -> m_TurretShooterSubsystem.zeroBaseRPSOffset()));
        operator.y().onTrue(new InstantCommand(() -> m_TurretShooterSubsystem.setHoodAngleOffset()));
        operator.povRight().onTrue(new InstantCommand(() -> m_TurretShooterSubsystem.decreaseTurretAngleOffset()));
        operator.povLeft().onTrue(new InstantCommand(() -> m_TurretShooterSubsystem.increaseTurretAngleOffset()));

        operator.button(8).onTrue(new InstantCommand(() -> drivetrain.seedHeadingWithVision())).onTrue(new InstantCommand(() -> m_TurretShooterSubsystem.zeroTurretAngleOffset()));

        operator.rightBumper().onTrue(startShakeCommand)
            .onFalse(stopShakeCommand)
            .onTrue(intakeCommand)
            .onFalse(stopIntakeCommand);


    }

    public void stopCommands() {
        CommandScheduler.getInstance().schedule(stopShooterCommand);
        CommandScheduler.getInstance().schedule(stopIntakeCommand);
        CommandScheduler.getInstance().schedule(hamburgerCommand);
        CommandScheduler.getInstance().schedule(stopShakeCommand);

    }

    public void recordLL() {
        drivetrain.saveRecording();
    }

    public boolean getRotateOverTurret() {
        return rotateOverTurret;
    }   

    public Command getAutonomousCommand() {
        return autoChooser.getSelected();
    }

    public boolean isInNoMansLand() {
        return !m_TurretShooterSubsystem.isInAllianceZone() && !m_TurretShooterSubsystem.isInOpponentZone();
    }
}
