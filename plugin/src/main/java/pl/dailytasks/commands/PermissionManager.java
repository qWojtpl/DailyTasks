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
        DailyTasks.main.getLogger().info("Plugin is trying to access null permission! " +
                "Please report it here https://github.com/qWojtpl/DailyTasks/issues");
        return null;
    }

    public static boolean checkSenderPermission(CommandSender sender, Permission permission) {
        if(permission == null) return true;
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
        registerPermission("dt.checkcomplete.day", "Check if player completed that day in DailyTasks");
        registerPermission("dt.checkcomplete.date", "Check if player completed that date in DailyTasks");
        registerPermission("dt.checktasks", "Check what tasks was/is in that date in DailyTasks");
        registerPermission("dt.checkrewards", "Check what rewards was/is in that date in DailyTasks");
        registerPermission("dt.taskpool", "See task pool in DailyTasks");
        registerPermission("dt.rewardpool", "See reward pool in DailyTasks");
        registerPermission("dt.add.task", "Add task to task pool in DailyTasks");
    }

}
