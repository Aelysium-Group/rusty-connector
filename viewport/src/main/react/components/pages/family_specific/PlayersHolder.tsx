

type PlayersHolder = {
    className: string;
};
export const PlayersHolder = (props: PlayersHolder) => {

    return (
        <div className={`${props.className}`}>
            <span className="block mb-20px w-full text-center font-bold text-6xl text-neutral-500">Players</span>
            <div className={`${props.className} bg-neutral-200 w-500px rounded-2xl h-600px`}>

            </div>
        </div>
    );
}