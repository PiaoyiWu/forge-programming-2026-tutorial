import fs from "fs";
import path from "path";
import {glob} from "glob";
import {codeToHast, codeToHtml} from "shiki";
import {toHtml} from "hast-util-to-html";
import FolderBar from "@/components/FolderBar";
import CodeBlock from "@/components/CodeBlock";
import SingleLineSingleComment from "@/components/SingleLineSingleComment";


export default async function FilePage({params}: {params: Promise<{filename: string}>}) {
    const {filename} = await params;

    const findFile = await glob(`4421-robot-code-2026/robot/**/${filename}.java`, {cwd: process.cwd()});

    if (findFile.length == 0) {
        return (
            <main>
                <h1>404 not found</h1>
            </main>
        )
    }

    const filePath = path.join(process.cwd(), findFile[0]);
        const dispFileCode = fs.readFileSync(filePath, "utf-8");
    
        // returns js object (instead of <pre> and <span> tags it will return an object with tagname or type.)
        // planning to use this so that you can manipulate each line so that you can add a class, line number, line id, or a comment
        const createHast = await codeToHast(dispFileCode, 
            {lang: "java", theme: "one-light"}
        )


        // currently the one in use
        const addTextColors = await codeToHtml(dispFileCode, {lang: "java", theme: "one-light"});
    

        const preTag = createHast.children[0] as any;
        const codeTag = preTag.children[0] as any;

        //add a filter right here so that it ignores all the "text: \n" or new lines or wtv
        const lines = codeTag.children.filter((node: any) => (node.type === "element" && node.tagName === "span")) as any[];
        const lineMap = lines.map((line, i) => ({lineNumber: i+1, lineToHTML: toHtml(line),}));

        // 0number of lines
        console.log(lines.length);

        
        return (
            <main className="flex flex-col h-screen max-h-screen overflow-hidden">
                <h1 className="font-bold p-5 text-5xl font-mono ml-5 flex-none">{filename}.java</h1>
                <div className="min-h-0 flex flex-1 border-t border-black">
                    <div className="h-full overflow-y-auto flex-none">
                        <FolderBar />
                    </div>
                    
                    <div className="h-full overflow-y-auto border-l border-black bg-[#FAFAFA] text-xs p-10 min-w-0 flex-1 overflow-x-auto [&_pre]:w-max [&_pre]:min-w-full">
                        <SingleLineSingleComment lineMap={lineMap} fileName={filename} />
                    </div>
    
                </div>
            </main>
    );


}