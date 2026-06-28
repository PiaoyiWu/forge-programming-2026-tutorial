package frc.robot.subsystems;

import static edu.wpi.first.units.Units.Rotation;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.configs.TalonFXConfigurator;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.controls.VelocityVoltage;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.SolveQuartic;

public class TurretShooterSubsystem extends SubsystemBase {
    // IMPORTANT: USE RPM FOR SPEED OF SHOOTER

    public Supplier<Pose2d> getPose2d;
    public DoubleSupplier getXVelocity;
    public DoubleSupplier getYVelocity;
    public DoubleSupplier getAngularVelocityDegrees;
    public Supplier<ChassisSpeeds> getChassisSpeeds;

    private TalonFX m_Turret;
    public TalonFX m_Hood;
    private TalonFX m_Shooter;
    private TalonFX m_FollowerShooter;

    private TalonFXConfigurator turretTalonFXConfigurator;
    private TalonFXConfigurator hoodTalonFXConfigurator;
    private TalonFXConfigurator shooterTalonFXConfigurator;
    private TalonFXConfigurator followerShooterTalonFXConfigurator;

    private MotorOutputConfigs turretMotorOutputConfigs;
    private MotorOutputConfigs hoodMotorOutputConfigs;
    private MotorOutputConfigs shooterMotorOutputConfigs;
    private MotorOutputConfigs followerShooterMotorOutputConfigs;

    private CurrentLimitsConfigs shooterCurrentLimitsConfigs;

    private CANcoder turretEncoder;
    private CANcoder hoodEncoder;

    private double turretPosition;
    private double hoodPosition;

    private double currentShooterSpeed;

    private PIDController turretPIDController;
    private PIDController hoodPIDController;

    private double torque = 0.0;
    private double inertia = 0.0;
    private double omegaShooterRadPerS = 0.0;
    private double omegaTurretRadPerS = 0.0;

    private double turret_kP;
    private double turret_kI;
    private double turret_kD;

    private double hood_kP;
    private double hood_kI;
    private double hood_kD;

    private double turretSpeed;
    private double hoodSpeed;

    private double hoodTargetAngle;
    private double turretTargetAngle;


    private double turretSetpoint;
    private double hoodSetpoint;

    private double zoneShooterSpeed;
    private boolean isRunningShooter;

    private Pose2d currentPose;
    private Pose2d currentTurretPose;

    private Pose3d targetPose;
    private Pose3d hubPose;
    private Pose3d lobPoseRight;
    private Pose3d lobPoseLeft;

    private double opponentZoneXVal = Constants.TurretShooterConstants.neutralZoneFrontBound;
    private double allianceZoneXVal = Constants.TurretShooterConstants.neutralZoneBackBound;

    // Math Variables

    private double deltaX;
    private double deltaY;
    private double deltaTheta;

    private double ballExitVelocity;
    // public boolean isTracking;
    private double getCurrentRotation;

    private double relativeVelocity;
    private double totalVelocity;
    private double relativeVelocityParallel;
    private double relativeVelocityPerpendicular;

    private double hoodAngleOffset;
    private double baseRPSOffset;
    private double turretAngleOffset;

    private Field2d field = new Field2d();

    private Slot0Configs slot0Configs = new Slot0Configs();

    // Ball Counter stuff
    private int ballsShot = 0;
    public enum ShooterStateRPS {
        IDLE, REVVING_UP, AT_SPEED, RPS_DIPPING,
    }
    public ShooterStateRPS shooterState = ShooterStateRPS.IDLE;
    private boolean isCountingBalls = true;
    private double expectedRPS;
    private double rpsDropPerShot = 2.0;
    private boolean hasJustShot = false;

