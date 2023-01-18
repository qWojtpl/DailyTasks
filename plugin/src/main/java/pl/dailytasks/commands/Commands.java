package pl.dailytasks.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.dailytasks.DailyTasks;
import pl.dailytasks.data.DataHandler;
import pl.dailytasks.util.DateManager;
import pl.dailytasks.gui.GUIHandler;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (!checkPlayerPermission(sender, "td.manage") || args.length == 0) {
                GUIHandler.New((Player) sender);
                return true;
            }
        }
        if (args.length == 0) {
            ShowHelp(sender, 1);
            return true;
        }
        if (args[0].equalsIgnoreCase("help")) {
            c_Help(sender, args);
        } else if(args[0].equalsIgnoreCase("reload")) {
            c_Reload(sender);
        } else if(args[0].equalsIgnoreCase("fakecalendar")) {
            c_FakeCalendar(sender, args);
        } else if(args[0].equalsIgnoreCase("removefake")) {
            c_RemoveFakeCalendar(sender);
        } else if(args[0].equalsIgnoreCase("autocomplete")) {
            c_AutoComplete(sender, args);
        } else if(args[0].equalsIgnoreCase("checkauto")) {
            c_CheckAuto(sender, args);
        } else {
            ShowHelp(sender, 1);
        }
        return true;
    }

    private static void c_Help(CommandSender sender, String[] args) {
        if(args.length < 2) {
            ShowHelp(sender, 1);
            return;
        }
        int page = 1;
        try {
            page = Integer.valueOf(args[1]);
        } catch(NumberFormatException e) {
            sender.sendMessage(DailyTasks.getMessage("prefix") + "§cCorrect usage: /dt help <page>");
        } finally {
            ShowHelp(sender, page);
        }
    }

    private static void c_Reload(CommandSender sender) {
        if(!checkPlayerPermission(sender, "dt.reload")) return;
        DailyTasks.main.Reload();
        sender.sendMessage(DailyTasks.getMessage("prefix") + " §aReloaded configuration!");
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
        if(DateManager.isUsingFakeCalendar()) {
            DateManager.removeFakeCalendar();
            sender.sendMessage(DailyTasks.getMessage("prefix") + " §aNow using real calendar!");
        } else {
            sender.sendMessage(DailyTasks.getMessage("prefix") + " §cYou're not using fake calendar!");
        }
    }

    private static void c_AutoComplete(CommandSender sender, String[] args) {
        if(!checkPlayerPermission(sender, "dt.setautocomplete")) return;
        if(args.length < 4) {
            sender.sendMessage(DailyTasks.getMessage("prefix") + " §cCorrect usage: /dt autocomplete <Y> <M> <D>");
            return;
        }
        String date = args[1] + "/" + args[2] + "/" + args[3];
        if(DataHandler.getAutoCompleteDates().contains(date)) {
            DataHandler.markDateAutoComplete(date, false);
            sender.sendMessage(DailyTasks.getMessage("prefix") + " §aMarked " + date + " as §4§lNOT AUTO-COMPLETE");
        } else {
            DataHandler.markDateAutoComplete(date, true);
            sender.sendMessage(DailyTasks.getMessage("prefix") + " §aMarked " + date + " as §a§lAUTO-COMPLETE");
        }
    }

    private static void c_CheckAuto(CommandSender sender, String[] args) {
        if(!checkPlayerPermission(sender, "dt.checkauto")) return;
        if(args.length < 4) {
            sender.sendMessage(DailyTasks.getMessage("prefix") + " §cCorrect usage: /dt checkauto <Y> <M> <D>");
            return;
        }
        String date = args[1] + "/" + args[2] + "/" + args[3];
        if(DataHandler.getAutoCompleteDates().contains(date)) {
            sender.sendMessage(DailyTasks.getMessage("prefix") + " §aDate " + date + " is marked as §a§lAUTO-COMPLETE");
        } else {
            sender.sendMessage(DailyTasks.getMessage("prefix") + " §aDate " + date + " is marked as §4§lNOT AUTO-COMPLETE");
        }
    }

    public static void ShowHelp(CommandSender sender, int page) {
        if(!checkPlayerPermission(sender, "dt.manage")) return;
        sender.sendMessage("§6<=============== §r§cDailyTasks §6===============>");
        switch(page) {
            case 1:
                sender.sendMessage("§c/dt §e- Shows daily tasks");
                sender.sendMessage("§c/dt reload §e- Reload configuration");
                sender.sendMessage("§c/dt fakecalendar <Y> <M> <D> <h> <m> <s> §e- Set calendar to values");
                sender.sendMessage("§c/dt removefake §e- Use normal calendar");
                sender.sendMessage("§c/dt autocomplete <Y> <M> <D> §e- Toggle date as auto-complete");
                break;
            case 2:
                sender.sendMessage("§c/dt checkauto <Y> <M> <D> §e- Check if date is marked as auto-complete");
                sender.sendMessage("§c/dt complete day <nick> §e- Complete this day for player (player won't get reward)");
                sender.sendMessage("§c/dt complete date <nick> <Y> <M> <D> §e- Complete date for player (player won't get reward)");
                sender.sendMessage("§c/dt complete progress <nick> <index> §e- Complete progress for player (player won't get reward if this is last progress)");
                sender.sendMessage("§c/dt checkcompleted day <nick> §e- Check if player completed tasks this day");
                break;
            case 3:
                sender.sendMessage("§c/dt checkcompleted date <nick> <Y> <M> <D> §e- Check if player completed this date");
                sender.sendMessage("§c/dt checktasks <Y> <M> <D> §e- Check what tasks was/is in this date");
                sender.sendMessage("§c/dt reservetask <Y> <M> <D> <task> §e- Reserve task for date");
                sender.sendMessage("§c/dt taskpool §e- See task pool");
                sender.sendMessage("§c/dt rewardpool day §e- See reward pool (for day-rewards)");
                break;
            case 4:
                sender.sendMessage("§c/dt rewardpool month §e- See reward pool (for month-rewards)");
                sender.sendMessage("§c/dt add task <task> §e- Add task to task pool");
                sender.sendMessage("§c/dt add reward day <command> §e- Add reward to reward pool (for days)");
                sender.sendMessage("§c/dt add reward month <command> §e- Add reward to reward pool (for month)");
                break;
            default:
                sender.sendMessage("§cNot found page: " + page);
        }
        sender.sendMessage("§6<============ §r§cShowing page " + page + "/4 §6============>");
    }

    public static boolean checkPlayerPermission(CommandSender sender, String permission) {
        if(!(sender instanceof Player)) return true;
        if(!sender.hasPermission(permission)) {
            sender.sendMessage(DailyTasks.getMessage("prefix") + " §cYou don't have permission!");
            return false;
        }
        return true;
    }

}
