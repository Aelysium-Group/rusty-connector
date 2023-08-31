import { Link } from 'react-router-dom';
import { clickable } from '../../../cursor/interactables/Clickable';
const families = [
    {name: "survival"},
    {name: "minigames"},
    {name: "party"},
    {name: "pants"},
    {name: "dick"},
]
export const FamiliesBar = () => {
    return (
        <div className="relative w-screen h-50px text-center">
            {
                families.map((entry, index) => (
                    <Link to={`/${entry.name}`} className='cursor-none'>
                        <clickable.span
                            className='inline-block rounded mx-10px text-neutral-50 text-lg py-5px px-15px mt-5px font-bold duration-500'
                            blurredClassNames='bg-neutral-700'
                            hoverClassNames='z-50 relative bg-transparent'
                        >
                            { entry.name }
                        </clickable.span>
                    </Link>
                ))
            }
        </div>
    );
}