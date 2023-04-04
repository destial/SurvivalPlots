package xyz.destiall.survivalplots.listeners;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import xyz.destiall.survivalplots.Messages;
import xyz.destiall.survivalplots.SurvivalPlotsPlugin;
import xyz.destiall.survivalplots.player.PlotPlayer;
import xyz.destiall.survivalplots.player.PlotPlayerManager;
import xyz.destiall.survivalplots.plot.PlotFlags;
import xyz.destiall.survivalplots.plot.PlotManager;
import xyz.destiall.survivalplots.plot.SurvivalPlot;

import static xyz.destiall.survivalplots.commands.PlotCommand.color;

public class PlotPlayerListener implements Listener {
    private final SurvivalPlotsPlugin plugin;

    public PlotPlayerListener(SurvivalPlotsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        PlotManager pm = plugin.getPlotManager();
        if (!e.getPlayer().hasPermission("svplots.user.fly.bypass") && e.getPlayer().getAllowFlight()) {
            e.getPlayer().sendMessage(color("&cYour flight has been disabled because you left the game!"));
            e.getPlayer().setAllowFlight(false);
            e.getPlayer().setFlying(false);
        }
        pm.getOwnedPlots(e.getPlayer()).forEach(SurvivalPlot::updateExpiry);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e) {
        PlotManager pm = plugin.getPlotManager();
        pm.getOwnedPlots(e.getPlayer()).forEach(SurvivalPlot::updateExpiry);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        PlotManager pm = plugin.getPlotManager();
        PlotPlayerManager ppm = plugin.getPlotPlayerManager();
        PlotPlayer player = ppm.getPlotPlayer(e.getPlayer());
        SurvivalPlot plot = pm.getPlotAt(e.getTo());
        SurvivalPlot before = pm.getPlotAt(e.getFrom());
        if (before != null && before != plot && (before.getOwner() == player || (before.hasFlag(PlotFlags.MEMBER_FLY) && player.isMember(before)))) {
            if (!e.getPlayer().hasPermission("svplots.user.fly.bypass") && e.getPlayer().getAllowFlight()) {
                e.getPlayer().sendMessage(color("&cYour flight has been disabled because you left the plot!"));
                e.getPlayer().setAllowFlight(false);
                e.getPlayer().setFlying(false);
            }
        }

        if (plot == null)
            return;

        if (player.isBanned(plot)) {
            e.setCancelled(true);
            BaseComponent[] component = TextComponent.fromLegacyText(Messages.Key.BANNED_FROM_PLOT_TITLE.get(e.getPlayer(), plot));
            BaseComponent[] component2 = TextComponent.fromLegacyText(Messages.Key.BANNED_FROM_PLOT_SUBTITLE.get(e.getPlayer(), plot));
            e.getPlayer().showTitle(component, component2, 5, 20, 5);
            return;
        }

        if (pm.getPlotAt(e.getFrom()) != plot) {
            if (plot.getOwner() != null) {
                BaseComponent[] component = TextComponent.fromLegacyText(Messages.Key.ENTER_OWNED_PLOT_TITLE.get(e.getPlayer(), plot));
                BaseComponent[] component2 = TextComponent.fromLegacyText(Messages.Key.ENTER_OWNED_PLOT_SUBTITLE.get(e.getPlayer(), plot));
                e.getPlayer().showTitle(component, component2, 5, 20, 5);

                if (plot.hasFlag(PlotFlags.SHOW_DESCRIPTION_ENTER)) {
                    e.getPlayer().sendMessage(color("&6" + plot.getDescription()));
                }
                return;
            }

            BaseComponent[] component = TextComponent.fromLegacyText(Messages.Key.ENTER_AVAILABLE_PLOT_TITLE.get(e.getPlayer(), plot));
            BaseComponent[] component2 = TextComponent.fromLegacyText(Messages.Key.ENTER_AVAILABLE_PLOT_SUBTITLE.get(e.getPlayer(), plot));
            e.getPlayer().showTitle(component, component2, 5, 20, 5);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        onPlayerMove(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        PlotManager pm = plugin.getPlotManager();
        SurvivalPlot plot = pm.getPlotAt(e.getRightClicked().getLocation());
        if (plot == null)
            return;

        PlotPlayerManager ppm = plugin.getPlotPlayerManager();
        PlotPlayer player = ppm.getPlotPlayer(e.getPlayer());
        if (!player.canInteractEntity(plot)) {
            e.getPlayer().sendMessage(Messages.Key.NO_INTERACT.get(e.getPlayer(), plot));
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        onPlayerInteractEntity(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        PlotManager pm = plugin.getPlotManager();

        SurvivalPlot plot = pm.getPlotAt(e.getEntity().getLocation());
        if (plot == null)
            return;

        if (e.getEntity() instanceof Player) {
            Entity damager = e.getDamager();
            boolean damageSourceIsPlayer = damager instanceof Player;
            if (damager instanceof Projectile) {
                Projectile projectile = (Projectile) damager;
                damageSourceIsPlayer = projectile.getShooter() instanceof Player;
            }

            if (damageSourceIsPlayer && !plot.hasFlag(PlotFlags.PVP_ON)) {
                e.setCancelled(true);
                e.setDamage(0);
                if (e.getDamager() instanceof Player) {
                    e.getEntity().sendMessage(Messages.Key.NO_PVP.get((Player) e.getDamager(), plot));
                }
                return;
            }
        }

        if (e.getEntity() instanceof Animals) {
            if (plot.hasFlag(PlotFlags.ANIMALS_INVINCIBLE)) {
                e.setCancelled(true);
                e.setDamage(0);
                if (e.getDamager() instanceof Player) {
                    e.getEntity().sendMessage(Messages.Key.NO_INTERACT.get((Player) e.getDamager(), plot));
                }
                return;
            }
        }

        if (e.getDamager() instanceof Player) {
            PlotPlayer player = plugin.getPlotPlayerManager().getPlotPlayer(e.getDamager().getName());
            if (!player.canInteractEntity(plot)) {
                e.setCancelled(true);
                e.setDamage(0);
                e.getEntity().sendMessage(Messages.Key.NO_INTERACT.get((Player) e.getDamager(), plot));
                return;
            }
        }

        if (e.getDamager() instanceof TNTPrimed || e.getDamager() instanceof ExplosiveMinecart) {
            if (!plot.hasFlag(PlotFlags.EXPLOSIONS_ON) || plot != pm.getPlotAt(e.getDamager().getLocation())) {
                e.setCancelled(true);
                e.setDamage(0);
            }
        }
    }
}