    private double flipConstant = 0.0;

    
    public TurretShooterSubsystem(Supplier<Pose2d> getPose2d, DoubleSupplier getXVelocity, DoubleSupplier getYVelocity, DoubleSupplier getAngularVelocityDegrees, Supplier<ChassisSpeeds> getChassisSpeeds) {
        this.getPose2d = getPose2d;
        this.getXVelocity = getXVelocity;
        this.getYVelocity = getYVelocity;
        this.getAngularVelocityDegrees = getAngularVelocityDegrees;
        this.getChassisSpeeds = getChassisSpeeds;

        m_Turret = new TalonFX(Constants.TurretShooterConstants.TurretID);
        m_Hood = new TalonFX(Constants.TurretShooterConstants.HoodID);
        m_Shooter = new TalonFX(Constants.TurretShooterConstants.ShooterRollerID);
        m_FollowerShooter = new TalonFX(Constants.TurretShooterConstants.FollowerShooterRollerID);

        turretTalonFXConfigurator = m_Turret.getConfigurator();
        hoodTalonFXConfigurator = m_Hood.getConfigurator();
        shooterTalonFXConfigurator = m_Shooter.getConfigurator();
        followerShooterTalonFXConfigurator = m_FollowerShooter.getConfigurator();

        turretMotorOutputConfigs = new MotorOutputConfigs();
        turretMotorOutputConfigs.Inverted = InvertedValue.Clockwise_Positive;
        turretMotorOutputConfigs.NeutralMode = NeutralModeValue.Brake;

        hoodMotorOutputConfigs = new MotorOutputConfigs();
        hoodMotorOutputConfigs.Inverted = InvertedValue.Clockwise_Positive;
        hoodMotorOutputConfigs.NeutralMode = NeutralModeValue.Brake;

        shooterMotorOutputConfigs = new MotorOutputConfigs();
        shooterMotorOutputConfigs.Inverted = InvertedValue.Clockwise_Positive;
        shooterMotorOutputConfigs.NeutralMode = NeutralModeValue.Coast;

        shooterCurrentLimitsConfigs = new CurrentLimitsConfigs();
        shooterCurrentLimitsConfigs.SupplyCurrentLimit = 35.0;
        shooterCurrentLimitsConfigs.SupplyCurrentLimitEnable = true;

        shooterTalonFXConfigurator.apply(shooterCurrentLimitsConfigs);



        
        slot0Configs.kS = Constants.TurretShooterConstants.shooter_kS;
        slot0Configs.kV = Constants.TurretShooterConstants.shooter_kV;
        slot0Configs.kP = Constants.TurretShooterConstants.shooter_kP;
        slot0Configs.kI = Constants.TurretShooterConstants.shooter_kI;
        slot0Configs.kD = Constants.TurretShooterConstants.shooter_kD;

        followerShooterMotorOutputConfigs = new MotorOutputConfigs();
        followerShooterMotorOutputConfigs.Inverted = InvertedValue.Clockwise_Positive;
        followerShooterMotorOutputConfigs.NeutralMode = NeutralModeValue.Coast;

        turretTalonFXConfigurator.apply(turretMotorOutputConfigs);
        hoodTalonFXConfigurator.apply(hoodMotorOutputConfigs);
        shooterTalonFXConfigurator.apply(shooterMotorOutputConfigs);
        followerShooterTalonFXConfigurator.apply(followerShooterMotorOutputConfigs);

        

        shooterTalonFXConfigurator.apply(slot0Configs);
        followerShooterTalonFXConfigurator.apply(slot0Configs);

        // Using follower:
        m_FollowerShooter.setControl(new Follower(Constants.TurretShooterConstants.ShooterRollerID, MotorAlignmentValue.Opposed));

        turretEncoder = new CANcoder(Constants.TurretShooterConstants.TurretEncoderID);
        hoodEncoder = new CANcoder(Constants.TurretShooterConstants.HoodEncoderID);

        ballExitVelocity = Constants.TurretShooterConstants.ballExitVelocity;

        getEncoderPositions();

        turret_kP = Constants.TurretShooterConstants.turret_kP;
        turret_kI = Constants.TurretShooterConstants.turret_kI;
        turret_kD = Constants.TurretShooterConstants.turret_kD;

        hood_kP = Constants.TurretShooterConstants.hood_kP;
        hood_kI = Constants.TurretShooterConstants.hood_kI;
        hood_kD = Constants.TurretShooterConstants.hood_kD;

        turretPIDController = new PIDController(turret_kP, turret_kI, turret_kD);
        hoodPIDController = new PIDController(hood_kP, hood_kI, hood_kD);


        currentPose = new Pose2d(0, 0, new Rotation2d(0));
        currentTurretPose = new Pose2d(0, 0, new Rotation2d(0));
        targetPose = new Pose3d(0, 0, 0, new Rotation3d(0, 0, 0));

        deltaX = 0.0;
        deltaY = 0.0;
        
        isRunningShooter = false;
        currentShooterSpeed = 0;
        zoneShooterSpeed = Constants.TurretShooterConstants.defaultShooterSpeed;

        hoodAngleOffset = 0.0;
        baseRPSOffset = Constants.TurretShooterConstants.defaultBaseRPSConstant;
        turretAngleOffset = 0.0;

        relativeVelocityParallel = 0;
        relativeVelocityPerpendicular = 0;
        relativeVelocity = 0;

        flipConstant = 0.0;

        Optional<DriverStation.Alliance> alliance = DriverStation.getAlliance();
        if (alliance.isPresent()) {
            if (alliance.get() == DriverStation.Alliance.Red) {
                hubPose = Constants.TurretShooterConstants.redHubPose;
                lobPoseRight = Constants.TurretShooterConstants.redLobPoseRight;
                lobPoseLeft = Constants.TurretShooterConstants.redLobPoseLeft;

                opponentZoneXVal = Constants.TurretShooterConstants.neutralZoneFrontBound;
                allianceZoneXVal = Constants.TurretShooterConstants.neutralZoneBackBound;
                
            } else{
                hubPose = Constants.TurretShooterConstants.blueHubPose;
                lobPoseRight = Constants.TurretShooterConstants.blueLobPoseRight;
                lobPoseLeft = Constants.TurretShooterConstants.blueLobPoseLeft;

                opponentZoneXVal = Constants.TurretShooterConstants.neutralZoneBackBound;
                allianceZoneXVal = Constants.TurretShooterConstants.neutralZoneFrontBound;

            }
        } else{
            hubPose = Constants.TurretShooterConstants.redHubPose;

            lobPoseRight = Constants.TurretShooterConstants.redLobPoseRight;
            lobPoseLeft = Constants.TurretShooterConstants.redLobPoseLeft;

            opponentZoneXVal = Constants.TurretShooterConstants.neutralZoneFrontBound;
            allianceZoneXVal = Constants.TurretShooterConstants.neutralZoneBackBound;
            
        }

        isCountingBalls = true;
        ballsShot = 0;

        updateTargetPose(hubPose);
        putSmartDashboardPIDValues();
        calculateDeltaXY();
        putSmartDashboard();
        updateTurretAdjustments();

    }

