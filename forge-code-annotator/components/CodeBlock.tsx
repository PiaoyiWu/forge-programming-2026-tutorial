'use client';

export default function CodeBlock({lineNumber, lineToHTML, fileName}: {lineNumber: number; lineToHTML: string, fileName: string}) {
    console.log(`${fileName}, line ${lineNumber}`);
    return (
        <div key={lineNumber} data-linenumber={lineNumber} className="flex whitespace-pre">
                <div>
                    <button onClick={() => console.log(`insert comment in ${fileName}.java at ${lineNumber}`)} className="text-green-600 cursor-pointer hover:text-2xl transition-all duration-250 pr-2">+</button>
                </div>
                <span className="text-gray-500 pr-2 min-w-8 select-none">{lineNumber}</span>
                <span dangerouslySetInnerHTML={{__html: lineToHTML}} />
            </div>
    );
}