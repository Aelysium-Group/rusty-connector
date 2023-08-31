import { stringToHex } from "../../../../lib/stringToHEX";
import { clickable } from '../../../cursor/interactables/Clickable';
import { useState } from 'react';

type FamilyIcon = {
    name: string;
}
export const FamilyIcon = (props: FamilyIcon) => {
    const [ hovered, setHovered ] = useState(false);
    
    return (
        <clickable.div
            className="relative block w-50px aspect-square rounded-full overflow-hidden duration-500"
            style={{ background: hovered ? "transparent" : "#" + stringToHex(props.name).slice(0, 6) }}
            onMouseEnter={() => setHovered(true)}
            onMouseLeave={() => setHovered(false)}
            borderRadius="10rem"
            onClick={() => {}}
            >
            <span className="block text-white text-2xl font-bold blend-mode-overlay w-full text-center pointer-events-none mt-9px">
                {props.name[0].toUpperCase()}
            </span>
        </clickable.div>
    );
}