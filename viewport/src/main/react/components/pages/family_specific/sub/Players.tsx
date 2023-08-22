import { Player } from "../../../../lib/entities/Player";


type Players = {
    className: string;
    players: Player[];
};
export const Players = (props: Players) => {

    return (
        <div className={`${props.className}`}>
            <span className="block w-full text-center font-bold text-6xl text-neutral-500">Players</span>
            <span className="block mt-10px text-center font-bold text-2xl text-neutral-400">{props.players.length}/100</span>
            <div className="mt-10px">
                {
                    props.players.length == 0 ?
                    <p className="text-center text-neutral-400">No players are currently connected to this family.</p>
                    : props.players.map((player, index) => player.badgeComponent(index))
                }
            </div>
        </div>
    );
}
