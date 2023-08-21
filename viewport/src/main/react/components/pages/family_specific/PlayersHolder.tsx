import { Player } from "../../../lib/entities/Player";


type PlayersHolder = {
    className: string;
    players: Player[];
};
export const PlayersHolder = (props: PlayersHolder) => {

    return (
        <div className={`${props.className}`}>
            <span className="block mb-20px w-full text-center font-bold text-6xl text-neutral-500">Players</span>
            <div className={`${props.className} w-500px rounded-2xl h-600px`}>
                {
                    props.players.length == 0 ?
                    <p className="text-center text-neutral-400">No players are currently connected to this family.</p>
                    : props.players.map((player, index) => player.badgeComponent(index))
                }
            </div>
        </div>
    );
}
