package pl.dailytasks.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.dailytasks.DailyTasks;
import pl.dailytasks.util.DateManager;
import pl.dailytasks.gui.GUIHandler;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            if(!checkPlayerPermission(sender, "td.manage") || args.length == 0) {
                GUIHandler.New((Player) sender);
                return true;
            }
        }
        if(args.length == 0) {
            ShowHelp(sender, 1);
            return true;
        }
        if(args[0].equalsIgnoreCase("fakecalendar")) {
            c_FakeCalendar(sender, args);
        } else if(args[0].equalsIgnoreCase("removefake")) {
            c_RemoveFakeCalendar(sender);
        } else {
            ShowHelp(sender, 1);
        }
        return true;
    }

    public static void ShowHelp(CommandSender sender, int page) {
        if(!checkPlayerPermission(sender, "dt.manage")) return;
        sender.sendMessage("§c<============ §eDailyTasks §c============>");
        switch(page) {
            case 1:
                sender.sendMessage("§b/dt §f- Shows daily tasks");
                break;
        }
        sender.sendMessage("§c<============ §eDailyTasks §c============>");
    }

    private static void c_FakeCalendar(CommandSender sender, String[] args) {
        if(!checkPlayerPermission(sender, "dt.fakecalendar")) return;
        if(args.length < 7) {
            sender.sendMessage(DailyTasks.getMessage("prefix") + " §cCorrect usage: /dt fakecalendar <year> <month> <day> <hour> <minute> <second>");
            return;
        }
        int[] i_args = new int[6];
        for(int i = 0; i < 6; i++) {
            i_args[i] = Integer.valueOf(args[i+1]);
        }
        DateManager.createFakeCalendar(i_args[0], i_args[1]-1, i_args[2], i_args[3], i_args[4], i_args[5]);
        sender.sendMessage(DailyTasks.getMessage("prefix") + " §aNow using fake calendar. Use /dt removefake to remove fake calendar.");
    }

    private static void c_RemoveFakeCalendar(CommandSender sender) {
        if(!checkPlayerPermission(sender, "dt.removefakecalendar")) return;
        DateManager.removeFakeCalendar();
        sender.sendMessage(DailyTasks.getMessage("prefix") + " §aNow using real calendar!");
    }

    public static boolean checkPlayerPermission(CommandSender sender, String permission) {
        if(!(sender instanceof Player)) return true;
        if(!sender.hasPermission(permission)) {
            sender.sendMessage("§cYou don't have permission!");
            return false;
        }
        return true;
    }

}
