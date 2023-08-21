import { useCallback, useState } from "react";
import Particles from "react-particles";
import { loadSlim } from "tsparticles-slim";
import { Engine } from "tsparticles-engine";
import { motion } from "framer-motion";
import Chart from "react-google-charts";
import { clickable } from "../../cursor/interactables/Clickable";
import { Icon, IconName } from "../../icons/Icon";
import useMeasure from "react-use-measure";
import { HealthIndicator } from "./HealthIndicator";

type ParticleHolder = {
    familyHealth: number[];
    className: string;
    balancerLevel: number;
    servers: ServerPartial[];
};
type ServerPartial = {
    id: string,
    name: string
};
export const ServersHolder = (props: ParticleHolder) => {
    const [ serverCountRef, serverCountBounds ] = useMeasure();

    const [ flipped, setFlipped ] = useState(false);
    const [ viewColors, setViewColors ] = useState(false);

    const particlesInit = useCallback(async (engine: Engine) => {
        await loadSlim(engine);
    }, []);

    const particlesLoaded = useCallback(async (container: any) => {
        container.addClickHandler((event: any, particles: any) => {
        });
    }, []);

    const getVariant = () => {
        if(flipped) return "flipped";

        return "default";
    }

    const generateServerDataSet = () => {
        const data: any[] = [["name", "index"]];

        props.servers.forEach(server => {
            data.push( [server.name, 100 * props.servers.length] );
        });

        return data;
    }

    return (
        <div className={props.className}>
            <div className="relative w-full h-full">
                <span className="absolute block top-0 w-full text-center font-bold text-6xl text-neutral-500">Servers</span>
                <div className="absolute bottom-0 w-500px aspect-square">
                    <div className="relative w-full h-full">
                        <motion.div
                            className={`relative inset-0 w-full h-full bg-neutral-800 rounded-full overflow-hidden ${flipped ? "duration-700" : "duration-[0.9s]"}`}
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
                            <Chart
                                className="absolute"
                                style={{ left: `calc(250px - 350px)`, top:  `calc(250px - 350px)`}}
                                chartType="PieChart"
                                data={generateServerDataSet()}
                                options={{
                                    backgroundColor: "#262626",
                                    dataLabels: { enabled: false},
                                 // pieHole: 0.7, // The pieHole is intentionally being made using a div and not this setting.
                                    legend: "none",
                                    pieSliceText: "label",
                                    pieSliceTextStyle: { color: "#262626" },
                                    tooltip: { trigger: "none" },
                                    colors: [ "#E5E5E5" ],
                                    
                                }}
                                width={"700px"}
                                height={"700px"}
                                chartEvents={[
                                    {
                                        eventName: "select",
                                        callback: ({ chartWrapper, google }) => {
                                            const chart = chartWrapper.getChart();
                                            try {
                                                const selected = props.servers[chart.getSelection()[0].row];

                                                console.log(selected);
                                            } catch(e) {}
                                        }
                                    }
                                ]}
                                />
                            <div className="absolute w-250px aspect-square rounded-full bg-neutral-800 pointer-events-none z-10" style={{ left: `calc(250px - 125px)`, top: `calc(250px - 125px)`}} />
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
                            onTap={viewColors ? () => {} : () => setFlipped(true)}
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
                                        background: {
                                            color: "#828282"
                                        },
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
                                                value: props.servers.length > 200 ? 200 : props.servers.length,
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
                                <div className="absolute inset-0 w-full h-full mix-blend-darken">
                                    <HealthIndicator
                                             serverHealth={props.familyHealth[0]}
                                             playerHealth={props.familyHealth[1]}
                                             stressHealth={props.familyHealth[2]}
                                        loadBalanceHealth={props.familyHealth[3]}  />
                                </div>
                                <div className={`absolute inset-0 bg-neutral-100/50 frosted-glass w-full h-full duration-500 ${viewColors ? "opacity-100" : "opacity-0"}`}>
                                    <div className="relative w-400px aspect-square m-50px p-30px">
                                        <div className="grid grid-cols-5 grid-rows-5 items-center gap-y-5">
                                            <div className="col-span-1 bg-amber-400/80 shadow-inset-md w-50px aspect-square rounded" />
                                            <span className="col-span-4 text-sm">Family is over-saturated with empty servers</span>
                                            
                                            <div className="col-span-1 bg-rose-500/80 shadow-inset-md w-50px aspect-square rounded" />
                                            <span className="col-span-4 text-sm">Family is over-saturated with players</span>
                                            
                                            <div className="col-span-1 bg-green-400/80 shadow-inset-md w-50px aspect-square rounded" />
                                            <span className="col-span-4 text-sm">Family has multiple servers with unhealthy levels of compute stress</span>
                                            
                                            <div className="col-span-1 bg-cyan-300/80 shadow-inset-md w-50px aspect-square rounded" />
                                            <span className="col-span-4 text-sm">Family is load balancing more than what's appropriate for the number of servers</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </motion.div>
                        <div className={`absolute w-full -bottom-30px text-sm text-center ${viewColors || flipped ? "pointer-events-none" : ""}`}>
                            <clickable.p
                                className={`inline-block text-2xs underline py-5px px-7px duration-500 ${viewColors || flipped ? "opacity-0" : "opacity-100"}`}
                                blurredClassNames="text-neutral-400"
                                hoverClassNames="z-50 relative text-neutral-100"
                                borderRadius="0.5rem"
                                onClick={() => setViewColors(true)}
                                >Colors indicate family health</clickable.p>
                        </div>
                        <clickable.div
                            borderRadius="5rem"
                            className={`absolute w-[53px] aspect-square p-7px duration-500 rounded-full -bottom-75px ${viewColors ? "opacity-100" : "opacity-0 pointer-events-none"}`}
                            hoverClassNames="absolute z-50 invert"
                            style={{left: "calc(250px - 26.5px)"}}
                            onClick={() => setViewColors(false)}>
                                <Icon className="w-full h-full pointer-events-none" iconName={IconName.CLOSE} />
                        </clickable.div>
                        <clickable.div
                            borderRadius="5rem"
                            className={`absolute w-[53px] aspect-square p-7px duration-1000 rounded-full -bottom-75px ${flipped ? "opacity-100" : "opacity-0 pointer-events-none"}`}
                            hoverClassNames="absolute z-50 invert"
                            style={{left: "calc(250px - 26.5px)"}}
                            onClick={() => setFlipped(false)}>
                                <Icon className="w-full h-full pointer-events-none" iconName={IconName.CLOSE} />
                        </clickable.div>
                        <span
                            ref={serverCountRef}
                            className={`absolute block w-full text-center font-bold text-8xl text-neutral-500 z-10 duration-200 pointer-events-none ${viewColors ? "opacity-0" : "opacity-95"}`}
                            style={{ top: `200px` }}
                            >{props.servers.length}</span>
                    </div>
                </div>
            </div>
        </div>
    );
}