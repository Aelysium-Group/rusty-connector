type CustomThProps = JSX.IntrinsicElements["p"] & {
}
  
const TypableP = ({ children, ...rest }: CustomThProps) => {
    return <p {...rest}>{ children } </p>;
}
  

export const typable = {
    p: TypableP,
}