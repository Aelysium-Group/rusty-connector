import { InterfaceColor } from "../../lib/InterfaceColor";
import { ViewportServices } from "../../lib/services/ViewportServices";
import { ContextEvent } from "../../lib/services/event_factory/events/context_menu/ContextEvent";
import { TriggerEvent } from "../../lib/services/event_factory/events/context_menu/triggers/TriggerEvent";
import { ContextEntry } from "../cursor/context_menu/ContextEntry";
import { InteractionText } from "../cursor/interactables/InteractionText";
import { ButtonInput } from "../inputs/ButtonInput";


const ctxs = [
    <ContextEntry icon={"pencil"} event={TriggerEvent.empty()} >hello this is a test!</ContextEntry>,
    <ContextEntry icon={"stop"} event={TriggerEvent.empty()} >hello this is a test2!</ContextEntry>
];

const eventFactory = ViewportServices.get().eventFactory();

export const NetworkOverview = () => {
    return (
        <>
            hello!
            <ButtonInput title="click me" onClick={() => {}} color={InterfaceColor.GREEN}/>
            <InteractionText>Hello this is some test text</InteractionText>
            <p
                onContextMenu={() => eventFactory.fire(new ContextEvent(true, ctxs))}
            >Hello this is some test text to see if this works</p>
            <div>

            </div>
        </>
    );
}