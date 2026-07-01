'use client';

import {useState} from "react";

export default function CodeBlock({lineNumber, lineToHTML, fileName, isOpen, toggleFunc}: {lineNumber: number; lineToHTML: string, fileName: string, isOpen: boolean, toggleFunc: () => void}) {

    // const [comment, changeCommenting] = useState(false);

    function commentLog() {
        console.log(`insert comment in ${fileName}.java at ${lineNumber}`);
    }

    
    function currentLineOpen(lineNumber: number) {
        return lineNumber;
    }

    return (<main>

   
        <div key={lineNumber} data-linenumber={lineNumber} className="flex whitespace-pre">
                <div>
                    <button onClick={() => {toggleFunc();commentLog();currentLineOpen(lineNumber);}} className="text-green-600 cursor-pointer hover:text-2xl transition-all duration-250 pr-2">+</button>
                </div>
                <span className="text-gray-500 pr-2 min-w-8 select-none">{lineNumber}</span>
                <span dangerouslySetInnerHTML={{__html: lineToHTML}} />
            
        </div>
        
            {isOpen && (
                <div className="w-full bg-gray-100 flex-none ml-0 pt-1 pb-4 animate-[bounce_0.25s_linear_0.5_forwards]">
                <textarea className="p-2 ml-8 w-full border border-gray-600 bg-white" placeholder="Comment">

                </textarea>
                </div>

            )}
</main>
    );
}