import { QueryMode, useScreen, useView } from "react-ui-breakpoints";
import { Player } from "../../../lib/entities/Player";
import { Header } from "./sub/Header";

const players = [
    new Player("d2281154f3c24ed19c7b5501722c3aaa", "SIVIN_Official"),
    new Player("a2d983f844db4f30916f927dedb6c37c", "notch"),
    new Player("c67a0be03b3540d48173ccad6672516a", "jeb_"),
    new Player("1e18d5ff643d45c8b50943b8461d8614", "deadmau5"),
];

export const Server = () => {

    const desktop = () => (
        <div className="w-screen h-screen overflow-y-auto pb-200px">
            <Header type={"scalar"} name={"survival-node-1"} familyName={"Survival"} />
            <div className="mt-100px bg-neutral-400 rounded-3xl h-[calc(100vh_-_400px)] w-[calc(100vw_-_400px)] mx-auto">
                
            </div>
        </div>
    );
    const mobile = () => (<></>);

    return useScreen(
        QueryMode.MOBILE_FIRST,
        useView("1000px", desktop()),
        useView("default", mobile()),
    );
}