    @Override
    public void periodic() {
        updateTargetPose(hubPose);
        updatePose();
        updateTurretPose();
        calculateDeltaXY();
        putSmartDashboard();
        getEncoderPositions();
        getSmartDashboardPIDValues();
        updateTurretAdjustments();
        
        final VelocityVoltage m_request = new VelocityVoltage(0).withSlot(0);

        isLobbing();
        calcNewShootOnTheMove();


        if (isRunningShooter) {       
            // calculateShootingWhileMovingTargetPose();
            
            gradientHoodShootingStatic();
            // calculateTurretAngleStatic();
            calculateLinearShooterSpeed();

            m_Shooter.setControl(m_request.withVelocity(zoneShooterSpeed).withFeedForward(0.5));

        } else {
            m_Shooter.set(0.0);
            hoodTargetAngle = 90.0;
            // turretTargetAngle = -90.0;

        }
        calculateTurretAngleStatic();

        calculateTurretSpeed();
        calculateHoodSpeed();

        m_Turret.set(turretSpeed);
        m_Hood.set(hoodSpeed);

        field.setRobotPose(currentPose);
        field.getObject("turretPose").setPose(new Pose2d(currentTurretPose.getX(), currentTurretPose.getY(), new Rotation2d(Math.toRadians(turretTargetAngle) + currentPose.getRotation().getRadians())));
        field.getObject("targetPose").setPose(new Pose2d(targetPose.getX(), targetPose.getY(), new Rotation2d(targetPose.getRotation().getAngle())));

        // BALL COUNTER!
        // ballCounterUpdateNumbers();
        // if (isCountingBalls) {
        //     switch (shooterState) {
        //         case IDLE:
        //             if (isRunningShooter) {
        //                 shooterState = ShooterStateRPS.REVVING_UP;
        //             }

        //             break;
        //         case REVVING_UP:
        //             if (!isRunningShooter) {
        //                 shooterState = ShooterStateRPS.IDLE;
        //             }

        //             if (hasReachedExpectedTargetRPS()) {
        //                 shooterState = ShooterStateRPS.AT_SPEED;
        //             }

        //             break;
        //         case AT_SPEED:
        //             if (!isRunningShooter) {
        //                 shooterState = ShooterStateRPS.IDLE;
        //             }

        //             if (!hasReachedExpectedTargetRPS()) {
        //                 hasJustShot = false;
        //                 shooterState = ShooterStateRPS.RPS_DIPPING;
        //             }
                    
        //             break;
        //         case RPS_DIPPING:
        //             if (!isRunningShooter) {
        //                 shooterState = ShooterStateRPS.IDLE;
        //             }

        //             if (!getIsLobShot() && m_Shooter.getVelocity().getValueAsDouble() < expectedRPS - rpsDropPerShot && !hasJustShot) {
        //                 ballsShot++;
        //                 hasJustShot = true;
        //             }


        //             if (hasReachedExpectedTargetRPS()) {
        //                 shooterState = ShooterStateRPS.AT_SPEED;
        //             }
                    
        //             break;
        //     }
        // }
        putSmartDashboard();
    }

    private void getEncoderPositions() {
        turretPosition = turretEncoder.getPosition().getValueAsDouble() * Constants.TurretShooterConstants.turretDegreesToEncoderTicks - 45; // turret is now at the back right corner of the robot
        hoodPosition = hoodAngleOffset + 90 - (hoodEncoder.getPosition().getValueAsDouble() * Constants.TurretShooterConstants.hoodDegreesToEncoderTicks + 0.0);

    }

