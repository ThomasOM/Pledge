package dev.thomazz.pledge;

import dev.thomazz.pledge.api.Direction;
import dev.thomazz.pledge.api.Pledge;
import dev.thomazz.pledge.api.event.TransactionListener;
import dev.thomazz.pledge.inject.InjectListener;
import dev.thomazz.pledge.inject.ServerInjector;
import dev.thomazz.pledge.inject.Injector;
import dev.thomazz.pledge.transaction.TransactionManager;
import dev.thomazz.pledge.transaction.recycle.TransactionRecycler;
import dev.thomazz.pledge.util.PacketUtil;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class PledgeImpl implements Pledge {
    public static PledgeImpl INSTANCE;
    public static final Logger LOGGER = Logger.getLogger(Pledge.class.getSimpleName());

    private final Injector injector;
    private final TransactionManager transactionManager;
    private final TransactionRecycler recycler;

    private boolean events;
    private boolean running;

    public PledgeImpl() {
        // Load cached values
        PacketUtil.wakeUp();

        this.injector = new ServerInjector();
        this.transactionManager = new TransactionManager();
        this.recycler = new TransactionRecycler();
    }

    @Override
    public PledgeImpl direction(Direction direction) {
        this.validateRunState("set direction");
        this.transactionManager.setDirection(direction);
        return this;
    }

    @Override
    public PledgeImpl range(int min, int max) {
        this.validateRunState("set range");

        // Should always use negative ids to prevent conflicts
        if (max >= 0) {
            throw new IllegalArgumentException("Range needs to be negative!");
        }

        // Quickly verify if range is valid
        if (min >= max) {
            throw new IllegalArgumentException("Min range boundary can not be higher than or equal to max!");
        }

        this.transactionManager.setRange(min, max);
        return this;
    }

    @Override
    public PledgeImpl events(boolean value) {
        this.validateRunState("set events");
        this.events = value;
        return this;
    }

    @Override
    public void start(JavaPlugin plugin) {
        this.validateRunState("start");

        // Register injection listener
        Bukkit.getPluginManager().registerEvents(new InjectListener(), plugin);

        // Inject into server
        try {
            this.injector.inject();
        } catch (Exception e) {
            PledgeImpl.LOGGER.severe("Exception encountered when trying to inject!");
            e.printStackTrace();
        }

        // Inject into currently online players (for reloads)
        Bukkit.getOnlinePlayers().forEach(player -> PledgeImpl.INSTANCE.getTransactionManager().createTransactionHandler(player));

        this.running = true;
        this.transactionManager.start(plugin);
    }

    @Override
    public void destroy() {
        try {
            this.injector.eject();
            // Remove handlers (for reloads)
            Bukkit.getOnlinePlayers().forEach(this.transactionManager::removeTransactionHandler);
        } catch (Exception e) {
            PledgeImpl.LOGGER.severe("Exception encountered when trying to eject!");
            e.printStackTrace();
        }
    }

    @Override
    public void addListener(TransactionListener listener) {
        this.transactionManager.addListener(listener);
    }

    @Override
    public void removeListener(TransactionListener listener) {
        this.transactionManager.removeListener(listener);
    }

    private void validateRunState(String action) throws IllegalStateException {
        if (this.running) {
            throw new IllegalStateException("Can not " + action + " while already running!");
        }
    }

    public TransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    public TransactionRecycler getRecycler() {
        return this.recycler;
    }

    public boolean hasEvents() {
        return this.events;
    }
}
