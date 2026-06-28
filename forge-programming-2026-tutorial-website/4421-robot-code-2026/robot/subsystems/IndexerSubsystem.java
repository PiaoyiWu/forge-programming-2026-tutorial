package frc.robot.subsystems;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.TalonFXConfigurator;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;

import edu.wpi.first.wpilibj.motorcontrol.Talon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class IndexerSubsystem extends SubsystemBase {
    private BooleanSupplier isAtShooterSpeed;
    private BooleanSupplier isAtTurretAngle;
    private BooleanSupplier isAtHoodAngle;

    private TalonFX m_IntakeSideIndexer;
    private TalonFX m_ShooterSideIndexer;
    private TalonFX m_Kicker;

    private TalonFXConfigurator IntakeSideIndexerConfig;
    private TalonFXConfigurator ShooterSideIndexerConfig;
    private TalonFXConfigurator KickerConfig;

    private MotorOutputConfigs IntakeSideIndexerOutputConfig;
    private MotorOutputConfigs ShooterSideIndexerOutputConfig;
    private MotorOutputConfigs KickerOutputConfig;

    private CurrentLimitsConfigs IntakeSideIndexerCurrentLimitConfig;
    private CurrentLimitsConfigs ShooterSideIndexerCurrentLimitConfig;
    private CurrentLimitsConfigs KickerCurrentLimitConfig;

    // private double IntakeSideIndexerSpeed = Constants.IndexerConstants.IntakeSideIndexerSpeed;
    // private double ShooterSideIndexerSpeed = Constants.IndexerConstants.ShooterSideIndexerSpeed;
    // private double KickerSpeed = Constants.IndexerConstants.KickerSpeed;

    private double IntakeSideIndexerSpeed;
    private double ShooterSideIndexerSpeed;
    private double KickerSpeed;

    public boolean isRunBackwards = false;
    

    public IndexerSubsystem(BooleanSupplier isAtShooterSpeed, BooleanSupplier isAtTurretAngle, BooleanSupplier isAtHoodAngle) {
        this.isAtShooterSpeed = isAtShooterSpeed;
        this.isAtTurretAngle = isAtTurretAngle;
        this.isAtHoodAngle = isAtHoodAngle;
        
        m_IntakeSideIndexer = new TalonFX(Constants.IndexerConstants.IntakeSideIndexerID);
        m_ShooterSideIndexer = new TalonFX(Constants.IndexerConstants.ShooterSideIndexerID);
        m_Kicker = new TalonFX(Constants.IndexerConstants.KickerID);

        IntakeSideIndexerConfig = m_IntakeSideIndexer.getConfigurator();
        ShooterSideIndexerConfig = m_ShooterSideIndexer.getConfigurator();
        KickerConfig = m_Kicker.getConfigurator();

        IntakeSideIndexerOutputConfig = new MotorOutputConfigs();
        ShooterSideIndexerOutputConfig = new MotorOutputConfigs();
        KickerOutputConfig = new MotorOutputConfigs();

        IntakeSideIndexerCurrentLimitConfig = new CurrentLimitsConfigs();
        ShooterSideIndexerCurrentLimitConfig = new CurrentLimitsConfigs();
        KickerCurrentLimitConfig = new CurrentLimitsConfigs();

        IntakeSideIndexerCurrentLimitConfig.SupplyCurrentLimit = Constants.IndexerConstants.CassetteSupplyCurrentLimit;
        ShooterSideIndexerCurrentLimitConfig.SupplyCurrentLimit = Constants.IndexerConstants.CassetteSupplyCurrentLimit;
        KickerCurrentLimitConfig.SupplyCurrentLimit = Constants.IndexerConstants.CassetteSupplyCurrentLimit;

        IntakeSideIndexerCurrentLimitConfig.SupplyCurrentLimitEnable = true;
        ShooterSideIndexerCurrentLimitConfig.SupplyCurrentLimitEnable = true;
        KickerCurrentLimitConfig.SupplyCurrentLimitEnable = true;

        IntakeSideIndexerConfig.apply(IntakeSideIndexerCurrentLimitConfig);
        ShooterSideIndexerConfig.apply(ShooterSideIndexerCurrentLimitConfig);
        KickerConfig.apply(KickerCurrentLimitConfig);

        IntakeSideIndexerOutputConfig.Inverted = InvertedValue.Clockwise_Positive;
        ShooterSideIndexerOutputConfig.Inverted = InvertedValue.CounterClockwise_Positive;
        KickerOutputConfig.Inverted = InvertedValue.Clockwise_Positive;

        IntakeSideIndexerOutputConfig.NeutralMode = NeutralModeValue.Coast;
        ShooterSideIndexerOutputConfig.NeutralMode = NeutralModeValue.Coast;
        KickerOutputConfig.NeutralMode = NeutralModeValue.Coast;

        IntakeSideIndexerConfig.apply(IntakeSideIndexerOutputConfig);
        ShooterSideIndexerConfig.apply(ShooterSideIndexerOutputConfig);
        KickerConfig.apply(KickerOutputConfig);

        IntakeSideIndexerSpeed = 0.0;
        ShooterSideIndexerSpeed = 0.0;
        KickerSpeed = 0.0;

        isRunBackwards = false;

        putSmartDashboard();
        putPIDSmartDashboard();

    }

    @Override
    public void periodic() {
        putSmartDashboard();
        
        isAtSetpoints();

        if (isRunBackwards) {
            IntakeSideIndexerSpeed = -0.25;
            ShooterSideIndexerSpeed = -0.25;
            KickerSpeed = -0.25;
        } else {
            isAtSetpoints();
        }

        m_IntakeSideIndexer.set(IntakeSideIndexerSpeed);
        m_ShooterSideIndexer.set(ShooterSideIndexerSpeed);
        m_Kicker.set(KickerSpeed);
    }


    private void putSmartDashboard() {
        SmartDashboard.putBoolean("isAtShooterSpeed", isAtShooterSpeed.getAsBoolean());
        SmartDashboard.putBoolean("isAtTurretAngle", isAtTurretAngle.getAsBoolean());
        SmartDashboard.putBoolean("isAtHoodAngle", isAtHoodAngle.getAsBoolean());

        SmartDashboard.putNumber("IntakeSideIndexerSpeed", IntakeSideIndexerSpeed);
        SmartDashboard.putNumber("ShooterSideIndexerSpeed", ShooterSideIndexerSpeed);
        SmartDashboard.putNumber("KickerSpeed", KickerSpeed);

        SmartDashboard.putNumber("IntakeCassetteCurrent", m_IntakeSideIndexer.getSupplyCurrent().getValueAsDouble());
        SmartDashboard.putNumber("ShooterCassetteCurrent", m_ShooterSideIndexer.getSupplyCurrent().getValueAsDouble());


    }

    private void putPIDSmartDashboard() {
        SmartDashboard.putNumber("Intake Cassette Speed", Constants.IndexerConstants.IntakeSideIndexerSpeed);
        SmartDashboard.putNumber("Shooter Cassette Speed", Constants.IndexerConstants.ShooterSideIndexerSpeed);
        SmartDashboard.putNumber("Kicker Cassette Speed", Constants.IndexerConstants.KickerSpeed);

    }

    private void isAtSetpoints() {
        if (isAtShooterSpeed.getAsBoolean() && isAtTurretAngle.getAsBoolean()) { // add  && isAtTurretAngle.getAsBoolean()
            IntakeSideIndexerSpeed = SmartDashboard.getNumber("Intake Cassette Speed", Constants.IndexerConstants.IntakeSideIndexerSpeed);
            ShooterSideIndexerSpeed = SmartDashboard.getNumber("Shooter Cassette Speed", Constants.IndexerConstants.ShooterSideIndexerSpeed);
            KickerSpeed = SmartDashboard.getNumber("Kicker Cassette Speed", Constants.IndexerConstants.KickerSpeed);

        } else {
            IntakeSideIndexerSpeed = 0.0;
            ShooterSideIndexerSpeed = 0.0;
            KickerSpeed = 0.0;

        }
    }

    public void isRunBackwardsF(boolean runBackBool) {
        isRunBackwards = runBackBool;
    }

    // public void runBackwards(){
    //     if (isAtShooterSpeed.getAsBoolean()){
    //         return;
    //     } else {
    //         IntakeSideIndexerSpeed = -0.50;
    //         ShooterSideIndexerSpeed = -0.50;
    //         KickerSpeed = 0.0;
    //     }

    // }
    
}
