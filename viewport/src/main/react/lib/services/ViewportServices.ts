import { EventFactory as EventFactoryService } from "./event_factory/EventFactory";
import { KeyControlService } from '../service-workers/key_control/KeyControlService';

export class ViewportServices {
    private static instance = new ViewportServices();
    public static get = () => this.instance;

    private services = {
        eventFactory: new EventFactoryService(),
        keyControl: new KeyControlService(),
    };

    public eventFactory = (): EventFactoryService => this.services.eventFactory;
    public keyControl = (): KeyControlService => this.services.keyControl;
}