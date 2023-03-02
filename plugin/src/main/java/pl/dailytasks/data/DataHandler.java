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
import pl.dailytasks.util.Messages;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataHandler {

    public static boolean deleteOldData = false;

    public void load() {
        loadCalendar(); // Load calendar (if using fake set calendar as this fake one)
        loadMessages(); // Load messages
        loadTasks(); // Load task pool
        loadRewards(); // Load rewards
        loadPluginData(); // Load plugin data (task history, rewards history, options, calendar etc)
    }

    public File createPlayerFile(Player p) {
        File dataFile = getPlayerFile(p); // Get player file
        if(!dataFile.exists()) { // If file is not exists
            try {
                File directory = new File(DailyTasks.getInstance().getDataFolder(), "/playerData/"); // Get directory
                if(!directory.exists()) directory.mkdir(); // Create directory
                dataFile.createNewFile(); // Create file
            } catch(IOException e) {
                DailyTasks.getInstance().getLogger().info("Cannot create " + p.getName() + ".yml");
                DailyTasks.getInstance().getLogger().info("IO Exception: " + e);
            }
        } else {
            if(!dataFile.canRead() || !dataFile.canWrite()) { // If file cannot be read or written - send info to console
                DailyTasks.getInstance().getLogger().info("Cannot create " + p.getName() + ".yml");
            }
        }
        return dataFile; // Return file
    }

    public void loadPlayer(Player p) {
        PlayerTasks pt = PlayerTasks.Create(p); // Get playertasks object
        loadPlayerAutoComplete(pt); // Load auto-complete days
        File playerFile = createPlayerFile(p); // Get player file (and create it if not exists)
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(playerFile); // Load YAML
        ConfigurationSection section = yml.getConfigurationSection("saved"); // Get YAML section
        if(section == null) return;
        for(String date : section.getKeys(false)) { // Loop through section keys (dates, eg. '2023/1/18')
            String[] dateArray = date.split("/");
            DateManager dm = DailyTasks.getInstance().getDateManager();
            if(!dm.isUsingFakeCalendar() && deleteOldData) {
                if(Integer.parseInt(dateArray[0]) <= dm.getYear() && Integer.parseInt(dateArray[1]) < dm.getMonth()) {
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
            pt.getSourceCompletedTasks().put(date, completed); // Put completed tasks as date key
            pt.getSourceProgress().put(date, progress); // Put tasks progress as date key
        }
        try {
            yml.save(playerFile);
        } catch(IOException e) {
            DailyTasks.getInstance().getLogger().info("Cannot save " + pt.getPlayer().getName() + ".yml");
            DailyTasks.getInstance().getLogger().info("IO Exception: " + e);
        }
    }

    public void loadPlayerAutoComplete(PlayerTasks pt) {
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

    public void addPlayerCompletedTask(PlayerTasks pt, int index) {
        File playerFile = createPlayerFile(pt.getPlayer());
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(playerFile);
        DateManager dm = DailyTasks.getInstance().getDateManager();
        yml.set("saved." + dm.getFormattedDate("%Y/%M/%D") + "." + index + ".c", 1);
        try {
            yml.save(playerFile);
        } catch(IOException e) {
            DailyTasks.getInstance().getLogger().info("Cannot save " + pt.getPlayer().getName() + ".yml");
            DailyTasks.getInstance().getLogger().info("IO Exception: " + e);
        }
    }

    public void addPlayerCompletedTaskByDate(PlayerTasks pt, int index, String date) {
        File playerFile = createPlayerFile(pt.getPlayer());
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(playerFile);
        yml.set("saved." + date + "." + index + ".c", 1);
        try {
            yml.save(playerFile);
        } catch(IOException e) {
            DailyTasks.getInstance().getLogger().info("Cannot save " + pt.getPlayer().getName() + ".yml");
            DailyTasks.getInstance().getLogger().info("IO Exception: " + e);
        }
    }

    public void updatePlayerProgress(PlayerTasks pt, int index) {
        File playerFile = createPlayerFile(pt.getPlayer());
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(playerFile);
        DateManager dm = DailyTasks.getInstance().getDateManager();
        yml.set("saved." + dm.getFormattedDate("%Y/%M/%D") + "." + index + ".p", pt.getProgress().get(index));
        try {
            yml.save(playerFile);
        } catch(IOException e) {
            DailyTasks.getInstance().getLogger().info("Cannot save " + pt.getPlayer().getName() + ".yml");
            DailyTasks.getInstance().getLogger().info("IO Exception: " + e);
        }
    }

    public File getPlayerFile(Player p) {
        return new File(DailyTasks.getInstance().getDataFolder(), "/playerData/" + p.getName() + ".yml");
    }

    public void loadCalendar() {
        File pluginDataFile = createPluginData();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(pluginDataFile);
        if(yml.getString("data.fakeCalendar") != null) {
            String args[] = yml.getString("data.fakeCalendar").split(" ");
            int i_args[] = new int[6];
            for(int i = 0; i < i_args.length; i++) {
                i_args[i] = Integer.parseInt(args[i]);
            }
            DateManager dm = DailyTasks.getInstance().getDateManager();
            dm.createFakeCalendar(i_args[0], i_args[1]-1, i_args[2], i_args[3], i_args[4], i_args[5]);
        }
    }

    public void saveCalendar() {
        File pluginDataFile = createPluginData();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(pluginDataFile);
        DateManager dm = DailyTasks.getInstance().getDateManager();
        if(dm.isUsingFakeCalendar()) {
            yml.set("data.fakeCalendar", dm.getYear() + " " + dm.getMonth()
                    + " " + dm.getDay() + " " + dm.getHour() + " " + dm.getMinute() + " " + dm.getSecond());
        } else {
            yml.set("data.fakeCalendar", null);
        }
        try {
            yml.save(pluginDataFile);
        } catch(IOException e) {
            DailyTasks.getInstance().getLogger().info("Cannot save pluginData.yml");
            DailyTasks.getInstance().getLogger().info("IO Exception: " + e);
        }
    }

    public File createPluginData() {
        File directory = new File(DailyTasks.getInstance().getDataFolder(), "/data/");
        if(!directory.exists()) directory.mkdir();
        File pluginDataFile = new File(DailyTasks.getInstance().getDataFolder() + "/data/pluginData.yml");
        if(!pluginDataFile.exists()) {
            try {
                pluginDataFile.createNewFile();
                YamlConfiguration yml = YamlConfiguration.loadConfiguration(pluginDataFile);
                yml.set("data.lastRandomized", "1970/01/01");
                yml.set("options.deleteOldData", true);
                yml.save(pluginDataFile);
            } catch(IOException e) {
                DailyTasks.getInstance().getLogger().info("Cannot create pluginData.yml");
                DailyTasks.getInstance().getLogger().info("IO Exception: " + e);
            }
        }
        return pluginDataFile;
    }

    public void loadPluginData() {
        File pluginDataFile = createPluginData();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(pluginDataFile);
        ConfigurationSection section = yml.getConfigurationSection("history");
        TaskManager tm = DailyTasks.getInstance().getTaskManager();
        if(section != null) {
            for(String key : section.getKeys(false)) {
                String[] dateArray = key.split("/");
                DateManager dm = DailyTasks.getInstance().getDateManager();
                if(!dm.isUsingFakeCalendar() && deleteOldData) {
                    if(Integer.parseInt(dateArray[0]) <= dm.getYear() && Integer.parseInt(dateArray[1]) < dm.getMonth()) {
                        yml.set("history." + key, null);
                        continue;
                    }
                }
                List<String> tasks = yml.getStringList("history." + key);
                List<TaskObject> initializedTasks = new ArrayList<>();
                for(String event : tasks) {
                    String[] taskInfo = event.split(" ");
                    TaskObject to = new TaskObject(event, Integer.parseInt(taskInfo[1]), Integer.parseInt(taskInfo[1]));
                    initializedTasks.add(to);
                }
                tm.getSourceTaskList().put(key, initializedTasks);
            }
        }
        String[] rewardTypes = new String[]{"day", "month"};
        for(String rewardType : rewardTypes) {
            section = yml.getConfigurationSection(rewardType + "-reward-history");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    RewardObject reward = new RewardObject(yml.getString(rewardType + "-reward-history." + key),
                            0, 0, (rewardType.equals("month")));
                    if(rewardType.equals("day")) {
                        tm.getSourceDayReward().put(key, reward);
                    } else {
                        tm.getSourceMonthReward().put(key, reward);
                    }
                    reward.initializedCommand = reward.command;
                }
            }
        }
        DailyTasks.getInstance().setLastRandomizedDate(yml.getString("data.lastRandomized"));
        DataHandler.deleteOldData = yml.getBoolean("options.deleteOldData");
        DailyTasks.getInstance().runDateCheck();
        try {
            yml.save(pluginDataFile);
        } catch(IOException e) {
            DailyTasks.getInstance().getLogger().info("Cannot save pluginData.yml");
            DailyTasks.getInstance().getLogger().info("IO Exception: " + e);
        }
    }

    public void saveTodayTasks() {
        File pluginDataFile = createPluginData();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(pluginDataFile);
        List<String> tasks = new ArrayList<>();
        for(TaskObject to : DailyTasks.getInstance().getTaskManager().getTodayTasks()) {
            tasks.add(to.initializedEvent);
        }
        DateManager dm = DailyTasks.getInstance().getDateManager();
        yml.set("history." + dm.getFormattedDate("%Y/%M/%D"), tasks);
        try {
            yml.save(pluginDataFile);
        } catch(IOException e) {
            DailyTasks.getInstance().getLogger().info("Cannot save pluginData.yml");
            DailyTasks.getInstance().getLogger().info("IO Exception: " + e);
        }
    }

    public void saveTodayReward() {
        File pluginDataFile = createPluginData();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(pluginDataFile);
        DateManager dm = DailyTasks.getInstance().getDateManager();
        yml.set("day-reward-history." + dm.getFormattedDate("%Y/%M/%D"),
                DailyTasks.getInstance().getTaskManager().getTodayReward().initializedCommand);
        try {
            yml.save(pluginDataFile);
        } catch(IOException e) {
            DailyTasks.getInstance().getLogger().info("Cannot save pluginData.yml");
            DailyTasks.getInstance().getLogger().info("IO Exception: " + e);
        }
    }

    public void saveMonthlyReward() {
        File pluginDataFile = createPluginData();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(pluginDataFile);
        DateManager dm = DailyTasks.getInstance().getDateManager();
        yml.set("month-reward-history." + dm.getFormattedDate("%Y/%M"),
                DailyTasks.getInstance().getTaskManager().getThisMonthReward().initializedCommand);
        try {
            yml.save(pluginDataFile);
        } catch(IOException e) {
            DailyTasks.getInstance().getLogger().info("Cannot save pluginData.yml");
            DailyTasks.getInstance().getLogger().info("IO Exception: " + e);
        }
    }

    public void saveLastRandomized(String date) {
        File pluginDataFile = createPluginData();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(pluginDataFile);
        yml.set("data.lastRandomized", date);
        try {
            yml.save(pluginDataFile);
        } catch(IOException e) {
            DailyTasks.getInstance().getLogger().info("Cannot save pluginData.yml");
            DailyTasks.getInstance().getLogger().info("IO Exception: " + e);
        }
    }

    public void markDateAutoComplete(String date, boolean markAs) {
        File pluginDataFile = createPluginData();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(pluginDataFile);
        if(markAs) {
            yml.set("auto-complete." + date, true);
        } else {
            yml.set("auto-complete." + date, null);
        }
        try {
            yml.save(pluginDataFile);
        } catch(IOException e) {
            DailyTasks.getInstance().getLogger().info("Cannot save pluginData.yml");
            DailyTasks.getInstance().getLogger().info("IO Exception: " + e);
        }
    }

    public List<String> getAutoCompleteDates() {
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

    public void loadMessages() {
        File messageFile = new File(DailyTasks.getInstance().getDataFolder(), "messages.yml");
        if(!messageFile.exists()) {
            DailyTasks.getInstance().saveResource("messages.yml", false);
        }
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(messageFile);
        ConfigurationSection section = yml.getConfigurationSection("messages");
        if(section == null) return;
        Messages messages = DailyTasks.getInstance().getMessages();
        messages.clearMessages();
        for(String key : section.getKeys(false)) {
            messages.addMessage(key, yml.getString("messages." + key));
        }
    }

    public File getTaskFile() {
        File taskFile = new File(DailyTasks.getInstance().getDataFolder(), "task-pool.yml");
        if(!taskFile.exists()) {
            DailyTasks.getInstance().saveResource("task-pool.yml", false);
        }
        return taskFile;
    }

    public void loadTasks() {
        File tasksFile = getTaskFile();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(tasksFile);
        ConfigurationSection section = yml.getConfigurationSection("tasks");
        if(section == null) return;
        for(String key : section.getKeys(false)) {
            if(!yml.getBoolean("tasks." + key + ".enabled")) {
                continue;
            }
            DailyTasks.getInstance().getTaskManager().getTaskPool().add(new TaskObject(yml.getString("tasks." + key + ".event"),
                    yml.getInt("tasks." + key + ".numberMin"), yml.getInt("tasks." + key + ".numberMax")));
            DailyTasks.getInstance().getLogger().info("Loaded task: " + key);
        }
    }

    public void addTaskToPool(TaskObject to, String id) {
        File tasksFile = getTaskFile();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(tasksFile);
        yml.set("tasks." + id + ".enabled", true);
        yml.set("tasks." + id + ".event", to.event);
        yml.set("tasks." + id + ".numberMin", to.min);
        yml.set("tasks." + id + ".numberMax", to.max);
        try {
            yml.save(tasksFile);
        } catch(IOException e) {
            DailyTasks.getInstance().getLogger().info("Cannot save task-pool.yml");
            DailyTasks.getInstance().getLogger().info("IO exception: " + e);
        }
    }

    public void loadRewards() {
        File rewardFile = new File(DailyTasks.getInstance().getDataFolder(), "reward-pool.yml");
        if(!rewardFile.exists()) {
            DailyTasks.getInstance().saveResource("reward-pool.yml", false);
        }
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(rewardFile);
        String[] rewardTypes = new String[]{"day", "month"};
        for(String rewardType : rewardTypes) {
            ConfigurationSection section = yml.getConfigurationSection(rewardType + "-rewards");
            if(section == null) continue;
            for(String key : section.getKeys(false)) {
                if(!yml.getBoolean(rewardType + "-rewards." + key + ".enabled")) continue;
                DailyTasks.getInstance().getTaskManager().getRewardPool()
                        .add( new RewardObject(yml.getString(rewardType + "-rewards." + key + ".command"),
                        yml.getInt(rewardType + "-rewards." + key + ".numberMin"),
                        yml.getInt(rewardType + "-rewards." + key + ".numberMax"),
                        (rewardType.equals("month")) )
                );
                DailyTasks.getInstance().getLogger().info("Loaded " + rewardType + " reward: " + key);
            }
        }
    }
}