    public void updateTargetPose(Pose3d hubPose) {
        Optional<DriverStation.Alliance> alliance = DriverStation.getAlliance();
        if (alliance.isPresent()) {
            if (alliance.get() == DriverStation.Alliance.Red) {
                hubPose = Constants.TurretShooterConstants.redHubPose;
                lobPoseRight = Constants.TurretShooterConstants.redLobPoseRight;
                lobPoseLeft = Constants.TurretShooterConstants.redLobPoseLeft;

                opponentZoneXVal = Constants.TurretShooterConstants.neutralZoneFrontBound;
                allianceZoneXVal = Constants.TurretShooterConstants.neutralZoneBackBound;
                
            } else{
                hubPose = Constants.TurretShooterConstants.blueHubPose;
                lobPoseRight = Constants.TurretShooterConstants.blueLobPoseRight;
                lobPoseLeft = Constants.TurretShooterConstants.blueLobPoseLeft;

                opponentZoneXVal = Constants.TurretShooterConstants.neutralZoneBackBound;
                allianceZoneXVal = Constants.TurretShooterConstants.neutralZoneFrontBound;

            }
        } else{
            hubPose = Constants.TurretShooterConstants.redHubPose;

            lobPoseRight = Constants.TurretShooterConstants.redLobPoseRight;
            lobPoseLeft = Constants.TurretShooterConstants.redLobPoseLeft;

            opponentZoneXVal = Constants.TurretShooterConstants.neutralZoneFrontBound;
            allianceZoneXVal = Constants.TurretShooterConstants.neutralZoneBackBound;
        }
        targetPose = hubPose;

    }

    private void putSmartDashboardPIDValues() {
        SmartDashboard.putData("Field", field);


        SmartDashboard.putNumber("turret_kP", turret_kP);
        SmartDashboard.putNumber("turret_kI", turret_kI);
        SmartDashboard.putNumber("turret_kD", turret_kD);

        SmartDashboard.putNumber("hood_kP", hood_kP);
        SmartDashboard.putNumber("hood_kI", hood_kI);
        SmartDashboard.putNumber("hood_kD", hood_kD);

        SmartDashboard.putNumber("zoneShooterSpeed", zoneShooterSpeed);
        SmartDashboard.putNumber("hoodTargetAngle", hoodTargetAngle);

        SmartDashboard.putNumber("shooter_kS", slot0Configs.kS);
        SmartDashboard.putNumber("shooter_kV", slot0Configs.kV);
        SmartDashboard.putNumber("shooter_kP", slot0Configs.kP);
        SmartDashboard.putNumber("shooter_kI", slot0Configs.kI);
        SmartDashboard.putNumber("shooter_kD", slot0Configs.kD);


    }

    public void calculateDeltaXY() {
        deltaX = targetPose.getX() - currentTurretPose.getX();
        deltaY = targetPose.getY() - currentTurretPose.getY();

    }

    private void putSmartDashboard() {
        SmartDashboard.putNumber("turretPosition", turretPosition);
        SmartDashboard.putNumber("hoodPosition", hoodPosition);

        SmartDashboard.putNumber("hoodEncoderPositionRaw", hoodEncoder.getPosition().getValueAsDouble());
        SmartDashboard.putNumber("turretEncoderPositionRaw", turretEncoder.getPosition().getValueAsDouble());

        SmartDashboard.putNumber("turretTargetAngle", turretTargetAngle);
        SmartDashboard.putNumber("hoodTargetAngle", hoodTargetAngle);

        SmartDashboard.putNumber("targetPoseX", targetPose.getX());
        SmartDashboard.putNumber("targetPoseY", targetPose.getY());
        SmartDashboard.putNumber("targetPoseZ", targetPose.getZ());

        SmartDashboard.putNumber("currentTurretPoseX", currentTurretPose.getX());
        SmartDashboard.putNumber("currentTurretPoseY", currentTurretPose.getY());

        SmartDashboard.putNumber("shooterCurrentSpeed", m_Shooter.getVelocity().getValueAsDouble());

        SmartDashboard.putNumber("currentTurretPoseX", currentTurretPose.getX());
        SmartDashboard.putNumber("currentTurretPoseY", currentTurretPose.getY());
        SmartDashboard.putNumber("currentTurretPoseRot", currentTurretPose.getRotation().getRadians());

        SmartDashboard.putNumber("targetPoseX", targetPose.getX());
        SmartDashboard.putNumber("targetPoseY", targetPose.getY());
        SmartDashboard.putNumber("targetPoseRot", targetPose.getRotation().getAngle());

        SmartDashboard.putNumber("deltaX", deltaX);
        SmartDashboard.putNumber("deltaY", deltaY);

        SmartDashboard.putNumber("calculateDistance", calculateDistance(deltaX, deltaY));
        
        SmartDashboard.putNumber("turretAbsolutePosition", turretEncoder.getAbsolutePosition().getValueAsDouble());
        SmartDashboard.putNumber("krakenEncoderPosition", (m_Turret.getPosition().getValueAsDouble() - 0.260742) * Constants.TurretShooterConstants.krakenEncoderConversionFactor);

        SmartDashboard.putNumber("zoneShooterSpeed", zoneShooterSpeed);

        SmartDashboard.putNumber("relativeVelocity", relativeVelocity);
        SmartDashboard.putNumber("relativeVelocityParallel", relativeVelocityParallel);
        SmartDashboard.putNumber("relativeVelocityPerpendicular", relativeVelocityPerpendicular);

        SmartDashboard.putNumber("hoodAngleOffset", hoodAngleOffset);

        SmartDashboard.putNumber("xFieldSpeed", getChassisSpeeds.get().vxMetersPerSecond);
        SmartDashboard.putNumber("yFieldSpeed", getChassisSpeeds.get().vyMetersPerSecond);

        SmartDashboard.putNumber("getXVelocity", getXVelocity.getAsDouble());
        SmartDashboard.putNumber("getYVelocity", getYVelocity.getAsDouble());

        SmartDashboard.putNumber("hoodCurrent", m_Hood.getStatorCurrent().getValueAsDouble());
        SmartDashboard.putNumber("baseRPSOffset", baseRPSOffset);
        SmartDashboard.putNumber("turretAngleOffset", turretAngleOffset);

        SmartDashboard.putNumber("shooterGetSupplyCurrent", m_Shooter.getSupplyCurrent().getValueAsDouble());
        SmartDashboard.putNumber("turretPositionDifference", turretTargetAngle - turretPosition);

        SmartDashboard.putNumber("expectedRPS", expectedRPS);
        SmartDashboard.putNumber("rpsDropPerShot", rpsDropPerShot);
        SmartDashboard.putNumber("ballsShot", ballsShot);
        SmartDashboard.putString("shooterState", shooterState.toString());
        SmartDashboard.putBoolean("hasJustShot", hasJustShot);
        // SmartDashboard.putBoolean("hasReachedExpectedTargetRPS", hasReachedExpectedTargetRPS());
        SmartDashboard.putNumber("RPSExpectedDifference", expectedRPS - m_Shooter.getVelocity().getValueAsDouble());
        SmartDashboard.putNumber("turretTarget angle Difference", turretTargetAngle - turretPosition);

        // SmartDashboard.putNumber("flipConstant", flipConstant);
    }

