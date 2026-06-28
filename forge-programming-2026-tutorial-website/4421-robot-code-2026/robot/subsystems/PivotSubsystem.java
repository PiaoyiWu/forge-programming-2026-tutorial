package frc.robot.subsystems;

import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.TalonFXConfigurator;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.ctre.phoenix6.controls.Follower;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import edu.wpi.first.wpilibj.Timer;

public class PivotSubsystem extends SubsystemBase {
    private TalonFX m_LeftPivot;
    private TalonFX m_RightPivot;

    private TalonFXConfigurator LeftPivotConfig;
    private TalonFXConfigurator RightPivotConfig;

    private MotorOutputConfigs LeftPivotOutputConfig;
    private MotorOutputConfigs RightPivotOutputConfig;

    private CurrentLimitsConfigs leftPivotCurrentLimitsConfigs;
    private CurrentLimitsConfigs rightPivotCurrentLimitsConfigs;

    private final double PivotMaxSpeed = Constants.PivotConstants.PivotSpeed * Constants.PivotConstants.PivotSpeedPercent;

    private double LeftPivotSpeed;
    private double RightPivotSpeed;

    private double ArmFeedfowardOutput;

    private PIDController pivotPidController;
    private ArmFeedforward armFeedforward;

    private double leftPivotBottomSetpoint = Constants.PivotConstants.leftPivotEncoderBottom;
    private double leftPivotMiddleSetpoint = Constants.PivotConstants.leftPivotEncoderMid;
    private double leftPivotTopSetpoint = Constants.PivotConstants.leftPivotEncoderStart;

    private double leftPivotSetpoint = leftPivotBottomSetpoint;

    private double rightPivotBottomSetpoint = Constants.PivotConstants.rightPivotEncoderBottom;
    private double rightPivotMiddleSetpoint = Constants.PivotConstants.rightPivotEncoderMid;
    private double rightPivotTopSetpoint = Constants.PivotConstants.rightPivotEncoderStart;

    private double rightPivotSetpoint = rightPivotBottomSetpoint;

    // private CANcoder e_LeftEncoder;
    // private CANcoder e_RightEncoder;

    private double leftEncoderPosition;
    private double rightEncoderPosition;

    private double ffAngle;

    public boolean shakeStatus;
    public enum ShakeState {
        TOP, BOTTOM, NONE
    }

    public ShakeState shakeState;
    private Timer shakeTimer = new Timer();
    private Timer shakeTimeout = new Timer();

    public PivotSubsystem() {
        m_LeftPivot = new TalonFX(Constants.PivotConstants.LeftPivotID);
        m_RightPivot = new TalonFX(Constants.PivotConstants.RightPivotID);

        LeftPivotConfig = m_LeftPivot.getConfigurator();
        RightPivotConfig = m_RightPivot.getConfigurator();

        LeftPivotOutputConfig = new MotorOutputConfigs();
        RightPivotOutputConfig = new MotorOutputConfigs();

        leftPivotCurrentLimitsConfigs = new CurrentLimitsConfigs();
        rightPivotCurrentLimitsConfigs = new CurrentLimitsConfigs();

        // Preserve previous inversion: left inverted, right not
        LeftPivotOutputConfig.Inverted = InvertedValue.Clockwise_Positive;
        RightPivotOutputConfig.Inverted = InvertedValue.CounterClockwise_Positive;

        // Pivot motors should brake when idle to hold position
        LeftPivotOutputConfig.NeutralMode = NeutralModeValue.Brake;
        RightPivotOutputConfig.NeutralMode = NeutralModeValue.Brake;

        leftPivotCurrentLimitsConfigs.SupplyCurrentLimit = 20.0;
        rightPivotCurrentLimitsConfigs.SupplyCurrentLimit = 20.0;

        leftPivotCurrentLimitsConfigs.SupplyCurrentLimitEnable = true;
        rightPivotCurrentLimitsConfigs.SupplyCurrentLimitEnable = true;

        LeftPivotConfig.apply(LeftPivotOutputConfig);
        RightPivotConfig.apply(RightPivotOutputConfig);

        LeftPivotConfig.apply(leftPivotCurrentLimitsConfigs);
        RightPivotConfig.apply(rightPivotCurrentLimitsConfigs);

        leftEncoderPosition = m_LeftPivot.getPosition().getValueAsDouble();
        rightEncoderPosition = m_RightPivot.getPosition().getValueAsDouble();

        LeftPivotSpeed = 0.0;
        RightPivotSpeed = 0.0;
        ArmFeedfowardOutput = 0.0;

        pivotPidController = new PIDController(Constants.PivotConstants.pivot_kP, Constants.PivotConstants.pivot_kI, Constants.PivotConstants.pivot_kD);

        armFeedforward = new ArmFeedforward(Constants.PivotConstants.pivot_kS, Constants.PivotConstants.pivot_kG, Constants.PivotConstants.pivot_kV);

        shakeStatus = false;
        shakeState = ShakeState.NONE;

        putSmartDashboard();
        putPIDSmartDashboard();
    }

