import { ViewportServices } from "../../../lib/services/ViewportServices";
import { Icon, IconName } from "../../icons/Icon";
import { TriggerEvent } from "../../../lib/services/event_factory/events/context_menu/triggers/TriggerEvent";
import { ContextEvent } from "../../../lib/services/event_factory/events/context_menu/ContextEvent";

const eventFactory = ViewportServices.get().eventFactory();

type ContextTrigger = {
    children: JSX.Element | JSX.Element[] | string | number;
    options: JSX.Element[];
}
const ContextTrigger = (props: ContextTrigger) => {
    return <div onContextMenu={() => eventFactory.fire(new ContextEvent(true, props.options))}>{props.children}</div>
}

type ContextOption = {
    icon: IconName;
    children: string;
    event: TriggerEvent;
    invert?: boolean;
}
export const ContextOption = (props: ContextOption) => {
    const eventFactory = ViewportServices.get().eventFactory();
    
    return (
        <div
            className="relative h-[38px] my-2px rounded block p-5px px-10px text-left duration-300 bg-transparent z-0"
            onClick={() => eventFactory.fire(props.event)}
            key={Math.random()}>
            <Icon className={`absolute top-5px left-10px w-[27px] aspect-square inline-block ${props.invert ? "invert" : ""}`} iconName={props.icon} />
            <span className="absolute top-6px left-50px text-white">{props.children}</span>
        </div>
    );
}


export const context = {
    trigger: ContextTrigger,
    option: ContextOption,
}