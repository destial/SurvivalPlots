package xyz.destiall.survivalplots.plot;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;
import xyz.destiall.survivalplots.events.PlotExpireEvent;
import xyz.destiall.survivalplots.events.PlotLoadEvent;
import xyz.destiall.survivalplots.hooks.ShopkeepersHook;
import xyz.destiall.survivalplots.hooks.WorldEditHook;
import xyz.destiall.survivalplots.player.PlotPlayer;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import static xyz.destiall.survivalplots.commands.PlotCommand.color;

public class SurvivalPlot {
    private final int id;
    private final String worldName;
    private final List<PlotFlags> flags;
    private final List<String> members;
    private final List<String> banned;
    private final BoundingBox bounds;
    private String description = "N/A";
    private String owner = "N/A";
    private Location center;
    private Location home;
    private Date expiryDate;
    private Timer expiry;

    SurvivalPlot(int id, BoundingBox bounds, String worldName) {
        this.id = id;
        this.bounds = bounds;
        this.worldName = worldName;

        this.flags = new ArrayList<>();
        this.flags.addAll(PlotFlags.def());
        this.members = new ArrayList<>();
        this.banned = new ArrayList<>();
    }

    SurvivalPlot(int id, ConfigurationSection section) {
        this(id, BoundingBox.of(section.getVector("corner1"), section.getVector("corner2")), section.getString("world"));

        flags.clear();
        section.getStringList("enabled-flags").forEach(s -> {
            PlotFlags flag = PlotFlags.getFlag(s);
            if (flag == null)
                return;
            flags.add(flag);
        });
        banned.addAll(section.getStringList("banned-members"));
        members.addAll(section.getStringList("trusted-members"));
        owner = section.getString("owner", "N/A");
        description = section.getString("description", "N/A");

        if (!owner.equalsIgnoreCase("N/A")) {
            if (section.getLong("expiry") == 0) {
                updateExpiry();
            } else {
                expiryDate = new Date(section.getLong("expiry"));
                scheduleExpiry();
            }
        }
    }

