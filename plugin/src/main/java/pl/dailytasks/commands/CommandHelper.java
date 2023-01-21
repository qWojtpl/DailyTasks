package pl.dailytasks.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import pl.dailytasks.util.DateManager;

import java.util.ArrayList;
import java.util.List;

public class CommandHelper implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player) || !sender.hasPermission("dt.manage")) {
            return null;
        }
        List<String> completions = new ArrayList<>();
        if(args.length > 1) {
            if(args[0].equalsIgnoreCase("help")) {
                if(args.length == 2) {
                    completions.add("1");
                    completions.add("2");
                    completions.add("3");
                    completions.add("4");
                }
            } else if(args[0].equalsIgnoreCase("fakecalendar")) {
                if(args.length == 2) {
                    completions.add(String.valueOf(DateManager.getYear()));
                } else if(args.length == 3) {
                    completions.add(String.valueOf(DateManager.getMonth()));
                } else if(args.length == 4) {
                    completions.add(String.valueOf(DateManager.getDay()));
                } else if(args.length == 5) {
                    completions.add(String.valueOf(DateManager.getHour()));
                } else if(args.length == 6) {
                    completions.add(String.valueOf(DateManager.getMinute()));
                } else if(args.length == 7) {
                    completions.add(String.valueOf(DateManager.getSecond()));
                }
            } else if(args[0].equalsIgnoreCase("autocomplete") || args[0].equalsIgnoreCase("checkauto")) {
                if(args.length == 2) {
                    completions.add(String.valueOf(DateManager.getYear()));
                } else if(args.length == 3) {
                    completions.add(String.valueOf(DateManager.getMonth()));
                } else if(args.length == 4) {
                    completions.add(String.valueOf(DateManager.getDay()));
                }
            } else if(args[0].equalsIgnoreCase("complete")) {
                if(args.length == 2) {
                    completions.add("day");
                    completions.add("date");
                    completions.add("progress");
                } else if(args.length == 3) {
                    for(Player p : Bukkit.getOnlinePlayers()) {
                        completions.add(p.getName());
                    }
                } else if(args.length >= 4) {
                    if(args[1].equalsIgnoreCase("date")) {
                        if(args.length == 4) {
                            completions.add(String.valueOf(DateManager.getYear()));
                        } else if(args.length == 5) {
                            completions.add(String.valueOf(DateManager.getMonth()));
                        } else if(args.length == 6) {
                            completions.add(String.valueOf(DateManager.getDay()));
                        }
                    } else if(args[1].equalsIgnoreCase("progress")) {
                        if(args.length == 4) {
                            for(int i = 0; i < 3; i++) {
                                completions.add(String.valueOf(i));
                            }
                        }
                    }
                }
            } else if(args[0].equalsIgnoreCase("checkcompleted")) {
                if(args.length == 2) {
                    completions.add("day");
                    completions.add("date");
                } else if(args.length == 3) {
                    for(Player p : Bukkit.getOnlinePlayers()) {
                        completions.add(p.getName());
                    }
                } else if(args.length == 4) {
                    if(args[1].equalsIgnoreCase("day")) {
                        completions.add(String.valueOf(DateManager.getDay()));
                    }
                }
                if(args.length >= 4) {
                    if(args[1].equalsIgnoreCase("date")) {
                        if(args.length == 4) {
                            completions.add(String.valueOf(DateManager.getYear()));
                        } else if(args.length == 5) {
                            completions.add(String.valueOf(DateManager.getMonth()));
                        } else if(args.length == 6) {
                            completions.add(String.valueOf(DateManager.getDay()));
                        }
                    }
                }
            } else if(args[0].equalsIgnoreCase("checktasks") || args[0].equalsIgnoreCase("checkrewards")) {
                if(args.length == 2) {
                    completions.add(String.valueOf(DateManager.getYear()));
                }
                if(args.length == 3) {
                    completions.add(String.valueOf(DateManager.getMonth()));
                }
                if(args.length == 4) {
                    completions.add(String.valueOf(DateManager.getDay()));
                }
            }
        } else {
            completions.add("help");
            completions.add("reload");
            completions.add("fakecalendar");
            completions.add("removefake");
            completions.add("autocomplete");
            completions.add("checkauto");
            completions.add("complete");
            completions.add("checkcompleted");
            completions.add("checktasks");
            completions.add("checkrewards");
            completions.add("reserve");
            completions.add("taskpool");
            completions.add("rewardpool");
            completions.add("add");
        }
        return StringUtil.copyPartialMatches(args[args.length-1], completions, new ArrayList<>());
    }
}