    public void updatePose() {
        currentPose = getPose2d.get();
        getCurrentRotation = currentPose.getRotation().getDegrees();
    }

    public void updateTurretPose() {
        updatePose();
        currentTurretPose = new Pose2d(currentPose.getX() + Constants.TurretShooterConstants.turretXOffset * Math.cos(currentPose.getRotation().getRadians()) - Constants.TurretShooterConstants.turretYOffset * Math.sin(currentPose.getRotation().getRadians()),
                                currentPose.getY() + Constants.TurretShooterConstants.turretXOffset * Math.sin(currentPose.getRotation().getRadians()) + Constants.TurretShooterConstants.turretYOffset * Math.cos(currentPose.getRotation().getRadians()),
                                currentPose.getRotation());


    }

    private void getSmartDashboardPIDValues() {
        turretPIDController.setP(SmartDashboard.getNumber("turret_kP", turret_kP));
        turretPIDController.setI(SmartDashboard.getNumber("turret_kI", turret_kI));
        turretPIDController.setD(SmartDashboard.getNumber("turret_kD", turret_kD));

        hoodPIDController.setP(SmartDashboard.getNumber("hood_kP", hood_kP));
        hoodPIDController.setI(SmartDashboard.getNumber("hood_kI", hood_kI));
        hoodPIDController.setD(SmartDashboard.getNumber("hood_kD", hood_kD));

        if (slot0Configs.kS != SmartDashboard.getNumber("shooter_kS", slot0Configs.kS) 
        || slot0Configs.kV != SmartDashboard.getNumber("shooter_kV", slot0Configs.kV) 
        || slot0Configs.kP != SmartDashboard.getNumber("shooter_kP", slot0Configs.kP) 
        || slot0Configs.kI != SmartDashboard.getNumber("shooter_kI", slot0Configs.kI) 
        || slot0Configs.kD != SmartDashboard.getNumber("shooter_kD", slot0Configs.kD)) {

            slot0Configs.kS = SmartDashboard.getNumber("shooter_kS", slot0Configs.kS);
            slot0Configs.kV = SmartDashboard.getNumber("shooter_kV", slot0Configs.kV);
            slot0Configs.kP = SmartDashboard.getNumber("shooter_kP", slot0Configs.kP);
            slot0Configs.kI = SmartDashboard.getNumber("shooter_kI", slot0Configs.kI);
            slot0Configs.kD = SmartDashboard.getNumber("shooter_kD", slot0Configs.kD);

            shooterTalonFXConfigurator.apply(slot0Configs);
            followerShooterTalonFXConfigurator.apply(slot0Configs);
            
        }

        
        
    }

