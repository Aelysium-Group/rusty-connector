import { useCallback, useState } from "react";
import Particles from "react-particles";
import { loadSlim } from "tsparticles-slim";
import { Engine } from "tsparticles-engine";
import useMeasure from "react-use-measure";
import { Gradient4Point } from "../../gradient/Gradient4Point";
import { RGBColor } from "../../../lib/InterfaceColor";
import { motion } from "framer-motion";

type ParticleHolder = {
    count: number;
    colors: RGBColor[];
    className: string;
    balancerLevel: number;
};
export const ServersHolder = (props: ParticleHolder) => {
    const [ serverCountRef, serverCountBounds ] = useMeasure();

    const [ flipped, setFlipped ] = useState(false);

    const particlesInit = useCallback(async (engine: Engine) => {
        await loadSlim(engine);
    }, []);

    const particlesLoaded = useCallback(async (container: any) => {
        container.addClickHandler((event: any, particles: any) => {
            console.log(particles);
        });
        await console.log(container);
    }, []);

    const getVariant = () => {
        if(flipped) return "flipped";

        return "default";
    }

    return (
        <div className={props.className}>
            <div className="relative w-full h-full">
                <span className="absolute block top-0 w-full text-center font-bold text-6xl text-neutral-500">Servers</span>
                <div className="absolute bottom-0 w-500px aspect-square">
                    <div className="relative w-full h-full">
                        <motion.div
                            className="relative inset-0 w-full h-full bg-neutral-800 duration-700 rot rounded-full overflow-hidden"
                            initial={{
                                rotateY: "-180deg",
                                zIndex: -1,
                                opacity: 0,
                            }}
                            variants={{
                                default: {
                                    rotateY: "-180deg",
                                    zIndex: -1,
                                    opacity: 0,
                                },
                                flipped: {
                                    rotateY: "0deg",
                                    zIndex: 1,
                                    opacity: 1,
                                }
                            }}
                            transition={{ type: "none" }}
                            animate={getVariant()}
                            >
                        </motion.div>
                        <motion.div
                            className="absolute inset-0 w-full h-full duration-700 rounded-full overflow-hidden"
                            variants={{
                                default: {
                                    rotateY: "0deg",
                                    zIndex: 2,
                                },
                                flipped: {
                                    rotateY: "180deg",
                                    zIndex: -1,
                                }
                            }}
                            transition={{ type: "none" }}
                            animate={getVariant()}
                            onTap={() => setFlipped(true)}
                            >
                            <div className="relative w-full h-full">
                                <Particles
                                    className="w-full aspect-square"
                            id="FamilySpecific-particles"
                            init={particlesInit}
                            loaded={particlesLoaded}
                            options={{
                                fullScreen: false,
                                fps_limit: 60,
                                pauseOnBlur: true,
                                interactivity: {
                                    events: {
                                        onHover: {
                                            enable: true,
                                            mode: "bubble"
                                        }
                                    },
                                    modes: {
                                        bubble: {
                                            distance: 100,
                                            duration: 0.5,
                                            size: 20,
                                        }
                                    }
                                },
                                particles: {
                                    color: {
                                        value: ["#000000"]
                                    },
                                    move: {
                                        enable: true,
                                        direction: "outside",
                                        drift: 10,
                                        spin: {
                                            enable: true,
                                        },
                                        trail: {
                                            enable: true,
                                            fill: {
                                                color: "#ffffff"
                                            },
                                            length: 20,
                                        },
                                        speed: 4,
                                        warp: true,
                                        outModes: {
                                            default: "bounce",
                                        },
                                        collisions: true
                                    },
                                    number: {
                                        value: props.count,
                                    },
                                    opacity: {
                                        value: 0.5,
                                    },
                                    shape: {
                                        type: "circle",
                                    },
                                    size: {
                                        value: 2,
                                    },
                                    collisions: {
                                        bounce: {
                                          horizontal: {
                                            random: {
                                              enable: true,
                                              minimumValue: 1
                                            },
                                            value: 1
                                          },
                                          vertical: {
                                            random: {
                                              enable: true,
                                              minimumValue: 1
                                            },
                                            value: 2
                                          }
                                        },
                                        enable: true,
                                        maxSpeed: 50,
                                        mode: "bounce",
                                        overlap: {
                                          enable: false,
                                          retries: 0
                                        }
                                    },
                                    reduceDuplicates: true
                                },
                                    }}
                                    />
                                <div className="absolute inset-0 mix-blend-lighten">
                                    <Gradient4Point />
                                </div>
                                <span
                                    ref={serverCountRef}
                                    className={`absolute block w-full text-center font-bold text-8xl text-neutral-500 opacity-95`}
                                    style={{ top: `200px` }}
                                    >{props.count}</span>
                            </div>
                        </motion.div>
                    </div>
                </div>
            </div>
        </div>
    );
}