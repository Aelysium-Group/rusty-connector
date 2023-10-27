import { useEffect, useState } from 'react';

import { Routes, Route, useLocation } from "react-router-dom";
import { TransitionGroup, CSSTransition } from "react-transition-group";
import { Overview } from '../pages/overview/Overview';
import { ServerList } from '../servers/ServerList';
import { ServerChat } from '../chat/ServerChat';

interface PageRouter {
}
export const PageRouter = (props: PageRouter) => {
    /*
    const canViewBugReports:     boolean = usePermission(PermissionCode.VIEW_BUG_REPORTS);
    const canViewPlayerReports:  boolean = usePermission(PermissionCode.VIEW_PLAYER_REPORTS);
    const canViewPlayerProfiles: boolean = usePermission(PermissionCode.VIEW_PLAYER_PROFILES, PermissionCode.VIEW_PLAYERS);
    const canViewPunishments:    boolean = usePermission(PermissionCode.VIEW_PUNISHMENTS);
    const canViewServers:        boolean = usePermission(PermissionCode.VIEW_SERVERS);
    const canViewRoles:          boolean = usePermission(PermissionCode.VIEW_ROLES);*/

    const [ transitionType, setTransitionType ] = useState('slideFront');
    const location = useLocation();
    
    const init = () => {
    }

    const transition = () => {
        //if(location.pathname.match(new RegExp('\/profiles\/.{1,}','g'))) return 'slideBack';

        return 'slideFront';
    }

    useEffect(() => {init()},[location.pathname]);

    const render = () => {
        return (
            <Routes location={location}>
                <Route path='/'    element={<Overview />}>
                    <Route path=':family_id'    element={<ServerList />}>
                        <Route path=':server_id'    element={<ServerChat />} />
                    </Route>
                </Route>
            </Routes>
        );
    }

    return render();
}