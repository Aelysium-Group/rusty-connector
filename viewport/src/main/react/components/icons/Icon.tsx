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
} as const;
export type IconName = typeof IconName[keyof typeof IconName];

type Icon = {
    className?: string;
    iconName: IconName;
}
export const Icon = (props: Icon) => (
   <div className={`${props.className} bg-no-repeat bg-center bg-cover`} style={{backgroundImage: `url(./icons/${props.iconName}.svg)`}}></div>
)
