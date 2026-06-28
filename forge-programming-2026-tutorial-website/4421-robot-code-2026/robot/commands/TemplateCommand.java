package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;

public class TemplateCommand extends Command {
    private boolean isDone = false;

    public TemplateCommand() {
        
        isDone = false;

        
    }

    
    @Override
    public void initialize() {
        

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

