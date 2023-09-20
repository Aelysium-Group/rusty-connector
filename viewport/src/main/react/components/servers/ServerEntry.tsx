import { Link, useParams } from "react-router-dom";
import { useEffect, useState } from 'react';
import { ServerPartial } from "./ServerList";

type ServerEntry = {
    server: ServerPartial;
    onClick: (server: ServerPartial) => void;
    lit?: boolean;
}
export const ServerEntry = (props: ServerEntry) => {
    const [ hovered, setHovered ] = useState(false);
    const { family_id } = useParams();

    useEffect(()=>{}, [hovered]);
    
    if(props.lit ?? false)
        return (
            <div
                className={`relative h-30px block w-full m-5px rounded overflow-hidden bg-neutral-300 rounded`}
                onClick={() => {}}
                >
                <span className={`relative left-20px block text-sm blend-mode-overlay w-full pointer-events-none mt-3px duration-500 text-neutral-900`}>
                    {props.server.name.toLowerCase().replace(" ","-")}
                </span>
            </div>
        );
    else
        return (
            <div
                className={`relative h-30px block w-full m-5px rounded overflow-hidden duration-500 hover:bg-neutral-300`}
                onMouseEnter={() => setHovered(true)}
                onMouseLeave={() => setHovered(false)}
                //borderRadius="0.2rem"
                onClick={() => props.onClick(props.server)}
                >
                <span className={`relative left-20px block text-sm blend-mode-overlay w-full pointer-events-none mt-3px duration-500
                                  ${ hovered ? "text-neutral-900" : "text-white"}`}>
                    {props.server.name.toLowerCase().replace(" ","-")}
                </span>
            </div>
        );
}