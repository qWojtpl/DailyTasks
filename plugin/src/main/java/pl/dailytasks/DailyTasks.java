package pl.dailytasks;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pl.dailytasks.commands.Commands;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public final class DailyTasks extends JavaPlugin {

    public static DailyTasks main;
    public static YamlConfiguration messages;
    public static HashMap<Player, PlayerTasks> playerTaskList = new HashMap<>();

    @Override
    public void onEnable() {
        main = this;
        getServer().getPluginManager().registerEvents(new Events(), this);
        getCommand("dailytasks").setExecutor(new Commands());
        loadMessages();
        getLogger().info("Loaded.");
        for(Player p : Bukkit.getOnlinePlayers()) {
            new PlayerTasks(p);
        }
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
                yml.save(messageFile);
            } catch(IOException e) {
                DailyTasks.main.getLogger().info("IO exception: " + e);
            }
        }
        messages = YamlConfiguration.loadConfiguration(messageFile);
    }

    public static String getMessage(String path) {
        return messages.getString("messages." + path);
    }



}
