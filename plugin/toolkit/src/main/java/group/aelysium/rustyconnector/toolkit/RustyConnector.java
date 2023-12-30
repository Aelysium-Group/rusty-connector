package group.aelysium.rustyconnector.toolkit;

import group.aelysium.rustyconnector.toolkit.mc_loader.central.IMCLoaderTinder;
import group.aelysium.rustyconnector.toolkit.premier.central.PremierTinder;
import group.aelysium.rustyconnector.toolkit.velocity.central.VelocityTinder;

import java.util.Optional;

public class RustyConnector {
    public static class Toolkit {
        private static IMCLoaderTinder mcLoaderTinder = null;
        private static VelocityTinder velocityTinder = null;
        private static PremierTinder premierTinder = null;

        /**
         * Fetches the MCLoader API for RustyConnector.
         * @return {@link IMCLoaderTinder}
         */
        public static Optional<IMCLoaderTinder> mcLoader() throws IllegalAccessError {
            if(mcLoaderTinder == null) return Optional.empty();
            return Optional.of(mcLoaderTinder);
        }

        /**
         * Fetches the Proxy API for RustyConnector.
         * @return {@link VelocityTinder}
         */
        public static Optional<VelocityTinder> proxy() throws IllegalAccessError {
            if(velocityTinder == null) return Optional.empty();
            return Optional.of(velocityTinder);
        }

        /**
         * Fetches the Premier API for RustyConnector.
         * @return {@link PremierTinder}
         */
        public static Optional<PremierTinder> premier() throws IllegalAccessError {
            if(premierTinder == null) return Optional.empty();

            return Optional.of(premierTinder);
        }

        public static void register(IMCLoaderTinder tinder) {
            mcLoaderTinder = tinder;
        }
        public static void register(VelocityTinder tinder) {
            velocityTinder = tinder;
        }
        public static void register(PremierTinder tinder) {
            premierTinder = tinder;
        }

        public static void unregister() {
            mcLoaderTinder = null;
            velocityTinder = null;
            premierTinder = null;
        }
    }
}