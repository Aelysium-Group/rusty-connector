type Header = {
    type: "static" | "scalar";
    name: string;
    familyName: string;
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
            </span>
            <span
                className={`relative w-full -top-20px block text-6xl font-bold opacity-50`}
                style={{ color: getColor() }}
                title="This server's family"
                >{props.familyName}</span>
        </div>
        );
}