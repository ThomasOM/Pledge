package dev.thomazz.pledge.inject;

import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.inject.net.BukkitChannelInitializer;
import dev.thomazz.pledge.inject.net.EnclosingChannelInitializer;
import dev.thomazz.pledge.util.collection.HookedListWrapper;
import dev.thomazz.pledge.util.MinecraftUtil;
import dev.thomazz.pledge.util.ReflectionUtil;
import dev.thomazz.pledge.util.collection.SynchronizedListWrapper;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.bukkit.plugin.PluginDescriptionFile;

public class BukkitInjector implements Injector {
    private static final Collection<String> TICKABLE_CLASS_NAMES = Arrays.asList("IUpdatePlayerListBox", "ITickable", "Runnable");

    private final boolean injectEvents;

    private final List<ChannelFuture> injectedFutures = new ArrayList<>();
    private Field syncedField;
    private Field hookedField;

    public BukkitInjector(boolean injectEvents) {
        this.injectEvents = injectEvents;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void inject() throws Exception {
        // Start channel initializer injection for events
        if (this.injectEvents) {
            Object connection = MinecraftUtil.getServerConnection();

            loop:
            for (Field field : connection.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(connection);
                if (value instanceof List<?>) {
                    // Check if list is of correct type and inject existing entries
                    for (Object o : (List<?>) value) {
                        if (o instanceof ChannelFuture) {
                            ChannelFuture future = (ChannelFuture) o;
                            this.injectChannelFuture(future);
                            this.injectedFutures.add(future);
                        } else {
                            continue loop; // Wrong list
                        }
                    }

                    // Allows us to inject new lists that get created here and synchronizing them
                    List<?> wrapper = new SynchronizedListWrapper<Object>((List<Object>) value) {
                        @Override
                        public void onAdd(Object o) {
                            if (o instanceof ChannelFuture) {
                                try {
                                    ChannelFuture future = (ChannelFuture) o;
                                    BukkitInjector.this.injectChannelFuture(future);
                                    BukkitInjector.this.injectedFutures.add(future);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };

                    field.set(connection, wrapper);
                    this.syncedField = field;
                    break;
                }
            }
        }

        // Start end of tick injection
        Class<?> serverClazz = MinecraftUtil.nms("MinecraftServer");
        Object server = ReflectionUtil.invokeStatic(serverClazz, "getServer");

        // Inject our hooked list for end of tick
        for (Field field : serverClazz.getDeclaredFields()) {
            if (field.getType().equals(List.class)) {
                // Check if type parameters match one of the tickable class names used throughout different versions
                Class<?> genericType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                if (!BukkitInjector.TICKABLE_CLASS_NAMES.contains(genericType.getSimpleName())) {
                    continue;
                }

                field.setAccessible(true);

                // Use a list wrapper to check when the size method is called
                HookedListWrapper<?> wrapper = new HookedListWrapper<Object>((List) field.get(server)) {
                    @Override
                    public void onSize() {
                        PledgeImpl.INSTANCE.getTransactionManager().endTick();
                    }
                };

                // Remove the final modifier if it's present and set the field
                ReflectionUtil.removeFinalModifier(field);
                field.set(server, wrapper);
                this.hookedField = field;
                break;
            }
        }
    }

    @Override
    public void eject() throws Exception {
        // Remove event injections only if it was enabled
        if (this.injectEvents) {
            // Eject from all injected channels
            for (ChannelFuture future : this.injectedFutures) {
                this.ejectChannelFuture(future);
            }

            this.injectedFutures.clear();

            // Replace synchronized wrapper with original
            if (this.hookedField != null) {
                Object connection = MinecraftUtil.getServerConnection();

                SynchronizedListWrapper<?> wrapper = (SynchronizedListWrapper<?>) this.syncedField.get(connection);

                this.syncedField.set(connection, wrapper.getBase());
                this.syncedField = null;
            }
        }

        // Replace hooked wrapper back to original
        if (this.hookedField != null) {
            Class<?> serverClazz = MinecraftUtil.nms("MinecraftServer");
            Object server = ReflectionUtil.invokeStatic(serverClazz, "getServer");

            HookedListWrapper<?> hookedListWrapper = (HookedListWrapper<?>) this.hookedField.get(server);

            this.hookedField.set(server, hookedListWrapper.getBase());
            this.hookedField = null;
        }
    }

    private void injectChannelFuture(ChannelFuture future) throws Exception {
        try {
            List<String> names = future.channel().pipeline().names();
            ChannelHandler bootstrapAcceptor = null;

            // Get the boss handler
            for (String name : names) {
                ChannelHandler handler = future.channel().pipeline().get(name);
                try {
                    ReflectionUtil.get(handler, "childHandler");
                    bootstrapAcceptor = handler;
                } catch (Exception e) {
                    // No child handler means it's not the boss handler
                }
            }

            // Default to first (Also allows blame to work)
            if (bootstrapAcceptor == null) {
                bootstrapAcceptor = future.channel().pipeline().first();
            }

            // Inject our initializer
            try {
                ChannelInitializer<SocketChannel> oldInit = ReflectionUtil.get(bootstrapAcceptor, "childHandler");
                ChannelInitializer<?> newInit = new BukkitChannelInitializer(oldInit);
                ReflectionUtil.set(bootstrapAcceptor, "childHandler", newInit);
            } catch (NoSuchFieldException e) {
                // Some plugin might be messing up our injection
                ClassLoader loader = bootstrapAcceptor.getClass().getClassLoader();
                if (loader.getClass().getName().equals("org.bukkit.plugin.java.PluginClassLoader")) {
                    PluginDescriptionFile yaml = ReflectionUtil.get(loader, "description");
                    throw new Exception("Unable to inject channel initializer, due to " + bootstrapAcceptor.getClass().getName() + ", try without the plugin " + yaml.getName());
                }

                throw e;
            }
        } catch (Exception e) {
            PledgeImpl.LOGGER.severe("Unable to inject channel initializer, do you have late bind enabled?");
            throw e;
        }
    }

    private void ejectChannelFuture(ChannelFuture future) throws Exception {
        List<String> names = future.channel().pipeline().names();
        ChannelHandler bootstrapAcceptor = null;

        // Get replaced channels that have a replaced initializer
        for (String name : names) {
            ChannelHandler handler = future.channel().pipeline().get(name);
            try {
                ChannelInitializer<SocketChannel> oldInit = ReflectionUtil.get(handler, "childHandler");
                if (oldInit instanceof EnclosingChannelInitializer) {
                    bootstrapAcceptor = handler;
                }
            } catch (Exception e) {
                // Wrong handler
            }
        }

        // Default to first
        if (bootstrapAcceptor == null) {
            bootstrapAcceptor = future.channel().pipeline().first();
        }

        // Set channel initializer back to original
        ChannelInitializer<SocketChannel> oldInit = ReflectionUtil.get(bootstrapAcceptor, "childHandler");
        if (oldInit instanceof EnclosingChannelInitializer) {
            ReflectionUtil.set(bootstrapAcceptor, "childHandler", ((EnclosingChannelInitializer) oldInit).getOriginal());
        }
    }
}
