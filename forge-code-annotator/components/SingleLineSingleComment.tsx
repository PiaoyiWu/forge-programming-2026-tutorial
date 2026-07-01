"use client";

import CodeBlock from "@/components/CodeBlock";
import {useState} from "react";


export default function SingleLineSingleComment({lineMap, fileName}: {lineMap: {lineNumber: number; lineToHTML: string;}[]; fileName: string}) {
    const [currentLineOpen, setLineOpen] = useState<number | null>(null);
    
    return (
        <div>

        {lineMap.map((line) => (
        <CodeBlock key={line.lineNumber} lineNumber={line.lineNumber} lineToHTML={line.lineToHTML} fileName={fileName} isOpen={(currentLineOpen === line.lineNumber)} toggleFunc={() => setLineOpen((line.lineNumber === currentLineOpen) ? null : line.lineNumber)} />

        ))}
        </div>

    );
}