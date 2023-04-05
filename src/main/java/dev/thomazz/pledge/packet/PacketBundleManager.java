package dev.thomazz.pledge.packet;
import dev.thomazz.pledge.util.MinecraftUtil;

import java.lang.reflect.Constructor;

public class PacketBundleManager {
    private final Class<?> bundleClass;
    private final Constructor<?> bundleConstructor;

    public PacketBundleManager() {
        Class<?> clazz;
        Constructor<?> constructor;

        try {
            clazz = MinecraftUtil.gamePacket("ClientboundBundlePacket");
            constructor = clazz.getConstructor(Iterable.class);
        } catch (Exception ex) {
            clazz = null;
            constructor = null;
        }

        this.bundleClass = clazz;
        this.bundleConstructor = constructor;
    }

    public Object createBundle(Iterable<Object> packets) throws Exception {
        return this.bundleConstructor.newInstance(packets);
    }

    public boolean isSupported() {
        return this.bundleClass != null;
    }
}
