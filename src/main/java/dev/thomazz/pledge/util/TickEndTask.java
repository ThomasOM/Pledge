package dev.thomazz.pledge.util;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.List;

@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public final class TickEndTask {
    private static List<Object> RUNNABLES;
    private static Class<?> RUNNABLE_CLASS;

    static {
        try {
            Server server = Bukkit.getServer();
            Object mcServer = server.getClass().getMethod("getServer").invoke(server);
            Field field = ReflectionUtil.getFieldByType(mcServer.getClass().getSuperclass(), List.class);
            TickEndTask.RUNNABLES = (List<Object>) field.get(mcServer);
            TickEndTask.RUNNABLE_CLASS = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        } catch (Exception ex) {
            throw new RuntimeException("Could not set up tick end runnable!", ex);
        }
    }

    private final Runnable runnable;
    private volatile Object registeredObject;

    public TickEndTask start() {
        if (this.registeredObject != null) {
            throw new IllegalStateException("Already registered!");
        }

        if (!Runnable.class.isAssignableFrom(TickEndTask.RUNNABLE_CLASS)) {
            this.registeredObject = Proxy.newProxyInstance(
                TickEndTask.RUNNABLE_CLASS.getClassLoader(),
                new Class[]{TickEndTask.RUNNABLE_CLASS},
                (proxy, method, args) -> {
                    this.runnable.run();
                    return null;
                }
            );
        } else {
            this.registeredObject = this.runnable;
        }

        TickEndTask.RUNNABLES.add(this.registeredObject);
        return this;
    }

    public TickEndTask cancel() {
        if (this.registeredObject == null) {
            throw new IllegalStateException("Not registered yet!");
        }

        TickEndTask.RUNNABLES.remove(this.registeredObject);
        this.registeredObject = null;
        return this;
    }
}
