import fs from "fs";
import path from "path";
import {glob} from "glob";
import {codeToHtml} from "shiki";
import FolderBar from "@/components/FolderBar"

export default async function FilePage({params}: {params: Promise<{filename: string}>}) {
    const {filename} = await params;

    const findFile = await glob(`4421-robot-code-2026/robot/**/${filename}.java`, {cwd: process.cwd()});

    if (findFile.length == 0) {
        return (
            <main>
                <div>404 not found</div>
            </main>
        )
    }

    const filePath = path.join(process.cwd(), findFile[0]);
        const dispFileCode = fs.readFileSync(filePath, "utf-8");
    
        const addTextColors = await codeToHtml(dispFileCode, {lang: "java", theme: "one-light"});
    
        return (
            <main className="flex flex-col h-screen max-h-screen overflow-hidden">
                <h1 className="font-bold p-4 text-5xl font-mono ml-5 flex-none">{filename}.java</h1>
                <div className="min-h-0 flex flex-1 border-t border-black">
                                <div className="h-full overflow-y-auto flex-none">
                                    <FolderBar />
                                </div>
                                
                                <div className="h-full overflow-y-auto border-l border-black bg-[#FAFAFA] text-xs p-10 min-w-0 flex-1 overflow-x-auto [&_pre]:w-max [&_pre]:min-w-full" dangerouslySetInnerHTML={{ __html: addTextColors }} />
                
                            </div>
            </main>
    );


}