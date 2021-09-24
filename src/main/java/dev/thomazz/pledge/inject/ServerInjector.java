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
    private static final Collection<String> TICKABLE_CLASS_NAMES = Arrays.asList("IUpdatePlayerListBox", "ITickable", "Runnable");
    private Field hookedField;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void inject() throws Exception {
        // Start end of tick injection
        Class<?> serverClazz = MinecraftUtil.nms("MinecraftServer");
        Object server = ReflectionUtil.invokeStatic(serverClazz, "getServer");

        // Inject our hooked list for end of tick
        for (Field field : serverClazz.getDeclaredFields()) {
            if (field.getType().equals(List.class)) {
                // Check if type parameters match one of the tickable class names used throughout different versions
                Class<?> genericType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                if (!ServerInjector.TICKABLE_CLASS_NAMES.contains(genericType.getSimpleName())) {
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
        // Replace hooked wrapper back to original
        if (this.hookedField != null) {
            Class<?> serverClazz = MinecraftUtil.nms("MinecraftServer");
            Object server = ReflectionUtil.invokeStatic(serverClazz, "getServer");

            HookedListWrapper<?> hookedListWrapper = (HookedListWrapper<?>) this.hookedField.get(server);

            this.hookedField.set(server, hookedListWrapper.getBase());
            this.hookedField = null;
        }
    }
}
