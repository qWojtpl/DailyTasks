package pl.dailytasks;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pl.dailytasks.commands.Commands;
import pl.dailytasks.data.DataManager;
import pl.dailytasks.events.Events;
import pl.dailytasks.gui.GUIHandler;
import pl.dailytasks.tasks.PlayerTasks;
import pl.dailytasks.tasks.TaskManager;
import pl.dailytasks.tasks.TaskObject;
import pl.dailytasks.util.DateManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class DailyTasks extends JavaPlugin {

    public static DailyTasks main;
    public static YamlConfiguration messages;
    public static HashMap<Player, PlayerTasks> playerTaskList = new HashMap<>();
    public static List<TaskObject> TaskPool = new ArrayList<>();
    public static String lastRandomizedDate = "";

    @Override
    public void onEnable() {
        main = this;
        getServer().getPluginManager().registerEvents(new Events(), this);
        getCommand("dailytasks").setExecutor(new Commands());
        DataManager.load();
        getLogger().info("Loaded.");
        for(Player p : Bukkit.getOnlinePlayers()) {
            PlayerTasks.Create(p);
        }
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, (Runnable) () -> {
            String now = DateManager.getFormattedDate("%Y/%M/%D");
            if(!lastRandomizedDate.equals(now)) {
                TaskManager.RandomizeTasks(3);
                lastRandomizedDate = now;
                DataManager.saveLastRandomized(now);
            }
        }, 0L, 20L);
    }

    @Override
    public void onDisable() {
        GUIHandler.closeAllInventories();
        DataManager.saveCalendar();
        getLogger().info("Bye!");
    }

    public static String getMessage(String path) {
        if(messages.getString("messages." + path) != null) {
            return messages.getString("messages." + path);
        }
        return "§cNULL§f";
    }

}
