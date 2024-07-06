package group.aelysium.rustyconnector.toolkit;

import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.mc_loader.IMCLoaderFlame;
import group.aelysium.rustyconnector.toolkit.proxy.IProxyFlame;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RustyConnector {
    public static class Toolkit {
        private static Particle.Flux<IMCLoaderFlame> mcLoaderKernel = null;
        private static Particle.Flux<IProxyFlame> velocityKernel = null;

        /**
         * Fetches the MCLoader API for RustyConnector.
         * @return {@link IMCLoaderFlame}
         */
        public static Optional<Particle.Flux<IMCLoaderFlame>> MCLoader() throws IllegalAccessError {
            return Optional.ofNullable(mcLoaderKernel);
        }

        /**
         * Fetches the Proxy API for RustyConnector.
         * @return {@link IProxyFlame}
         */
        public static Optional<Particle.Flux<IProxyFlame>> Proxy() throws IllegalAccessError {
            return Optional.ofNullable(velocityKernel);
        }

        public static void registerMCLoader(@NotNull Particle.Flux<IMCLoaderFlame> kernel) {
            mcLoaderKernel = kernel;
        }
        public static void registerProxy(@NotNull Particle.Flux<IProxyFlame> kernel) {
            velocityKernel = kernel;
        }

        public static void unregister() {
            mcLoaderKernel = null;
            velocityKernel = null;
        }
    }
}