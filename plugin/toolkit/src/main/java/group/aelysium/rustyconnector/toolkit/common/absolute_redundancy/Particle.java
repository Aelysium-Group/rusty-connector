package group.aelysium.rustyconnector.toolkit.common.absolute_redundancy;

import group.aelysium.rustyconnector.toolkit.proxy.util.LiquidTimestamp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Particles are the backbone of the Absolute Redundancy Architecture.
 * By leveraging {@link Flux}, Particles are able to exist in a state of super-positioning.
 */
public interface Particle extends AutoCloseable {
    /**
     * Tinder exists as an ignition point for new Particles.
     * @param <P> The Particles that will be launched via this Tinder.
     */
    abstract class Tinder<P extends Particle> {
        protected Tinder() {}

        public final Flux<P> flux() {
            return new Flux<>(this);
        }

        /**
         * Based on the contents of this Tinder, ignite a new Particle.
         * Only two results are acceptable, either a fully-functioning Particle is returned.
         * Or this method throws an exception.
         * @return A fully functional Particle.
         * @throws Exception If there is any issue at all constructing the microservice.
         */
        public abstract @NotNull P ignite() throws Exception;
    }

    /**
     * All microservices exist in a state of flux.
     * {@link Particle.Flux} exists to manage this state.
     * @param <P> The underlying Particle that exists within this flux.
     */
    class Flux<P extends Particle> implements AutoCloseable {
        private static final ExecutorService executor = Executors.newCachedThreadPool();
        private @Nullable CompletableFuture<P> resolvable = null;
        private @NotNull Tinder<P> tinder;

        protected Flux(@NotNull Tinder<P> tinder) {
            this.tinder = tinder;
        }