    @Override
    public void periodic() {
        getPIDFromSmartDashboard();
        putSmartDashboard();
        calculatePivotSpeed();

        // if (shakeStatus && goingTop()) {
        //     shakeState = ShakeState.TOP;
        // } else if (shakeStatus && goingBottom()) {
        //     shakeState = ShakeState.BOTTOM;
        // }
        
        // else {
        //     shakeState = ShakeState.NONE;
        // }

        switch (shakeState) {
            case TOP:
                if (!shakeStatus) {
                    shakeState = ShakeState.NONE;
                    break;
                }

                topPivotSetpoint();
                calculatePivotSpeed();

                if (shakeTimeout.get() > Constants.PivotConstants.pivotShakeTimeout && hasStopped()) {
                    shakeTimer.reset();
                    shakeTimer.start();
                    shakeTimeout.stop();
                    shakeState = ShakeState.BOTTOM;
                }
                

                if ((isStalling() && ((hasStopped() && !AtBottomWithDeadband()) || AtTopWithDeadband())) || (AtTopWithDeadband() && hasStopped())) {
                    shakeTimer.reset();
                    shakeTimer.start();
                    shakeTimeout.stop();
                    shakeState = ShakeState.BOTTOM;
                }

                break;
            
            case BOTTOM:
                if (!shakeStatus) {
                    shakeState = ShakeState.NONE;
                    break;
                }
                bottomPivotSetpoint();
                calculatePivotSpeed();

                if (shakeTimer.get() > Constants.PivotConstants.pivotShakeDelay && AtBottomWithDeadband() && (hasStopped() || isStalling())) {
                    shakeTimer.stop();
                    shakeTimeout.reset();
                    shakeTimeout.start();
                    shakeState = ShakeState.TOP;
                }

                break;

            case NONE:
                calculatePivotSpeed();

                if (shakeStatus) {
                    shakeState = ShakeState.TOP;
                }

                break;
        }


        m_LeftPivot.set(LeftPivotSpeed + ArmFeedfowardOutput);
        m_RightPivot.set(RightPivotSpeed + ArmFeedfowardOutput);
        putSmartDashboard();
        
    }

    public void putSmartDashboard() {
        // SmartDashboard.putNumber("Pivot Encoder Position", encoderPosition);
        SmartDashboard.putNumber("Pivot Speed", LeftPivotSpeed);
        // SmartDashboard.putNumber("Pivot Bottom Setpoint", pivotBottomSetpoint);
        // SmartDashboard.putNumber("Pivot Top Setpoint", pivotTopSetpoint);
        SmartDashboard.putNumber("Pivot Feedforward Output", ArmFeedfowardOutput);

        SmartDashboard.putNumber("leftPivotCurrent", m_LeftPivot.getSupplyCurrent().getValueAsDouble());
        SmartDashboard.putNumber("rightPivotCurrent", m_RightPivot.getSupplyCurrent().getValueAsDouble());

        SmartDashboard.putNumber("Left Pivot Encoder", m_LeftPivot.getPosition().getValueAsDouble());
        SmartDashboard.putNumber("Right Pivot Encoder", m_RightPivot.getPosition().getValueAsDouble());

        SmartDashboard.putBoolean("pivot hasStopped", hasStopped());
        SmartDashboard.putBoolean("pivot isStalling", isStalling());
        SmartDashboard.putNumber("left pivotSpeed", m_LeftPivot.getVelocity().getValueAsDouble());
        SmartDashboard.putNumber("right pivotSpeed", m_RightPivot.getVelocity().getValueAsDouble());
        SmartDashboard.putNumber("left pivotCurrent", m_LeftPivot.getStatorCurrent().getValueAsDouble());
        SmartDashboard.putNumber("right pivotCurrent", m_RightPivot.getStatorCurrent().getValueAsDouble());
        SmartDashboard.putString("shakeState", shakeState.toString());
        SmartDashboard.putBoolean("shakeStatus", shakeStatus);
        SmartDashboard.putBoolean("pivot AtBottom", AtBottomWithDeadband());
        SmartDashboard.putBoolean("pivot AtTop", AtTopWithDeadband());

    }

