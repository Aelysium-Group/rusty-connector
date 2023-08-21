import { Player } from "../../../lib/entities/Player";
import { OverviewHolder } from "./OverviewHolder";
import { PlayersHolder } from "./PlayersHolder";
import { ServersHolder } from "./ServersHolder";

const servers = [
    {name: "server1", id: "gdssfgd"},
    {name: "server2", id: "gdsrhetsfgd"},
    {name: "server3", id: "gdsrhsfgd"},
    {name: "server4", id: "gdssnteyfgd"},
    {name: "server5", id: "gdserhntysfgd"},
    {name: "server6", id: "gdsmetysfgd"},
    {name: "server7", id: "gdssmteyfgd"},
    {name: "server8", id: "gdssetmyfgd"},
    {name: "server9", id: "gdssymtefgd"},
    {name: "server10", id: "gdswrjtsfgd"},
    {name: "server11", id: "gdss,ufgd"},
    {name: "server12", id: "gdsrwjsfgd"},
    {name: "server13", id: "gdssfrwjgd"},
    {name: "server14", id: "gdsswrjyfgd"},
    {name: "server11", id: "gdss,ufgd"},
];
const players = [
    new Player("d2281154f3c24ed19c7b5501722c3aaa", "SIVIN_Official"),
    new Player("a2d983f844db4f30916f927dedb6c37c", "Besoj"),
    new Player("c67a0be03b3540d48173ccad6672516a", "Bdan"),
    new Player("1e18d5ff643d45c8b50943b8461d8614", "deadmau5"),
];

export const FamilySpecific = () => {
    return (
        <>
            <div className="absolute block top-25px w-full text-center font-bold text-7xl text-neutral-700">
                <span className="block z-10">Super Large Name</span>
                <div className="-z-10 relative -top-10px overflow-hidden h-200px opacity-50">
                    <div
                        className="absolute -top-[380px] inset-0 w-500px aspect-square bg-contain"
                        style={{
                            background: `radial-gradient(circle, #09DBAA 0%, #14EBE000 70%)`,
                            left: "calc(50vw - 250px)"
                            }} />
                </div>
            </div>
            <div className="absolute inset-0 grid grid-rows-1 grid-cols-3 w-screen h-screen justify-items-center items-center">
                <OverviewHolder className="col-start-1" />
                <ServersHolder className="col-start-2 w-500px h-600px" colors={[]} balancerLevel={40} servers={servers} />
                <PlayersHolder className="col-start-3" players={players} />
            </div>
        </>
    );
}