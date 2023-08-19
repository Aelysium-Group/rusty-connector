import { useEffect, useState } from 'react';

import { Routes, Route, useLocation } from "react-router-dom";
import { TransitionGroup, CSSTransition } from "react-transition-group";
import { NetworkOverview } from '../pages/NetworkOverview';
import { FamilySpecific } from '../pages/family_specific/FamilySpecific';

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
            <TransitionGroup component={null}>
                <CSSTransition
                    key={location.pathname}
                    classNames={transitionType}
                    timeout={1000}>
                    <Routes location={location}>
                        <Route path='*'        element={<NetworkOverview />}/>
                        <Route path='family/:family_id'    element={<FamilySpecific />}/>
                        {/*
                        <Route path='*'        element={<Page404 />}/>
                        <Route path='login'    element={<LoginPage />}/>
                        <Route path='you'      element={<OwnProfilePage />}/>
                        <Route path='reports'  element={<Page404 />}>
                            { canViewBugReports     && Navigator.get('bugReports'   ).routes() }
                            { canViewPlayerReports  && Navigator.get('playerReports').routes() }
                        </Route>
                        { canViewPlayerProfiles     && Navigator.get('profiles'     ).routes() }
                        { canViewPunishments        && Navigator.get('punishments'  ).routes() }
                        { canViewServers            && Navigator.get('servers'      ).routes() }
                        { canViewRoles              && Navigator.get('roles'        ).routes() }
        */}
                    </Routes>
                </CSSTransition>
            </TransitionGroup>
        );
    }

    return render();
}