interface PasswordInput {
    onChange: Function;
    value: string;
    placeholder?: string;
}
export const PasswordInput = (props: PasswordInput) => (
    <input
        className="block bg-neutral-300 h-47px w-full my-10px px-20px py-4px pt-3px font-bold text-xl text-zinc-700 border-none rounded-xl appearance-none shadow-inset-md"
        type='password'
        value={props.value}
        onChange={(event) => props.onChange(event.target.value)}
        placeholder={props.placeholder ?? ''}
        onPointerDownCapture={e => e.stopPropagation()}
        />
);