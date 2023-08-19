import { RGBColor } from "../../lib/InterfaceColor"

type Gradient4Point = {
    topLeft?: RGBColor;
    topRight?: RGBColor;
    bottomLeft?: RGBColor;
    bottomRight?: RGBColor;
    center?: RGBColor;
}
export const Gradient4Point = (props: Gradient4Point) => {
    return (
        <div className="relative w-full h-full">
            { props.topLeft == undefined ? <></> : <div className="absolute inset-0 w-full h-full" style={{ background: `linear-gradient(250deg,  rgba(${props.topLeft.r},${props.topLeft.g},${props.topLeft.b},1) 0%, rgba(${props.topLeft.r},${props.topLeft.g},${props.topLeft.b},0) 100%)` }} />}
            { props.topRight == undefined ? <></> : <div className="absolute inset-0 w-full h-full" style={{ background: `linear-gradient(45deg, rgba(${props.topRight.r},${props.topRight.g},${props.topRight.b},1) 0%, rgba(${props.topRight.r},${props.topRight.g},${props.topRight.b},0) 100%)` }} />}
            { props.bottomLeft == undefined ? <></> : <div className="absolute inset-0 w-full h-full" style={{ background: `linear-gradient(135deg, rgba(${props.bottomLeft.r},${props.bottomLeft.g},${props.bottomLeft.b},1) 0%, rgba(${props.bottomLeft.r},${props.bottomLeft.g},${props.bottomLeft.b},0) 100%)` }} />}
            { props.bottomRight == undefined ? <></> : <div className="absolute inset-0 w-full h-full" style={{ background: `linear-gradient(225deg, rgba(${props.bottomRight.r},${props.bottomRight.g},${props.bottomRight.b},1) 0%, rgba(${props.bottomRight.r},${props.bottomRight.g},${props.bottomRight.b},0) 100%)` }} />}
            { props.center == undefined ? <></> : <div className="absolute inset-0 w-full h-full" style={{ background: `radial-gradient(circle, rgba(${props.center.r},${props.center.g},${props.center.b},1) 0%, rgba(${props.center.r},${props.center.g},${props.center.b},0) 100%)` }} />}
        </div>
    );
}