import { QueryMode, useScreen, useView } from "react-ui-breakpoints";
import { Player } from "../../../lib/entities/Player";
import { Outlet, useParams } from "react-router-dom";
import { FamiliesBar } from "./sub/FamiliesBar";

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
    new Player("a2d983f844db4f30916f927dedb6c37c", "notch"),
    new Player("c67a0be03b3540d48173ccad6672516a", "jeb_"),
    new Player("1e18d5ff643d45c8b50943b8461d8614", "deadmau5"),
];

const familyHealth = [ 0, 0, 0, 0 ];

export const Overview = (props: {}) => {
    const urlParams = useParams();

    const desktop = () => (
        <div className="w-screen h-screen overflow-y-auto pb-200px bg-neutral-800">
            <FamiliesBar />
            <Outlet />
            <Outlet />
        </div>
    );
    const mobile = () => (
        <></>
    );

    return useScreen(
        QueryMode.MOBILE_FIRST,
        useView("1000px", desktop()),
        useView("default", mobile()),
    );
}