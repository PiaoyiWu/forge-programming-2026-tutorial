package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.TurretShooterSubsystem;

public class autoHoodAngleOffsetCommand extends Command {
    private boolean isDone = false;

    public TurretShooterSubsystem m_TurretShooterSubsystem;

    public autoHoodAngleOffsetCommand(TurretShooterSubsystem m_TurretShooterSubsystem) {
        
        isDone = false;
        this.m_TurretShooterSubsystem = m_TurretShooterSubsystem;
        
    }

    
    @Override
    public void initialize() {
        
        
    }

    
    @Override
    public void execute() {
        m_TurretShooterSubsystem.slowDecreaseHoodAngleOffset();

        if (m_TurretShooterSubsystem.getStatorCurrent() > 17.9) {
            m_TurretShooterSubsystem.setHoodAngleOffset();
            isDone = true;
        } 
        
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

