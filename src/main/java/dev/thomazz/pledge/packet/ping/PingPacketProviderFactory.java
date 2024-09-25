package dev.thomazz.pledge.packet.ping;

import com.google.common.collect.ImmutableSet;
import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@UtilityClass
public class PingPacketProviderFactory {
    private final Set<ThrowingSupplier<PingPacketProvider>> suppliers = ImmutableSet.of(
        TransactionPacketProvider::new,
        PingPongPacketProvider::new
    );

    public PingPacketProvider buildPingProvider() {
        return PingPacketProviderFactory.suppliers.stream()
            .map(PingPacketProviderFactory::buildProvider)
            .flatMap(optional -> optional.map(Stream::of).orElseGet(Stream::empty))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Could not create packet provider!"));
    }

    private Optional<PingPacketProvider> buildProvider(ThrowingSupplier<PingPacketProvider> supplier) {
        try {
            return Optional.of(supplier.get());
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
