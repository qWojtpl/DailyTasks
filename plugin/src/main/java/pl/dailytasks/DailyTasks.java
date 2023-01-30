package pl.dailytasks;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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

@Getter
public final class DailyTasks extends JavaPlugin {

    private static DailyTasks main; // Main instance
    public static YamlConfiguration messages; // Messages
    public static HashMap<Player, PlayerTasks> playerTaskList = new HashMap<>(); // PlayerTasks list
    public static List<TaskObject> TaskPool = new ArrayList<>(); // Task pool to randomize tasks
    public static List<RewardObject> RewardPool = new ArrayList<>();
    public static String lastRandomizedDate = ""; // Last randomized date
    public static int dateCheckTask;
    public static boolean dataCheckInitialized = false;
    private PermissionManager permissionManager;
    private TaskManager taskManager;
    private DateManager dateManager;

    @Override
    public void onEnable() {
        main = this; // Set main instance as this
        permissionManager = new PermissionManager();
        permissionManager.loadPermissions(); // Register permissions
        taskManager = new TaskManager();
        dateManager = new DateManager();
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
        taskManager.getSourceDayReward().clear();
        taskManager.getSourceMonthReward().clear();
        taskManager.getSourceTaskList().clear();
        DataHandler.load(); // Load data (configs, tasks etc)
        getLogger().info("Reloaded.");
        for(Player p : Bukkit.getOnlinePlayers()) {
            PlayerTasks.Create(p); // Create PlayerTasks for all players on the server
        }
    }

    public void runDateCheck() {
        if(dataCheckInitialized) {
            Bukkit.getScheduler().cancelTask(dateCheckTask); // If ever date check initialized, cancel previous task
        }
        dataCheckInitialized = true;
        dateCheckTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(DailyTasks.getInstance(), (Runnable) () -> { // Check date every second to randomize tasks
            String now = dateManager.getFormattedDate("%Y/%M/%D"); // Set now as date
            if(!lastRandomizedDate.equals(now)) { // If date is not last randomized date
                taskManager.RandomizeTasks(3); // Randomize 3 new tasks
                if(dateManager.getFormattedDate("%D").equals("1")
                        || taskManager.getThisMonthReward() == null) {
                    taskManager.RandomizeMonthReward();
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

    public static DailyTasks getInstance() {
        return DailyTasks.main;
    }

}
