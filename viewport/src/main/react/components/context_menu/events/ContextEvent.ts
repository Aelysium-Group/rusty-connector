import { ViewportEvent } from "../../../lib/services/event_factory/ViewportEvent";

export class ContextEvent extends ViewportEvent {
    private static _empty = new ContextEvent(false, []);
    public static empty = () => this._empty;

    readonly name = "ContextEvent";

    readonly open: boolean;
    readonly children: JSX.Element[];

    constructor(open: boolean, children: JSX.Element[]) {
        super();
        this.open = open;
        this.children = children;
    }
}