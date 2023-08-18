export const InterfaceColor = {
    RED: "#BE123C",     // "bg-rose-700"
    YELLOW: "#EAB308",  // "bg-yellow-500"
    ORANGE: "#F97316",  // "bg-orange-500"
    GREEN: "#65A30D",   // "bg-lime-600"
    BLUE: "#06B6D4",    // "bg-cyan-500"
    PURPLE: "#C026D3",  // "bg-fuchsia-600"
    GRAY: "#71717A",    // "bg-zinc-500"
    BLACK: "#1E293B",   // "bg-slate-800"
    WHITE: "#D4D4D4",   // "bg-neutral-300"
} as const;
export type InterfaceColor = typeof InterfaceColor[keyof typeof InterfaceColor];