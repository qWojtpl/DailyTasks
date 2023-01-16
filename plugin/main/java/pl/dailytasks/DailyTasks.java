package pl.dailytasks;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pl.dailytasks.commands.Commands;
import pl.dailytasks.events.Events;
import pl.dailytasks.gui.GUIHandler;
import pl.dailytasks.tasks.PlayerTasks;
import pl.dailytasks.tasks.TaskObject;
import pl.dailytasks.util.DateManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class DailyTasks extends JavaPlugin {

    public static DailyTasks main;
    public static YamlConfiguration messages;
    public static HashMap<Player, PlayerTasks> playerTaskList = new HashMap<>();
    public static List<TaskObject> TaskPool = new ArrayList<>();
    public static List<TaskObject> todayTasks = new ArrayList<>();
    public static String lastRandomizedDate = "";

    @Override
    public void onEnable() {
        main = this;
        getServer().getPluginManager().registerEvents(new Events(), this);
        getCommand("dailytasks").setExecutor(new Commands());
        loadMessages();
        loadTasks();
        getLogger().info("Loaded.");
        for(Player p : Bukkit.getOnlinePlayers()) {
            PlayerTasks.Create(p);
        }
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, (Runnable) () -> {
            String now = DateManager.getFormattedDate("%Y/%M/%D");
            if(!lastRandomizedDate.equals(now)) {
                TaskObject.RandomizeTasks(3);
                lastRandomizedDate = now;
            }
        }, 0L, 20L);
    }

    @Override
    public void onDisable() {
        GUIHandler.closeAllInventories();
        getLogger().info("Bye!");
    }

    public static void loadMessages() {
        File messageFile = new File(DailyTasks.main.getDataFolder(), "messages.yml");
        if(!messageFile.exists()) {
            try {
                YamlConfiguration yml = new YamlConfiguration();
                yml.set("messages.prefix", "§f§l[§b§lDailyTasks§f§l]");
                yml.set("messages.title", "Daily tasks");
                yml.set("messages.day", "§6§lDay §c§l");
                yml.set("messages.tasks", "§cTasks:");
                yml.set("messages.info-title", "§6§lInformation");
                yml.set("messages.info-description", "§aComplete daily tasks to get rewards.%nl%§aComplete all monthly tasks to get special reward!");
                yml.set("messages.locked-task", "§cTask locked! Come back in a few days!");
                yml.set("messages.not-completed", "%nl%§cYou didn't completed this task!");
                yml.set("messages.completed", "%nl%§aYou completed this task!");
                yml.save(messageFile);
            } catch(IOException e) {
                DailyTasks.main.getLogger().info("IO exception: " + e);
            }
        }
        messages = YamlConfiguration.loadConfiguration(messageFile);
    }

    public static void loadTasks() {
        File tasksFile = new File(DailyTasks.main.getDataFolder(), "task-pool.yml");
        if(!tasksFile.exists()) {
            try {
                YamlConfiguration yml = new YamlConfiguration();
                yml.set("tasks.0.enabled", true);
                yml.set("tasks.0.event", "kill %rdm% villager");
                yml.set("tasks.0.numberMin", 1);
                yml.set("tasks.0.numberMax", 10);
                yml.save(tasksFile);
            } catch(IOException e) {
                DailyTasks.main.getLogger().info("IO exception: " + e);
            }
        }
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(tasksFile);
        ConfigurationSection section = yml.getConfigurationSection("tasks");
        for(String key : section.getKeys(false)) {
            if(!yml.getBoolean("tasks." + key + ".enabled")) {
                continue;
            }
            new TaskObject(key, yml.getString("tasks." + key + ".event"),
                    yml.getInt("tasks." + key + ".numberMin"), yml.getInt("tasks." + key + ".numberMax"));
            DailyTasks.main.getLogger().info("Loaded task: " + key);
        }
    }

    public static String getMessage(String path) {
        if(messages.getString("messages." + path) != null) {
            return messages.getString("messages." + path);
        }
        return "§cNULL§f";
    }



}
