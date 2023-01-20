package pl.dailytasks;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import pl.dailytasks.commands.CommandHelper;
import pl.dailytasks.commands.Commands;
import pl.dailytasks.commands.PermissionManager;
import pl.dailytasks.data.DataHandler;
import pl.dailytasks.events.Events;
import pl.dailytasks.gui.GUIHandler;
import pl.dailytasks.tasks.PlayerTasks;
import pl.dailytasks.tasks.RewardObject;
import pl.dailytasks.tasks.TaskManager;
import pl.dailytasks.tasks.TaskObject;
import pl.dailytasks.util.DateManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class DailyTasks extends JavaPlugin {

    public static DailyTasks main; // Main instance
    public static YamlConfiguration messages; // Messages
    public static HashMap<Player, PlayerTasks> playerTaskList = new HashMap<>(); // PlayerTasks list
    public static List<TaskObject> TaskPool = new ArrayList<>(); // Task pool to randomize tasks
    public static List<RewardObject> RewardPool = new ArrayList<>();
    public static String lastRandomizedDate = ""; // Last randomized date
    public static int dateCheckTask;
    public static boolean dataCheckInitialized = false;

    @Override
    public void onEnable() {
        main = this; // Set main instance as this
        PermissionManager.loadPermissions(); // Register permissions
        getServer().getPluginManager().registerEvents(new Events(), this); // Register events
        getCommand("dailytasks").setExecutor(new Commands()); // Register command
        getCommand("dailytasks").setTabCompleter(new CommandHelper()); // Register tab completer
        DataHandler.load(); // Load data (configs, tasks etc)
        getLogger().info("Loaded.");
        for(Player p : Bukkit.getOnlinePlayers()) {
            PlayerTasks.Create(p); // Create PlayerTasks for all players on the server
        }
    }

    @Override
    public void onDisable() {
        GUIHandler.closeAllInventories(); // Close all GUI inventories
        DataHandler.saveCalendar(); // Save calendar (if using fake calendar)
        getLogger().info("Bye!");
    }

    public void Reload() {
        GUIHandler.closeAllInventories(); // Close all GUI inventories
        DataHandler.saveCalendar(); // Save calendar (if using fake calendar)
        getLogger().info("Reloading..");
        messages = null;
        playerTaskList = new HashMap<>();
        TaskPool = new ArrayList<>();
        RewardPool = new ArrayList<>();
        lastRandomizedDate = "";
        dataCheckInitialized = false;
        TaskManager.dayRewardList = new HashMap<>();
        TaskManager.monthRewardList = new HashMap<>();
        TaskManager.taskList = new HashMap<>();
        DataHandler.load(); // Load data (configs, tasks etc)
        getLogger().info("Reloaded.");
        for(Player p : Bukkit.getOnlinePlayers()) {
            PlayerTasks.Create(p); // Create PlayerTasks for all players on the server
        }
    }

    public static void runDateCheck() {
        if(dataCheckInitialized) {
            Bukkit.getScheduler().cancelTask(dateCheckTask); // If ever date check initialized, cancel previous task
        }
        dataCheckInitialized = true;
        dateCheckTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(DailyTasks.main, (Runnable) () -> { // Check date every second to randomize tasks
            String now = DateManager.getFormattedDate("%Y/%M/%D"); // Set now as date
            if(!lastRandomizedDate.equals(now)) { // If date is not last randomized date
                TaskManager.RandomizeTasks(3); // Randomize 3 new tasks
                if(DateManager.getFormattedDate("%D").equals("1")
                        || TaskManager.getThisMonthReward() == null) {
                    TaskManager.RandomizeMonthReward();
                }
                lastRandomizedDate = now; // Set randomized date to now
                DataHandler.saveLastRandomized(now); // Save randomized tasks
            }
        }, 0L, 20L);
    }

    public static String getMessage(String path) {
        if(messages.getString("messages." + path) != null) {
            return messages.getString("messages." + path);
        }
        return "§cNULL§f ";
    }

}
