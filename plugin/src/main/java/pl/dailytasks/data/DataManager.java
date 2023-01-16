package pl.dailytasks.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import pl.dailytasks.DailyTasks;
import pl.dailytasks.tasks.PlayerTasks;
import pl.dailytasks.tasks.TaskManager;
import pl.dailytasks.tasks.TaskObject;
import pl.dailytasks.util.DateManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataManager {

    public static void createPlayer(Player p) {
        createPlayerFile(p);

    }

    public static File getPlayerFile(Player p) {
        return new File(DailyTasks.main.getDataFolder(), "/playerData/" + p.getName() + ".yml");
    }

    public static File createTaskHistory() {
        File directory = new File(DailyTasks.main.getDataFolder(), "/data/");
        if(!directory.exists()) directory.mkdir();
        File taskHistoryFile = new File(DailyTasks.main.getDataFolder() + "/data/taskhistory.yml");
        if(!taskHistoryFile.exists()) {
            try {
                taskHistoryFile.createNewFile();
                YamlConfiguration yml = YamlConfiguration.loadConfiguration(taskHistoryFile);
                yml.set("data.lastRandomized", "1970/01/01");
                yml.save(taskHistoryFile);
            } catch(IOException e) {
                DailyTasks.main.getLogger().info("Cannot create taskhistory.yml");
                DailyTasks.main.getLogger().info("IO Exception: " + e);
            }
        }
        return taskHistoryFile;
    }

    public static void saveLastRandomized(String date) {
        File taskHistoryFile = createTaskHistory();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(taskHistoryFile);
        yml.set("data.lastRandomized", date);
        try {
            yml.save(taskHistoryFile);
        } catch(IOException e) {
            DailyTasks.main.getLogger().info("Cannot save taskhistory.yml");
            DailyTasks.main.getLogger().info("IO Exception: " + e);
        }
    }

    public static void loadTaskHistory() {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(createTaskHistory());
        ConfigurationSection section = yml.getConfigurationSection("history");
        if(section == null) return;
        for(String key : section.getKeys(false)) {
            List<String> tasks = (List<String>) yml.getList("history." + key);
            List<TaskObject> initializedTasks = new ArrayList<>();
            for(String event : tasks) {
                String[] taskInfo = event.split(":");
                if(taskInfo.length != 2) {
                    DailyTasks.main.getLogger().info("Broken taskhistory.yml - cannot load tasks..");
                    return;
                }
                int number = Integer.valueOf(taskInfo[1].split(" ")[1]);
                TaskObject to = new TaskObject(taskInfo[0], taskInfo[1], number, number);
                initializedTasks.add(to);
            }
            TaskManager.todayTasks.put(key, initializedTasks);
        }
        DailyTasks.lastRandomizedDate = yml.getString("data.lastRandomized");
    }

    public static void saveTodayTasks() {
        File taskHistoryFile = createTaskHistory();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(taskHistoryFile);
        List<String> tasks = new ArrayList<>();
        for(TaskObject to : TaskManager.getTodayTasks()) {
            tasks.add(to.ID + ":" + to.initializedEvent);
        }
        yml.set("history." + DateManager.getFormattedDate("%Y/%M/%D"), tasks);
        try {
            yml.save(taskHistoryFile);
        } catch(IOException e) {
            DailyTasks.main.getLogger().info("Cannot save taskhistory.yml");
            DailyTasks.main.getLogger().info("IO Exception: " + e);
        }
    }

    public static void saveCalendar() {
        File taskHistoryFile = createTaskHistory();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(taskHistoryFile);
        if(DateManager.isUsingFakeCalendar()) {
            yml.set("data.fakeCalendar", DateManager.getYear() + " " + DateManager.getMonth()
                    + " " + DateManager.getDay() + " " + DateManager.getHour() + " " + DateManager.getMinute() + " " + DateManager.getSecond());
        } else {
            yml.set("data.fakeCalendar", null);
        }
        try {
            yml.save(taskHistoryFile);
        } catch(IOException e) {
            DailyTasks.main.getLogger().info("Cannot save taskhistory.yml");
            DailyTasks.main.getLogger().info("IO Exception: " + e);
        }
    }

    public static void loadCalendar() {
        File taskHistoryFile = createTaskHistory();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(taskHistoryFile);
        if(yml.getString("data.fakeCalendar") != null) {
            String args[] = yml.getString("data.fakeCalendar").split(" ");
            int i_args[] = new int[6];
            for(int i = 0; i < i_args.length; i++) {
                i_args[i] = Integer.parseInt(args[i]);
            }
            DateManager.createFakeCalendar(i_args[0], i_args[1]-1, i_args[2], i_args[3], i_args[4], i_args[5]);
        }
    }

    public static void createPlayerFile(Player p) {
        File dataFile = getPlayerFile(p);
        if(!dataFile.exists()) {
            try {
                File directory = new File(DailyTasks.main.getDataFolder(), "/playerData/");
                if(!directory.exists()) directory.mkdir();
                dataFile.createNewFile();
            } catch(IOException e) {
                DailyTasks.main.getLogger().info("Cannot create " + p.getName() + ".yml");
                DailyTasks.main.getLogger().info("IO Exception: " + e);
            }
        } else {
            if(!dataFile.canRead() || !dataFile.canWrite()) {
                DailyTasks.main.getLogger().info("Cannot create " + p.getName() + ".yml");
            }
        }
    }

    public static void addPlayerCompletedTask(PlayerTasks pt, int index) {

    }

    public static void load() {
        loadCalendar();
        loadMessages();
        loadTasks();
        loadTaskHistory();
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
                yml.set("messages.complete-day", "§c----------------%nl% %nl%§e§lYou completed day §a{0}%nl% %nl%§c----------------");
                yml.save(messageFile);
            } catch(IOException e) {
                DailyTasks.main.getLogger().info("IO exception: " + e);
            }
        }
        DailyTasks.messages = YamlConfiguration.loadConfiguration(messageFile);
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
            DailyTasks.TaskPool.add(new TaskObject(key, yml.getString("tasks." + key + ".event"),
                    yml.getInt("tasks." + key + ".numberMin"), yml.getInt("tasks." + key + ".numberMax")));
            DailyTasks.main.getLogger().info("Loaded task: " + key);
        }
    }

}
