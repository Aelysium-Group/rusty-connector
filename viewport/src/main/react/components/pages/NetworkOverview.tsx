import { InterfaceColor } from "../../lib/InterfaceColor";
import { ViewportServices } from "../../lib/services/ViewportServices";
import { ContextEvent } from "../context_menu/events/ContextEvent";
import { TriggerEvent } from "../context_menu/events/triggers/TriggerEvent";
import { ContextEntry } from "../context_menu/ContextEntry";
import { InteractionText } from "../cursor/interactables/InteractionText";
import { ButtonInput } from "../inputs/ButtonInput";
import { useLog } from "../../lib/hooks/useLog";


const ctxs = [
    <ContextEntry icon={"pencil"} event={TriggerEvent.empty()} >hello this is a test!</ContextEntry>,
    <ContextEntry icon={"stop"} event={TriggerEvent.empty()} >hello this is a test2!</ContextEntry>
];

const eventFactory = ViewportServices.get().eventFactory();

export const NetworkOverview = () => {
    const logs = useLog();

    return (
        <>
            hello!
            <ButtonInput title="click me" onClick={() => logs.add.confirm("test message! hello this is a test! please work!")} color={InterfaceColor.GREEN}/>
            <ButtonInput title="click me" onClick={() => logs.add.error("test message! hello this is a test! please work!")} color={InterfaceColor.GREEN}/>
            <InteractionText >Hello this is some test text</InteractionText>
            <p onContextMenu={() => eventFactory.fire(new ContextEvent(true, ctxs))}
            >Hello this is some test text to see if this works</p>
        </>
    );
}