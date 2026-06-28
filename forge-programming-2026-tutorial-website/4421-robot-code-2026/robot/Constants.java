package frc.robot;

import java.util.Map;
import java.util.TreeMap;

import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Pose3d;

public class Constants {
    public class CommandSwerveDrivetrainConstants {
        public static final double ambiguityThreshold = 0.55; // previously 0.85
        public static final double areaThreshold = 0.06;
        public static final double limelightStdDevs = 1.5; // 0.7

        public static final double shootingSpeed = 0.12;
        public static final double slowModeSpeed = 0.27;
        
        public static final double rotatingWhileStrafingTestMultiplier = 1.0;

    }

    public class IntakeConstants {
        public static final int IntakeRollerID = 32;
        public static final double defaultIntakeSpeed = 1.0;
        
    }

    public class PivotConstants {
        public static final int LeftPivotID = 30;
        public static final int RightPivotID = 31;

        public static final int PivotEncoderID = 34;

        public static final double PivotSpeedPercent = 1.0;
        public static final double PivotSpeed = 1.0;

        public static final double PivotEncoderBottomSetpoint = 0.58;
        public static final double PivotEncoderMiddleSetpoint = 0.2267;
        public static final double PivotEncoderTopSetpoint = 0.0;

        public static final double pivot_kP = 0.1;
        public static final double pivot_kI = 0;
        public static final double pivot_kD = 0.001;

        public static final double pivot_kS = 0.0;
        public static final double pivot_kG = 0.0;
        public static final double pivot_kV = 0.0;

        public static final double encoderToAngleConversion = 199.5885;
        public static final double encoderToZeroOffset = 0.090576;

        public static final double leftPivotEncoderStart = 0.0;
        public static final double rightPivotEncoderStart = 0.0;
        
        public static final double leftPivotEncoderMid = 0.7;
        public static final double rightPivotEncoderMid = 0.7;

        public static final double leftPivotEncoderBottom = 2.87630859375;
        public static final double rightPivotEncoderBottom = 2.97630859375;

        public static final double topBottomShakeSetpointDeadband = 0.3;
        public static final double shakeSpeedIsStoppedDeadband = 1.0;
        public static final double shakeStatorCurrentDeadband = 15.0; // tuning required

        public static final double bottomSwitchDeadbandZone = 2.67;
        public static final double topSwitchDeadbandZone = 0.5;

        public static final double pivotShakeDelay = 0.4;
        public static final double pivotShakeTimeout = 1.0;

    }

    public class IndexerConstants {
        public static final int IntakeSideIndexerID = 46;
        public static final int ShooterSideIndexerID = 47;

        public static final int KickerID = 48;

        public static final double IntakeSideIndexerSpeed = 0.8;
        public static final double ShooterSideIndexerSpeed = 0.8;
        public static final double KickerSpeed = 1.0;

        public static final double CassetteSupplyCurrentLimit = 35.0;

    }

    public class TurretShooterConstants {
        public static final int FollowerShooterRollerID = 40;
        public static final int ShooterRollerID = 41;
        public static final int TurretID = 42;
        public static final int HoodID = 43;

        public static final int TurretEncoderID = 44;
        public static final int HoodEncoderID = 45;

        public static final double MaxShooterSpeedRPM = 0.50 * 7530;
        //public static final double shooterSpeed = 0.50;
        public static final double defaultShooterSpeed = 70; // rps

        public static final double turret_kP = 0.015;

        public static final double turret_kI = 0.0;
        public static final double turret_kD = 0.0001;

        public static final double hood_kP = 0.02;
        public static final double hood_kI = 0.0;
        public static final double hood_kD = 0.0001;

        public static final double shooter_kS = 0.1;
        public static final double shooter_kV = 0.12;
        public static final double shooter_kP = 0.11;
        public static final double shooter_kI = 0.0;
        public static final double shooter_kD = 0.02;

        public static final double FeedForwardMultiplier = 1.10;

        public static final double TurretMaxSpeed = 0.40;
        public static final double HoodMaxSpeed = 0.30;

        // z (height) may need to be recalculated to be the vertical distance between the shooter wheels and our hub height
        public static final Pose3d blueHubPose = new Pose3d(4.625594, 4.034536, 1.8288, new Rotation3d(0.0, 0.0, 0.0));
        public static final Pose3d redHubPose = new Pose3d(11.91539, 4.034536, 1.8288, new Rotation3d(0.0, 0.0, 0.0));

        public static final Pose3d blueLobPoseRight = new Pose3d((91.055 * 0.0254), (79.4225 * 0.0254), 0.0, new Rotation3d(0.0, 0.0, 0.0));
        public static final Pose3d blueLobPoseLeft = new Pose3d((91.055 * 0.0254), (238.2675 * 0.0254), 0.0, new Rotation3d(0.0, 0.0, 0.0));
        public static final Pose3d redLobPoseRight = new Pose3d((560.165 * 0.0254), (79.4225 * 0.0254), 0.0, new Rotation3d(0.0, 0.0, 0.0));
        public static final Pose3d redLobPoseLeft = new Pose3d((560.165 * 0.0254), (238.2675 * 0.0254), 0.0, new Rotation3d(0.0, 0.0, 0.0));

        public static final double neutralZoneFrontBound = 182.11 * 0.0254; // 182.11 inches to meters
        public static final double neutralZoneBackBound = (651.22 - 182.11) * 0.0254;

        public static final double gravitationalConstant = 9.80665;
        public static final double hoodDegreesToEncoderTicks = 26.06;
        public static final double turretDegreesToEncoderTicks = 120.0;

        public static final double krakenEncoderConversionFactor = 360 / 27.0;

        public static final double ballExitVelocity = 7.3891;

        public static final double turretXOffset = -4.987 * 0.0254; // -4 inches to meters
        public static final double turretYOffset = -6 * 0.0254; // -5 inches to meters

        public static final double shooterMinimumTargetRPSDeadband = 8.0; // RPS
        public static final double turretAngleDeadband = 10.0; // degrees
        public static final double hoodAngleDeadband = 1.0; // degree(s)

        public static final double hoodLobAngle = 58.567;

        public static final double turretExperimentalTimeToTarget = 1.1;

        public static final double defaultBaseRPSConstant = 2.0;
        // public static final double speedCurvesOffsetConstant = 2.18; // no longer used, now change offset directly in speedCurves 
        // Hood angle = -11.23(distance) + 104.9, speed of 55 rps
        // Hood angle = -10.10(distance) + 106.6, speed of 60 rps
        // Hood angle = -9.185(distance) + 107.1, speed of 65 rps
        // Hood angle = -7.575(distance) + 107.1, speed of 70 rps
        // Hood angle = -6.986(distance) + 107.4, speed of 75 rps
        
        public static final TreeMap<Double, double[]> speedCurves = new TreeMap<>(Map.of(
                            55.0, new double[]{-11.23, 104.9 - 5},
                            60.0, new double[]{-10.10,  106.6 - 7},
                            65.0, new double[]{-9.185,  107.1 - 7},
                            70.0, new double[]{-7.575,  107.1 - 7},
                            75.0, new double[]{-6.986,  107.4 - 7}));
                            
        public static final double ballCounterDeadband = 1.0;
        public static final double expectedRPSOffset = 0.0;
    }
}