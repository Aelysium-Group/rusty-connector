

type OverviewHolder = {
    className: string;
};
export const OverviewHolder = (props: OverviewHolder) => {

    return (
        <div className={`${props.className}`}>
            <span className="block mb-20px w-full text-center font-bold text-6xl text-neutral-500">Overview</span>
            <div className={`${props.className} bg-neutral-200 w-500px rounded-2xl h-600px`}>

            </div>
        </div>
    );
}