import { Service } from "../Service";
import { EventListener } from "./EventListener";
import { ViewportEvent } from './ViewportEvent';

export class EventFactory extends Service {
    private listeners: EventListener<ViewportEvent>[] = [];

    constructor() {
        super();
    }

    /**
     * Used to define a new event listener.
     * @param event The event to listen for. You can reuse an empty instance of the event using {@link ViewportEvent.empty()}
     * @param callback The event handler.
     * @returns An {@link EventListener}.
     */
    public on = <E extends ViewportEvent>(event: E, callback: (event: E) => void): EventListener<E> => {
        const listener = new EventListener(event.name, callback);

        this.listeners.push(listener as EventListener<ViewportEvent>);
        return listener;
    };

    /**
     * Unregister the listener from the {@link EventFactory}. This should be the same listener instance that {@link EventFactory.on()} returns.
     * @param listener The {@link EventListener} to remove.
     */
    public off = <E extends ViewportEvent>(listener: EventListener<E>): void => {
        this.listeners.splice(this.listeners.indexOf(listener as EventListener<ViewportEvent>), 1);
    };

    /**
     * Fires a {@link ViewportEvent} to all listening {@link EventListener}.
     * @param event A {@link ViewportEvent}.
     */
    public fire = (event: ViewportEvent) => {
        this.listeners
            .filter(listener => listener.event == event.name)
            .forEach(item => item.fire(event));
    }

    public kill = () => {
        this.listeners = [];
    }
}