package me.dirkjan.goldiriath.mobspawn;

import me.dirkjan.goldiriath.util.ConfigLoadable;
import me.dirkjan.goldiriath.util.Util;
import me.dirkjan.goldiriath.util.Validatable;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class MobSpawnProfile implements ConfigLoadable, Validatable {

    private final String id;
    //
    private EntityType type;
    private String customName;
    private long spawnThreshold;
    private ItemStack carryItem;
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;

    public MobSpawnProfile(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return id;
    }

    public EntityType getType() {
        return type;
    }

    public boolean hasSpawnThreshold() {
        return spawnThreshold >= 0;
    }

    public long getSpawnThreshold() {
        return spawnThreshold;
    }

    public String getCustomName() {
        return customName;
    }

    public ItemStack getCarryItem() {
        return carryItem;
    }

    public ItemStack getHelmet() {
        return helmet;
    }

    public ItemStack getChestplate() {
        return chestplate;
    }

    public ItemStack getLeggings() {
        return leggings;
    }

    public ItemStack getBoots() {
        return boots;
    }

    @Override
    public void loadFrom(ConfigurationSection config) {

        final String entityTypeName = config.getString("type", null);
        if (entityTypeName == null) {
            type = null;
        } else {
            type = EntityType.fromName(entityTypeName);
        }

        customName = config.getString("name", null);
        spawnThreshold = config.getInt("spawn_threshold", -1);

        carryItem = Util.parseItem(config.getString("item", null));

        helmet = Util.parseItem(config.getString("helmet", null));
        chestplate = Util.parseItem(config.getString("chestplate", null));
        leggings = Util.parseItem(config.getString("leggings", null));
        boots = Util.parseItem(config.getString("boots", null));

        if (customName != null) {
            customName = ChatColor.translateAlternateColorCodes('&', customName);
        }
    }

    public LivingEntity spawn(Location location) {
        if (!isValid()) {
            return null;
        }

        final LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, type);

        final EntityEquipment equipment = entity.getEquipment();

        entity.setCanPickupItems(false);
        equipment.setItemInHandDropChance(0);
        equipment.setHelmetDropChance(0);
        equipment.setChestplateDropChance(0);
        equipment.setLeggingsDropChance(0);
        equipment.setBootsDropChance(0);

        if (customName != null) {
            entity.setCustomName(customName);
        }

        if (carryItem != null) {
            equipment.setItemInHand(carryItem);
        }

        if (helmet != null) {
            equipment.setHelmet(helmet);
        }

        if (chestplate != null) {
            equipment.setChestplate(chestplate);
        }

        if (leggings != null) {
            equipment.setLeggings(leggings);
        }

        if (boots != null) {
            equipment.setBoots(boots);
        }

        return entity;
    }

    @Override
    public boolean isValid() {
        return id != null
                && type != null
                && type.isAlive()
                && type.isSpawnable();
    }

}
