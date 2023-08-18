import { ViewportEvent } from "../../../../lib/services/event_factory/ViewportEvent";

export class TriggerEvent extends ViewportEvent {
    private static _empty = new TriggerEvent();
    public static empty = () => this._empty;

    readonly name = "TriggerEvent";
}