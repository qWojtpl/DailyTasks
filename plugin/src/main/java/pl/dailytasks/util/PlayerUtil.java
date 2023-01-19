package pl.dailytasks.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.dailytasks.DailyTasks;

public class PlayerUtil {

    public static boolean checkPlayerPermission(CommandSender sender, String permission) {
        if(!(sender instanceof Player)) return true;
        if(!sender.hasPermission(permission)) {
            sender.sendMessage(DailyTasks.getMessage("prefix") + " §cYou don't have permission!");
            return false;
        }
        return true;
    }

    public static Player getPlayerByNick(String nick) {
        for(Player p : Bukkit.getOnlinePlayers()) {
            if(p.getName().equals(nick)) {
                return p;
            }
        }
        return null;
    }
}
