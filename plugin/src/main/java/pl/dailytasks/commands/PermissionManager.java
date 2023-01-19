package pl.dailytasks.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import pl.dailytasks.DailyTasks;

import java.util.HashMap;

public class PermissionManager {

    public static HashMap<String, Permission> permissions = new HashMap<>();

    public static void registerPermission(String permission, String description) {
        Permission perm = new Permission(permission, description);
        DailyTasks.main.getServer().getPluginManager().addPermission(perm);
        permissions.put(permission, perm);
    }

    public static Permission getPermission(String permission) {
        if(PermissionManager.permissions.containsKey(permission)) {
            return PermissionManager.permissions.get(permission);
        }
        return null;
    }

    public static boolean checkSenderPermission(CommandSender sender, Permission permission) {
        if(permission == null) {
            DailyTasks.main.getLogger().info("Plugin is trying to access null permission! " +
                    "Please report it here https://github.com/qWojtpl/DailyTasks/issues");
            return true;
        }
        if(!(sender instanceof Player)) return true;
        if(!sender.hasPermission(permission)) {
            sender.sendMessage(DailyTasks.getMessage("prefix") + " §cYou don't have permission!");
            return false;
        }
        return true;
    }

    public static void loadPermissions() {
        registerPermission("dt.use", "Use DailyTasks");
        registerPermission("dt.manage", "Manage DailyTasks");
        registerPermission("dt.reload", "Reload DailyTasks configuration");
        registerPermission("dt.fakecalendar", "Create DailyTasks fake calendar");
        registerPermission("dt.removefake", "Remove DailyTasks fake calendar - use normal calendar");
        registerPermission("dt.setautocomplete", "Set DailyTasks auto-complete date");
        registerPermission("dt.checkauto", "Check if date is marked as auto-complete in DailyTasks");
        registerPermission("dt.complete.day", "Complete this day for player in DailyTasks");
        registerPermission("dt.complete.date", "Complete date for player in DailyTasks");
        registerPermission("dt.complete.progress", "Complete progress for player in DailyTasks");
    }

}
