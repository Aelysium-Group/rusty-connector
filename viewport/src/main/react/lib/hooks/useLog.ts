import { DynavLogTransportEvent } from "../../components/dynav/events/DynavLogTransportEvent";
import { InterfaceColor } from "../InterfaceColor";
import { ViewportServices } from "../services/ViewportServices";
import { dirtyClone } from "./dirtyClone";
let index = 0;
let messages = {};
interface Log {
    'add': {
        'message': Function,
        'confirm': Function,
        'error':   Function
    },
    'remove': Function,
    'getAll': Function
}

export type LogMessages = {
    [key: number]: {
        message: string;
        color: InterfaceColor;
    }
}

/**
 * Add, remove, and access the App's client log
 */
export const useLog = (): Log => {
    /**
     * Add a log entry to the log.
     * @param message The text contents of the log.
     * @param color The color of the log
     */
    const addLog = (message: string, color: InterfaceColor): void => {
        const newIndex: number = index + 1;
        const newMessages: LogMessages = dirtyClone(messages);
    
        newMessages[newIndex] = {
            message,
            color
        };
        
        messages = newMessages;
        index = newIndex;

        ViewportServices.get().eventFactory().fire(new DynavLogTransportEvent(messages));
    }
    
    /**
     * Remove a log entry from the log.
     * @param index The index of the log to remove.
     */
    const removeLog = (index: string): void => {
        const newMessages: LogMessages = dirtyClone(messages);
        delete newMessages[parseInt(index)];

        messages = newMessages;

        ViewportServices.get().eventFactory().fire(new DynavLogTransportEvent(messages));
    }
    
    /**
     * Add a log entry of type `message` to the log.
     * @param contents The text contents of the log.
     */
    const sendMessage = (contents: string): void => addLog(contents, InterfaceColor.GRAY);
    
    /**
     * Add a log entry of type `confirm` to the log.
     * @param contents The text contents of the log.
     */
    const sendConfirm = (contents: string): void => addLog(contents, InterfaceColor.GREEN);
    
    /**
     * Add a log entry of type `error` to the log.
     * @param contents The text contents of the log.
     */
    const sendError = (contents: string): void => addLog(contents, InterfaceColor.RED);
    
    /**
     * Returns all logs
     * @returns All logs
     */
    const getAll = (): LogMessages => messages;

    const returnBody = {
        'add': {
            'message': sendMessage,
            'confirm': sendConfirm,
            'error':   sendError
        },
        'remove': removeLog,
        getAll
    };
    return (returnBody as Log);
}
