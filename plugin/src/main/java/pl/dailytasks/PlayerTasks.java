package pl.dailytasks;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerTasks {

    public Player player;

    public List<Integer> completedDays = new ArrayList<>();

    public PlayerTasks(Player p) {
        this.player = p;
        DailyTasks.playerTaskList.put(p, this);
    }

    public Player getPlayer() {
        return this.player;
    }
}
