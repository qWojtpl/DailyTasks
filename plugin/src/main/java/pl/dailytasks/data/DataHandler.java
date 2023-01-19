package pl.dailytasks.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import pl.dailytasks.DailyTasks;
import pl.dailytasks.tasks.PlayerTasks;
import pl.dailytasks.tasks.RewardObject;
import pl.dailytasks.tasks.TaskManager;
import pl.dailytasks.tasks.TaskObject;
import pl.dailytasks.util.DateManager;
import pl.dailytasks.util.RandomNumber;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataHandler {

    public static boolean deleteOldData = false;

    public static void load() {
        loadCalendar(); // Load calendar (if using fake set calendar as this fake one)
        loadMessages(); // Load messages
        loadTasks(); // Load task pool
        loadRewards(); // Load rewards
        loadPluginData(); // Load plugin data (task history, rewards history, options, calendar etc)
    }

    public static File createPlayerFile(Player p) {
        File dataFile = getPlayerFile(p); // Get player file
        if(!dataFile.exists()) { // If file is not exists
            try {
                File directory = new File(DailyTasks.main.getDataFolder(), "/playerData/"); // Get directory
                if(!directory.exists()) directory.mkdir(); // Create directory
                dataFile.createNewFile(); // Create file
            } catch(IOException e) {
                DailyTasks.main.getLogger().info("Cannot create " + p.getName() + ".yml");
                DailyTasks.main.getLogger().info("IO Exception: " + e);
            }
        } else {
            if(!dataFile.canRead() || !dataFile.canWrite()) { // If file cannot be read or written - send info to console
                DailyTasks.main.getLogger().info("Cannot create " + p.getName() + ".yml");
            }
        }
        return dataFile; // Return file
    }

    public static void loadPlayer(Player p) {
        PlayerTasks pt = PlayerTasks.Create(p); // Get playertasks object
        loadPlayerAutoComplete(pt); // Load auto-complete days
        File playerFile = createPlayerFile(p); // Get player file (and create it if not exists)
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(playerFile); // Load YAML
        ConfigurationSection section = yml.getConfigurationSection("saved"); // Get YAML section
        if(section == null) return;
        for(String date : section.getKeys(false)) { // Loop through section keys (dates, eg. '2023/1/18')
            String[] dateArray = date.split("/");
            if(!DateManager.isUsingFakeCalendar() && deleteOldData) {
                if(Integer.parseInt(dateArray[0]) <= DateManager.getYear() && Integer.parseInt(dateArray[1]) < DateManager.getMonth()) {
                    yml.set("saved." + date, null);
                    continue;
                }
            }
            List<Integer> completed = new ArrayList<>(); // Player's completed tasks
            List<Integer> progress = new ArrayList<>(); // Player's progress
            for(int i = 0; i < 3; i++) { // Loop through daily tasks
                if(yml.getInt("saved." + date + "." + i + ".c") == 1) { // If task is completed...
                    completed.add(i); // Add to completed tasks
                }
                progress.add(yml.getInt("saved." + date + "." + i + ".p")); // Add progress - if not set - default int value is 0
            }
            pt.completedTasks.put(date, completed); // Put completed tasks as date key
            pt.progress.put(date, progress); // Put tasks progress as date key
        }
        try {
            yml.save(playerFile);
        } catch(IOException e) {
            DailyTasks.main.getLogger().info("Cannot save " + pt.getPlayer().getName() + ".yml");
            DailyTasks.main.getLogger().info("IO Exception: " + e);
        }
    }

    public static void loadPlayerAutoComplete(PlayerTasks pt) {
        File autoCompleteFile = createPluginData();
        YamlConfiguration yml2 = YamlConfiguration.loadConfiguration(autoCompleteFile);
        ConfigurationSection section2 = yml2.getConfigurationSection("auto-complete");
        if(section2 == null) return;
        for(String key : section2.getKeys(false)) {
            if(!yml2.getBoolean("auto-complete." + key)) continue;
            for(int i = 0; i < 3; i++) {
                addPlayerCompletedTaskByDate(pt, i, key);
            }
        }
    }

    public static void addPlayerCompletedTask(PlayerTasks pt, int index) {
        File playerFile = createPlayerFile(pt.getPlayer());
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(playerFile);
        yml.set("saved." + DateManager.getFormattedDate("%Y/%M/%D") + "." + index + ".c", 1);
        try {
            yml.save(playerFile);
        } catch(IOException e) {
            DailyTasks.main.getLogger().info("Cannot save " + pt.getPlayer().getName() + ".yml");
            DailyTasks.main.getLogger().info("IO Exception: " + e);
        }
    }

    public static void addPlayerCompletedTaskByDate(PlayerTasks pt, int index, String date) {
        File playerFile = createPlayerFile(pt.getPlayer());
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(playerFile);
        yml.set("saved." + date + "." + index + ".c", 1);
        try {
            yml.save(playerFile);
        } catch(IOException e) {
            DailyTasks.main.getLogger().info("Cannot save " + pt.getPlayer().getName() + ".yml");
            DailyTasks.main.getLogger().info("IO Exception: " + e);
        }
    }

    public static void updatePlayerProgress(PlayerTasks pt, int index) {
        File playerFile = createPlayerFile(pt.getPlayer());
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(playerFile);
        yml.set("saved." + DateManager.getFormattedDate("%Y/%M/%D") + "." + index + ".p", pt.getProgress().get(index));
        try {
            yml.save(playerFile);
        } catch(IOException e) {
            DailyTasks.main.getLogger().info("Cannot save " + pt.getPlayer().getName() + ".yml");
            DailyTasks.main.getLogger().info("IO Exception: " + e);
        }
    }

    public static File getPlayerFile(Player p) {
        return new File(DailyTasks.main.getDataFolder(), "/playerData/" + p.getName() + ".yml");
    }

    public static void loadCalendar() {
        File pluginDataFile = createPluginData();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(pluginDataFile);
        if(yml.getString("data.fakeCalendar") != null) {
            String args[] = yml.getString("data.fakeCalendar").split(" ");
            int i_args[] = new int[6];
            for(int i = 0; i < i_args.length; i++) {
                i_args[i] = Integer.parseInt(args[i]);
            }
            DateManager.createFakeCalendar(i_args[0], i_args[1]-1, i_args[2], i_args[3], i_args[4], i_args[5]);
        }
    }

    public static void saveCalendar() {
        File pluginDataFile = createPluginData();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(pluginDataFile);
        if(DateManager.isUsingFakeCalendar()) {
            yml.set("data.fakeCalendar", DateManager.getYear() + " " + DateManager.getMonth()
                    + " " + DateManager.getDay() + " " + DateManager.getHour() + " " + DateManager.getMinute() + " " + DateManager.getSecond());
        } else {
            yml.set("data.fakeCalendar", null);
        }
        try {
            yml.save(pluginDataFile);
        } catch(IOException e) {
            DailyTasks.main.getLogger().info("Cannot save pluginData.yml");
            DailyTasks.main.getLogger().info("IO Exception: " + e);
        }
    }

    public static File createPluginData() {
        File directory = new File(DailyTasks.main.getDataFolder(), "/data/");
        if(!directory.exists()) directory.mkdir();
        File pluginDataFile = new File(DailyTasks.main.getDataFolder() + "/data/pluginData.yml");
        if(!pluginDataFile.exists()) {
            try {
                pluginDataFile.createNewFile();
                YamlConfiguration yml = YamlConfiguration.loadConfiguration(pluginDataFile);
                yml.set("data.lastRandomized", "1970/01/01");
                yml.set("options.deleteOldData", true);
                yml.save(pluginDataFile);
            } catch(IOException e) {
                DailyTasks.main.getLogger().info("Cannot create pluginData.yml");
                DailyTasks.main.getLogger().info("IO Exception: " + e);
            }
        }
        return pluginDataFile;
    }

    public static void loadPluginData() {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(createPluginData());
        ConfigurationSection section = yml.getConfigurationSection("history");
        if(section != null) {
            for(String key : section.getKeys(false)) {
                List<String> tasks = yml.getStringList("history." + key);
                List<TaskObject> initializedTasks = new ArrayList<>();
                for(String event : tasks) {
                    String[] taskInfo = event.split(" ");
                    TaskObject to = new TaskObject(event, Integer.parseInt(taskInfo[1]), Integer.parseInt(taskInfo[1]));
                    initializedTasks.add(to);
                }
                TaskManager.taskList.put(key, initializedTasks);
            }
        }
        String[] rewardTypes = new String[]{"day", "month"};
        for(String rewardType : rewardTypes) {
            section = yml.getConfigurationSection(rewardType + "-reward-history");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    RewardObject reward = new RewardObject(yml.getString(rewardType + "-reward-history." + key),
                            0, 0, (rewardType == "month"));
                    TaskManager.dayRewardList.put(key, reward);
                    reward.initializedCommand = reward.command;
                }
            }
        }
        DailyTasks.lastRandomizedDate = yml.getString("data.lastRandomized");
        DataHandler.deleteOldData = yml.getBoolean("options.deleteOldData");
        DailyTasks.runDateCheck();
    }

    public static void saveTodayTasks() {
        File pluginDataFile = createPluginData();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(pluginDataFile);
        List<String> tasks = new ArrayList<>();
        for(TaskObject to : TaskManager.getTodayTasks()) {
            tasks.add(to.initializedEvent);
        }
        yml.set("history." + DateManager.getFormattedDate("%Y/%M/%D"), tasks);
        try {
            yml.save(pluginDataFile);
        } catch(IOException e) {
            DailyTasks.main.getLogger().info("Cannot save pluginData.yml");
            DailyTasks.main.getLogger().info("IO Exception: " + e);
        }
    }

    public static void saveTodayReward() {
        File pluginDataFile = createPluginData();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(pluginDataFile);
        yml.set("day-reward-history." + DateManager.getFormattedDate("%Y/%M/%D"), TaskManager.getTodayReward().initializedCommand);
        try {
            yml.save(pluginDataFile);
        } catch(IOException e) {
            DailyTasks.main.getLogger().info("Cannot save pluginData.yml");
            DailyTasks.main.getLogger().info("IO Exception: " + e);
        }
    }

    public static void saveMonthlyReward() {
        File pluginDataFile = createPluginData();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(pluginDataFile);
        yml.set("month-reward-history." + DateManager.getFormattedDate("%Y/%M"), TaskManager.getThisMonthReward().initializedCommand);
        try {
            yml.save(pluginDataFile);
        } catch(IOException e) {
            DailyTasks.main.getLogger().info("Cannot save pluginData.yml");
            DailyTasks.main.getLogger().info("IO Exception: " + e);
        }
    }

    public static void saveLastRandomized(String date) {
        File pluginDataFile = createPluginData();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(pluginDataFile);
        yml.set("data.lastRandomized", date);
        try {
            yml.save(pluginDataFile);
        } catch(IOException e) {
            DailyTasks.main.getLogger().info("Cannot save pluginData.yml");
            DailyTasks.main.getLogger().info("IO Exception: " + e);
        }
    }

    public static void markDateAutoComplete(String date, boolean markAs) {
        File pluginDataFile = createPluginData();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(pluginDataFile);
        if(markAs) {
            yml.set("auto-complete." + date, markAs);
        } else {
            yml.set("auto-complete." + date, null);
        }
        try {
            yml.save(pluginDataFile);
        } catch(IOException e) {
            DailyTasks.main.getLogger().info("Cannot save pluginData.yml");
            DailyTasks.main.getLogger().info("IO Exception: " + e);
        }
    }

    public static List<String> getAutoCompleteDates() {
        File pluginDataFile = createPluginData();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(pluginDataFile);
        ConfigurationSection section = yml.getConfigurationSection("auto-complete");
        if(section == null) return new ArrayList<>();
        List<String> dates = new ArrayList<>();
        for(String date : section.getKeys(false)) {
            if(yml.getBoolean("auto-complete." + date)) dates.add(date);
        }
        return dates;
    }

    public static void loadMessages() {
        File messageFile = new File(DailyTasks.main.getDataFolder(), "messages.yml");
        if(!messageFile.exists()) {
            try {
                YamlConfiguration yml = new YamlConfiguration();
                yml.set("messages.prefix", "§c§l[§e§lDailyTasks§c§l]");
                yml.set("messages.title", "Daily tasks");
                yml.set("messages.day", "§6§lDay §c§l");
                yml.set("messages.tasks", "§cTasks:");
                yml.set("messages.day-without-task", "§cThis day doesn't have any tasks! :D");
                yml.set("messages.info-title", "§6§lInformation");
                yml.set("messages.info-description", "§aComplete daily tasks to get rewards.%nl%§aComplete all monthly tasks to get special reward!");
                yml.set("messages.locked-task", "§cTask locked! Come back in a few days!");
                yml.set("messages.not-completed", "%nl%§cYou didn't completed this task!");
                yml.set("messages.completed", "%nl%§aYou completed this task!");
                yml.set("messages.complete-day", "§c----------------------------%nl% %nl%§e§lYou completed day §a{0}%nl%§e§lReward: §a{1}%nl% %nl%§c----------------------------");
                yml.set("messages.complete-month", "§c§k----------------------------%nl% %nl%§e§lYou completed month §a{0}%nl%§e§lReward: §a{1} %nl% %nl%§c§k----------------------------");
                yml.save(messageFile);
            } catch(IOException e) {
                DailyTasks.main.getLogger().info("IO exception: " + e);
            }
        }
        DailyTasks.messages = YamlConfiguration.loadConfiguration(messageFile);
    }

    public static File createTasksFile() {
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
                DailyTasks.main.getLogger().info("Cannot create task-pool.yml");
                DailyTasks.main.getLogger().info("IO exception: " + e);
            }
        }
        return tasksFile;
    }

    public static void loadTasks() {
        File tasksFile = createTasksFile();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(tasksFile);
        ConfigurationSection section = yml.getConfigurationSection("tasks");
        for(String key : section.getKeys(false)) {
            if(!yml.getBoolean("tasks." + key + ".enabled")) {
                continue;
            }
            DailyTasks.TaskPool.add(new TaskObject(yml.getString("tasks." + key + ".event"),
                    yml.getInt("tasks." + key + ".numberMin"), yml.getInt("tasks." + key + ".numberMax")));
            DailyTasks.main.getLogger().info("Loaded task: " + key);
        }
    }

    public static File createRewardFile() {
        File rewardFile = new File(DailyTasks.main.getDataFolder(), "reward-pool.yml");
        if(!rewardFile.exists()) {
            try {
                rewardFile.createNewFile();
                YamlConfiguration yml = new YamlConfiguration();
                yml.set("day-rewards.0.enabled", true);
                yml.set("day-rewards.0.command", "give %player% minecraft:emerald %rdm%");
                yml.set("day-rewards.0.numberMin", 10);
                yml.set("day-rewards.0.numberMax", 15);
                yml.set("month-rewards.0.enabled", true);
                yml.set("month-rewards.0.command", "give %player% minecraft:diamond_block %rdm%");
                yml.set("month-rewards.0.numberMin", 10);
                yml.set("month-rewards.0.numberMax", 32);
                yml.save(rewardFile);
            } catch(IOException e) {
                DailyTasks.main.getLogger().info("Cannot create task-pool.yml");
                DailyTasks.main.getLogger().info("IO exception: " + e);
            }
        }
        return rewardFile;
    }

    public static void loadRewards() {
        File rewardFile = createRewardFile();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(rewardFile);
        String[] rewardTypes = new String[]{"day", "month"};
        for(String rewardType : rewardTypes) {
            ConfigurationSection section = yml.getConfigurationSection(rewardType + "-rewards");
            if(section == null) continue;
            for(String key : section.getKeys(false)) {
                DailyTasks.RewardPool.add( new RewardObject(yml.getString(rewardType + "-rewards." + key + ".command"),
                        yml.getInt(rewardType + "-rewards." + key + ".numberMin"),
                        yml.getInt(rewardType + "-rewards." + key + ".command"),
                        (rewardType.equals("month")) )
                );
            }
        }
    }
}
