
import { motion } from "framer-motion";
import { InterfaceColor } from "../../resources/InterfaceColor";
import useMeasure from "react-use-measure";
import { ControlType } from "../generic/controls/Control";

interface ControlSelectInput {
    onChange: Function;
    value: ControlType;
    color: InterfaceColor;
}
export const ControlSelectInput = (props: ControlSelectInput) => {
    const [ ref, bounds ] = useMeasure();

    const render = () => {
        const isHoldButton      = props.value == ControlType.BUTTON_HOLD;
        const isTapButton       = props.value == ControlType.BUTTON_TAP;
        const isToggleButton    = props.value == ControlType.BUTTON_TOGGLE;
        const isLabel           = props.value == ControlType.LABEL;

        const calculatedMiddle = (bounds.width * 0.5) - (124 * 0.5);

        let currentControlName = "Choose a control";
        if(isHoldButton)        currentControlName = "Button (Hold)";
        if(isTapButton)         currentControlName = "Button (Click)";
        if(isToggleButton)      currentControlName = "Button (Toggle)";
        if(isLabel)             currentControlName = "Text Label";

        return (
            <div className="w-full relative">
                <div ref={ref} className="relative h-200px shadow-inset-xl rounded-lg bg-template-gray" >
                    <span className="absolute top-10px right-20px text-lg font-bold text-neutral-500">{currentControlName}</span>
                </div>
                <div className="relative h-75px"/>
                <motion.div
                    className={`absolute overflow-hidden ${!isHoldButton ? "cursor-pointer" : ""}`}
                    animate={{
                        left: isHoldButton ? calculatedMiddle : 8,
                        bottom: isHoldButton ? 115 : -25,
                        scale: isHoldButton ? 1 : 0.5
                    }}
                    transition={{ type: "spring", stiffness: 400, damping: 12 }}
                    onTapStart={() => props.onChange(ControlType.BUTTON_HOLD)}
                    whileHover={{ scale: isHoldButton ? 1 : 0.6 }}
                    whileTap={{ scale: 1 }}
                    >
                    <div className={`relative rounded-3xl w-100px m-12px aspect-square overflow-hidden`} >
                        <div
                            className={`absolute inset-0 w-full aspect-square shadow-inset-xl duration-200`}
                            style={{ background: props.color ?? InterfaceColor.RED }}
                            />
                    </div>
                </motion.div>
                <motion.div
                    className={`absolute overflow-hidden ${!isTapButton ? "cursor-pointer" : ""}`}
                    animate={{
                        left: isTapButton ? calculatedMiddle : 100,
                        bottom: isTapButton ? 115 : -25,
                        scale: isTapButton ? 1 : 0.5
                    }}
                    transition={{ type: "spring", stiffness: 400, damping: 12 }}
                    onTapStart={() => props.onChange(ControlType.BUTTON_TAP)}
                    whileHover={{ scale: isTapButton ? 1 : 0.6 }}
                    whileTap={{ scale: 1 }}
                    >
                    <div className={`relative rounded-full w-100px m-12px aspect-square overflow-hidden`} >
                        <div
                            className={`absolute inset-0 w-full aspect-square shadow-inset-xl duration-200`}
                            style={{ background: props.color ?? InterfaceColor.RED }}
                            />
                    </div>
                </motion.div>
                <motion.div
                    className={`absolute overflow-hidden ${!isToggleButton ? "cursor-pointer" : ""}`}
                    animate={{
                        left: isToggleButton ? calculatedMiddle : 200,
                        bottom: isToggleButton ? 115 : -25,
                        scale: isToggleButton ? 1 : 0.5
                    }}
                    transition={{ type: "spring", stiffness: 400, damping: 12 }}
                    onTapStart={() => props.onChange(ControlType.BUTTON_TOGGLE)}
                    whileHover={{ scale: isToggleButton ? 1 : 0.6 }}
                    whileTap={{ scale: 1 }}
                    >
                    <div className={`relative rounded w-100px m-12px aspect-square overflow-hidden`} >
                        <div
                            className={`absolute inset-0 w-full aspect-square shadow-inset-xl duration-200`}
                            style={{ background: props.color ?? InterfaceColor.RED }}
                            />
                    </div>
                </motion.div>
                <motion.div
                    className={`absolute overflow-hidden ${!isLabel ? "cursor-pointer" : ""}`}
                    animate={{
                        left: isLabel ? calculatedMiddle : 300,
                        bottom: isLabel ? 140 : 0,
                        scale: isLabel ? 1 : 0.5
                    }}
                    transition={{ type: "spring", stiffness: 400, damping: 12 }}
                    onTapStart={() => props.onChange(ControlType.LABEL)}
                    whileHover={{ scale: isLabel ? 1 : 0.6 }}
                    whileTap={{ scale: 1 }}
                    >
                    <div className={`relative rounded-lg w-100px h-50px m-12px overflow-hidden`} >
                        <span
                            className={`absolute inset-0 w-full pt-6px pl-12px shadow-inset duration-200 text-3xl font-bold`}
                            style={{ background: props.color ?? InterfaceColor.RED, color: props.color == InterfaceColor.WHITE ? InterfaceColor.BLACK : InterfaceColor.WHITE }}
                            >
                                Label
                            </span>
                    </div>
                </motion.div>
            </div>
        )
    }
    return render();
}