package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.PivotSubsystem;

public class PivotDownCommand extends Command {
    private boolean isDone = false;
    public PivotSubsystem m_PivotSubsystem;

    public PivotDownCommand(PivotSubsystem m_pivotSubsystem) {
        this.m_PivotSubsystem = m_pivotSubsystem;
        isDone = false;

        
    }

    
    @Override
    public void initialize() {
        m_PivotSubsystem.bottomPivotSetpoint();

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

