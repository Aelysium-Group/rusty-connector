import { HEXColor } from "../../lib/InterfaceColor"

type Gradient4Point = {
    colors: (HEXColor | undefined)[];
}
export const Gradient4Point = (props: Gradient4Point) => {
    return (
        <div className="relative w-full h-full">
            { props.colors[0] == undefined ? <></> : <div className="absolute inset-0 w-full h-full" style={{ background: `linear-gradient(250deg, ${props.colors[0]} 0%, ${props.colors[0]}00 70%)` }} />}
            { props.colors[1] == undefined ? <></> : <div className="absolute inset-0 w-full h-full" style={{ background: `linear-gradient(45deg,  ${props.colors[1]} 0%, ${props.colors[1]}00 70%)` }} />}
            { props.colors[2] == undefined ? <></> : <div className="absolute inset-0 w-full h-full" style={{ background: `radial-gradient(circle, ${props.colors[2]} 0%, ${props.colors[2]}00 70%)` }} />}
            { props.colors[3] == undefined ? <></> : <div className="absolute inset-0 w-full h-full" style={{ background: `linear-gradient(135deg, ${props.colors[3]} 0%, ${props.colors[3]}00 70%)` }} />}
            { props.colors[4] == undefined ? <></> : <div className="absolute inset-0 w-full h-full" style={{ background: `linear-gradient(225deg, ${props.colors[4]} 0%, ${props.colors[4]}00 70%)` }} />}
        </div>
    );
}