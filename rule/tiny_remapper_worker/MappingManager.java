package top.fifthlight.fabazel.remapper;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.extras.MappingTreeRemapper;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.tinyremapper.IMappingProvider;
import net.fabricmc.tinyremapper.TinyUtils;
import org.objectweb.asm.commons.Remapper;

import java.io.Closeable;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class MappingManager implements Closeable {
    private final Duration cleanupTimeout = Duration.ofSeconds(30);
    private final Map<Argument.Key, CacheEntry> mappings = new ConcurrentHashMap<>();
    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private final ReentrantLock lock = new ReentrantLock();
    private ScheduledFuture<?> scheduledFuture = null;

    public static class CacheEntry {
        private final IMappingProvider provider;
        private final Remapper remapper;
        private Instant lastUsed;

        public CacheEntry(IMappingProvider provider, Remapper remapper, Instant lastUsed) {
            this.provider = provider;
            this.remapper = remapper;
            this.lastUsed = lastUsed;
        }

        public IMappingProvider getProvider() {
            return provider;
        }

        public Remapper getRemapper() {
            return remapper;
        }

        public Instant getLastUsed() {
            return lastUsed;
        }

        public void setLastUsed(Instant lastUsed) {
            this.lastUsed = lastUsed;
        }
    }

    public static class Argument {
        private final Path mapping;
        private final String mappingHash;
        private final String fromNamespace;
        private final String toNamespace;
        private Key key;
        private MemoryMappingTree mappingTree;
        private MappingTreeRemapper remapper;
        private IMappingProvider mappingProvider;

        public Argument(Path mapping, String mappingHash, String fromNamespace, String toNamespace) {
            this.mapping = mapping;
            this.mappingHash = mappingHash;
            this.fromNamespace = fromNamespace;
            this.toNamespace = toNamespace;
        }

        public Key getKey() {
            if (key == null) {
                key = new Key(mappingHash, fromNamespace, toNamespace);
            }
            return key;
        }

        public MemoryMappingTree getMappingTree() {
            if (mappingTree == null) {
                try {
                    mappingTree = new MemoryMappingTree();
                    MappingReader.read(mapping, mappingTree);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return mappingTree;
        }

        public MappingTreeRemapper getRemapper() {
            if (remapper == null) {
                remapper = new MappingTreeRemapper(getMappingTree(), fromNamespace, toNamespace);
            }
            return remapper;
        }

        public IMappingProvider getMappingProvider() {
            if (mappingProvider == null) {
                mappingProvider = TinyUtils.createMappingProvider(getMappingTree(), fromNamespace, toNamespace);
            }
            return mappingProvider;
        }

        public static class Key {
            private final String mappingHash;
            private final String fromNamespace;
            private final String toNamespace;

            public Key(String mappingHash, String fromNamespace, String toNamespace) {
                this.mappingHash = mappingHash;
                this.fromNamespace = fromNamespace;
                this.toNamespace = toNamespace;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                var key = (Key) o;
                return Objects.equals(mappingHash, key.mappingHash) &&
                        Objects.equals(fromNamespace, key.fromNamespace) &&
                        Objects.equals(toNamespace, key.toNamespace);
            }

            @Override
            public int hashCode() {
                return Objects.hash(mappingHash, fromNamespace, toNamespace);
            }
        }
    }

    public CacheEntry get(Argument argument) {
        var now = Instant.now();
        var key = argument.getKey();

        var entry = mappings.compute(key, (k, existing) -> {
            if (existing != null) {
                existing.setLastUsed(now);
                return existing;
            } else {
                return new CacheEntry(
                        argument.getMappingProvider(),
                        argument.getRemapper(),
                        now
                );
            }
        });

        scheduleCleanupIfNeeded(entry.getLastUsed().plus(cleanupTimeout));
        return entry;
    }

    private void scheduleCleanupIfNeeded(Instant expiration) {
        lock.lock();
        try {
            Duration currentDelay = null;
            if (scheduledFuture != null) {
                if (!scheduledFuture.isDone()) {
                    currentDelay = Duration.of(scheduledFuture.getDelay(TimeUnit.MILLISECONDS), ChronoUnit.MILLIS);
                }
            }

            var newDelay = Duration.between(Instant.now(), expiration);
            if (newDelay.isNegative()) {
                newDelay = Duration.ZERO;
            }

            if (currentDelay == null || newDelay.compareTo(currentDelay) < 0) {
                if (scheduledFuture != null) {
                    scheduledFuture.cancel(false);
                }
                scheduledFuture = executor.schedule(
                        this::performCleanup,
                        newDelay.toMillis(),
                        TimeUnit.MILLISECONDS
                );
            }
        } finally {
            lock.unlock();
        }
    }

    private void performCleanup() {
        var now = Instant.now();
        var cutoff = now.minus(cleanupTimeout);

        mappings.entrySet().removeIf(entry -> entry.getValue().getLastUsed().isBefore(cutoff));

        lock.lock();
        try {
            Instant nextExpiration = null;
            for (var entry : mappings.values()) {
                if (nextExpiration == null || entry.getLastUsed().isBefore(nextExpiration)) {
                    nextExpiration = entry.getLastUsed();
                }
            }
            if (nextExpiration != null) {
                scheduleCleanupIfNeeded(nextExpiration.plus(cleanupTimeout));
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        executor.shutdownNow();
        mappings.clear();
    }
}
