import useMeasure, { RectReadOnly } from "react-use-measure";
import { ViewportServices } from "../../../lib/services/ViewportServices";
import { useEffect, useState } from "react";
import { CursorHoverEvent, Radius } from "../events/CursorHoverEvent";
import { QueryMode, useScreen, useView } from "react-ui-breakpoints";
const eventFactory = ViewportServices.get().eventFactory();

const mouseHover = (bounds: RectReadOnly, hovered: boolean, borderRadius: Radius, callback: (hovered: boolean) => void) => {
    callback(hovered);
    eventFactory.fire(new CursorHoverEvent(bounds, hovered, borderRadius));
}

type div = JSX.IntrinsicElements["div"] & {
    borderRadius?: Radius;
    blurredClassNames?: string;
    hoverClassNames?: string;
    forceClosed?: boolean;
}
  
const div = ({borderRadius, hoverClassNames, blurredClassNames, forceClosed, children, ...rest }: div) => {
    const [ ref, bounds ] = useMeasure();
    const [ hovered, setHovered ] = useState(false);

    useEffect(()=>{},[ bounds ]);

    const desktop = () => {
        return (
            <div
                {...rest}
                className={`${rest.className} ${forceClosed ? "" : hovered ? hoverClassNames ?? "z-50 relative" : blurredClassNames ?? ""}`}
                ref={ref}
                onMouseEnter={() => mouseHover(bounds, true, borderRadius ?? "6px", (hovered) => setHovered(hovered))}
                onMouseOut={() => mouseHover(bounds, false, borderRadius ?? "6px", (hovered) => setHovered(hovered))}
            >{children ?? <></>}</div>
        );
    }

    const mobile = () => {
        return (
            <div
                className={`${rest.className} ${hovered ? "z-50" : ""}`}
            >{children ?? <></>}</div>
        );
    }

    return useScreen(
        QueryMode.MOBILE_FIRST,
        useView('1000px', desktop()),
        useView("default", mobile()),
    );
}

type p = JSX.IntrinsicElements["p"] & {
    borderRadius?: Radius;
    blurredClassNames?: string;
    hoverClassNames?: string;
    forceClosed?: boolean;
}
  
const p = ({borderRadius, hoverClassNames, blurredClassNames, forceClosed, children, ...rest }: div) => {
    const [ ref, bounds ] = useMeasure();
    const [ hovered, setHovered ] = useState(false);

    useEffect(()=>{},[ bounds ]);

    const desktop = () => {
        return (
            <p
                {...rest}
                className={`${rest.className} ${forceClosed ? "" : hovered ? hoverClassNames ?? "z-50 relative" : blurredClassNames ?? ""}`}
                ref={ref}
                onMouseEnter={() => mouseHover(bounds, true, borderRadius ?? "6px", (hovered) => setHovered(hovered))}
                onMouseOut={() => mouseHover(bounds, false, borderRadius ?? "6px", (hovered) => setHovered(hovered))}
            >{children ?? <></>}</p>
        );
    }

    const mobile = () => {
        return (
            <div
                className={`${rest.className} ${hovered ? "z-50" : ""}`}
            >{children ?? <></>}</div>
        );
    }

    return useScreen(
        QueryMode.MOBILE_FIRST,
        useView('1000px', desktop()),
        useView("default", mobile()),
    );
}
  

export const clickable = {
    div,
    p
}