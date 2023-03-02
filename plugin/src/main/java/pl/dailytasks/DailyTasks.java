package pl.dailytasks;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pl.dailytasks.commands.CommandHelper;
import pl.dailytasks.commands.Commands;
import pl.dailytasks.commands.PermissionManager;
import pl.dailytasks.data.DataHandler;
import pl.dailytasks.events.Events;
import pl.dailytasks.gui.GUIHandler;
import pl.dailytasks.tasks.PlayerTasks;
import pl.dailytasks.tasks.TaskManager;
import pl.dailytasks.util.DateManager;
import pl.dailytasks.util.Messages;
import pl.dailytasks.util.PlayerUtil;

import java.util.HashMap;

@Getter
public final class DailyTasks extends JavaPlugin {

    private static DailyTasks main; // Main instance
    private final HashMap<Player, PlayerTasks> playerTaskList = new HashMap<>(); // PlayerTasks list
    @Setter
    public String lastRandomizedDate = ""; // Last randomized date
    public int dateCheckTask;
    public boolean dataCheckInitialized = false;
    private DataHandler dataHandler;
    private PermissionManager permissionManager;
    private TaskManager taskManager;
    private DateManager dateManager;
    private PlayerUtil playerUtil;
    private Messages messages;

    @Override
    public void onEnable() {
        main = this; // Set main instance as this
        this.permissionManager = new PermissionManager();
        this.permissionManager.loadPermissions(); // Register permissions
        this.taskManager = new TaskManager();
        this.dateManager = new DateManager();
        this.playerUtil = new PlayerUtil();
        this.messages = new Messages();
        this.dataHandler = new DataHandler();
        getServer().getPluginManager().registerEvents(new Events(), this); // Register events
        getCommand("dailytasks").setExecutor(new Commands()); // Register command
        getCommand("dailytasks").setTabCompleter(new CommandHelper()); // Register tab completer
        dataHandler.load(); // Load data (configs, tasks etc)
        getLogger().info("Loaded.");
        for(Player p : Bukkit.getOnlinePlayers()) {
            PlayerTasks.Create(p); // Create PlayerTasks for all players on the server
        }
    }

    @Override
    public void onDisable() {
        GUIHandler.closeAllInventories(); // Close all GUI inventories
        dataHandler.saveCalendar(); // Save calendar (if using fake calendar)
        getLogger().info("Bye!");
    }

    public void Reload() {
        GUIHandler.closeAllInventories(); // Close all GUI inventories
        dataHandler.saveCalendar(); // Save calendar (if using fake calendar)
        playerTaskList.clear();
        lastRandomizedDate = "";
        dataCheckInitialized = false;
        taskManager.getSourceDayReward().clear();
        taskManager.getSourceMonthReward().clear();
        taskManager.getSourceTaskList().clear();
        dataHandler.load(); // Load data (configs, tasks etc)
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
        dateCheckTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(DailyTasks.getInstance(), () -> { // Check date every second to randomize tasks
            String now = dateManager.getFormattedDate("%Y/%M/%D"); // Set now as date
            if(!lastRandomizedDate.equals(now)) { // If date is not last randomized date
                taskManager.RandomizeTasks(3); // Randomize 3 new tasks
                if(dateManager.getFormattedDate("%D").equals("1")
                        || taskManager.getThisMonthReward() == null) {
                    taskManager.RandomizeMonthReward();
                }
                lastRandomizedDate = now; // Set randomized date to now
                dataHandler.saveLastRandomized(now); // Save randomized tasks
            }
        }, 0L, 20L);
    }

    public static DailyTasks getInstance() {
        return DailyTasks.main;
    }

}
