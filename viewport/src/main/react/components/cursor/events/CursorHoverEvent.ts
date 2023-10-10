import { RectReadOnly } from "react-use-measure";
import { ViewportEvent } from "../../../lib/services/event_factory/ViewportEvent";

export type Radius = `${number}px` | `${number}rem` | `${number}%`;
export class CursorHoverEvent extends ViewportEvent {
    private static _empty = new CursorHoverEvent({} as RectReadOnly, false, "1rem");
    public static empty = () => this._empty;

    readonly name = "CursorHoverEvent";

    readonly entering: boolean;
    readonly boundingBox: RectReadOnly;
    readonly radius: Radius;

    constructor(boundingBox: RectReadOnly, entering: boolean, radius: Radius) {
        super();
        this.boundingBox = boundingBox;
        this.entering = entering;
        this.radius = radius;
    }
}