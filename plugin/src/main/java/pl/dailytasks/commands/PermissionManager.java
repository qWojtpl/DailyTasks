package pl.dailytasks.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import pl.dailytasks.DailyTasks;

import java.util.HashMap;

public class PermissionManager {

    private final HashMap<String, Permission> permissions = new HashMap<>();

    public void registerPermission(String permission, String description) {
        Permission perm = new Permission(permission, description);
        DailyTasks.getInstance().getServer().getPluginManager().addPermission(perm);
        permissions.put(permission, perm);
    }

    public Permission getPermission(String permission) {
        if(this.permissions.containsKey(permission)) {
            return this.permissions.get(permission);
        }
        DailyTasks.getInstance().getLogger().info("Plugin is trying to access null permission! " +
                "Please report it here https://github.com/qWojtpl/DailyTasks/issues");
        return null;
    }

    public boolean checkSenderPermission(CommandSender sender, Permission permission) {
        if(permission == null) return true;
        if(!(sender instanceof Player)) return true;
        if(!sender.hasPermission(permission)) {
            sender.sendMessage(DailyTasks.getInstance().getMessages().getMessage("prefix") + " §cYou don't have permission!");
            return false;
        }
        return true;
    }

    public void loadPermissions() {
        registerPermission("dt.use", "Use DailyTasks");
        registerPermission("dt.manage", "Manage DailyTasks");
        registerPermission("dt.reload", "Reload DailyTasks configuration");
        registerPermission("dt.fakecalendar", "Create DailyTasks fake calendar");
        registerPermission("dt.removefake", "Remove DailyTasks fake calendar - use normal calendar");
        registerPermission("dt.setautocomplete", "Set DailyTasks auto-complete date");
        registerPermission("dt.checkauto", "Check if date is marked as auto-complete in DailyTasks");
        registerPermission("dt.complete.day", "Complete this day for player in DailyTasks");
        registerPermission("dt.complete.date", "Complete date for player in DailyTasks");
        registerPermission("dt.complete.task", "Complete task index for player in DailyTasks");
        registerPermission("dt.checkcomplete.day", "Check if player completed that day in DailyTasks");
        registerPermission("dt.checkcomplete.date", "Check if player completed that date in DailyTasks");
        registerPermission("dt.checkcomplete.task", "Check if player completed task index in specified date in DailyTasks");
        registerPermission("dt.checktasks", "Check what tasks was/is in that date in DailyTasks");
        registerPermission("dt.checkrewards", "Check what rewards was/is in that date in DailyTasks");
        registerPermission("dt.taskpool", "See task pool in DailyTasks");
        registerPermission("dt.rewardpool", "See reward pool in DailyTasks");
        registerPermission("dt.reserve.task", "Reserve day tasks for specific date in DailyTasks");
        registerPermission("dt.reserve.reward.day", "Reserve day reward for specific date in DailyTasks");
        registerPermission("dt.reserve.reward.month", "Reserve month reward for specific date in DailyTasks");
        registerPermission("dt.add.task", "Add task to task pool in DailyTasks");
        registerPermission("dt.add.reward.day", "Add day reward to reward pool in DailyTasks");
        registerPermission("dt.add.reward.month", "Add month reward to reward pool in DailyTasks");
    }

}
