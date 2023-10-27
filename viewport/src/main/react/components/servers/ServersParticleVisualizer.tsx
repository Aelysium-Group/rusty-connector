import { useCallback, useState } from "react";
import Particles from "react-particles";
import { loadSlim } from "tsparticles-slim";
import { Engine } from "tsparticles-engine";
import { HealthIndicator } from "../pages/family_specific/sub/HealthIndicator";

type Servers = {
    familyHealth: number[];
    className: string;
    balancerLevel: number;
    servers: ServerPartial[];
};
type ServerPartial = {
    id: string,
    name: string
};
export const ServersParticleVisualizer = (props: Servers) => {
    const particlesInit = useCallback(async (engine: Engine) => {
        await loadSlim(engine);
    }, []);

    return (
        <div className={props.className}>
            <Particles
                id="FamilySpecific-particles"
                init={particlesInit}
                className="z-20"
                width="300px"
                height="170px"
                options={{
                    fullScreen: false,
                    fps_limit: 60,
                    pauseOnBlur: true,
                    background: {
                        color: "#D4D4D4"
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
                            distance: 50,
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
            <div className="absolute inset-0 w-full aspect-square mix-blend-darken">
                <HealthIndicator
                         serverHealth={props.familyHealth[0]}
                         playerHealth={props.familyHealth[1]}
                         stressHealth={props.familyHealth[2]}
                    loadBalanceHealth={props.familyHealth[3]}  />
            </div>
        </div>
    );
}