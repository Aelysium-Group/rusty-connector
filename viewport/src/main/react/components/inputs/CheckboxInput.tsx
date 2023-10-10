import { useEffect, useState } from "react";

interface Checkbox {
    onChange?: Function;
    insteadOfChange?: Function;
    defaultValue?: boolean;
}
export const Checkbox = (props: Checkbox) => {
    const [ isActive, setActivity ] = useState(false);

    const init = () => setActivity(props.defaultValue ?? false);

    const handleClick = () => {
        let newIsActive = false;
        
        if(!isActive) newIsActive = true;
        
        if(props.insteadOfChange) return props.insteadOfChange(newIsActive);
        
        setActivity(newIsActive);
        if(props.onChange)
            props.onChange(newIsActive);
    }

    useEffect(()=>{init();},[props.defaultValue]);

    const render = () => (
        <div 
            className="relative block h-[26px] w-[26px] overflow-hidden rounded"
            onClick={handleClick}>
            <div
                className={`relative rounded h-full w-full duration-500 border-4
                          ${isActive ? 'border-green-600' : 'border-neutral-500'}`}>
                <div className={`absolute bg-green-600 duration-300 rounded-full overflow-hidden
                               ${isActive ? '-left-1/2 -top-1/2 h-[200%] w-[200%]' : 'left-1/2 top-1/2 h-0 w-0'}`}>
                </div>
            </div>
            <div className={`absolute left-0 duration-200
                          ${isActive ? 'opacity-100 top-0' : 'opacity-0 top-full'}`}>
                L
            </div>
        </div>
    );

    return render();
}