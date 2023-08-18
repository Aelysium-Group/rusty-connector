import { AnimatePresence, motion } from "framer-motion";
import { Dispatch, SetStateAction, useEffect, useState } from "react";
import { QueryMode, useScreen, useView } from "react-ui-breakpoints";
import { Coordinate } from "../../lib/modals/Coordinate";
import { ViewportServices } from "../../lib/services/ViewportServices";
import { RectReadOnly } from "react-use-measure";
import { CursorChangeEvent, CursorType } from "../../lib/services/event_factory/events/cursor/CursorChangeEvent";
import { CursorHoverEvent } from "../../lib/services/event_factory/events/cursor/CursorHoverEvent";
import { ContextEvent } from "../../lib/services/event_factory/events/context_menu/ContextEvent";
import { ViewportEvent } from "../../lib/services/event_factory/ViewportEvent";
import { EventListener } from "../../lib/services/event_factory/EventListener";

const eventFactory = ViewportServices.get().eventFactory();

export const Cursor = () => {
    const [ mouseCoordinate, setMouseCoordinate ] = useState(new Coordinate(0, 0));
    const [ type, setType ]: [ CursorType, Dispatch<SetStateAction<CursorType>> ] = useState("default" as CursorType);


    const [ hoveredCoordinate, setHoveredCoordinate ] = useState({} as RectReadOnly);
    const [ hoveredRadius, setHoveredRadius ] = useState("1rem");
    const [ hovered, setHovered ] = useState(false);


    const [ contextContent, setContextContent ] = useState([] as JSX.Element[]);
    const [ context, setContext ] = useState(false);

    
    const mouseMove = (event: MouseEvent) => {
        if(context) return;
        
        setMouseCoordinate(
            new Coordinate(
                event.clientX - 10,
                event.clientY - 10
            )
        );
    }

    const contextMenu = (event: ContextEvent) => {
        console.log(event.open);
        setContext(event.open);
        setContextContent(event.children);
    }

    const cursorHover = (event: CursorHoverEvent) => {
        if(event.entering) {
            setHoveredCoordinate(event.boundingBox);
            setHovered(true);
            setHoveredRadius(event.radius);
        } else {
            setHoveredCoordinate({} as RectReadOnly);
            setHovered(false);
            setHoveredRadius("1rem");
        }
    }

    const cursorType = (event: CursorChangeEvent) => {
        setType(event.type);
    }

    useEffect(() => {
        window.addEventListener("mousemove", mouseMove);
        window.addEventListener("contextmenu", event => event.preventDefault());
        const listeners = [
            eventFactory.on(CursorHoverEvent.empty(), cursorHover),
            eventFactory.on(CursorChangeEvent.empty(), cursorType),
            eventFactory.on(ContextEvent.empty(), contextMenu),
        ] as EventListener<ViewportEvent>[];
        return () => {
            window.removeEventListener("mousemove", mouseMove);
            window.removeEventListener("contextmenu", event => event.preventDefault());
            listeners.forEach(listener => eventFactory.off(listener));
        }
    }, [mouseCoordinate, context]);

    const render = () => {
        const variants = {
            default: {
                x: mouseCoordinate.x,
                y: mouseCoordinate.y,
                borderRadius: "1rem",
                transition: {
                    type: "spring",
                    damping: 50,
                    stiffness: 500
                }
            },
            context: {
                x: mouseCoordinate.x - 150, y: mouseCoordinate.y - 50,
                width: "300px", height: "auto",
                borderRadius: "20px",
                transition: {
                    type: "spring",
                    damping: 10,
                    stiffness: 100
                }
            },
            hovered: {
                x: hoveredCoordinate.x, y: hoveredCoordinate.y,
                width: hoveredCoordinate.width, height: hoveredCoordinate.height,
                borderRadius: hoveredRadius,
                transition: {
                    type: "spring",
                    damping: 10,
                    stiffness: 100
                }
            },
            text: {
                x: mouseCoordinate.x + 10,
                y: mouseCoordinate.y + 10,
                width: "4px",
                borderRadius: "1rem",
                transition: {
                    type: "spring",
                    damping: 30,
                    stiffness: 800
                }
            }
        };

        const getVariant = () => {
            if(context) return "context";
            if(hovered) return "hovered";

            return type;
        }

        return (
            <>
                <AnimatePresence>
                    {context ?
                        <motion.div
                            key={0}
                            className="fixed bg-neutral-600/70 cursor-glass z-40"
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
                    key={1}
                        className={`overflow-hidden fixed top-0 left-0 z-40 bg-neutral-900/30 cursor-glass w-20px h-20px ${ context ? "" : "pointer-events-none"}`}
                        variants={variants}
                        animate={ getVariant() }
                        onMouseLeave={() => eventFactory.fire(new ContextEvent(false, []))}
                    >
                        {context ?
                        <>
                            <div className="p-10px">
                                {contextContent}
                            </div>
                        </> : <></>}
                    </motion.div>
                </AnimatePresence>
            </>
        );
    };

    return useScreen(
        QueryMode.MOBILE_FIRST,
        useView('1000px', render()),
        useView("default", <></>),
    );
}