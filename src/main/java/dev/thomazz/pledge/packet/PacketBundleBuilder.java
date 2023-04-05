package dev.thomazz.pledge.packet;
import dev.thomazz.pledge.util.MinecraftUtil;

import java.lang.reflect.Constructor;

public class PacketBundleBuilder {
    private final Constructor<?> bundleConstructor;

    public PacketBundleBuilder() {
        Constructor<?> constructor;

        try {
            Class<?> clazz = MinecraftUtil.gamePacket("ClientboundBundlePacket");
            constructor = clazz.getConstructor(Iterable.class);
        } catch (Exception ex) {
            constructor = null;
        }

        this.bundleConstructor = constructor;
    }

    public Object buildBundle(Iterable<?> packets) throws Exception {
        return this.bundleConstructor.newInstance(packets);
    }

    public boolean isSupported() {
        return this.bundleConstructor != null;
    }
}
