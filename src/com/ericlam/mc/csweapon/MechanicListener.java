package com.ericlam.mc.csweapon;


import com.ericlam.mc.csweapon.api.KnockBackManager;
import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;
import com.shampaggon.crackshot.events.WeaponExplodeEvent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class MechanicListener implements Listener, KnockBackManager {
    private Set<OfflinePlayer> customKBDisabled = new HashSet<>();

    @EventHandler
    public void onFlashbang(WeaponExplodeEvent e) {
        if (!ConfigManager.getFlashbangs().contains(e.getWeaponTitle())) return;
        Location flash = e.getLocation();
        Collection<Player> nearbyPlayers = flash.getNearbyPlayers(ConfigManager.flash_radius);
        playerloop:
        for (Player player : nearbyPlayers) {
            Location loc = player.getEyeLocation();
            Vector vec = new Vector(loc.getX() - flash.getX(), loc.getY() - flash.getY(), loc.getZ() - flash.getZ());
            double dot = loc.getDirection().dot(vec);
            BlockIterator iterator = new BlockIterator(player.getWorld(), flash.toVector(), vec, 0, (int) flash.distance(loc));
            while (iterator.hasNext()) {
                Material current = iterator.next().getType();
                if (current != Material.AIR && !ConfigManager.getFlashBypass().contains(current.toString()))
                    continue playerloop;
            }
            if (dot < 0) {
                if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) continue;
                if (player.hasPotionEffect(PotionEffectType.BLINDNESS))
                    player.removePotionEffect(PotionEffectType.BLINDNESS);
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 120, 255));
                player.spawnParticle(Particle.CLOUD, 30, 30, 30, 30);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKnockback(WeaponDamageEntityEvent e) {
        if (e.isCancelled()) return;
        if (!ConfigManager.noKnockBack) return;
        Entity damager = e.getDamager();
        Entity damagee = e.getVictim();
        if (!(damagee instanceof Player)) return;
        Player vPlayer = (Player) damagee;
        if (customKBDisabled.contains(vPlayer)) return;
        if (!(damager instanceof Projectile)) return;
        final double kb = ConfigManager.useDamagePercent ? e.getDamage() * ConfigManager.customKnockBack : ConfigManager.customKnockBack;
        this.createKnockBack(damager, damagee, kb);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!ConfigManager.noKnockBack) return;
        Optional.ofNullable(e.getPlayer().getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)).ifPresent(attr -> {

            for (AttributeModifier attrMod : attr.getModifiers()) {
                if (attrMod.getName().equalsIgnoreCase("cs_kbr")) {
                    attr.removeModifier(attrMod);
                    break;
                }
            }

            attr.addModifier(new AttributeModifier("cs_kbr", 3.0D, AttributeModifier.Operation.ADD_NUMBER));
        });
    }

    @Override
    public void createKnockBack(Entity damager, Entity victim, double value) {
        if (value == 0.0) return;
        (new KnockBackRunnable(damager, victim, value)).runTaskLater(CustomCSWeapon.getPlugin(CustomCSWeapon.class), 1L);
    }

    @Override
    public void setCustomKnockBack(Player player, boolean enable) {
        if (enable) customKBDisabled.remove(player);
        else customKBDisabled.add(player);
    }
}