    public void putPIDSmartDashboard() {
        SmartDashboard.putNumber("Pivot kP", pivotPidController.getP());
        SmartDashboard.putNumber("Pivot kI", pivotPidController.getI());
        SmartDashboard.putNumber("Pivot kD", pivotPidController.getD());

        SmartDashboard.putNumber("Pivot kS", Constants.PivotConstants.pivot_kS);
        SmartDashboard.putNumber("Pivot kG", Constants.PivotConstants.pivot_kG);
        SmartDashboard.putNumber("Pivot kV", Constants.PivotConstants.pivot_kV);
    }

    public void getPIDFromSmartDashboard() {
        pivotPidController.setP(SmartDashboard.getNumber("Pivot kP", pivotPidController.getP()));
        pivotPidController.setI(SmartDashboard.getNumber("Pivot kI", pivotPidController.getI()));
        pivotPidController.setD(SmartDashboard.getNumber("Pivot kD", pivotPidController.getD()));

        armFeedforward.setKs(SmartDashboard.getNumber("Pivot kS", Constants.PivotConstants.pivot_kS));
        armFeedforward.setKg(SmartDashboard.getNumber("Pivot kG", Constants.PivotConstants.pivot_kG));
        armFeedforward.setKv(SmartDashboard.getNumber("Pivot kV", Constants.PivotConstants.pivot_kV));

    }

    private void calculatePivotSpeed() {
        leftEncoderPosition = m_LeftPivot.getPosition().getValueAsDouble();
        rightEncoderPosition = m_RightPivot.getPosition().getValueAsDouble();

        // ffAngle = (leftEncoderPosition - Constants.PivotConstants.encoderToZeroOffset) * Constants.PivotConstants.encoderToAngleConversion;

        LeftPivotSpeed = MathUtil.clamp(pivotPidController.calculate(leftEncoderPosition, leftPivotSetpoint), -PivotMaxSpeed, PivotMaxSpeed);
        RightPivotSpeed = MathUtil.clamp(pivotPidController.calculate(rightEncoderPosition, rightPivotSetpoint), -PivotMaxSpeed, PivotMaxSpeed);
        ArmFeedfowardOutput = armFeedforward.calculate(Math.toRadians(ffAngle), 0);
    }

    public void bottomPivotSetpoint() {
        leftPivotSetpoint = leftPivotBottomSetpoint;
        rightPivotSetpoint = rightPivotBottomSetpoint;

    }

    public void topPivotSetpoint() {
        leftPivotSetpoint = leftPivotTopSetpoint;
        rightPivotSetpoint = rightPivotTopSetpoint;

    }

    public void middlePivotSetpoint() {
        leftPivotSetpoint = leftPivotMiddleSetpoint;
        rightPivotSetpoint = rightPivotMiddleSetpoint;
    }

    public void shakeStatusChange(boolean status) {
        shakeStatus = status;
    }

    public boolean goingTop() {
        if (Math.abs(leftPivotSetpoint - leftPivotTopSetpoint) < Constants.PivotConstants.topBottomShakeSetpointDeadband || Math.abs(rightPivotSetpoint - rightPivotTopSetpoint) < Constants.PivotConstants.topBottomShakeSetpointDeadband) {
            return true;

        } else {
            return false;

        }
        
    }

    public boolean goingBottom() {
        if (Math.abs(leftPivotSetpoint - leftPivotBottomSetpoint) < Constants.PivotConstants.topBottomShakeSetpointDeadband || Math.abs(rightPivotSetpoint - rightPivotBottomSetpoint) < Constants.PivotConstants.topBottomShakeSetpointDeadband) {
            return true;

        } else {
            return false;

        }
        
    }

    public boolean hasStopped() {
        if (m_LeftPivot.getVelocity().getValueAsDouble() < Constants.PivotConstants.shakeSpeedIsStoppedDeadband && m_RightPivot.getVelocity().getValueAsDouble() < Constants.PivotConstants.shakeSpeedIsStoppedDeadband) {
            return true;
        } else {
            return false;
        }
        
    }

    public boolean isStalling() {
        if (m_LeftPivot.getStatorCurrent().getValueAsDouble() > Constants.PivotConstants.shakeStatorCurrentDeadband || m_RightPivot.getStatorCurrent().getValueAsDouble() > Constants.PivotConstants.shakeStatorCurrentDeadband) {
            return true;

        } else {
            return false;
        }

    }

    public boolean AtBottomWithDeadband() {
        if (leftEncoderPosition > Constants.PivotConstants.bottomSwitchDeadbandZone || rightEncoderPosition > Constants.PivotConstants.bottomSwitchDeadbandZone) {
            return true;
        } else {
            return false;
        }

    }

    public boolean AtTopWithDeadband() {
        if (leftEncoderPosition < Constants.PivotConstants.topSwitchDeadbandZone || rightEncoderPosition < Constants.PivotConstants.topSwitchDeadbandZone) {
            return true;
        } else {
            return false;
        }

    }

}