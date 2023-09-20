import { QueryMode, useScreen, useView } from "react-ui-breakpoints";
import { Outlet } from "react-router-dom";
import { FamilyList } from "../../families/FamilyList";
import { useEffect } from "react";

export const Overview = (props: {}) => {
    useEffect(() => {}, [props]);

    const desktop = () => (
        <div className="w-screen h-screen overflow-y-auto pb-200px bg-neutral-900">
            <Outlet />
            <FamilyList />
        </div>
    );
    const mobile = () => (
        <></>
    );

    return useScreen(
        QueryMode.MOBILE_FIRST,
        useView("1000px", desktop()),
        useView("default", mobile()),
    );
}