    public void isLobbing() {
        
            if (!isInAllianceZone()) {
                double distanceToRightLobPose = calculateDistance(getCurrentTurretOffsetPosition().getX() - lobPoseRight.getX(), getCurrentTurretOffsetPosition().getY() - lobPoseRight.getY());
                double distanceToLeftLobPose = calculateDistance(getCurrentTurretOffsetPosition().getX() - lobPoseLeft.getX(), getCurrentTurretOffsetPosition().getY() - lobPoseLeft.getY());
                if (distanceToLeftLobPose > distanceToRightLobPose) {
                    targetPose = lobPoseRight;

                } else {
                    targetPose = lobPoseLeft;

                }
                calculateDeltaXY();
            }
        
    }

    public boolean isInOpponentZone() {
        if (allianceZoneXVal < opponentZoneXVal) { // basically means if it's blue XD
            return currentTurretPose.getX() > opponentZoneXVal;
        } else {
            return currentTurretPose.getX() < opponentZoneXVal;
        }
    }

    public boolean isInAllianceZone() {
        if (allianceZoneXVal < opponentZoneXVal) {
            return currentTurretPose.getX() < allianceZoneXVal;
        } else {
            return currentTurretPose.getX() > allianceZoneXVal;
        }
    }
    
    public Pose2d getCurrentTurretOffsetPosition(){
        return currentTurretPose;

    }

    public double calculateDistance(double deltaX, double deltaY) {
        return Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
    }

    public void calculateShootingWhileMovingTargetPose() {
    ChassisSpeeds fieldSpeeds = getChassisSpeeds.get(); 

    double vx = fieldSpeeds.vxMetersPerSecond;
    double vy = fieldSpeeds.vyMetersPerSecond;
    double omega = fieldSpeeds.omegaRadiansPerSecond;
    double t = Constants.TurretShooterConstants.turretExperimentalTimeToTarget;

    double xDist;
    double yDist;

    if (Math.abs(omega) > 0.0001) {
        double sinOmegaT = Math.sin(omega * t);
        double cosOmegaT = Math.cos(omega * t);

        xDist = (vx * sinOmegaT + vy * (cosOmegaT - 1)) / omega;
        yDist = (vy * sinOmegaT - vx * (cosOmegaT - 1)) / omega;
    } else {
       
        xDist = vx * t;
        yDist = vy * t;
    }

    targetPose = new Pose3d(
        targetPose.getX() - xDist,
        targetPose.getY() - yDist,
        targetPose.getZ(),
        new Rotation3d(0, 0, 0)
    );

    calculateDeltaXY();
}

public void calcNewShootOnTheMove() {
    ChassisSpeeds corSpeeds = getChassisSpeeds.get();
    double vx = corSpeeds.vxMetersPerSecond;
    double vy = corSpeeds.vyMetersPerSecond;
    double omega = corSpeeds.omegaRadiansPerSecond;
    double t = Constants.TurretShooterConstants.turretExperimentalTimeToTarget;

    double heading = currentPose.getRotation().getRadians();
    double tx = Constants.TurretShooterConstants.turretXOffset;
    double ty = Constants.TurretShooterConstants.turretYOffset;

    double offsetXNow = tx * Math.cos(heading) - ty * Math.sin(heading);
    double offsetYNow = tx* Math.sin(heading) + ty * Math.cos(heading);
    double offsetXFuture = tx * Math.cos(heading + omega*t) - ty * Math.sin(heading + omega*t);
    double offsetYFuture = tx * Math.sin(heading + omega*t) + ty * Math.cos(heading + omega * t);

    double xDist = vx * t + (offsetXFuture - offsetXNow);
    double yDist = vy * t + (offsetYFuture - offsetYNow);

    targetPose = new Pose3d(targetPose.getX() - xDist, targetPose.getY() - yDist, targetPose.getZ(), new Rotation3d(0, 0, 0));
    calculateDeltaXY();
}

    // public void calculateTargetPoseWithAngularVelocity() {
    //     ChassisSpeeds chassisSpeeds = getChassisSpeeds.get();
    //     double xSpeed = chassisSpeeds.vxMetersPerSecond;
    //     double ySpeed = chassisSpeeds.vyMetersPerSecond;


    //     // Angular Velocity Code

    //     double angularVelocity = chassisSpeeds.omegaRadiansPerSecond; // CCW
    //     double radius = Math.sqrt(Math.pow(Constants.TurretShooterConstants.turretXOffset, 2) + Math.pow(Constants.TurretShooterConstants.turretYOffset, 2));
    //     // 90 degs cancel out
    //     double fieldRelativeAngle_ResultingVector = getPose2d.get().getRotation().getDegrees() + Math.toDegrees(Math.asin(Constants.TurretShooterConstants.turretXOffset / radius));
    //     double tangentialVelocity = radius * angularVelocity;

    //     xSpeed += Math.cos(Math.toRadians(fieldRelativeAngle_ResultingVector)) * tangentialVelocity;
    //     ySpeed += Math.sin(Math.toRadians(fieldRelativeAngle_ResultingVector)) * tangentialVelocity; // 2 for testing

