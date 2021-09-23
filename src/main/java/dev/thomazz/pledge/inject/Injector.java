package dev.thomazz.pledge.inject;

// Simple injector interface, might add more injectors for Bukkit alternatives
public interface Injector {
    void inject() throws Exception;

    void eject() throws Exception;
}
