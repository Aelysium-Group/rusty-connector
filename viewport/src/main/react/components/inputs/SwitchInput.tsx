import * as React from "react";
import { useEffect, useState } from "react";

interface Switch {
    onChange?: Function;
    insteadOfChange?: Function;
    defaultValue?: boolean;
}
export const Switch = (props: Switch) => {
    const [ isActive, setActivity ] = useState(false);

    const init = () => setActivity(props.defaultValue);

    const handleClick = () => {
        let newIsActive = false;
        
        if(!isActive) newIsActive = true;
        
        if(props.insteadOfChange) return props.insteadOfChange(newIsActive);
        
        setActivity(newIsActive);
        props.onChange(newIsActive);
    }

    useEffect(()=>{init();},[props.defaultValue]);

    const render = () => (
        <div
            className={`relative block rounded-full h-[26px] w-50px cursor-pointer overflow-hidden duration-200
                      ${isActive ? 'bg-green-600' : 'bg-neutral-500'}`}
            onClick={handleClick}>
            <div className={`relative top-3px left-3px rounded-full h-20px w-[44px] overflow-hidden`}>
                <div className={`relative rounded-full bg-white h-20px w-30px duration-500
                               ${isActive ? 'left-[24px]' : '-left-10px'}`}>
                    <span className={`absolute top-4px text-2xs duration-100
                                    ${isActive ? '-left-20px' : '-left-50px'}`}>
                        ON
                    </span>
                    <div className='relative rounded-full bg-white h-20px w-30px'></div>
                    <span className={`absolute top-4px text-2xs duration-100
                                    ${isActive ? 'left-50px' : 'left-[33px]'}`}>
                        OFF
                    </span>
                </div>
            </div>
        </div>
    );

    return render();
}