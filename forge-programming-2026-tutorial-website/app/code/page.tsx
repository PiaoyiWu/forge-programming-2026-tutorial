import fs from "fs";
import path from "path";
import { codeToHtml } from "shiki";
import FolderBar from "@/components/FolderBar";

export default async function CodePage() {

    const turretShooterSubsystemPath = path.join(process.cwd(), "4421-robot-code-2026/robot/subsystems/TurretShooterSubsystem.java");
    const dispTurretShooterSubsystemCode = await fs.readFileSync(turretShooterSubsystemPath, "utf-8");

    const addTextColors = await codeToHtml(dispTurretShooterSubsystemCode, {lang: "java", theme: "one-light"});

    return (
        <main>
            <h1 className="font-bold p-4 text-5xl font-mono ml-5">CODE</h1>
            <div className="flex">
                <FolderBar />
                <div className="text-xs ml-5" dangerouslySetInnerHTML={{ __html: addTextColors }} />

            </div>
        </main>
    );
}