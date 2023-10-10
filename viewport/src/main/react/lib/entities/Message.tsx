import { Cacheable } from "./Cacheable";
import { useState } from 'react';
import { Player } from "./Player";
import { clickable } from "../../components/cursor/interactables/Clickable";
import { ContextEvent } from "../../components/context_menu/events/ContextEvent";
import { ViewportServices } from "../services/ViewportServices";

const eventFactory = ViewportServices.get().eventFactory();

export class Message extends Cacheable {
    readonly player: Player;
    readonly contents: string;
    readonly timestamp: string;

    constructor(player: Player, contents: string, timestamp: string) {
        super();
        this.player = player;
        this.contents = contents;
        this.timestamp = timestamp;
    }
    
    public messageComponent = (key: number = 0) => {
        const [ forceClosed, setForceClosed ] = useState(false);

        return (
            <div key={key} className="w-full p-10px pl-60px">
                <div className="relative">
                    <div className="absolute top-0 -left-[55px] cursor-pointer">
                        {this.player.badgeComponent()}
                    </div>
                    
                    <clickable.span
                        key={key}
                        className="text-white font-bold hover:underline cursor-pointer"
                        borderRadius="0.5rem"
                        onClick={() => {
                            eventFactory.fire(new ContextEvent(true, [this.player.profileComponent()]));
                            setForceClosed(true);
                        }}
                        onMouseLeave={() => setForceClosed(false)}
                        onBlur={() => setForceClosed(false)}
                        forceClosed={forceClosed}
                        >
                        {this.player.username}
                    </clickable.span>
                    <span className="relative left-10px -top-2px text-xs text-neutral-500">
                        {this.timestamp}
                    </span>
                </div>
                <p className="text-neutral-100">
                    {this.contents}
                </p>
            </div>
        );
    }
}