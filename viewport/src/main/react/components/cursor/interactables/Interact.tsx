import { PropsWithChildren } from "react";
import { ViewportServices } from "../../../lib/services/ViewportServices";
import { CursorChangeEvent } from "../events/CursorChangeEvent";

type TextChildren = JSX.Element | JSX.Element[] | string | number;
const eventFactory = ViewportServices.get().eventFactory();

type InteractP = {
    children?: TextChildren;
}
const InteractP = (props: InteractP | JSX.IntrinsicElements["p"]) => {
    return <p
        onMouseEnter={() => eventFactory.fire(new CursorChangeEvent("text"))}
        onMouseLeave={() => eventFactory.fire(new CursorChangeEvent("default"))}
        >{props.children}</p>
}

type InteractSpan = {
    children?: TextChildren;
}
const InteractSpan = (props: InteractP | PropsWithChildren) => {
    return <span
        onMouseEnter={() => eventFactory.fire(new CursorChangeEvent("text"))}
        onMouseLeave={() => eventFactory.fire(new CursorChangeEvent("default"))}
        >{props.children}</span>
}

export const interact = {
    p: InteractP,
    span: InteractSpan,
}