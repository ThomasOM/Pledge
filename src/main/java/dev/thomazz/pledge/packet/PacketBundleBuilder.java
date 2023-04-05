package dev.thomazz.pledge.packet;

import java.lang.reflect.Constructor;

public class PacketBundleBuilder {
    private final Constructor<?> bundleConstructor;

    public PacketBundleBuilder() {
        Constructor<?> constructor;

        try {
            Class<?> clazz = Class.forName("net.minecraft.network.protocol.BundleDelimiterPacket");
            constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
        } catch (Exception ex) {
            constructor = null;
        }

        this.bundleConstructor = constructor;
    }

    public Object buildDelimiter() throws Exception {
        return this.bundleConstructor.newInstance();
    }

    public boolean isSupported() {
        return this.bundleConstructor != null;
    }
}
