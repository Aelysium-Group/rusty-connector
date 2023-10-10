import { ViewportServices } from "../../lib/services/ViewportServices";
import { TriggerEvent } from "./events/triggers/TriggerEvent";
import { Icon, IconName } from "../icons/Icon";

type ContextMenuOption = {
    icon: IconName;
    children: string;
    event: TriggerEvent;
    invert?: boolean;
}
export const ContextEntry = (props: ContextMenuOption) => {
    const eventFactory = ViewportServices.get().eventFactory();
    
    return (
        <div
            className="relative h-[38px] my-2px rounded-xl block p-5px px-10px text-left duration-300 bg-transparent z-0 hover:bg-neutral-800/50"
            onClick={() => eventFactory.fire(props.event)}
            key={Math.random()}>
            <Icon className={`absolute top-5px left-10px w-[27px] aspect-square inline-block ${props.invert ? "invert" : ""}`} iconName={props.icon} />
            <span className="absolute top-6px left-50px text-sm text-white">{props.children}</span>
        </div>
    );
}