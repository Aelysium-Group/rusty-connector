import { ViewportEvent } from './ViewportEvent';
export class EventListener<E extends ViewportEvent> {
    readonly event: string;
    private callback: (event: E) => void;

    constructor(event: string, callback: (event: E) => void) {
        this.event = event;
        this.callback = callback;
    }

    public fire = (event: E) => this.callback(event);
}