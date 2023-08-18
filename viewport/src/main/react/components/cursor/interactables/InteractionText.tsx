import useMeasure from "react-use-measure";
import { ViewportServices } from "../../../lib/services/ViewportServices";
import { useState } from "react";
import { QueryMode, useScreen, useView } from "react-ui-breakpoints";
import { CursorHoverEvent } from "../../../lib/services/event_factory/events/cursor/CursorHoverEvent";

type InteractionText = {
    children?: JSX.Element | JSX.Element[] | string;
};
export const InteractionText = (props: InteractionText) => {
    const [ref, bounds] = useMeasure();
    const eventFactory = ViewportServices.get().eventFactory();
    const [ hovered, setHovered ] = useState(false);

    const desktop = () => {
        const onMouseOver = () => {
            setHovered(true);
            eventFactory.fire(new CursorHoverEvent(bounds, true, "6px"));
        }
    
        const onMouseOut = () => {
            setHovered(false);
            eventFactory.fire(new CursorHoverEvent(bounds, false, "8px"));
        }
    
        return (
            <span
                className={`inline-block relative select-none px-20px py-10px ${hovered ? "z-50" : ""}`}
                ref={ref}
                onMouseEnter={onMouseOver}
                onMouseOut={onMouseOut}
            >{props.children ?? <></>}</span>
        );
    }

    const mobile = () => {
        return (
            <span
                className={`inline-block relative select-none px-20px py-10px ${hovered ? "z-50" : ""}`}
            >{props.children ?? <></>}</span>
        );
    }

    return useScreen(
        QueryMode.MOBILE_FIRST,
        useView('1000px', desktop()),
        useView("default", mobile()),
    );
}