package dev.thomazz.pledge.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
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

    private TickEndTask start() {
        if (this.registeredObject != null) {
            throw new IllegalStateException("Already registered!");
        }

        // Hack to add runnable to tickables
        if (!Runnable.class.isAssignableFrom(TickEndTask.RUNNABLE_CLASS)) {
            Object handle = new Object();
            this.registeredObject = Proxy.newProxyInstance(
                TickEndTask.RUNNABLE_CLASS.getClassLoader(),
                new Class[]{TickEndTask.RUNNABLE_CLASS},
                (proxy, method, args) -> {
                    Class<?> declaring = method.getDeclaringClass();
                    if (declaring.equals(Object.class)) {
                        return method.invoke(handle, args);
                    } else {
                        this.runnable.run();
                        return null;
                    }
                }
            );
        } else {
            this.registeredObject = this.runnable;
        }

        TickEndTask.RUNNABLES.add(this.registeredObject);
        return this;
    }

    public void cancel() {
        if (this.registeredObject == null) {
            throw new IllegalStateException("Not registered yet!");
        }

        TickEndTask.RUNNABLES.remove(this.registeredObject);
        this.registeredObject = null;
    }

    public static TickEndTask create(Runnable runnable) {
        return new TickEndTask(runnable).start();
    }
}
