import { Icon, IconName } from "../../../icons/Icon";

export type LoadBalancerSettings = {
    type: "RoundRobin" | "LeastConnection" | "MostConnection";
    weighted: boolean;
    persistence: number;
}
export type TPASettings = {
    enabled: boolean;
    friendsOnly: boolean;
    ignorePlayerCap: boolean;
}
type Overview = {
    className: string;
    parentFamily: string;
    loadBalancer: LoadBalancerSettings;
    tpa: TPASettings;
    anchors: string[];
    whitelist: boolean;
};
export const Overview = (props: Overview) => {

    return (
        <div className={`${props.className}`}>
            <span className="block w-full text-center text-3xl text-neutral-600">Parent Family:
                <span className="font-bold text-neutral-800"> {props.parentFamily}</span>
            </span>

            <LoadBalancer settings={props.loadBalancer} />
            <TPA settings={props.tpa} />
            <Anchors anchors={props.anchors} />
        </div>
    );
}

const LoadBalancer = (props: { settings: LoadBalancerSettings }) => {

    const label = () => {
        if(props.settings.type == "RoundRobin") return <span className="block w-min mx-auto my-10px px-10px py-2px font-black text-2xl text-white bg-orange-500 rounded-lg">{props.settings.type}</span>;
        if(props.settings.type == "LeastConnection") return <span className="block w-min mx-auto my-10px px-10px py-2px font-black text-2xl text-white bg-yellow-500 rounded-lg">{props.settings.type}</span>;
        if(props.settings.type == "MostConnection") return <span className="block w-min mx-auto my-10px px-10px py-2px font-black text-2xl text-white bg-cyan-500 rounded-lg">{props.settings.type}</span>;

        
        return <span className="block w-min mx-auto my-10px px-10px py-2px font-black text-2xl text-white bg-neutral-500 rounded-lg">{props.settings.type}</span>;
    }

    return (
        <div className="my-20px p-20px border border-neutral-200 rounded-xl">
            <span className="block w-full text-center font-bold text-5xl text-neutral-400">Load Balancer</span>
            {label()}
            <div className="grid grid-cols-2 grid-rows-1 gap-20px h-50px mt-30px justify-items-center">
                <div className="grid grid-cols-2 grid-rows-1 place-items-center w-[120px]" title="Is the load balancer weighted">
                    <Icon iconName={IconName.WEIGHT} className="w-40px aspect-square bg-neutral-500 rounded bg-[length:75%]" inverted={true}/>
                    <span className="font-bold text-3xl text-neutral-400">{props.settings.weighted ? "Yes" : "No"}</span>
                </div>
                <div className="grid grid-cols-2 grid-rows-1 place-items-center w-[120px]" title="Is the load balancer persistent">
                    <Icon iconName={IconName.BOUNCE} className="w-40px aspect-square bg-neutral-500 rounded bg-[length:75%]" inverted={true}/>
                    <span className="font-bold text-3xl text-neutral-400">{props.settings.persistence == 0 ? "No" : props.settings.persistence}</span>
                </div>
            </div>
        </div>
        );
}

const TPA = (props: { settings: TPASettings }) => {

    const label = () => {
        if(props.settings.enabled) return <span className="block w-min mx-auto my-10px px-10px py-2px font-black text-2xl text-white bg-green-500 rounded-lg">Enabled</span>;
        return <span className="block w-min mx-auto my-10px px-10px py-2px font-black text-2xl text-white bg-red-500 rounded-lg">Disabled</span>;
    }

    return (
        <div className="my-20px p-20px border border-neutral-200 rounded-xl">
            <span className="block w-full text-center font-bold text-5xl text-neutral-400">TPA</span>
            {label()}
            {props.settings.enabled ? <>
            <div className="grid grid-cols-2 grid-rows-1 gap-20px h-50px mt-30px justify-items-center">
                <div className="grid grid-cols-2 grid-rows-1 place-items-center w-[120px]" title="Bound by server player caps">
                    <Icon iconName={IconName.BRICKS} className="w-40px aspect-square bg-neutral-500 rounded bg-[length:75%]" inverted={true}/>
                    <span className="font-bold text-3xl text-neutral-400">{props.settings.ignorePlayerCap ? "No" : "Yes"}</span>
                </div>
                <div className="grid grid-cols-2 grid-rows-1 place-items-center w-[120px]" title="Only friends can tpa">
                    <Icon iconName={IconName.FRIEND} className="w-40px aspect-square bg-neutral-500 rounded bg-[length:75%]" inverted={true}/>
                    <span className="font-bold text-3xl text-neutral-400">{props.settings.friendsOnly ? "Yes" : "No"}</span>
                </div>
            </div>
            </> : <></>}
        </div>
        );
}

const Anchors = (props: { anchors: string[] }) => {

    const label = () => {
        if(props.anchors.length > 0) return <span className="block w-min mx-auto my-10px px-10px py-2px font-black text-2xl text-white bg-green-500 rounded-lg">Enabled</span>;
        return <span className="block w-min mx-auto my-10px px-10px py-2px font-black text-2xl text-white bg-red-500 rounded-lg">Disabled</span>;
    }

    return (
        <div className="my-20px p-20px border border-neutral-200 rounded-xl h-fit">
            <span className="block w-full text-center font-bold text-5xl text-neutral-400">Anchors</span>
            {label()}
            <div className="grid grid-cols-1 gap-20px mt-30px">
            {
                props.anchors.map((anchor, index) => (
                        <div className="grid grid-cols-5 place-items-center" title={`Can be run by players using /${anchor}`}>
                            <Icon iconName={IconName.SLASH} className="w-40px aspect-square bg-neutral-500 rounded bg-[length:75%]" inverted={true}/>
                            <span className="w-full col-span-4 font-bold text-3xl text-neutral-400">{anchor}</span>
                        </div>
                ))
            }
            </div>
        </div>
        );
}