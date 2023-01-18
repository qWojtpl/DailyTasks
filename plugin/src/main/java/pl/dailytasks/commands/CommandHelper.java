package pl.dailytasks.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class CommandHelper implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            return null;
        }
        List<String> completions = new ArrayList<>();
        return StringUtil.copyPartialMatches(args[args.length-1], completions, new ArrayList<>());
    }
}