    //     double xDist = xSpeed * Constants.TurretShooterConstants.turretExperimentalTimeToTarget;
    //     double yDist = ySpeed * Constants.TurretShooterConstants.turretExperimentalTimeToTarget;

    //     targetPose = new Pose3d(targetPose.getX() - xDist, targetPose.getY() - yDist, targetPose.getZ(), new Rotation3d(0, 0, 0));
    //     calculateDeltaXY();

    // }

    public void gradientHoodShootingStatic() {
        currentShooterSpeed = m_Shooter.getVelocity().getValueAsDouble() - baseRPSOffset; // rps

        TreeMap<Double, double[]> curves = Constants.TurretShooterConstants.speedCurves;

        Double lowerKey = curves.floorKey(currentShooterSpeed);
        Double upperKey = curves.ceilingKey(currentShooterSpeed);    

        double distance = calculateDistance(deltaX, deltaY);

        if (lowerKey == null) {
            hoodTargetAngle = curves.firstEntry().getValue()[0] * distance + curves.firstEntry().getValue()[1];
            return;
        }
        if (upperKey == null) {
            hoodTargetAngle = curves.lastEntry().getValue()[0] * distance + curves.lastEntry().getValue()[1];
            return;
        }
        if (lowerKey.equals(upperKey)) {
            double[] c = curves.get(lowerKey);
            hoodTargetAngle = c[0] * distance + c[1];
            return;
        }
        
        double t = (currentShooterSpeed - lowerKey) / (upperKey - lowerKey);
        double hoodLower = curves.get(lowerKey)[0] * distance + curves.get(lowerKey)[1];
        double hoodUpper = curves.get(upperKey)[0] * distance + curves.get(upperKey)[1];
        
        hoodTargetAngle = (1 - t) * hoodLower + t * hoodUpper;

        if (hoodTargetAngle <= 65 && !isInOpponentZone()) {
            hoodTargetAngle = 65;
        }
        
        if (hoodTargetAngle <= 57 && isInOpponentZone()) {
            hoodTargetAngle = 57;
        }
    }

    public void calculateTurretAngleStatic() {
        turretTargetAngle = Math.toDegrees(Math.atan2(deltaY, deltaX)) - getCurrentRotation;
    
        // May requrie deadband to prevent oscillation
        // supposed to be 90 and -270, however turret does not have standard -180 to 180
        // this doesnt work, but it rotates around 120 and 240 instead of 120 and 300 because it recalculates turret at 120 if it hovers around that point
        
        turretTargetAngle += flipConstant * 360;

        if (flipConstant == 0) {
            if (turretTargetAngle > 120) {
                flipConstant = -1;
                turretTargetAngle -= 360;
            }

            else if (turretTargetAngle < -300) {
                flipConstant = 1;
                turretTargetAngle += 360;
            }
        }

        if (flipConstant == 1) {
            if (turretTargetAngle > 120) {
                flipConstant = 0;
            }
            else if (turretTargetAngle < -300) {
                flipConstant = 0;
            }
        }

        if (flipConstant == -1) {
            if (turretTargetAngle > 120) {
                flipConstant = 0;
            }
            else if (turretTargetAngle < -300) {
                flipConstant = 0;
            }
        }

        // if (turretTargetAngle > 120) {
        //     turretTargetAngle -= 360;

        // } else if (turretTargetAngle < -300) {
        //     turretTargetAngle += 360;

        // }



        
    }

    public void calculateLinearShooterSpeed() {
        double distanceToTarget = calculateDistance(deltaX, deltaY);

        zoneShooterSpeed = Math.pow(distanceToTarget / 5.7, 1.7) * 25 + 51 + baseRPSOffset;

        // Caps shooter speed to 78 + offset (offset default 5.0 RPS)
        if (zoneShooterSpeed > 80 + baseRPSOffset && !isInOpponentZone()) {
            zoneShooterSpeed = 80 + baseRPSOffset;
        }

        // Caps shooter speed to 65 RPS + offset if isLobbing
        if (!isInAllianceZone() && !isInOpponentZone()) {
            if (zoneShooterSpeed > 70 + baseRPSOffset) {
                zoneShooterSpeed = 70 + baseRPSOffset;
            }
        }

        if (isInOpponentZone()) {
            if (zoneShooterSpeed > 80) {
                zoneShooterSpeed = 80; // speed cap at 97 so cassette keeps running
            }
        }

        if (!isInOpponentZone()) {
            zoneShooterSpeed /= 1.777777;
        }

        if (!isInOpponentZone() && !isInAllianceZone() && (Math.abs(currentTurretPose.getX() - opponentZoneXVal) < Math.abs(currentTurretPose.getX() - allianceZoneXVal))) {
            zoneShooterSpeed *= 1.2;
        }
        // zoneShooterSpeed = SmartDashboard.getNumber("zoneShooterSpeed", zoneShooterSpeed);
        
    }

