package pl.dailytasks.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.dailytasks.DailyTasks;
import pl.dailytasks.data.DataHandler;
import pl.dailytasks.tasks.PlayerTasks;
import pl.dailytasks.tasks.TaskManager;
import pl.dailytasks.tasks.TaskObject;
import pl.dailytasks.util.DateManager;
import pl.dailytasks.gui.GUIHandler;
import pl.dailytasks.util.PlayerUtil;

import java.util.ArrayList;
import java.util.List;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if(!PermissionManager.checkSenderPermission(sender, PermissionManager.getPermission("dt.use"))) return true;
            if (!PermissionManager.checkSenderPermission(sender, PermissionManager.getPermission("dt.manage")) || args.length == 0) {
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
        } else if(args[0].equalsIgnoreCase("complete")) {
            c_Complete(sender, args);
        } else if(args[0].equalsIgnoreCase("checkcompleted")) {
            c_CheckComplete(sender, args);
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
            sender.sendMessage(DailyTasks.getMessage("prefix") + " §cCorrect usage: /dt help <page>");
        } finally {
            ShowHelp(sender, page);
        }
    }

    private static void c_Reload(CommandSender sender) {
        if(!PermissionManager.checkSenderPermission(sender, PermissionManager.getPermission("dt.reload"))) return;
        DailyTasks.main.Reload();
        sender.sendMessage(DailyTasks.getMessage("prefix") + " §aReloaded configuration!");
    }

    private static void c_FakeCalendar(CommandSender sender, String[] args) {
        if(!PermissionManager.checkSenderPermission(sender, PermissionManager.getPermission("dt.fakecalendar"))) return;
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
        if(!PermissionManager.checkSenderPermission(sender, PermissionManager.getPermission("dt.removefake"))) return;
        if(DateManager.isUsingFakeCalendar()) {
            DateManager.removeFakeCalendar();
            sender.sendMessage(DailyTasks.getMessage("prefix") + " §aNow using real calendar!");
        } else {
            sender.sendMessage(DailyTasks.getMessage("prefix") + " §cYou're not using fake calendar!");
        }
    }

    private static void c_AutoComplete(CommandSender sender, String[] args) {
        if(!PermissionManager.checkSenderPermission(sender, PermissionManager.getPermission("dt.setautocomplete"))) return;
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
        if(!PermissionManager.checkSenderPermission(sender, PermissionManager.getPermission("dt.checkauto"))) return;
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

    public static void c_Complete(CommandSender sender, String[] args) {
        if(args.length < 2) {
            ShowHelp(sender, 2);
            return;
        }
        if(args[1].equalsIgnoreCase("day")) {
            if(!PermissionManager.checkSenderPermission(sender, PermissionManager.getPermission("dt.complete.day"))) return;
            if(args.length < 3) {
                sender.sendMessage(DailyTasks.getMessage("prefix") + " §cCorrect usage: /dt complete day <nick>");
                return;
            }
            Player p = PlayerUtil.getPlayerByNick(args[2]);
            if(p == null) {
                sender.sendMessage(DailyTasks.getMessage("prefix") + " §cCannot find this player!");
                return;
            }
            PlayerTasks pt = PlayerTasks.Create(p);
            int i = 0;
            for(TaskObject to : TaskManager.getTodayTasks()) {
                pt.getProgress().set(i, to.currentRandom); // Set progress to max
                DataHandler.updatePlayerProgress(pt, i); // Save progress
                to.Complete(pt, i); // Complete task
                i++;
            }
            TaskManager.CheckRewards(pt); // Check rewards
            sender.sendMessage(DailyTasks.getMessage("prefix") + " §aAdded this day as completed day for " + p.getName() + "!");
        } else if(args[1].equalsIgnoreCase("date")) {
            if(!PermissionManager.checkSenderPermission(sender, PermissionManager.getPermission("dt.complete.date"))) return;
            if(args.length < 6) {
                sender.sendMessage(DailyTasks.getMessage("prefix") + " §cCorrect usage: /dt complete date <nick> <Y> <M> <D>");
                return;
            }
            Player p = PlayerUtil.getPlayerByNick(args[2]);
            if(p == null) {
                sender.sendMessage(DailyTasks.getMessage("prefix") + " §cCannot find this player!");
                return;
            }
            PlayerTasks pt = PlayerTasks.Create(p);
            String date = args[3] + "/" + args[4] + "/" + args[5];
            List<Integer> threeTasks = new ArrayList<>();
            for(int i = 0; i < 3; i++) {
                threeTasks.add(i);
                DataHandler.addPlayerCompletedTaskByDate(pt, i, date);
            }
            pt.completedTasks.put(DateManager.getFormattedDate(date), threeTasks);
            sender.sendMessage(DailyTasks.getMessage("prefix") + " §aAdded " + date + " as completed date for " + p.getName() + "!");
        } else if(args[1].equalsIgnoreCase("progress")) {
            if(!PermissionManager.checkSenderPermission(sender, PermissionManager.getPermission("dt.complete.progress"))) return;
            if(args.length < 4) {
                sender.sendMessage(DailyTasks.getMessage("prefix") + " §cCorrect usage: /dt complete progress <nick> <index>");
                return;
            }
            Player p = PlayerUtil.getPlayerByNick(args[2]);
            if(p == null) {
                sender.sendMessage(DailyTasks.getMessage("prefix") + " §cCannot find this player!");
                return;
            }
            PlayerTasks pt = PlayerTasks.Create(p);
            int index;
            try {
                index = Integer.parseInt(args[3]);
            } catch(NumberFormatException e) {
                sender.sendMessage(DailyTasks.getMessage("prefix") + " §cCorrect usage: /dt complete progress <nick> <index>");
                return;
            }
            TaskObject task = TaskManager.getTodayTasks().get(index); // Get task from index
            pt.getProgress().set(index, task.currentRandom); // Set progress to max
            DataHandler.updatePlayerProgress(pt, index); // Save progress
            task.Complete(pt, index); // Complete task and save it
            TaskManager.CheckRewards(pt); // Check rewards
            sender.sendMessage(DailyTasks.getMessage("prefix") + " §aMarked progress " + index + " of this day as completed for " + p.getName() + "!");
        }
    }

    private static void c_CheckComplete(CommandSender sender, String[] args) {
        if(args.length < 2) {
            ShowHelp(sender, 2);
            return;
        }
        if(args[1].equalsIgnoreCase("day")) {
            if(!PermissionManager.checkSenderPermission(sender, PermissionManager.getPermission("dt.checkcomplete.day"))) return;
            if(args.length < 4) {
                sender.sendMessage(DailyTasks.getMessage("prefix") + " §cCorrect usage: /dt checkcompleted day <nick> <D>");
                return;
            }
            Player p = PlayerUtil.getPlayerByNick(args[2]);
            PlayerTasks pt = PlayerTasks.Create(p);
            int day;
            try {
                day = Integer.parseInt(args[3]);
            } catch(NumberFormatException e) {
                sender.sendMessage(DailyTasks.getMessage("prefix") + " §cCorrect usage: /dt checkcompleted day <nick> <D>");
                return;
            }
            if(pt.checkIfCompletedDay(day)) {
                sender.sendMessage(DailyTasks.getMessage("prefix") + " §a" + p.getName() + " completed " + day + " day tasks!");
            } else {
                sender.sendMessage(DailyTasks.getMessage("prefix") + " §c" + p.getName() + " doesn't completed " + day + " day tasks!");
            }
        } else if(args[1].equalsIgnoreCase("date")) {
            if(!PermissionManager.checkSenderPermission(sender, PermissionManager.getPermission("dt.checkcomplete.date"))) return;
            if(args.length < 6) {
                sender.sendMessage(DailyTasks.getMessage("prefix") + " §cCorrect usage: /dt checkcompleted date <nick> <Y> <M> <D>");
                return;
            }
            Player p = PlayerUtil.getPlayerByNick(args[2]);
            PlayerTasks pt = PlayerTasks.Create(p);
            String formatDate = args[3] + "/" + args[4] + "/" + args[5];
            if(pt.checkIfCompletedDayByDate(formatDate)) {
                sender.sendMessage(DailyTasks.getMessage("prefix") + " §a" + p.getName() + " completed " + formatDate + " tasks!");
            } else {
                sender.sendMessage(DailyTasks.getMessage("prefix") + " §c" + p.getName() + " doesn't completed " + formatDate + " tasks!");
            }
            if(DataHandler.deleteOldData) {
                sender.sendMessage(DailyTasks.getMessage("prefix") + " §cATTENTION! Delete old data is turned on, " +
                        "so you can't see old player's tasks");
            }
        } else {
            ShowHelp(sender, 2);
        }
    }

    public static void ShowHelp(CommandSender sender, int page) {
        if (!PermissionManager.checkSenderPermission(sender, PermissionManager.getPermission("dt.manage"))) return;
        sender.sendMessage("§6<=============== §r§cDailyTasks §6===============>");
        switch (page) {
            case 1:
                sender.sendMessage("§c/dt §e- Shows daily tasks");
                sender.sendMessage("§c/dt reload §e- Reload configuration");
                sender.sendMessage("§c/dt fakecalendar §6<§cY§6> <§cM§6> <§cD§6> <§ch§6> <§cm§6> <§cs§6> §e- Set calendar to values");
                sender.sendMessage("§c/dt removefake §e- Use normal calendar");
                sender.sendMessage("§c/dt autocomplete §6<§cY§6> <§cM§6> <§cD§6> §e- Toggle date as auto-complete");
                break;
            case 2:
                sender.sendMessage("§c/dt checkauto §6<§cY§6> <§cM§6> <§cD§6> §e- Check if date is marked as auto-complete");
                sender.sendMessage("§c/dt complete day §6<§cnick§6> §e- Complete this day for player");
                sender.sendMessage("§c/dt complete date §6<§cnick§6> <§cY§6> <§cM§6> <§cD§6> §e- Complete date for player (player won't get reward)");
                sender.sendMessage("§c/dt complete progress §6<§cnick§6> <§cindex§6> §e- Complete progress for player");
                sender.sendMessage("§c/dt checkcompleted day §6<§cnick§6> §6<§cD§6> §e- Check if player completed tasks that day");
                break;
            case 3:
                sender.sendMessage("§c/dt checkcompleted date §6<§cnick§6> <§cM§6> §e- Check if player completed this date");
                sender.sendMessage("§c/dt checktasks §6<§cY§6> <§cM§6> <§cD§6> §e- Check what tasks was/is in this date");
                sender.sendMessage("§c/dt reservetask §6<§cY§6> <§cM§6> <§cD§6> <task> §e- Reserve task for date");
                sender.sendMessage("§c/dt taskpool §e- See task pool");
                sender.sendMessage("§c/dt rewardpool day §e- See reward pool (for day-rewards)");
                break;
            case 4:
                sender.sendMessage("§c/dt rewardpool month §e- See reward pool (for month-rewards)");
                sender.sendMessage("§c/dt add task §6<§ctask§6> §e- Add task to task pool");
                sender.sendMessage("§c/dt add reward day §6<§ccommand§6> §e- Add reward to reward pool (for days)");
                sender.sendMessage("§c/dt add reward month §6<§ccommand§6> §e- Add reward to reward pool (for month)");
                break;
            default:
                sender.sendMessage("§cNot found page: " + page);
        }
        sender.sendMessage("§6<============ §r§cShowing page " + page + "/4 §6============>");
    }

}
