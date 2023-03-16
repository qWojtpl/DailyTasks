package pl.dailytasks.data;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.A;
import pl.dailytasks.DailyTasks;
import pl.dailytasks.gui.GUIHandler;
import pl.dailytasks.tasks.PlayerTasks;
import pl.dailytasks.tasks.RewardObject;
import pl.dailytasks.tasks.TaskManager;
import pl.dailytasks.tasks.TaskObject;
import pl.dailytasks.util.DateManager;
import pl.dailytasks.util.Messages;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class DataHandler {

    private boolean deleteOldData;
    private boolean useYAML;
    private boolean useSQL;
    private boolean logSave;
    private int saveInterval = 300;
    private int saveTask = -1;
    private final HashMap<String, YamlConfiguration> playerYAML = new HashMap<>();
    private final HashMap<String, String> SQLInfo = new HashMap<>();
    private final HashMap<String, PlayerTasks> pendingYAMLs = new HashMap<>();

    public void load() {
        GUIHandler.closeAllInventories(); // Close all GUI inventories
        saveAll(false);
        DailyTasks.getInstance().getPlayerTaskList().clear();
        DailyTasks.getInstance().setLastRandomizedDate("");
        DailyTasks.getInstance().setDataCheckInitialized(false);
        TaskManager tm = DailyTasks.getInstance().getTaskManager();
        tm.getSourceDayReward().clear();
        tm.getSourceMonthReward().clear();
        tm.getSourceTaskList().clear();
        tm.getTaskPool().clear();
        tm.getRewardPool().clear();
        loadConfig();
        if(DailyTasks.getInstance().isForcedDisable()) return;
        loadCalendar(); // Load calendar (if using fake set calendar as this fake one)
        loadMessages(); // Load messages
        loadTasks(); // Load task pool
        if(DailyTasks.getInstance().isForcedDisable()) return;
        loadRewards(); // Load rewards
        if(DailyTasks.getInstance().isForcedDisable()) return;
        loadPluginData(); // Load plugin data (task history, rewards history, options, calendar etc)
        for(Player p : Bukkit.getOnlinePlayers()) {
            PlayerTasks.Create(p); // Create PlayerTasks for all players on the server
        }
    }

    public void addToPending(PlayerTasks pt) {
        getPendingYAMLs().put(pt.getPlayer().getName(), pt);
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
        if(useYAML) {
            File playerFile = createPlayerFile(p); // Get player file (and create it if not exists)
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(playerFile); // Load YAML
            getPlayerYAML().put(p.getName(), yml);
            loadPlayerAutoComplete(pt); // Load auto-complete days
            ConfigurationSection section = yml.getConfigurationSection("saved"); // Get YAML section
            if (section == null) return;
            for (String date : section.getKeys(false)) { // Loop through section keys (dates, eg. '2023/1/18')
                if (deleteOldData && canDeleteOldData(date)) {
                    yml.set("saved." + date, null);
                    continue;
                }
                List<Integer> completed = new ArrayList<>(); // Player's completed tasks
                List<Integer> progress = new ArrayList<>(); // Player's progress
                for (int i = 0; i < 3; i++) { // Loop through daily tasks
                    if (yml.getInt("saved." + date + "." + i + ".c") == 1) { // If task is completed...
                        completed.add(i); // Add to completed tasks
                    }
                    progress.add(yml.getInt("saved." + date + "." + i + ".p")); // Add progress - if not set - default int value is 0
                }
                pt.getSourceCompletedTasks().put(date, completed); // Put completed tasks as date key
                pt.getSourceProgress().put(date, progress); // Put tasks progress as date key
            }
            try {
                yml.save(playerFile);
            } catch (IOException e) {
                DailyTasks.getInstance().getLogger().info("Cannot save " + pt.getPlayer().getName() + ".yml");
                DailyTasks.getInstance().getLogger().info("IO Exception: " + e);
            }
        }
    }

    public void saveYAML(boolean async) {
        if(async) {
            Bukkit.getScheduler().runTaskAsynchronously(DailyTasks.getInstance(), () -> {
                HashMap<String, PlayerTasks> yamls = new HashMap<>(getPendingYAMLs());
                getPendingYAMLs().clear();
                for(String key : yamls.keySet()) {
                    PlayerTasks pt = yamls.get(key);
                    try {
                        getPlayerYAML().get(key).save(createPlayerFile(pt.getPlayer()));
                    } catch(IOException e) {
                        DailyTasks.getInstance().getLogger().info("Cannot save " + getPendingYAMLs().get(key).getPlayer().getName() + ".yml");
                        DailyTasks.getInstance().getLogger().info("IO Exception: " + e);
                    }
                }
            });
        } else {
            HashMap<String, PlayerTasks> yamls = new HashMap<>(getPendingYAMLs());
            getPendingYAMLs().clear();
            for(String key : yamls.keySet()) {
                PlayerTasks pt = yamls.get(key);
                try {
                    getPlayerYAML().get(key).save(createPlayerFile(pt.getPlayer()));
                } catch(IOException e) {
                    DailyTasks.getInstance().getLogger().info("Cannot save " + getPendingYAMLs().get(key).getPlayer().getName() + ".yml");
                    DailyTasks.getInstance().getLogger().info("IO Exception: " + e);
                }
            }
        }
    }

    public void saveAll(boolean async) {
        if(logSave) {
            DailyTasks.getInstance().getLogger().info("Saving data...");
        }
        if(useYAML) {
            saveYAML(async);
        }
        if(useSQL) {

        }
    }

    public void loadPlayerAutoComplete(PlayerTasks pt) {
        File autoCompleteFile = createPluginData();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(autoCompleteFile);
        ConfigurationSection section = yml.getConfigurationSection("auto-complete");
        if(section == null) return;
        for(String key : section.getKeys(false)) {
            if(!yml.getBoolean("auto-complete." + key)) continue;
            for(int i = 0; i < 3; i++) {
                addPlayerCompletedTaskByDate(pt, i, key);
                List<Integer> list = new ArrayList<>();
                for(int j = 0; j < 3; j++) {
                    list.add(j);
                }
                pt.getSourceCompletedTasks().put(key, list);
            }
        }
    }

    public void addPlayerCompletedTask(PlayerTasks pt, int index) {
        YamlConfiguration yml = getPlayerYAML().get(pt.getPlayer().getName());
        DateManager dm = DailyTasks.getInstance().getDateManager();
        yml.set("saved." + dm.getFormattedDate("%Y/%M/%D") + "." + index + ".c", 1);
        addToPending(pt);
    }

    public void addPlayerCompletedTaskByDate(PlayerTasks pt, int index, String date) {
        YamlConfiguration yml = getPlayerYAML().get(pt.getPlayer().getName());
        yml.set("saved." + date + "." + index + ".c", 1);
        addToPending(pt);
    }

    public void updatePlayerProgress(PlayerTasks pt, int index) {
        YamlConfiguration yml = getPlayerYAML().get(pt.getPlayer().getName());
        DateManager dm = DailyTasks.getInstance().getDateManager();
        yml.set("saved." + dm.getFormattedDate("%Y/%M/%D") + "." + index + ".p", pt.getProgress().get(index));
        addToPending(pt);
    }

    public File getPlayerFile(Player p) {
        return new File(DailyTasks.getInstance().getDataFolder(), "/playerData/" + p.getName() + ".yml");
    }

    public void loadCalendar() {
        File pluginDataFile = createPluginData();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(pluginDataFile);
        DateManager dm = DailyTasks.getInstance().getDateManager();
        dm.removeFakeCalendar();
        if(yml.getString("data.fakeCalendar") != null) {
            String args[] = yml.getString("data.fakeCalendar").split(" ");
            int i_args[] = new int[6];
            for(int i = 0; i < i_args.length; i++) {
                i_args[i] = Integer.parseInt(args[i]);
            }
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
                if(deleteOldData && canDeleteOldData(key)) {
                    yml.set("history." + key, null);
                    continue;
                }
                List<String> tasks = yml.getStringList("history." + key);
                List<TaskObject> initializedTasks = new ArrayList<>();
                int i = 0;
                for(String event : tasks) {
                    i++;
                    if(i > 3) {
                        DailyTasks.getInstance().getLogger().warning("Broken day! Too many tasks! " +
                                "Look at pluginData history - " + key);
                        break;
                    }
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
                    if(deleteOldData && canDeleteOldData(key)) {
                        yml.set(rewardType + "-reward-history." + key, null);
                        continue;
                    }
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
        String date = yml.getString("data.lastRandomized");
        if(date == null) {
            date = "";
        }
        DailyTasks.getInstance().setLastRandomizedDate(date);
        DailyTasks.getInstance().runDateCheck();
        try {
            yml.save(pluginDataFile);
        } catch(IOException e) {
            DailyTasks.getInstance().getLogger().info("Cannot save pluginData.yml");
            DailyTasks.getInstance().getLogger().info("IO Exception: " + e);
        }
    }

    public void loadConfig() {
        File configFile = new File(DailyTasks.getInstance().getDataFolder(), "config.yml");
        if(!configFile.exists()) {
            DailyTasks.getInstance().saveResource("config.yml", false);
        }
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(configFile);
        useYAML = yml.getBoolean("config.useYAML");
        useSQL = yml.getBoolean("config.useSQL");
        saveInterval = yml.getInt("config.saveInterval");
        logSave = yml.getBoolean("config.logSave");
        deleteOldData = yml.getBoolean("config.deleteOldData");
        if(useYAML && useSQL) {
            DailyTasks.getInstance().getLogger().warning("Attention! " +
                    "You're using YAML and SQL to save data at the same time. It can cause errors.");
        }
        getSQLInfo().put("host", yml.getString("sql.host"));
        getSQLInfo().put("user", yml.getString("sql.user"));
        getSQLInfo().put("password", yml.getString("sql.password"));
        getSQLInfo().put("database", yml.getString("sql.database"));
        getSQLInfo().put("port", String.valueOf(yml.getInt("sql.port")));
        if(saveTask != -1) {
            DailyTasks.getInstance().getServer().getScheduler().cancelTask(saveTask);
            saveTask = DailyTasks.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(DailyTasks.getInstance(), () ->
                saveAll(true), 20L * saveInterval, 20L * saveInterval);
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
        if(section == null) {
            tooLittle();
            return;
        }
        if(section.getKeys(false).size() < 3) {
            tooLittle();
            return;
        }
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
            if(section == null) {
                tooLittle();
                continue;
            }
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

    private boolean canDeleteOldData(String date) {
        DateManager dm = DailyTasks.getInstance().getDateManager();
        String[] dateArray = date.split("/");
        return (Integer.parseInt(dateArray[0]) <= dm.getYear() && Integer.parseInt(dateArray[1]) < dm.getMonth());
    }

    private void tooLittle() {
        DailyTasks.getInstance().getLogger().severe(
                "DailyTasks must contain at least 3 tasks in task pool, " +
                        "1 reward in daily-reward pool and 1 reward in monthly-reward pool to work properly. " +
                        "One or more of this requirements aren't completed. Can't keep up this plugin.");
        DailyTasks.getInstance().forceDisable();
    }
}