        /**
         * Ignites a new Particle via the Tinder associated with this Flux and returns it.
         * @return A Future that will either resolve into the Particle or resolve exceptionally.
         */
        private CompletableFuture<P> ignite(@NotNull Tinder<P> tinder) {
            CompletableFuture<P> future = new CompletableFuture<>();

            executor.submit(() -> {
                try {
                    P p = tinder.ignite();
                    future.complete(p);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });

            return future;
        }

        /**
         * Re-ignites the Particle governed by this Flux.
         * This method will ignite a new instance of this particle using the already existing Tinder in this Flux.
         * @param rollback If the passed Tinder fails to ignite. Should we attempt to ignite the old Tinder that already existed on this Flux?
         *                 If this setting is enabled, the current thread will lock until the Tinder either succeeds in ignition or until it fails Exceptionally.
         * @throws Exception If there's an issue shutting down the current Particle (If one exists)
         */
        public CompletableFuture<P> reignite(boolean rollback) throws Exception {
            return this.reignite(this.tinder, rollback);
        }

        /**
         * Re-ignites the Particle governed by this Flux.
         * This method will ignite a new instance of this particle using a new Tinder.
         * <br />
         * If this method properly ignites a new Particle with the new Tinder, this Flux will store the new Tinder and use it going forward.
         * @param tinder The tinder to ignite a new Particle with.
         * @param rollback If the passed Tinder fails to ignite. Should we attempt to ignite the old Tinder that already existed on this Flux?
         *                 If this setting is enabled, the current thread will lock until the Tinder either succeeds in ignition or until it fails Exceptionally.
         * @throws Exception If there's an issue shutting down the current Particle (If one exists)
         */
        public CompletableFuture<P> reignite(@NotNull Tinder<P> tinder, boolean rollback) throws Exception {
            if(this.resolvable != null) {
                P particle = this.resolvable.getNow(null);
                if(particle == null) this.resolvable.cancel(true);
                else particle.close();
            }

            this.resolvable = this.ignite(tinder);

            if(rollback)
                try {
                    this.resolvable.get();

                    // If succeeds, this is the new Tinder
                    this.tinder = tinder;
                } catch (Exception ignore) {
                    this.resolvable = this.ignite(this.tinder);
                }

            return this.resolvable;
        }

        /**
         * Returns the underlying Particle is it exists, or throws an exception if it doesn't.
         * @return The underlying Particle.
         * @throws NoSuchElementException If no Particle exists.
         */
        public P orElseThrow() throws NoSuchElementException {
            P p = this.access().getNow(null);
            if(p == null) throw new NoSuchElementException();
            return p;
        }

        /**
         * Runs the Consumer using the Particle.
         * If the Particle doesn't exist, this method will store the consumer on a parallel thread and wait for the Particle to attempt ignition.
         * <br />
         * If the Particle is unable to ignite in under 1 minute, the consumer will be thrown away.
         * <br />
         * To set longer timeouts you can use {@link #executeParallel(Consumer, LiquidTimestamp)}.
         * @param consumer The consumer to execute with the ignited Particle.
         */
        public void executeParallel(Consumer<P> consumer) {
            this.executeParallel(consumer, LiquidTimestamp.from(1, TimeUnit.MINUTES));
        }

        /**
         * Runs the Consumer using the Particle.
         * If the Particle doesn't exist, this method will store the consumer on a parallel thread and wait for the Particle to attempt ignition.
         * <br />
         * If the Particle is unable to ignite in under 1 minute, the consumer will be thrown away.
         * <br />
         * To set longer timeouts you can use {@link #executeParallel(Consumer, LiquidTimestamp)}.
         * @param consumer The consumer to execute with the ignited Particle.
         * @param timeout The amount of time to give the Particle for ignition before giving up.
         */
        public void executeParallel(Consumer<P> consumer, LiquidTimestamp timeout) {
            if(this.resolvable == null) return;
            if(this.resolvable.isCancelled()) return;
            if(this.resolvable.isCompletedExceptionally()) return;
            if(this.exists()) {
                try {
                    consumer.accept(this.access().get(timeout.value(), timeout.unit()));
                } catch (Exception ignore) {}
                return;
            }

            executor.submit(()->{
                try {
                    consumer.accept(this.access().get(timeout.value(), timeout.unit()));
                } catch (Exception ignore) {}
            });
        }

        /**
         * Runs the Consumer using the Particle.
         * This method is not thread-locking and will always execute the Consumer instantly.
         * <br/>
         * This method respects Exceptions that may be thrown within the Consumer.
         * <br />
         * If the Particle is not available, the Consumer is thrown away.
         * @param success The consumer to execute if the Particle is available.
         */
        public void executeNow(Consumer<P> success) {
            this.executeNow(success, ()->{});
        }

        /**
         * Runs either Consumer or Runnable based on if the Particle is available.
         * This method is not thread-locking and will always execute either the Consumer or the Runnable instantly.
         * <br/>
         * This method respects Exceptions that may be thrown within the Consumer or Runnable.
         * Any exceptions that might be thrown will be passed along to the caller to handle.
         * @param success The consumer to execute if the Particle is available.
         * @param failed The Runnable if the Particle isn't available.
         */
        public void executeNow(Consumer<P> success, Runnable failed) {
            this.executeLocking(success, failed, LiquidTimestamp.from(0, TimeUnit.SECONDS));
        }

        /**
         * Runs the Consumer using the Particle.
         * This method is thread locking for no longer than the duration of the timeout.
         * <br/>
         * If the Particle currently exists, it will resolve instantly. If the Particle doesn't exist, timeout will determine how
         * long this method will wait before running either the success Consumer or the failed Runnable.
         * <br/>
         * This method respects Exceptions that may be thrown within the Consumer.
         * Any exceptions that might be thrown will be passed along to the caller to handle.
         * @param success The consumer to execute if the Particle is available.
         * @param timeout The amount of time to give the Particle to resolve before throwing away the Consumer.
         */
        public void executeLocking(Consumer<P> success, LiquidTimestamp timeout) {
            this.executeLocking(success, ()->{}, timeout);
        }

        /**
         * Executes either the Consumer or the Runnable based on if the Particle is available or not after the delay.
         * This method is thread locking for no longer than the duration of the timeout.
         * <br />
         * If the Particle currently exists, it will resolve instantly. If the Particle doesn't exist, timeout will determine how
         * long this method will wait before running either the success Consumer or the failed Runnable.
         * <br/>
         * This method respects Exceptions that may be thrown within the Consumer or Runnable.
         * Any exceptions that might be thrown will be passed along to the caller to handle.
         * @param success The consumer to execute if the Particle is available.
         * @param failed The Runnable if the Particle isn't available.
         * @param timeout The amount of time to give the Particle to resolve before running the Runnable.
         */
        public void executeLocking(Consumer<P> success, Runnable failed, LiquidTimestamp timeout) {
            if(this.exists()) {
                Optional<P> p = Optional.empty();
                try {
                    if(timeout.value() == 0) p = Optional.ofNullable(this.access().getNow(null));
                    else p = Optional.ofNullable(this.access().get(timeout.value(), timeout.unit()));
                } catch (Exception ignore) {}

                if(p.isPresent()) {
                    success.accept(p.orElseThrow());
                    return;
                }
            }

            failed.run();
        }

        /**
         * Access the underlying Particle.
         * Particles exist in a state of super-position, there's no way to know if a microservice is currently active until you observe it.
         * This method is equivalent to calling {@link #access() .access()}{@link CompletableFuture#get() .get()}.
         * @return A Particle if it was able to ignite. If the Particle wasn't able to ignite, this method will throw an exception.
         * @throws Exception If the future completes exceptionally. i.e. the Particle failed to ignite.
         */
        public P observe() throws Exception {
            return this.access().get();
        }

        /**
         * Access the Particle through it's CompletableFuture.
         * Particles exist in a state of super-position, there's no way to know if a Particle is currently active until you observe it.
         * <br />
         * If this Particle does not exist, this method will attempt to ignite a new instance of this Particle.
         * @return A future that will resolve to the finished Particle if it's able to boot. If the Particle wasn't able to boot, the future will complete exceptionally.
         */
        public CompletableFuture<P> access() {
            if(this.resolvable != null)
                return this.resolvable;

            this.resolvable = this.ignite(this.tinder);

            return this.resolvable;
        }

        /**
         * Checks if the Particle exists.
         * If this returns true, you should be able to instantly access the Particle.
         * @return `true` if the Particle exists. `false` otherwise.
         */
        public boolean exists() {
            if(this.resolvable == null) return false;
            return this.resolvable.isDone() && !this.resolvable.isCancelled() && !this.resolvable.isCompletedExceptionally();
        }

        /**
         * Fetches the Tinder being used by this flux.
         * @return The Tinder.
         */
        public Tinder<P> tinder() {
            return this.tinder;
        }

        public void close() throws Exception {
            if(this.resolvable == null) return;
            if(this.resolvable.isDone()) {
                this.resolvable.get().close();
                return;
            }
            this.resolvable.completeExceptionally(new InterruptedException("Particle boot was interrupted by Hypervisor closing!"));
        }

        /**
         * Implements {@link Object#equals(Object)} where the {@link Tinder} of the two Fluxes are compared.
         * @param o The Flux to compare with.
         * @return `true` or `false` based on the equality of the two Fluxes.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Flux<?> flux = (Flux<?>) o;
            return Objects.equals(tinder, flux.tinder);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tinder);
        }

        /**
         * Capacitor is a collection of Flux.
         * As long as the keys are unique, you can store as much Flux as you want here.
         */
        public static class Capacitor implements AutoCloseable {
            private final Map<String, Flux<? extends Particle>> flux = new ConcurrentHashMap<>();

            public Capacitor() {}

            /**
             * Stores the passed tinder in the flux capacitor.
             * This method creates a Particle Flux backed by the passed tinder.
             * @param key A unique key that can reference this Flux.
             * @param tinder The tinder used to back the Flux.
             */
            public void store(String key, Particle.Tinder<? extends Particle> tinder) {
                this.flux.put(key, tinder.flux());
            }

            public Optional<Flux<? extends Particle>> fetch(String key) {
                return Optional.ofNullable(this.flux.get(key));
            }

            @Override
            public void close() throws Exception {
                this.flux.values().forEach(f -> {
                    try {
                        f.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                this.flux.clear();
            }
        }
    }
}