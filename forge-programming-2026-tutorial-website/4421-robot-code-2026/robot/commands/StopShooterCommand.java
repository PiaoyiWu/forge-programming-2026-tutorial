package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants.CommandSwerveDrivetrainConstants;
import frc.robot.subsystems.CommandSwerveDrivetrain;
import frc.robot.subsystems.TurretShooterSubsystem;

public class StopShooterCommand extends Command {
    private boolean isDone = false;

    public TurretShooterSubsystem m_TurretShooterSubsystem;
    public CommandSwerveDrivetrain drivetrain;

    public StopShooterCommand(TurretShooterSubsystem turretShooterSubsystem, CommandSwerveDrivetrain drivetrain) {
        this.m_TurretShooterSubsystem = turretShooterSubsystem;
        this.drivetrain = drivetrain;

        isDone = false;

        
    }

    
    @Override
    public void initialize() {
        

        drivetrain.setRotOverTurret(false);
        m_TurretShooterSubsystem.stopShooter();

        isDone = true;
        
    }

    
    @Override
    public void execute() {
        
    }

    
    @Override
    public void end(boolean interrupted) {
        isDone = false;
    }

    
    @Override
    public boolean isFinished() {
        return isDone;
    }

}

