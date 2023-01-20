package pl.dailytasks.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerUtil {

    public static Player getPlayerByNick(String nick) {
        for(Player p : Bukkit.getOnlinePlayers()) {
            if(p.getName().equals(nick)) {
                return p;
            }
        }
        return null;
    }
}
