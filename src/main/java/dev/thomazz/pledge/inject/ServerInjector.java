package dev.thomazz.pledge.inject;

import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.util.collection.HookedListWrapper;
import dev.thomazz.pledge.util.MinecraftUtil;
import dev.thomazz.pledge.util.ReflectionUtil;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ServerInjector implements Injector {
    private static final Collection<String> LEGACY_TICKABLES = Arrays.asList("IUpdatePlayerListBox", "ITickable");
    private Field hookedField;
    private Runnable injectedRunnable;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void inject() throws Exception {
        // Start end of tick injection
        Object server = MinecraftUtil.getMinecraftServer();
        Class<?> serverClass = ReflectionUtil.getSuperClassByName(server.getClass(), "MinecraftServer");

        // Inject hooked list or runnable
        for (Field field : serverClass.getDeclaredFields()) {
            try {
                if (field.getType().equals(List.class)) {
                    // Check if type parameters match one of the tickable class names used throughout different versions
                    Class<?> genericType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    if (ServerInjector.LEGACY_TICKABLES.contains(genericType.getSimpleName())) {
                        field.setAccessible(true);

                        // Use a list wrapper to check when the size method is called
                        HookedListWrapper<?> wrapper = new HookedListWrapper<Object>((List) field.get(server)) {
                            @Override
                            public void onSize() {
                                PledgeImpl.INSTANCE.getTransactionManager().endTick();
                            }
                        };

                        ReflectionUtil.removeFinalModifier(field);
                        field.set(server, wrapper);
                    } else if (genericType.equals(Runnable.class)) {
                        Collection<Runnable> runnables = (Collection<Runnable>) field.get(server);
                        this.injectedRunnable = PledgeImpl.INSTANCE.getTransactionManager()::endTick;
                        runnables.add(this.injectedRunnable);
                    } else {
                        continue;
                    }

                    this.hookedField = field;
                    break;
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void eject() throws Exception {
        if (this.hookedField != null) {
            Object server = MinecraftUtil.getMinecraftServer();

            if (this.injectedRunnable != null) {
                // Remove injected runnable
                Collection<?> collection = (Collection<?>) this.hookedField.get(server);
                collection.removeIf(this.injectedRunnable::equals);
            } else {
                // Replace hooked wrapper with original
                HookedListWrapper<?> hookedListWrapper = (HookedListWrapper<?>) this.hookedField.get(server);
                this.hookedField.set(server, hookedListWrapper.getBase());
            }

            this.hookedField = null;
        }
    }
}
