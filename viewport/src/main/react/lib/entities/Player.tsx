import { ContextEvent } from "../../components/context_menu/events/ContextEvent";
import { clickable } from "../../components/cursor/interactables/Clickable";
import { Icon, IconName } from "../../components/icons/Icon";
import { ViewportServices } from "../services/ViewportServices";
import { Cacheable } from "./Cacheable";
import { useState } from 'react';

const eventFactory = ViewportServices.get().eventFactory();

export class Player extends Cacheable {
    readonly uuid: string;
    readonly username: string;

    constructor(uuid: string, username: string) {
        super();
        this.uuid = uuid;
        this.username = username;
    }

    /**
     * Returns the full body avatar of the uuid provided.
     * @returns A URL containing the full body avatar of the player.
     */
    public avatar = () => `https://crafatar.com/renders/body/${this.uuid}`;

    /**
     * Returns a head render of the uuid provided.
     * @returns A URL containing the head render of the player.
     */
    public icon = () => `https://crafatar.com/avatars/${this.uuid}`;

    
    public badgeComponent = (key: number = 0) => {
        const [ forceClosed, setForceClosed ] = useState(false);

        return (
            <clickable.div
                key={key}
                className=" p-7px w-50px aspect-square overflow-hidden rounded-md inline-block"
                borderRadius="0.5rem"
                onClick={() => {
                    eventFactory.fire(new ContextEvent(true, [this.profileComponent()]));
                    setForceClosed(true);
                }}
                onMouseLeave={() => setForceClosed(false)}
                onBlur={() => setForceClosed(false)}
                forceClosed={forceClosed}
                >
                <img className="w-full aspect-square pointer-events-none" src={this.icon()} alt={`${this.username}'s minecraft profile picture`}/>
            </clickable.div>
        );
    }

    
    public profileComponent = (key: number = 0) => {
        return (
            <div className="w-full h-300px">
                <div className="relative bg-neutral-600/50 w-[75%] p-25px mt-15px mx-auto aspect-square overflow-hidden rounded-lg">
                    <div
                        className="absolute top-10px right-10px rounded p-5px w-40px aspect-square bg-transparent duration-500 hover:bg-neutral-400/80"
                        >
                        <Icon
                            iconName={IconName.SERVER}
                            inverted={true}
                            className="w-full aspect-square bg-contain object-fit"
                            title={`Go to ${this.username}'s Server`} />
                    </div>
                    <img className="w-full aspect-square pointer-events-none bg-contain object-contain" src={this.avatar()} alt={`${this.username}'s minecraft profile picture`}/>
                </div>
                <span className="block text-neutral-300 text-2xl mt-10px font-bold text-center">{this.username}</span>
                <span className="block text-neutral-300 text-2xs font-bold text-center">{this.uuid}</span>
            </div>
        );
    }
}