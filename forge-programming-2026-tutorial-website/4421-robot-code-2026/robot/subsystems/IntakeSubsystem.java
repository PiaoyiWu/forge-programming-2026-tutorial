package frc.robot.subsystems;

import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.TalonFXConfigurator;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class IntakeSubsystem extends SubsystemBase {
    private TalonFX m_IntakeRoller;

    private TalonFXConfigurator IntakeRollerConfig;

    private MotorOutputConfigs IntakeOutputConfig;
    private CurrentLimitsConfigs IntakeCurrentLimit;

    private double IntakeRollerSpeed;


    public IntakeSubsystem() {

        m_IntakeRoller = new TalonFX(Constants.IntakeConstants.IntakeRollerID);

        IntakeRollerConfig = m_IntakeRoller.getConfigurator();
        IntakeOutputConfig = new MotorOutputConfigs();
        IntakeCurrentLimit = new CurrentLimitsConfigs();

        IntakeOutputConfig.Inverted = InvertedValue.Clockwise_Positive;
        IntakeOutputConfig.NeutralMode = NeutralModeValue.Coast;
        
        IntakeCurrentLimit.SupplyCurrentLimit = 30;
        IntakeCurrentLimit.SupplyCurrentLimitEnable = true;

        IntakeRollerConfig.apply(IntakeOutputConfig);
        IntakeRollerConfig.apply(IntakeCurrentLimit);

        IntakeRollerSpeed = 0.0;
    }

    @Override
    public void periodic() {
        m_IntakeRoller.set(IntakeRollerSpeed);
    }

    public void StopIntake() {
        IntakeRollerSpeed = 0.0;
    }

    public void OutTake() {
        IntakeRollerSpeed = -Constants.IntakeConstants.defaultIntakeSpeed;
    }

    public void SetIntakeSpeed(double customHighSpeed) {
        IntakeRollerSpeed = customHighSpeed;
    }



}
