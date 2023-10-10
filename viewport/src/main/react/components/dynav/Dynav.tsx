import { AnimatePresence, motion } from "framer-motion";
import { useEffect, useState } from "react";
import { QueryMode, useScreen, useView } from "react-ui-breakpoints";
import { ViewportServices } from "../../lib/services/ViewportServices";
import useMeasure, { RectReadOnly } from "react-use-measure";
import { ContextEvent } from "../context_menu/events/ContextEvent";
import { CursorHoverEvent } from "../cursor/events/CursorHoverEvent";
import { DynavLogTransportEvent } from "./events/DynavLogTransportEvent";
import { ViewportEvent } from "../../lib/services/event_factory/ViewportEvent";
import { EventListener } from "../../lib/services/event_factory/EventListener";
import { LogMessages } from "../../lib/hooks/useLog";
import { Message } from "./log/Message";
import { InterfaceColor } from '../../lib/InterfaceColor';
import { KeyPressEvent } from "../../lib/service-workers/key_control/events/KeyPressEvent";

const eventFactory = ViewportServices.get().eventFactory();
const keyControl = ViewportServices.get().keyControl();
keyControl.declare("Space", "Open Dynav", "Hold to open the Dynav menu.");

export const Dynav = () => {
    const [ ref, bounds ] = useMeasure();
    const [ opened, setOpened ] = useState(false);
    const [ hovered, setHovered ] = useState(false);
    const [ ignoreMouse, setIgnoreMouse ] = useState(false);

    const [ logs, setLog ] = useState({} as LogMessages);

    const getVariant = () => {
        if(opened) return "opened";
        if(Object.entries(logs).length > 0) return "logs";

        return "default";
    }

    const spaceKey = (event: KeyPressEvent) => {
        setHovered(false);
        setIgnoreMouse(event.down);
        setOpened(event.down);
        eventFactory.fire(new CursorHoverEvent(bounds, false, "1rem"));
    }

    const cursorHover = (enter: boolean) => {
        if(ignoreMouse) return

        setHovered(enter);
        const newBounds = {
            x: bounds.x,
            y: bounds.y,
            height: bounds.height,
            width: bounds.width,
            top: bounds.top,
            left: bounds.left,
            right: bounds.right,
            bottom: bounds.bottom
        } as RectReadOnly;
        eventFactory.fire(new CursorHoverEvent(newBounds, enter, "2rem"));
    }

    useEffect(() => {
        const listeners = [
            eventFactory.on(DynavLogTransportEvent.empty(), event => setLog(event.messages)),
            eventFactory.on(KeyPressEvent.empty(), spaceKey),
        ] as EventListener<ViewportEvent>[];
        return () => {
            listeners.forEach(listener => eventFactory.off(listener));
        }
    }, [bounds, logs]);

    const desktop = () => {
        const hasLogs = Object.entries(logs).length > 0;

        const fetchLog = () => {
            if(!hasLogs) return;

            const entry = Object.entries(logs)[0];

            return (
                <Message key={entry[0]} index={entry[0]} color={entry[1].color} children={entry[1].message} />
            );
        }

        const variants = {
            default: {
                x: `calc(50vw - ${140 * 0.5}px)`, bottom: "50px",
                width: "120px", height: "40px",
                margin: "10px",
                transition: {
                    type: "spring",
                    damping: 50,
                    stiffness: 500
                }
            },
            opened: {
                x: `calc(50vw - ${500 * 0.5}px)`, bottom: "30px",
                width: "500px", height: "500px",
                transition: {
                    type: "spring",
                    damping: 10,
                    stiffness: 100
                }
            },
            logs: {
                x: `calc(50vw - ${500 * 0.5}px)`, bottom: "50px",
                width: "500px", height: "30px",
                borderWidth: "3px",
                padding: "0px",
                borderRadius: "1rem",
                borderColor: hasLogs ? Object.entries(logs)[0][1].color ?? InterfaceColor.GRAY : InterfaceColor.GRAY,
                transition: {
                    type: "spring",
                    damping: 50,
                    stiffness: 500
                }
            },
        };

        return (
            <>
                <AnimatePresence>
                    {opened ?
                        <motion.div
                            key={0}
                            className="fixed inset-0 bg-neutral-600/70 frosted-glass-light z-40"
                            initial={{
                                x: 0,
                                y: 0,
                                width: "100vw",
                                height: "100vh",
                                opacity: 0
                            }}
                            exit={{
                                x: 0,
                                y: 0,
                                opacity: 0,
                                width: "100vw",
                                height: "100vh",
                                transitionDuration: "1"
                            }}
                            animate={{
                                x: 0,
                                y: 0,
                                opacity: 1,
                                width: "100vw",
                                height: "100vh",
                                transitionDuration: "1"
                            }}
                            onClick={() => eventFactory.fire(new ContextEvent(false, []))}
                        />
                    : <></>}
                    <motion.div
                        ref={ref}
                        key={1}
                        className={`fixed w-100px p-10px h-60px ${hovered ? "z-50" : "z-40"}`}
                        variants={variants}
                        animate={ getVariant() }
                        initial={"default"}
                        onTap={ e => {
                            if(opened) return;
                            setOpened(true);
                            cursorHover(false);
                        } }
                        onMouseEnter={ () => cursorHover(true) }
                        onMouseLeave={ () => ignoreMouse ? {} : opened ? setOpened(false) : cursorHover(false) }
                    >
                        <div className={`overflow-hidden w-full h-full dynav-glass
                                        ${opened ? "rounded-3xl" : "rounded-2xl"} 
                                        ${hovered ? "z-50 bg-neutral-200/90 duration-500" : "z-40 bg-neutral-900/90"}`}>
                            <div className="relative w-full h-full">
                                <div className={`absolute h-20px duration-500 ${ opened ? hasLogs ? `bottom-30px bg-neutral-700 pt-9px pb-30px` : "bottom-30px" : "bottom-0px" } w-full`}>
                                    <AnimatePresence>
                                        { fetchLog() }
                                    </AnimatePresence>
                                </div>
                            </div>
                        </div>
                    </motion.div>
                </AnimatePresence>
            </>
        );
    };

    const mobile = () => {
        const variants = {
            default: {
                x: `calc(50vw - ${100 * 0.5}px)`, bottom: "20px",
                width: "100px", height: "20px",
                borderRadius: "1rem",
                transition: {
                    type: "spring",
                    damping: 50,
                    stiffness: 500
                }
            },
            opened: {
                x: `0px`, bottom: "0px",
                width: "100vh", height: "100vw",
                borderRadius: "0",
                transition: {
                    type: "spring",
                    damping: 10,
                    stiffness: 100
                }
            }
        };

        return (
            <>
                <motion.div
                    ref={ref}
                    className={`overflow-hidden fixed bottom-100px left-0 z-40 bg-neutral-900/90 dynav-glass w-100px h-20px`}
                    variants={variants}
                    animate={ getVariant() }
                    onClick={ () => opened ? {} : setOpened(true) }
                >
                </motion.div>
            </>
        );
    };

    return useScreen(
        QueryMode.MOBILE_FIRST,
        useView('1000px', desktop()),
        useView("default", mobile()),
    );
}