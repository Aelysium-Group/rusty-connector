import { motion } from 'framer-motion';
import ScrollToBottom from 'react-scroll-to-bottom';
import { Player } from '../../lib/entities/Player';
import { useServer } from '../servers/ServerList';
import { Message } from '../../lib/entities/Message';
import { MembersPanel } from './MembersPanel';

const players = [
    new Player("d2281154f3c24ed19c7b5501722c3aaa", "SIVIN_Official"),
    new Player("a2d983f844db4f30916f927dedb6c37c", "notch"),
    new Player("c67a0be03b3540d48173ccad6672516a", "jeb_"),
    new Player("1e18d5ff643d45c8b50943b8461d8614", "deadmau5"),
];

type ChatMessage = {
    poster: Player;
    contents: string;
    timestamp: string;
}
export const ServerChat = () => {
    const { server } = useServer();
    const messages = [
        new Message(players[0],"Hey! Fuck you!","31646431"),
        new Message(players[1],"Fuck off","dsfahd"),
        new Message(players[2],"bastard","4678"),
        new Message(players[3],"I hate lal of you!","346315173"),
    ];

    return (
        <motion.div
            className={`absolute right-0 top-0 bg-neutral-900 overflow-hidden duration-500`}
            initial={{
                height: `calc(100vh - 70px)`,
                width: "0px",
            }}
            animate={{
                width: `calc(100vw - 300px)`,
            }}
            exit={{
                width: "0px",
            }}
            >
            <div className='relative w-full h-full'>
                <div
                    style={{
                        width: "calc(100% - 300px)",
                        height: "100%",
                    }}
                    >
                    <ScrollToBottom
                        className='h-full w-full grid content-end p-20px'
                        >
                        {
                            messages.map((item, index) => (
                                item.messageComponent(index)
                            ))
                        }
                    </ScrollToBottom>
                </div>
                <MembersPanel players={players} />
                <div className='absolute top-0 left-0 h-[47px] w-full bg-neutral-800 drop-shadow-lg'>
                    <span className='relative left-13px top-6px text-white text-xl'>
                        <span className='relative text-2xl top-2px font-bold pr-5px text-neutral-600'>#</span>
                        {server.name}
                    </span>
                </div>
            </div>
        </motion.div>
    );
}