import { QueryMode, useScreen, useView } from "react-ui-breakpoints";
import { Player } from "../../../lib/entities/Player";
import { Players } from "./sub/Players";
import { Servers } from "./sub/Servers";
import { Icon, IconName } from "../../icons/Icon";
import { clickable } from "../../cursor/interactables/Clickable";
import { useState } from 'react';
import { LoadBalancerSettings, Overview } from "./sub/Overview";
import { Header } from "./sub/Header";

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
    {name: "server1", id: "gdssfgd"},
    {name: "server2", id: "gdsrhetsfgd"},
    {name: "server3", id: "gdsrhsfgd"},
    {name: "server4", id: "gdssnteyfgd"},
    {name: "server5", id: "gdserhntysfgd"},
    {name: "server6", id: "gdsmetysfgd"},
    {name: "server7", id: "gdssmteyfgd"},
    {name: "server8", id: "gdssetmyfgd"},
];
const players = [
    new Player("d2281154f3c24ed19c7b5501722c3aaa", "SIVIN_Official"),
    new Player("a2d983f844db4f30916f927dedb6c37c", "Besoj"),
    new Player("c67a0be03b3540d48173ccad6672516a", "Bdan"),
    new Player("1e18d5ff643d45c8b50943b8461d8614", "deadmau5"),
];

const familyHealth = [ 0, 0, 0, 0 ];

export const FamilySpecific = () => {
    const [ view, setView ] = useState("servers");

    const desktop = () => (
        <div className="w-screen h-screen overflow-y-auto pb-200px">
            <Header
                type="scalar"
                name="Minigames"
                whitelist={true} />
            <div className="relative left-50px mx-auto">
                    <Servers className={`absolute left-0 top-100px w-500px aspect-square mx-auto duration-500 ${view == "servers" ? "opacity-100" : "opacity-0 -z-10"}`} familyHealth={familyHealth} balancerLevel={40} servers={servers} />
                    <Players className={`absolute left-0 top-100px w-500px aspect-square mx-auto duration-500 ${view == "players" ? "opacity-100" : "opacity-0 -z-10"}`} players={players} />
                    <Overview
                        className={`absolute left-0 top-100px w-500px aspect-square mx-auto duration-500 ${view == "overview" ? "opacity-100" : "opacity-0 -z-10"}`}
                        loadBalancer={{
                            type: "LeastConnection",
                            weighted: true,
                            persistence: 10,
                        }}
                        parentFamily="lobby"
                        whitelist={false}
                        tpa={{
                            enabled: true,
                            ignorePlayerCap: false,
                            friendsOnly: true
                        }}
                        anchors={["minigames", "minigame"]}
                        />
                <div className="absolute right-0 top-250px gap-y-10px grid grid-rows-3 grid-cols-1 p-5px bg-neutral-200 rounded-lg">
                    <clickable.div
                        className={`row-start-1 w-40px aspect-square duration-500 rounded ${view == "servers" ? "bg-neutral-300 invert pointer-events-none" : ""}`}
                        hoverClassNames="z-50 invert"
                        onClick={() => setView("servers")}>
                        <Icon iconName={IconName.TARGET} className="row-start-1 w-40px aspect-square" />
                    </clickable.div>
                    <clickable.div
                        className={`row-start-2 w-40px aspect-square duration-500 rounded ${view == "players" ? "bg-neutral-300 invert pointer-events-none" : ""}`}
                        hoverClassNames="z-50 invert"
                        onClick={() => setView("players")}>
                        <Icon iconName={IconName.USER} className="row-start-1 w-40px aspect-square" />
                    </clickable.div>
                    <clickable.div
                        className={`row-start-3 w-40px aspect-square duration-500 rounded ${view == "overview" ? "bg-neutral-300 invert pointer-events-none" : ""}`}
                        hoverClassNames="z-50 invert"
                        onClick={() => setView("overview")}>
                        <Icon iconName={IconName.LIST} className="row-start-1 w-40px aspect-square bg-[length:80%]" />
                    </clickable.div>
                </div>
            </div>
        </div>
    );
    const mobile = () => (
        <div className="w-screen h-screen overflow-y-auto overflow-x-hidden pb-200px">
            <div className="scale-50">
                <Header
                    type="scalar"
                    name="Minigames"
                    whitelist={true} />
            </div>
            <div className="mt-100px mx-auto w-fit gap-x-50px grid grid-cols-3 grid-rows-1 p-5px bg-neutral-200 rounded-lg">
                <clickable.div
                    className={`col-start-1 w-50px aspect-square duration-500 rounded ${view == "servers" ? "bg-neutral-300 invert pointer-events-none" : ""}`}
                    hoverClassNames="z-50 invert"
                    onClick={() => setView("servers")}>
                    <Icon iconName={IconName.TARGET} className="row-start-1 w-50px aspect-square" />
                </clickable.div>
                <clickable.div
                    className={`col-start-2 w-50px aspect-square duration-500 rounded ${view == "players" ? "bg-neutral-300 invert pointer-events-none" : ""}`}
                    hoverClassNames="z-50 invert"
                    onClick={() => setView("players")}>
                    <Icon iconName={IconName.USER} className="row-start-1 w-50px aspect-square" />
                </clickable.div>
                <clickable.div
                    className={`col-start-3 w-50px aspect-square duration-500 rounded ${view == "overview" ? "bg-neutral-300 invert pointer-events-none" : ""}`}
                    hoverClassNames="z-50 invert"
                    onClick={() => setView("overview")}>
                    <Icon iconName={IconName.LIST} className="row-start-1 w-50px aspect-square bg-[length:80%]" />
                </clickable.div>
            </div>
            <div className="relative mt-50px w-screen px-100px">
                    <Servers className={`absolute left-0 w-full aspect-square mx-auto duration-500 ${view == "servers" ? "opacity-100" : "opacity-0 -z-10"}`} familyHealth={familyHealth} balancerLevel={40} servers={servers} />
                    <Players className={`absolute left-0 w-full aspect-square mx-auto duration-500 ${view == "players" ? "opacity-100" : "opacity-0 -z-10"}`} players={players} />
                    <Overview
                        className={`absolute left-0 w-full aspect-square mx-auto duration-500 ${view == "overview" ? "opacity-100" : "opacity-0 -z-10"}`}
                        loadBalancer={{
                            type: "LeastConnection",
                            weighted: true,
                            persistence: 10,
                        }}
                        parentFamily="lobby"
                        whitelist={false}
                        tpa={{
                            enabled: true,
                            ignorePlayerCap: false,
                            friendsOnly: true
                        }}
                        anchors={["minigames", "minigame"]}
                        />
            </div>
        </div>
    );

    return useScreen(
        QueryMode.MOBILE_FIRST,
        useView("1000px", desktop()),
        useView("default", mobile()),
    );
}