import { ViewportEvent } from "../../../services/event_factory/ViewportEvent";

export class KeyPressEvent extends ViewportEvent {
    private static _empty = new KeyPressEvent("", true);
    public static empty = () => this._empty;

    readonly name = "KeyPressEvent";

    readonly key: string;
    readonly down: boolean;
    readonly shift: boolean;
    readonly ctrl: boolean;
    readonly alt: boolean;

    constructor(key: string, down: boolean, shift: boolean = false, ctrl: boolean = false, alt: boolean = false) {
        super();
        this.key = key;
        this.down = down;
        this.shift = shift;
        this.ctrl = ctrl;
        this.alt = alt;
    }
}