import { motion } from "framer-motion";
import { InterfaceColor } from "../../lib/InterfaceColor";

interface ColorInput {
    onChange: Function;
    value: InterfaceColor;
    barOnly?: boolean;
}
export const ColorInput = (props: ColorInput) => {
    const render = () => {
        const array = [
            InterfaceColor.RED,
            InterfaceColor.ORANGE,
            InterfaceColor.YELLOW,
            InterfaceColor.GREEN,
            InterfaceColor.BLUE,
            InterfaceColor.PURPLE,
            InterfaceColor.GRAY,
            InterfaceColor.BLACK,
            InterfaceColor.WHITE,
        ]

        return (
            <div className="w-full">
                { (props.barOnly ?? false) ?
                        <></> :
                        <div
                            className={`w-full h-100px rounded-t duration-300`}
                            style={{ background: props.value ?? InterfaceColor.RED}}
                            />
                }
                <div className="grid grid-cols-9 mt-5px">
                    {
                        array.map((color) => (
                            <motion.div
                                key={color}
                                className={`w-full h-50px ${color}`}
                                initial={{ background: color ?? InterfaceColor.GRAY }}
                                whileHover={{ scale: 1.2, boxShadow: "0 10px 50px 10px rgb(0 0 0 / 0.5)", borderRadius: "10px", zIndex: 50 }}
                                whileTap={{ scale: 0.7 }}
                                onTapStart={() => props.onChange(color)}
                                transition={{ type: "spring", stiffness: 400, damping: 10 }}
                                />
                        ))
                    }
                </div>
            </div>
        )
    }
    return render();
}