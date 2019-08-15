package com.ericlam.mc.csweapon;

import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

public class KnockBackRunnable extends BukkitRunnable {
    private org.bukkit.entity.Entity Player;
    private Entity Entity;
    private double kb;

    KnockBackRunnable(Entity player, Entity ent, double kb2) {
        this.Player = player;
        this.Entity = ent;
        this.kb = kb2;
    }

    public void run() {
        if (this.Entity != null && this.Player != null) {
            this.Entity.setVelocity(this.Player.getVelocity().multiply(this.kb).setY(0));
        }
    }
}
