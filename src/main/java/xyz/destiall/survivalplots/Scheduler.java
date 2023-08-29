package xyz.destiall.survivalplots;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/// Utility class for wrapping the scheduler to add support for Folia
public final class Scheduler {
    private final Plugin plugin;
    private boolean folia = false;
    private final List<Task> foliaTasks;
    private boolean isCancellingAll = false;

    public Scheduler(Plugin plugin) {
        this.plugin = plugin;
        this.foliaTasks = new CopyOnWriteArrayList<>();

        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
            folia = true;
            plugin.getLogger().info("Detected Folia implementation! Using Folia's regional scheduler...");
        } catch (ClassNotFoundException ignored) {}
    }

    /**
     * Get the owned plugin of this Scheduler
     * @return The owned plugin
     */
    public Plugin getOwningPlugin() {
        return plugin;
    }

    /**
     * Cancel all tasks owned by this scheduler's owning plugin.
     */
    public void cancelTasks() {
        if (isFolia()) {
            isCancellingAll = true;
            List<Task> copy = new ArrayList<>(foliaTasks);
            copy.forEach(Task::cancel);
            foliaTasks.clear();
            isCancellingAll = false;
            return;
        }

        plugin.getServer().getScheduler().cancelTasks(plugin);
    }

    /**
     * Schedules a task to be executed on the global region scheduler on the next tick.
     * @param runnable The task to execute
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTask(Runnable runnable) {
        if (isFolia()) {
            if (isCancellingAll)
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getGlobalRegionScheduler().run(plugin, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }));
        }

        return new Task(this, plugin.getServer().getScheduler().runTask(plugin, runnable));
    }

    /**
     * Schedules an async task to be executed on the global region scheduler on the next tick.
     * @param runnable The task to execute
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskAsync(Runnable runnable) {
        if (isFolia()) {
            if (isCancellingAll)
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getGlobalRegionScheduler().run(plugin, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    /**
     * Schedules a task to be executed on the region which owns the location on the next tick.
     * @param runnable The task to execute
     * @param location The region to execute the task in
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTask(Runnable runnable, Location location) {
        if (isFolia()) {
            if (isCancellingAll)
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getRegionScheduler().run(plugin, location, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }));
        }

        return new Task(this, plugin.getServer().getScheduler().runTask(plugin, runnable));
    }

    /**
     * Schedules an async task to be executed on the region which owns the location on the next tick.
     * @param runnable The task to execute
     * @param location The region to execute the task in
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskAsync(Runnable runnable, Location location) {
        if (isFolia()) {
            if (isCancellingAll)
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getRegionScheduler().run(plugin, location, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    /**
     * Schedules a task to be executed on the region which owns the entity on the next tick.
     * @param runnable The task to execute
     * @param entity The entity to execute the task from
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTask(Runnable runnable, Entity entity) {
        if (isFolia()) {
            if (isCancellingAll)
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, entity.getScheduler().run(plugin, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }, null));
        }

        return new Task(this, plugin.getServer().getScheduler().runTask(plugin, runnable));
    }

    /**
     * Schedules an async task to be executed on the region which owns the entity on the next tick.
     * @param runnable The task to execute
     * @param entity The entity to execute the task from
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskAsync(Runnable runnable, Entity entity) {
        if (isFolia()) {
            if (isCancellingAll)
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, entity.getScheduler().run(plugin, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }, null));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    /**
     * Schedules a repeated task to be executed on the global region scheduler.
     * @param runnable The task to execute
     * @param delay The delay in ticks from task initialization
     * @param period The period in ticks to execute the task
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskTimer(Runnable runnable, long delay, long period) {
        if (isFolia()) {
            if (isCancellingAll)
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> runnable.run(), delay, period));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period));
    }

    /**
     * Schedules an async repeated task to be executed on the global region scheduler.
     * @param runnable The task to execute
     * @param delay The delay in ticks from task initialization
     * @param period The period in ticks to execute the task
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskTimerAsync(Runnable runnable, long delay, long period) {
        if (isFolia()) {
            if (isCancellingAll)
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> runnable.run(), delay, period));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period));
    }

    /**
     * Schedules a repeated task to be executed on the region which owns the location.
     * @param runnable The task to execute
     * @param location The region to execute the task in
     * @param delay The delay in ticks from task initalization
     * @param period The period in ticks to execute the task
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskTimer(Runnable runnable, Location location, long delay, long period) {
        if (isFolia()) {
            if (isCancellingAll)
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getRegionScheduler().runAtFixedRate(plugin, location, task -> runnable.run(), delay, period));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period));
    }

    /**
     * Schedules an async repeated task to be executed on the region which owns the location.
     * @param runnable The task to execute
     * @param location The region to execute the task in
     * @param delay The delay in ticks from task initalization
     * @param period The period in ticks to execute the task
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskTimerAsync(Runnable runnable, Location location, long delay, long period) {
        if (isFolia()) {
            if (isCancellingAll)
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getRegionScheduler().runAtFixedRate(plugin, location, task -> runnable.run(), delay, period));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period));
    }

    /**
     * Schedules a repeated task to be executed on the region which owns the entity.
     * @param runnable The task to execute
     * @param entity The entity to execute the task from
     * @param delay The delay in ticks from task initialization
     * @param period The period in ticks to execute the task
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskTimer(Runnable runnable, Entity entity, long delay, long period) {
        if (isFolia()) {
            if (isCancellingAll)
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, entity.getScheduler().runAtFixedRate(plugin, task -> runnable.run(), null, delay, period));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period));
    }

    /**
     * Schedules an async repeated task to be executed on the region which owns the entity.
     * @param runnable The task to execute
     * @param entity The entity to execute the task from
     * @param delay The delay in ticks from task initialization
     * @param period The period in ticks to execute the task
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskTimerAsync(Runnable runnable, Entity entity, long delay, long period) {
        if (isFolia()) {
            if (isCancellingAll)
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, entity.getScheduler().runAtFixedRate(plugin, task -> runnable.run(), null, delay, period));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period));
    }

    /**
     * Schedules a delayed task to be executed on the global region scheduler.
     * @param runnable The task to execute
     * @param delay The delay in ticks from task initialization
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskLater(Runnable runnable, long delay) {
        if (isFolia()) {
            if (isCancellingAll)
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }, delay));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay));
    }

    /**
     * Schedules an async delayed task to be executed on the global region scheduler.
     * @param runnable The task to execute
     * @param delay The delay in ticks from task initialization
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskLaterAsync(Runnable runnable, long delay) {
        if (isFolia()) {
            if (isCancellingAll)
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }, delay));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay));
    }

    /**
     * Schedules a delayed task to be executed on the region which owns the location.
     * @param runnable The task to execute
     * @param location The region to execute the task in
     * @param delay The delay in ticks from task initialization
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskLater(Runnable runnable, Location location, long delay) {
        if (isFolia()) {
            if (isCancellingAll)
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getRegionScheduler().runDelayed(plugin, location, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }, delay));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay));
    }

    /**
     * Schedules an async delayed task to be executed on the region which owns the location.
     * @param runnable The task to execute
     * @param location The region to execute the task in
     * @param delay The delay in ticks from task initialization
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskLaterAsync(Runnable runnable, Location location, long delay) {
        if (isFolia()) {
            if (isCancellingAll)
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getRegionScheduler().runDelayed(plugin, location, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }, delay));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay));
    }

    /**
     * Schedules a delayed task to be executed on the region which owns the entity.
     * @param runnable The task to execute
     * @param entity The entity to execute the task from
     * @param delay The delay in ticks from task initialization
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskLater(Runnable runnable, Entity entity, long delay) {
        if (isFolia()) {
            if (isCancellingAll)
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, entity.getScheduler().runDelayed(plugin, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }, null, delay));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay));
    }

    /**
     * Schedules an async delayed task to be executed on the region which owns the entity.
     * @param runnable The task to execute
     * @param entity The entity to execute the task from
     * @param delay The delay in ticks from task initialization
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskLaterAsync(Runnable runnable, Entity entity, long delay) {
        if (isFolia()) {
            if (isCancellingAll)
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, entity.getScheduler().runDelayed(plugin, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }, null, delay));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay));
    }

    /**
     * If the current server implementation is running Folia
     * @return true if Folia is underneath
     */
    public boolean isFolia() {
        return folia;
    }

    private static final class Task {
        private final Scheduler owningScheduler;
        private BukkitTask bukkitTask;
        private ScheduledTask foliaTask;

        private Task(Scheduler owningScheduler, BukkitTask bukkitTask) {
            this.owningScheduler = owningScheduler;
            this.bukkitTask = bukkitTask;
        }

        private Task(Scheduler owningScheduler, ScheduledTask foliaTask) {
            this.owningScheduler = owningScheduler;
            this.foliaTask = foliaTask;

            owningScheduler.foliaTasks.add(this);
        }

        /**
         * Get the Scheduler that scheduled this task
         * @return The scheduler that scheduled this task
         */
        public Scheduler getScheduler() {
            return owningScheduler;
        }

        /**
         * If this task is being executed as Folia.
         * @return true if Folia is underneath
         */
        public boolean isFolia() {
            return foliaTask != null;
        }

        /**
         * If this task is being run asynchronously.
         * @return true by default if on Folia.
         */
        public boolean isAsync() {
            if (isFolia()) {
                return true;
            }

            return !bukkitTask.isSync();
        }

        /**
         * Get the owned plugin that executed this task
         * @return The owned plugin
         */
        public Plugin getOwnedPlugin() {
            if (isFolia()) {
                return foliaTask.getOwningPlugin();
            }

            return bukkitTask.getOwner();
        }

        /**
         * Cancel this task from further execution. If this task is a timer or delayed task, it will halt.
         */
        public void cancel() {
            if (isFolia()) {
                foliaTask.cancel();
                this.owningScheduler.foliaTasks.remove(this);
                return;
            }

            bukkitTask.cancel();
        }

        /**
         * If this task has been cancelled
         * @return Cancellation state of the task
         */
        public boolean isCancelled() {
            if (isFolia()) {
                return foliaTask.isCancelled();
            }

            return bukkitTask.isCancelled();
        }
    }
}
