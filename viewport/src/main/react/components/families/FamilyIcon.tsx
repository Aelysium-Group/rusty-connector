import { Link } from "react-router-dom";
import { stringToHex } from "../../lib/stringToHEX";
import { clickable } from '../cursor/interactables/Clickable';
import { useEffect, useState } from 'react';

type FamilyIcon = {
    name: string;
}
export const FamilyIcon = (props: FamilyIcon) => {
    const [ hovered, setHovered ] = useState(false);

    useEffect(()=>{}, [hovered]);
    
    return (
        <Link
            to={`/${props.name.replace(" ","_")}`}
            >
            <clickable.div
                className={`relative inline-block w-50px m-5px aspect-square rounded-full overflow-hidden duration-500 ${hovered ? "z-50" : "text-white"}`}
                style={{ background: hovered ? "transparent" : "#" + stringToHex(props.name).slice(0, 6) }}
                onMouseOver={() => setHovered(true)}
                onMouseLeave={() => setHovered(false)}
                borderRadius="10rem"
                onClick={() => {}}
                >
                <span className={`relative block text-2xl font-bold blend-mode-overlay w-full text-center pointer-events-none mt-9px
                                  ${ hovered ? "text-neutral-900 z-50" : "text-white"}`}>
                    {props.name[0].toUpperCase()}
                </span>
            </clickable.div>
        </Link>
    );
}