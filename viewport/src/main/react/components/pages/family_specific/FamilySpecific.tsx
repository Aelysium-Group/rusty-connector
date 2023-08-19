import { OverviewHolder } from "./OverviewHolder";
import { PlayersHolder } from "./PlayersHolder";
import { ServersHolder } from "./ServersHolder";

export const FamilySpecific = () => {
    return (
        <div className="grid grid-rows-1 grid-cols-3 w-screen h-screen justify-items-center items-center">
            <OverviewHolder className="col-start-1" />
            <ServersHolder className="col-start-2 w-500px h-600px" count={4} colors={[]} balancerLevel={40} />
            <PlayersHolder className="col-start-3" />
        </div>
    );
}