export type HealthScore = {
    /**
     * Indicates if the family is over-saturated with empty servers
     */
    serverHealth: number;
    /**
     * Indicates if the family is over-saturated with players
     */
    playerHealth: number;
    /**
     * Indicates if the family has multiple servers with unhealthy levels of compute stress
     */
    stressHealth: number;
    /**
     * Indicates if the family is load balancing more than what's appropriate for the number of servers
     */
    loadBalanceHealth: number;
}
export const HealthIndicator = (props: HealthScore) => {
    return (
        <div className="relative w-full h-full">
            <div className="absolute inset-0 w-full h-full" style={{ background: `linear-gradient(315deg, #ffbb0a 0%, #ffbb0a00 50%)`, opacity: 0.01 * props.serverHealth}} />
            <div className="absolute inset-0 w-full h-full" style={{ background: `linear-gradient(45deg,  #fe395b 0%, #fe395b00 50%)`, opacity: 0.01 * props.playerHealth}} />
            <div className="absolute inset-0 w-full h-full" style={{ background: `linear-gradient(135deg, #2fff7b 0%, #2fff7b00 50%)`, opacity: 0.01 * props.stressHealth}} />
            <div className="absolute inset-0 w-full h-full" style={{ background: `linear-gradient(225deg, #67e8f9 0%, #67e8f900 50%)`, opacity: 0.01 * props.loadBalanceHealth}} />
        </div>
    );
}