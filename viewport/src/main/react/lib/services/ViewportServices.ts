import { EventFactory } from "./event_factory/EventFactory";

export class ViewportServices {
    private static instance = new ViewportServices();
    public static get = () => this.instance;

    private services = {
        eventFactory: new EventFactory(),
    };

    public eventFactory = (): EventFactory => this.services.eventFactory;
}