    public void scheduleExpiry() {
        if (expiryDate == null)
            return;

        disableExpiryTimer();

        expiry = new Timer();
        expiry.schedule(new TimerTask() {
            @Override
            public void run() {
                PlotPlayer player = getOwner();
                if (player == null)
                    return;

                if (player.isOnline()) {
                    updateExpiry();
                    SurvivalPlotsPlugin.getInst().getPlotManager().update();
                    return;
                }

                if (!new PlotExpireEvent(SurvivalPlot.this).callEvent()) {
                    updateExpiry();
                    SurvivalPlotsPlugin.getInst().info("PlotExpireEvent was cancelled, re-updating expiry date...");
                    return;
                }

                WorldEditHook.backupPlot(SurvivalPlot.this, getOwner().getName());
                Schematic def = WorldEditHook.loadPlot(SurvivalPlot.this, "default");
                if (def != null) {
                    if (!SurvivalPlot.this.loadSchematic(def)) {
                        expiryDate = new Date(expiryDate.getTime() + 1000 * 60);
                        updateExpiry();
                        SurvivalPlotsPlugin.getInst().getPlotManager().update();
                        return;
                    }
                }
                setExpiryDate(null);
                disableExpiryTimer();
                getMembers().clear();
                getBanned().clear();
                getFlags().clear();
                getFlags().addAll(PlotFlags.def());
                setOwner("N/A");
            }
        }, expiryDate);
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void disableExpiryTimer() {
        if (expiry != null)
            expiry.cancel();
        expiry = null;
    }

    public void addToConfig(YamlConfiguration config) {
        config.set("plots." + id + ".corner1", bounds.getMin());
        config.set("plots." + id + ".corner2", bounds.getMax());
        config.set("plots." + id + ".world", worldName);
        config.set("plots." + id + ".enabled-flags", flags.stream().map(PlotFlags::name).collect(Collectors.toList()));
        config.set("plots." + id + ".trusted-members", members);
        config.set("plots." + id + ".banned-members", banned);
        config.set("plots." + id + ".owner", owner);
        config.set("plots." + id + ".expiry", expiryDate != null ? expiryDate.getTime() : 0);
        config.set("plots." + id + ".description", description);
    }

    public boolean contains(Vector pos) {
        return pos.getBlockX() >= bounds.getMinX() && pos.getBlockX() <= bounds.getMaxX() &&
                pos.getBlockY() >= bounds.getMinY() && pos.getBlockY() <= bounds.getMaxY() &&
                pos.getBlockZ() >= bounds.getMinZ() && pos.getBlockZ() <= bounds.getMaxZ();
    }

    public boolean contains(Location location) {
        return location.getWorld() == getWorld() && contains(location.toVector());
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public Location getMin() {
        return new Location(getWorld(), getBounds().getMinX(), getBounds().getMinY(), getBounds().getMinZ());
    }

    public Location getMax() {
        return new Location(getWorld(), getBounds().getMaxX(), getBounds().getMaxY(), getBounds().getMaxZ());
    }

    public int getId() {
        return id;
    }

    public Collection<PlotFlags> getFlags() {
        return flags;
    }

    public boolean hasFlag(PlotFlags flag) {
        return flags.contains(flag);
    }

    public boolean addFlag(PlotFlags flag) {
        if (flags.contains(flag))
            return false;
        flags.add(flag);
        SurvivalPlotsPlugin.getInst().getPlotManager().update();
        return true;
    }

    public boolean removeFlag(PlotFlags flag) {
        if (flags.remove(flag)) {
            if (flag == PlotFlags.MEMBER_FLY) {
                getPlayersInPlot().forEach(player -> {
                    if (player.isMember(this)) {
                        player.getOnlinePlayer().sendMessage(Messages.Key.NO_PERMS_ON_PLOT.get(player.getOnlinePlayer(), this));
                        Player p = player.getOnlinePlayer();
                        if (p.getAllowFlight()) {
                            p.setFlying(false);
                            p.setAllowFlight(false);
                        } else {
                            p.setAllowFlight(true);
                            p.setFlying(true);
                        }
                    }
                });
            }
            SurvivalPlotsPlugin.getInst().getPlotManager().update();
            return true;
        }
        return false;
    }

    public List<String> getMembers() {
        return members;
    }

    public List<PlotPlayer> getPlayersInPlot() {
        return getWorld().getNearbyEntities(bounds, f -> f instanceof Player).stream()
                .map(p -> SurvivalPlotsPlugin.getInst().getPlotPlayerManager().getPlotPlayer((Player) p))
                .collect(Collectors.toList());
    }

    public boolean trust(Player player) {
        if (members.stream().anyMatch(m -> m.equalsIgnoreCase(player.getName())))
            return false;

        if (player.getName().equalsIgnoreCase(getOwner().getName()))
            return false;

        members.add(player.getName());
        SurvivalPlotsPlugin.getInst().getPlotManager().update();
        return true;
    }

    public boolean untrust(String name) {
        if (members.stream().noneMatch(m -> m.equalsIgnoreCase(name)))
            return false;

        members.removeIf(m -> m.equalsIgnoreCase(name));
        SurvivalPlotsPlugin.getInst().getPlotManager().update();
        return true;
    }

    public List<String> getBanned() {
        return banned;
    }

    public boolean ban(Player player) {
        if (banned.stream().anyMatch(b -> b.equalsIgnoreCase(player.getName())))
            return false;

        if (player.getName().equalsIgnoreCase(getOwner().getName()))
            return false;

        if (members.stream().anyMatch(m -> m.equalsIgnoreCase(player.getName())))
            return false;

        banned.add(player.getName());
        SurvivalPlotsPlugin.getInst().getPlotManager().update();
        if (contains(player.getLocation())) {
            player.teleport(getHome());
            player.sendMessage(color("&cYou have been banned from this plot!"));
        }
        return true;
    }

    public boolean unban(String name) {
        if (banned.stream().noneMatch(b -> b.equalsIgnoreCase(name)))
            return false;

        banned.removeIf(b -> b.equalsIgnoreCase(name));
        SurvivalPlotsPlugin.getInst().getPlotManager().update();
        return true;
    }

    public String getDescription() {
        return description;
    }

    public PlotPlayer getOwner() {
        if (owner.equals("N/A"))
            return null;

        return SurvivalPlotsPlugin.getInst().getPlotPlayerManager().getPlotPlayer(owner);
    }

    public String getRawOwner() {
        return owner;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public void updateExpiry() {
        if (owner.equalsIgnoreCase("N/A")) {
            expiryDate = null;
            if (expiry != null)
                expiry.cancel();
            expiry = null;
            return;
        }
        Duration expiryLength = SurvivalPlotsPlugin.getDuration(SurvivalPlotsPlugin.getInst().getConfig().getString("plot-expiry", "30d"));
        expiryDate = Date.from(Instant.now().plus(expiryLength));
        scheduleExpiry();
    }

    public void setOwner(String owner) {
        this.owner = owner;

        if (owner.equalsIgnoreCase("N/A")) {
            description = "N/A";
        }
        updateExpiry();
        SurvivalPlotsPlugin.getInst().getPlotManager().update();
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Location getCenter() {
        boolean tried = false;
        if (center == null) {
            center = new Location(getWorld(), bounds.getCenterX(), bounds.getMaxY(), bounds.getCenterZ());
            if (!center.getChunk().isLoaded()) {
                center.getChunk().load();
            }
            RayTraceResult result = getWorld().rayTraceBlocks(center, new Vector(0, -1, 0), bounds.getMaxY() - bounds.getMinY(), FluidCollisionMode.ALWAYS, true);
            if (result == null || result.getHitBlock() == null)
                return center;

            Block block = result.getHitBlock();
            center.set(block.getX(), block.getY(), block.getZ());
            center.add(0, 1, 0);
            tried = true;
        }
        if (!tried && center.getBlock().getType() != Material.AIR) {
            center = null;
            return getCenter();
        }
        return center;
    }

    public Location getHome() {
        boolean tried = false;
        if (home == null) {
            home = new Location(getWorld(), bounds.getCenterX(), bounds.getMaxY(), bounds.getMaxZ() + 2);
            if (!home.getChunk().isLoaded()) {
                home.getChunk().load();
            }
            RayTraceResult result = getWorld().rayTraceBlocks(home, new Vector(0, -1, 0), bounds.getMaxY() - bounds.getMinY(), FluidCollisionMode.ALWAYS, true);
            if (result == null || result.getHitBlock() == null)
                return home;

            Block block = result.getHitBlock();
            home.set(block.getX(), block.getY(), block.getZ());
            home.add(0, 1, 0);
            home.setDirection(new Vector(0, 0, -1));
            tried = true;
        }
        if (!tried && home.getBlock().getType() != Material.AIR) {
            home = null;
            return getHome();
        }
        return home;

    }

    /// Source: PlotSquared v6
    public boolean loadSchematic(Schematic schematic) {
        Clipboard clipboard = schematic.getClipboard();
        if (clipboard == null && schematic.getAsyncClipboard() == null)
            return false;

        if (clipboard == null) {
            try {
                return schematic.getAsyncClipboard().thenApply(clip -> loadSchematic(schematic)).get();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        if (!new PlotLoadEvent(this, schematic).callEvent()) {
            SurvivalPlotsPlugin.getInst().info("PlotLoadEvent was cancelled, skipped loading schematic...");
            return true;
        }

        getCenter().getWorld().getNearbyEntities(bounds).forEach(en -> {
            if (!(en instanceof Player)) {
                en.remove();

                ShopkeepersHook.removeEntity(en);
            }
        });

        final BlockVector3 dimension = clipboard.getDimensions();
        final int WIDTH = dimension.getX();
        final int LENGTH = dimension.getZ();
        final int HEIGHT = dimension.getY();

        CuboidRegion region = WorldEditHook.adapt(getWorld(), getBounds());
        boolean sizeMismatch =
                ((region.getMaximumPoint().getX() - region.getMinimumPoint().getX()) < WIDTH) ||
                ((region.getMaximumPoint().getY() - region.getMinimumPoint().getY()) < HEIGHT) ||
                ((region.getMaximumPoint().getZ() - region.getMinimumPoint().getZ()) < LENGTH);
        if (sizeMismatch) {
            boolean ignore = SurvivalPlotsPlugin.getInst().getConfig().getBoolean("ignore-size-mismatch", true);
            SurvivalPlotsPlugin.getInst()
                    .warning("Schematic size mismatch!")
                    .warning("Region X:" + (region.getMaximumPoint().getX() - region.getMinimumPoint().getX()) + " Dimension X:" + (WIDTH))
                    .warning("Region Y:" + (region.getMaximumPoint().getY() - region.getMinimumPoint().getY()) + " Dimension Y:" + (HEIGHT))
                    .warning("Region Z:" + (region.getMaximumPoint().getZ() - region.getMinimumPoint().getZ()) + " Dimension Z:" + (LENGTH))
                    .warning(ignore ? "Ignoring warning due to config..." : "Skipping...");

            if (!ignore)
                return false;
        }

        final int p1x = region.getMinimumPoint().getX();
        final int p1y = region.getMinimumPoint().getY();
        final int p1z = region.getMinimumPoint().getZ();

        for (int ry = 0; ry < HEIGHT; ry++) {
            final int finalry = ry;
            SurvivalPlotsPlugin.getInst().getScheduler().runTask(() -> {
                int yy = p1y + finalry;
                for (int rz = 0; rz < LENGTH; rz++) {
                    for (int rx = 0; rx < WIDTH; rx++) {
                        int xx = p1x + rx;
                        int zz = p1z + rz;
                        BaseBlock id = clipboard.getFullBlock(BlockVector3.at(rx, finalry, rz));
                        Location plotLoc = new Location(getWorld(), xx, yy, zz);
                        plotLoc.getBlock().setBlockData(BukkitAdapter.adapt(id));
                    }
                }
            }, getCenter());
        }

        return true;
    }
}
