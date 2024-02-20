package com.gmail.theminiluca.addon.luca;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import com.gmail.theminiluca.addon.luca.effects.team.TeamInfo;
import io.github.theminiluca.api.LucaAPI;
import io.github.theminiluca.api.messages.BaseUser;
import io.github.theminiluca.api.utils.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class LucaSK extends JavaPlugin implements Listener {

    static LucaSK instance;
    SkriptAddon addon;



    public static Map<String, Inventory> chests = new HashMap<>();
    public static Map<UUID, Map<UUID, TeamInfo>> teamColor = new HashMap<>();

    public static final ConfigManager.Option AUTO_SAVE = new ConfigManager.Option("auto-save", Integer.class);
    public static final ConfigManager.Option UPDATE_TEAM_COLOR = new ConfigManager.Option("update-team-color", Integer.class);

    @Override
    public void onEnable() {
        // All you have to do is adding the following two lines in your onEnable method.
        // You can find the plugin ids of your plugins on the page https://bstats.org/what-is-my-plugin-id
        int pluginId = 21064; // <-- Replace with the id of your plugin!
        new Metrics(this, pluginId);

        instance = this;
        new LucaAPI() {
            @Override
            public BaseUser getBaseUser(UUID uuid) {
                return null;
            }
        }.onEnable(this);

        addon = Skript.registerAddon(this);
        new ConfigManager(this);
        try {
            //This will register all our syntax for us. Explained below
            addon.loadClasses("com.gmail.theminiluca.addon.luca", "effects");
        } catch (IOException e) {
            e.printStackTrace();
        }
        load();
        Bukkit.getLogger().info("has been enabled!");
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            new Thread(LucaSK::upload).start();
        }, (ConfigManager.instance.getInt(AUTO_SAVE) * 20L), (ConfigManager.instance.getInt(AUTO_SAVE)) * 20L);
    }


    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof Horse horse) {
            AttributeInstance attribute = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            if (attribute == null) return;
            attribute.setBaseValue(attribute.getBaseValue() * 1.6d);
        }
    }
    public static void upload() {
        long ms = System.currentTimeMillis();
        getInstance().getLogger().log(Level.INFO, "데이터 저장중...");
        for (Map.Entry<String, Inventory> entry : chests.entrySet()) {
            File file = new File(getInstance().getDataFolder() + "\\storage", entry.getKey() + "-storage.yml");
            YamlConfiguration yaml;

            if (file.exists()) {
                yaml = YamlConfiguration.loadConfiguration(file);
            } else {
                yaml = new YamlConfiguration();
            }
            yaml.set("static-id", entry.getKey());
            yaml.set("static-size", entry.getValue().getSize());
            int i = 0;
            for (ItemStack is : entry.getValue().getContents()) {
                yaml.set("contents." + i++, is);
            }
            try {
                yaml.save(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        getInstance().getLogger().log(Level.INFO, "데이터 저장완료! ( " + (System.currentTimeMillis() - ms) + "ms )");
    }

    public static void load() {
        long ms = System.currentTimeMillis();
        getInstance().getLogger().log(Level.INFO, "데이터 불러오는중...");
        for (File file : Objects.requireNonNull(new File(getInstance().getDataFolder() + "\\storage").listFiles())) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            if (!(yaml.isSet("static-size") || yaml.isSet("static-id"))) continue;
            String id = Objects.requireNonNull(yaml.getString("static-id"));
            Inventory inventory = Bukkit.createInventory(null, yaml.getInt("static-size"), id);
            getInstance().getLogger().log(Level.INFO, "%s-storage.yml을 로드 했습니다. ( ".formatted(id) + (System.currentTimeMillis() - ms) + "ms )");
            for (String path : yaml.getKeys(true)) {
                if (path.contains("contents")) {
                    String[] splits = path.split("\\.");
                    if (splits.length == 2)
                        inventory.setItem(Integer.parseInt(splits[1]), yaml.getItemStack(path));
                }
            }
            chests.put(id, inventory);
        }
        getInstance().getLogger().log(Level.INFO, "데이터 불러오기완료! ( " + (System.currentTimeMillis() - ms) + "ms )");
    }

    @Override
    public void onDisable() {
        upload();
    }

    public static LucaSK getInstance() {
        return instance;
    }

    public SkriptAddon getAddonInstance() {
        return addon;
    }
}
