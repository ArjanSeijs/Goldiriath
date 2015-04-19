package me.dirkjan.goldiriath.listener;

import java.util.UUID;
import me.dirkjan.goldiriath.Goldiriath;
import me.dirkjan.goldiriath.Items;
import me.dirkjan.goldiriath.Profile;
import me.dirkjan.goldiriath.Stage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerListener extends GoldiriathListener {

    public PlayerListener(Goldiriath plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        UUID uuid = player.getUniqueId();
        plugin.logger.info(entity.getUniqueId().toString());
        Location location = new Location(player.getWorld(), 1433, 117, 1100);

        if (entity.getUniqueId().toString().equals("e61d65db-e377-41d4-8020-b7d68dc57ad4")) {
            event.setCancelled(true);
            if (!plugin.questmap.containsKey(uuid)) {
                plugin.questmap.put(uuid, Stage.STAGE_A);
                player.setLevel(1);
                Profile.TORE.msg(player, "ah, you're finaly awake");
                Profile.TORE.msg(player, "you have been sleeping for three days");
                Profile.TORE.msg(player, "the night i found you a ship stranded on the beach to the north");
                Profile.TORE.msg(player, "you should go have a look after you repay me");
                Profile.TORE.msg(player, "get me 3 eggs from the chickens in the back and we'll call it even");
            } else {
                if (plugin.questmap.get(uuid).equals(Stage.STAGE_A)) {
                    if (player.getInventory().contains(Material.EGG, 3)) {
                        player.getInventory().remove(Material.EGG);
                        plugin.questmap.put(uuid, Stage.STAGE_B);
                        Profile.TORE.msg(player, "thank you, now you should go look at the ship");
                        Profile.TORE.msg(player, "go out of my house and to the left");
                        Profile.TORE.msg(player, "now go before i decide to change my mind and make you work here for a month");
                        player.teleport(location);
                    } else {
                        Profile.TORE.msg(player, "go get those eggs or you're not getting out of here");
                    }
                } else {
                    if (plugin.questmap.get(uuid).equals(Stage.STAGE_B)) {
                        Profile.TORE.msg(player, "how did you even get in here");
                    }
                }
            }

        }
        if (entity.getUniqueId().toString().equals("ec64d86c-9976-43e2-ab09-e6c3cd791a2c")) {
            event.setCancelled(true);
            if (plugin.questmap.get(uuid).equals(Stage.STAGE_B)) {
                plugin.questmap.put(uuid, Stage.STAGE_C);
                Profile.CAPTAIN.msg(player, "great looks like you survived.");
                Profile.CAPTAIN.msg(20, player, "we're searching the coast for other survivors of bodies but we're finding very little");
                Profile.CAPTAIN.msg(40, player, "scouts found an old temple to the east, the treasure in there should keep us fed for a while");
                Profile.CAPTAIN.msg(60, player, "here's some equipment, you go check it out");
                player.getInventory().addItem(Items.WOODEN_SWORD.getItem());

            }
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        EntityType hit = event.getEntityType();
        Entity damager = event.getDamager();
        double damage = event.getDamage();
        if (damage < 1) {
            event.setCancelled(true);
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageEvent(EntityDamageEvent event) {
        EntityType hit = event.getEntityType();
        double damage = event.getDamage();

        if (hit.equals(EntityType.VILLAGER)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event) {
        LivingEntity killed = event.getEntity();
        if (killed instanceof Player) {
            return;
        }
        if (killed.getCustomName() != null && killed.getCustomName().equals("zombie")) {
            event.getDrops().removeAll(event.getDrops());
            event.getDrops().add(Items.WOODEN_SWORD.getItem());
        }

    }

    @Override
    public void unregister() {
        PlayerInteractEntityEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
        EntityDamageEvent.getHandlerList().unregister(this);
        EntityDeathEvent.getHandlerList().unregister(this);
    }

}
