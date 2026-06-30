'use client';

import {useState} from "react";

export default function CodeBlock({lineNumber, lineToHTML, fileName}: {lineNumber: number; lineToHTML: string, fileName: string}) {

    const [comment, changeCommenting] = useState(false);

    function commentLog() {
        console.log(`insert comment in ${fileName}.java at ${lineNumber}`);
    }

    
    function currentLineOpen(lineNumber: number) {
        return lineNumber;
    }

    return (<main>

   
        <div key={lineNumber} data-linenumber={lineNumber} className="flex whitespace-pre">
                <div>
                    <button onClick={() => {changeCommenting(!comment);commentLog();currentLineOpen(lineNumber);}} className="text-green-600 cursor-pointer hover:text-2xl transition-all duration-250 pr-2">+</button>
                </div>
                <span className="text-gray-500 pr-2 min-w-8 select-none">{lineNumber}</span>
                <span dangerouslySetInnerHTML={{__html: lineToHTML}} />
            
        </div>
        
            {comment && (
                <div className="flex-none ml-8 pt-0 pb-4 animate-[bounce_0.5s_ease-out_1.5_forwards]">
                <textarea className="p-2 w-full border border-gray-600" placeholder="Comment">

                </textarea>
                </div>

            )}
</main>
    );
}