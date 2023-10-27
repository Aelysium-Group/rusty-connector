import { AnimatePresence, motion } from 'framer-motion';
import ScrollToBottom from 'react-scroll-to-bottom';
import { Player } from '../../lib/entities/Player';
import { useServer } from '../servers/ServerList';
import { Message } from '../../lib/entities/Message';

const players = [
    new Player("d2281154f3c24ed19c7b5501722c3aaa", "SIVIN_Official"),
    new Player("a2d983f844db4f30916f927dedb6c37c", "notch"),
    new Player("c67a0be03b3540d48173ccad6672516a", "jeb_"),
    new Player("1e18d5ff643d45c8b50943b8461d8614", "deadmau5"),
];

type MembersPanel = {
    players: Player[];
}
export const MembersPanel = (props: MembersPanel) => {
    return (
        <div
            className={`absolute right-0 top-[47px] w-300px bg-neutral-800 p-10px overflow-hidden duration-500`}
            style={{
                height: `calc(100% - 47px)`,
            }}
            >
                {
                    props.players.map((item, index) => (
                        item.labelComponent(index)
                    ))
                }
        </div>
    );
}