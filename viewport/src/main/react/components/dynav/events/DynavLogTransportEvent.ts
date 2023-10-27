import { ViewportEvent } from "../../../lib/services/event_factory/ViewportEvent";
import { LogMessages } from "../../../lib/hooks/useLog";

export type Radius = `${number}px` | `${number}rem` | `${number}%`;
export class DynavLogTransportEvent extends ViewportEvent {
    private static _empty = new DynavLogTransportEvent({});
    public static empty = () => this._empty;

    readonly name = "DynavLogTransportEvent";

    readonly messages: LogMessages;

    constructor(messages: LogMessages) {
        super();
        this.messages = messages;
    }
}