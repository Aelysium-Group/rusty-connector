import { ViewportEvent } from "../../ViewportEvent";

export type CursorType = "default" | "text" | "deny";
export class CursorChangeEvent extends ViewportEvent {
    private static _empty = new CursorChangeEvent("default");
    public static empty = () => this._empty;

    readonly name = "CursorChangeEvent";

    readonly type: CursorType;

    constructor(type: CursorType) {
        super();
        this.type = type;
    }
}