    private void calculateTurretSpeed() {
        setTurretSetpoint();
        double extraVel = 1; // May be used in the future
        turretSpeed = MathUtil.clamp(turretPIDController.calculate(turretPosition, turretSetpoint + turretAngleOffset) * extraVel, -Constants.TurretShooterConstants.TurretMaxSpeed, Constants.TurretShooterConstants.TurretMaxSpeed);
    }

    private void calculateHoodSpeed() {

        hoodSetpoint = hoodTargetAngle;

        if (hoodSetpoint <= 65 && !isInOpponentZone()) {
            hoodSetpoint = 65;
        }
        if (hoodSetpoint <= 54 && isInOpponentZone()) {
            hoodSetpoint = 54;
        }

        if (!isInOpponentZone() && !isInAllianceZone() && (Math.abs(currentTurretPose.getX() - opponentZoneXVal) < Math.abs(currentTurretPose.getX() - allianceZoneXVal))) {
            hoodSetpoint -= 7;
            if (hoodSetpoint <= 57){
                hoodSetpoint = 57;
            }
        }

        hoodSpeed = hoodPIDController.calculate(hoodPosition, hoodSetpoint);
        if (hoodPosition > hoodSetpoint) {
            hoodSpeed *= Constants.TurretShooterConstants.FeedForwardMultiplier;
        }
        else {
            hoodSpeed /= Constants.TurretShooterConstants.FeedForwardMultiplier;
        }
        hoodSpeed = MathUtil.clamp(hoodSpeed, -Constants.TurretShooterConstants.HoodMaxSpeed, Constants.TurretShooterConstants.HoodMaxSpeed);
    }

    private void updateTurretAdjustments() {

    }

    public void setTurretSetpoint() {
        turretSetpoint = turretTargetAngle;
    }

    public void setHoodSetpoint() {
        hoodSetpoint = hoodTargetAngle;
    }

    public void runShooter() {
        isRunningShooter = true;

    }

    public void stopShooter() {
        isRunningShooter = false;

    }

    
    
    public boolean isAtShooterSpeed() {
        if (isInOpponentZone()) {
        return m_Shooter.getVelocity().getValueAsDouble() > (zoneShooterSpeed - (Constants.TurretShooterConstants.shooterMinimumTargetRPSDeadband * 1.2));

        }
        return m_Shooter.getVelocity().getValueAsDouble() > (zoneShooterSpeed - Constants.TurretShooterConstants.shooterMinimumTargetRPSDeadband);
    }

    public boolean isAtTurretAngle() {
        return Math.abs(turretPosition - turretSetpoint) < Constants.TurretShooterConstants.turretAngleDeadband;

    }

    public boolean isAtHoodAngle() {
        return Math.abs(hoodPosition - hoodSetpoint) < Constants.TurretShooterConstants.hoodAngleDeadband;

    }

    public void setHoodAngleOffset() {
        hoodAngleOffset = 90 - (hoodPosition - hoodAngleOffset);

    }

    public void decreaseHoodAngleOffset() {
        hoodAngleOffset -= 0.5;

    }

    public void increaseHoodAngleOffset() {
        hoodAngleOffset += 0.5;
    
    }

    public void slowDecreaseHoodAngleOffset() {
        hoodAngleOffset -= 0.333;

    }

    public double getStatorCurrent() {
        return m_Hood.getStatorCurrent().getValueAsDouble();

    }

    public void decreaseBaseRPSOffset() {
        baseRPSOffset -= 1;
    }

    public void increaseBaseRPSOffset() {
        baseRPSOffset += 1;
    }

    public void zeroBaseRPSOffset() {
        baseRPSOffset = Constants.TurretShooterConstants.defaultBaseRPSConstant;
    }

    public void increaseTurretAngleOffset() {
        turretAngleOffset += 1.0;
    }

    public void decreaseTurretAngleOffset() {
        turretAngleOffset -= 1.0;
    }

    public void zeroTurretAngleOffset() {
        turretAngleOffset = 0.0;
    }

    // public boolean getIsLobShot() {
    //     return Constants.TurretShooterConstants.neutralZoneFrontBound < getCurrentTurretOffsetPosition().getX() && getCurrentTurretOffsetPosition().getX() < Constants.TurretShooterConstants.neutralZoneBackBound;
    // }

    // public int getBallCounter() {
    //     return ballsShot;
    // }

    // public void ballCounterUpdateNumbers() {
    //     expectedRPS = zoneShooterSpeed + Constants.TurretShooterConstants.expectedRPSOffset;
    //     // rps drops more at lower speeds per shot
    //     rpsDropPerShot = 2.5;

        
    // }

    // public boolean hasReachedExpectedTargetRPS() {
    //     return Math.abs(m_Shooter.getVelocity().getValueAsDouble() - expectedRPS) < Constants.TurretShooterConstants.ballCounterDeadband;
    // }

}