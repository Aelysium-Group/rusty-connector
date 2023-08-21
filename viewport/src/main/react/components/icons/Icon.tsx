export const IconName = {
    DOTS: "dots",
    THREE_DOTS: "dots",

    TRASH: "trash",
    DELETE: "trash",

    CREATE: "create",
    ADD: "create",
    
    PENCIL: "pencil",
    EDIT: "pencil",
    
    STOP: "stop",
    
    CLOSE: "close",
    
    TRANSFER: "transfer",
    
    SERVER: "server",
} as const;
export type IconName = typeof IconName[keyof typeof IconName];

type Icon = JSX.IntrinsicElements["div"] & {
    iconName: IconName;
    inverted?: boolean
}
export const Icon = ({iconName, inverted, children, ...rest }: Icon) => (
   <div
   {...rest}
    className={`${rest.className} bg-no-repeat bg-center bg-cover ${ inverted ? "invert" : ""}`}
    style={{backgroundImage: `url(../../src/main/resources/icons/${iconName}.svg)`}} />
)