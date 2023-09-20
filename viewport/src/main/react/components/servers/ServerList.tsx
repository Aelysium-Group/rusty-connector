import { ServerEntry } from './ServerEntry';
import { Outlet, useNavigate, useOutletContext, useParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ServersParticleVisualizer } from './ServersParticleVisualizer';
import { useEffect, useState } from 'react';

export type ServerPartial = {
    id: string;
    name: string;
}

type ServerList = {
}
export const ServerList = (props: ServerList) => {
    const [ selectedServer, setSelectedServer ] = useState({} as ServerPartial);
    const navigate = useNavigate();
    const { family_id } = useParams();
    const { server_id } = useParams();
    const servers: ServerPartial[] = [
        {name: "server1", id: "1"},
        {name: "server2", id: "2"},
        {name: "server3", id: "3"},
        {name: "server4", id: "4"},
        {name: "server5", id: "5"},
        {name: "server6", id: "6"},
        {name: "server7", id: "7"},
        {name: "server8", id: "8"},
        {name: "server9", id: "9"},
        {name: "server10", id: "10"},
        {name: "server11", id: "11"},
        {name: "server12", id: "12"},
        {name: "server13", id: "13"},
        {name: "server14", id: "14"},
        {name: "server11", id: "15"},
        {name: "server12", id: "16"},
        {name: "server13", id: "17"},
        {name: "server14", id: "18"},
        {name: "server15", id: "19"},
        {name: "server16", id: "20"},
        {name: "server17", id: "21"},
        {name: "server18", id: "22"},
        {name: "server19", id: "23"},
    ];
    const familyHealth = [ 100, 0, 100, 0 ];
    
    const selectServer = (server: ServerPartial) => {
        setSelectedServer(server);
        navigate(`/${family_id}/${server.id}`);
    }

    useEffect(()=>{},[]);
    
    return (
        <>
            <motion.div
                className={`absolute left-0 top-0 bg-neutral-800 overflow-hidden duration-500`}
                initial={{
                    height: `calc(100vh - 70px)`,
                    width: "0px",
                }}
                animate={{
                    width: "300px",
                }}
                >
                    <div
                        className='p-10px h-full overflow-x-hidden'
                        style={{
                            height: `calc(100% - 150px)`
                        }}>
                        {
                            servers.map((entry, index) => {
                                if(entry.id == server_id)
                                    return <ServerEntry onClick={selectServer} key={index} server={entry} lit={true} />;
                                else
                                    return <ServerEntry onClick={selectServer} key={index} server={entry} />;
                            })
                        }
                    </div>
                    <div className='relative w-300px h-150px overflow-hidden grid content-center z-10'>
                        <ServersParticleVisualizer
                            className={''}
                            familyHealth={familyHealth}
                            balancerLevel={1}
                            servers={servers}
                            />
                        <div
                            className='absolute top-0 left-0 w-full h-200px'
                            style={{
                                background: "linear-gradient(0deg, rgba(38,38,38,0) 0%, rgba(38,38,38,1) 100%)"
                            }}
                        />
                        <span className='absolute top-20px left-10px text-sm w-300px block text-white font-bold pl-10px drop-shadow-lg'>
                            {family_id}
                        </span>
                    </div>
            </motion.div>
            
            <Outlet context={{ server: selectedServer }} />
        </>
    );
}

type Context = {
    server: ServerPartial;
}
export const useServer = () => {
    return useOutletContext<Context>();
}