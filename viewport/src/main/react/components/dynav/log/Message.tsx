import { useState } from 'react';
import { motion } from 'framer-motion';
import { useLog } from '../../../lib/hooks/useLog';
import { useTimeout } from '../../../lib/hooks/useTimeout.ts';
import useMeasure from 'react-use-measure';

interface Message {
    index: string;
    children: JSX.Element | string;
    color: string
}
export const Message = (props: Message) => {
    const [ isVisible, setVisible] = useState(false);
    const [ ref, bounds ] = useMeasure();

    const log = useLog();
    
    useTimeout(
        () => setVisible(true),
        20
    );
    useTimeout(
        () => setVisible(false),
        3 * 1000
    );
    useTimeout(
        () => log.remove(props.index),
        (3 * 1000) + 1000
    );

    return (
        <motion.span
                ref={ref}
                className={`relative block rounded w-full text-sm text-center duration-700 text-neutral-100`}
                initial={{ left: "500px" }}
                animate={{ left: isVisible ? `calc(${500 * 0.5}px - ${bounds.width * 0.5}px)` : "-500px" }}
                transition={{ type: "none" }}
            >
            {props.children}
        </motion.span>
    );
}