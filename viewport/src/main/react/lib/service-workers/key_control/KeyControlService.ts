import { Service } from "../../services/Service";
import { ViewportServices } from "../../services/ViewportServices";
import { KeyPressEvent } from "./events/KeyPressEvent";

type Keystroke = {
    key: string,
    shift: boolean,
    control: boolean,
    alt: boolean
}
type KeyboardMapping = {
    key: Keystroke,
    name: string,
    description: string,
}
export class KeyControlService extends Service {
    private mappings: KeyboardMapping[] = [];

    private sendKey = (event: KeyboardEvent, down: boolean) => {
        if(this.mappings.find(mapping => {
            return mapping.key.key == event.code &&
                   mapping.key.shift == event.shiftKey &&
                   mapping.key.control == event.ctrlKey &&
                   mapping.key.alt == event.altKey;
        }) == undefined) return;

        ViewportServices.get().eventFactory().fire(new KeyPressEvent(event.code, down, event.shiftKey, event.ctrlKey, event.altKey));
    }

    constructor() {
        super();
        window.addEventListener('keydown', event => this.sendKey(event, true));
        window.addEventListener('keyup', event => this.sendKey(event, false));
    }

    public declare = (key: string, name: string, description: string, shift: boolean = false, control: boolean = false, alt: boolean = false) =>
        this.mappings.push({ "key": {"key": key, shift, control, alt}, name, description });

    public kill = () => {
        window.removeEventListener('keydown', event => this.sendKey(event, true));
        window.removeEventListener('keyup', event => this.sendKey(event, false));
    }
}