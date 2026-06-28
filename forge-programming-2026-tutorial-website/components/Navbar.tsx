import Link from "next/link";
// use link not <a> so the entire website doesnt get re renedered every tiome someone clicks a navlink

export default function Navbar() {
    // big notes
    // had to add flex to div otherwise gap-4 dont work
    // link classname is for hover (same one i used in the past but changed a bit to fit the theme!)
    const fatNavLinkAnimationClass = "relative after:absolute after:bottom-0 after:left-0 after:h-0.5 after:w-0 after:bg-black after:transition-all after:duration-250 hover:after:w-full"
    return (
        <nav className="flex border-b shadow-md">
            <div className="flex pl-10 p-3">
                <h1 className="text-2xl font-mono font-bold">4421 FORGE</h1>
            </div>
            <div className="ml-auto flex gap-5 pr-10 p-4">

            <Link className={fatNavLinkAnimationClass} href="/">HOME</Link>
            <Link className={fatNavLinkAnimationClass} href="/code">CODE</Link>

            </div>
        </nav>
    );
}