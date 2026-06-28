'use client'

import { useState } from "react";

export default function FolderBar() {

    const fileTreeFormat = {
        name: "robot",
        type: "folder",
        children: [
            {
                name: "commands",
                type: "folder",
                children: [
                    {name: "autoHoodAngleOffsetCommand.java", type: "file"},
                    {name: "hamburgerCommand.java", type: "file"},
                    {name: "IntakeCommand.java", type: "file"},
                    {name: "OuttakeCommand.java", type: "file"},
                    {name: "PivotDownCommand.java", type: "file"},
                    {name: "PivotUpCommand.java", type: "file"},
                    {name: "ReverseCassetteCommand.java", type: "file"},
                    {name: "ShootCommand.java", type: "file"},
                    {name: "StartShakeCommand.java", type: "file"},
                    {name: "StopIntakeCommand.java", type: "file"},
                    {name: "StopShakeCommand.java", type: "file"},
                    {name: "StopShooterCommand.java", type: "file"},
                    {name: "TemplateCommand.java", type: "file"},
                    
                ]
            },
            {
                name: "generated",
                type: "folder",
                children: [
                    {name: "TunerConstants.java", type: "file"},
                ]
            },
            {
                name: "subsystems",
                type: "folder",
                children: [
                    {name: "CommandSwerveDrivetrain.java", type: "file"},
                    {name: "IndexerSubsystem.java", type: "file"},
                    {name: "IntakeSubsystem.java", type: "file"},
                    {name: "LightSubsystem.java", type: "file"},
                    {name: "PivotSubsystem.java", type: "file"},
                    {name: "TurretShooterSubsystem.java", type: "file"},
                ]
            },
            {name: "Constants.java", type: "file"},
            {name: "LimelightHelpers.java", type: "file"},
            {name: "Main.java", type: "file"},
            {name: "Robot.java", type: "file"},
            {name: "RobotContainer.java", type: "file"},
            {name: "SolveQuartic.java", type: "file"},
            {name: "Telemetry.java", type: "file"},
        ]
    };

    return (
        <main>
            <div className="m-5">
                <h1>files</h1>
            </div>
        </main>
    );
}