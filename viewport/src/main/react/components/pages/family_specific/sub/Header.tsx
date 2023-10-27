import { Icon, IconName } from "../../../icons/Icon";

type Header = {
    type: "static" | "scalar";
    name: string;
    whitelist: boolean;
}
export const Header = (props: Header) => {

    const getColor = () => {
        if(props.type == "static") return "#f97316";

        return "#09dbaa";
    }

    return (
        <div className="block w-fit relative top-25px text-center mx-auto h-100px">
            <span className="relative z-10 text-7xl font-bold text-neutral-700">
                {props.name}
                { props.whitelist ? 
                    <Icon iconName={IconName.LOCKED} title="Whitelist enabled" className="absolute top-20px -right-20px rounded w-20px aspect-square bg-pink-500 bg-[length:90%]" inverted={true} />
                  : <Icon iconName={IconName.UNLOCKED} title="Whitelist disabled" className="absolute top-20px -right-20px rounded w-20px aspect-square bg-neutral-500 bg-[length:90%]" inverted={true} />
                }
            </span>
            <span
                className={`relative w-full -top-20px block text-6xl font-bold opacity-50`}
                style={{ color: getColor() }}
                >{props.type}</span>
        </div>
        );
}