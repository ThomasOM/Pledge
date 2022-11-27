package dev.thomazz.pledge.packet;

import com.google.common.collect.ImmutableSet;
import dev.thomazz.pledge.packet.providers.PingPongPacketProvider;
import dev.thomazz.pledge.packet.providers.TransactionPacketProvider;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public final class PacketProviderFactory {
    private static final Set<ThrowingSupplier<PacketProvider>> suppliers = ImmutableSet.of(
        TransactionPacketProvider::new,
        PingPongPacketProvider::new
    );

    public static PacketProvider build() {
        return PacketProviderFactory.suppliers.stream()
            .map(PacketProviderFactory::buildProvider)
            .flatMap(optional -> optional.map(Stream::of).orElseGet(Stream::empty))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Could not create packet provider!"));
    }

    private static Optional<PacketProvider> buildProvider(ThrowingSupplier<PacketProvider> supplier) {
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
