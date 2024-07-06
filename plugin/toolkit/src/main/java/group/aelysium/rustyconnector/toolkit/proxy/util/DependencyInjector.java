package group.aelysium.rustyconnector.toolkit.proxy.util;

public class DependencyInjector {
    public static <D1> DI1<D1> inject(D1 dependency1) {
        return new DI1<>(dependency1);
    }
    public static <D1, D2> DI2<D1, D2> inject(D1 dependency1, D2 dependency2) {
        return new DI2<>(dependency1, dependency2);
    }
    public static <D1, D2, D3> DI3<D1, D2, D3> inject(D1 dependency1, D2 dependency2, D3 dependency3) {
        return new DI3<>(dependency1, dependency2, dependency3);
    }
    public static <D1, D2, D3, D4> DI4<D1, D2, D3, D4> inject(D1 dependency1, D2 dependency2, D3 dependency3, D4 dependency4) {
        return new DI4<>(dependency1, dependency2, dependency3, dependency4);
    }
    public static <D1, D2, D3, D4, D5> DI5<D1, D2, D3, D4, D5> inject(D1 dependency1, D2 dependency2, D3 dependency3, D4 dependency4, D5 dependency5) {
        return new DI5<>(dependency1, dependency2, dependency3, dependency4, dependency5);
    }
    public static <D1, D2, D3, D4, D5, D6> DI6<D1, D2, D3, D4, D5, D6> inject(D1 dependency1, D2 dependency2, D3 dependency3, D4 dependency4, D5 dependency5, D6 dependency6) {
        return new DI6<>(dependency1, dependency2, dependency3, dependency4, dependency5, dependency6);
    }

    public record DI1<D1>(D1 d1) {}
    public record DI2<D1, D2>(D1 d1, D2 d2) {}
    public record DI3<D1, D2, D3>(D1 d1, D2 d2, D3 d3) {}
    public record DI4<D1, D2, D3, D4>(D1 d1, D2 d2, D3 d3, D4 d4) {}
    public record DI5<D1, D2, D3, D4, D5>(D1 d1, D2 d2, D3 d3, D4 d4, D5 d5) {}
    public record DI6<D1, D2, D3, D4, D5, D6>(D1 d1, D2 d2, D3 d3, D4 d4, D5 d5, D6 d6) {}
}