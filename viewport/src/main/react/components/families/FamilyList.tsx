import { useEffect } from "react";
import { QueryMode, useScreen, useView } from "react-ui-breakpoints";
import useMeasure from "react-use-measure";
import { FamilyIcon } from "./FamilyIcon";

export const FamilyList = () => {
    const [ ref, bounds ] = useMeasure();
    useEffect(()=>{},[ bounds ]);

    const desktop = () => {
        return (
            <div className="absolute bottom-0 w-screen h-70px bg-neutral-700">
                <div
                    ref={ref}
                    className={`z-40 mx-auto min-w-fit`}
                >
                    <div className={`overflow-hidden w-full rounded-full h-70px p-5px`}>
                        <div className="relative w-full h-full">
                            <FamilyIcon name="Survival"/>
                            <FamilyIcon name="minigame"/>
                            <FamilyIcon name="spleef"/>
                            <FamilyIcon name="mario kart"/>
                            <FamilyIcon name="kit"/>
                            <FamilyIcon name="hunger games"/>
                            <FamilyIcon name="pvp"/>
                        </div>
                    </div>
                    <span className="absolute right-20px top-16px text-3xl text-white font-bold">Families</span>
                </div>
            </div>
        );
    };

    return useScreen(
        QueryMode.MOBILE_FIRST,
        useView('1000px', desktop()),
        useView("default", desktop()),
    );
}