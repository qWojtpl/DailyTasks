package pl.dailytasks.tasks;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import pl.dailytasks.DailyTasks;
import pl.dailytasks.data.DataManager;
import pl.dailytasks.util.DateManager;
import pl.dailytasks.util.RandomNumber;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {

    public static HashMap<String, List<TaskObject>> todayTasks = new HashMap<>();

    public static void Check(Player p, String checkable) {
        int i = -1;
        PlayerTasks pt = PlayerTasks.Create(p);
        if(pt.completedTasks.size() >= 3) return;
        for(TaskObject to : getTodayTasks()) {
            i++;
            String[] taskEvent = to.initializedEvent.split(" ");
            String[] givenEvent = checkable.split(" ");
            if(!taskEvent[0].equalsIgnoreCase(givenEvent[0])) {
                continue;
            }
            if(!taskEvent[2].equalsIgnoreCase(givenEvent[1])) {
                continue;
            }
            if(pt.checkIfCompleted(to)) {
                continue;
            }
            int progress = pt.getProgress().get(i);
            progress++;
            if(progress >= to.currentRandom) {
                to.Complete(pt, i);
            }
            pt.getProgress().set(i, progress);
            DailyTasks.main.getLogger().info(i + " ");
            int playerSum = 0;
            int max = 0;
            int j = 0;
            for(TaskObject to2 : getTodayTasks()) {
                max += to2.currentRandom;
                playerSum += pt.getProgress().get(j);
                j++;
            }
            if(playerSum >= max) {
                pt.getPlayer().playSound(pt.getPlayer().getLocation(), Sound.UI_LOOM_TAKE_RESULT, 1.0F, 1.0F);
                String[] splittedMessage = DailyTasks.getMessage("complete-day").split("%nl%");
                for(String message : splittedMessage) {
                    pt.getPlayer().sendMessage(MessageFormat.format(message, DateManager.getDay()));
                }
            }
        }
    }

    public static void RandomizeTasks(int numberOfTasks) {
        List<TaskObject> pool = new ArrayList<>(DailyTasks.TaskPool);
        for(int i = 0; i < numberOfTasks; i++) {
            if(pool.size() == 0 || getTodayTasks().size() >= numberOfTasks) break;
            int index = RandomNumber.randomInt(0, pool.size()-1);
            TaskObject to = pool.get(index);
            getTodayTasks().add(new TaskObject(to.ID, to.event, to.min, to.max));
            pool.remove(index);
        }
        DataManager.saveTodayTasks();
    }

    public static List<TaskObject> getTodayTasks() {
        if(!todayTasks.containsKey(DateManager.getFormattedDate("%Y/%M/%D"))) {
            todayTasks.put(DateManager.getFormattedDate("%Y/%M/%D"), new ArrayList<>());
        }
        return todayTasks.get(DateManager.getFormattedDate("%Y/%M/%D"));
    }

    public static List<String> getTodayTasksEvents() {
        List<String> events = new ArrayList<>();
        for(TaskObject to : getTodayTasks()) {
            events.add(to.initializedEvent);
        }
        return events;
    }

    public static List<TaskObject> getTasks(int day) {
        if(!todayTasks.containsKey(DateManager.getFormattedDate("%Y/%M/" + day))) {
            todayTasks.put(DateManager.getFormattedDate("%Y/%M/" + day), new ArrayList<>());
        }
        return todayTasks.get(DateManager.getFormattedDate("%Y/%M/" + day));
    }

    public static void RemoveTodayTasks() {
        List<TaskObject> tasks = new ArrayList<>(getTodayTasks());
        for(TaskObject to : tasks) {
            getTodayTasks().remove(to);
        }
    }

    public static TaskObject getTaskByID(String ID) {
        for(TaskObject to : DailyTasks.TaskPool) {
            if(to.ID.equals(ID)) {
                return to;
            }
        }
        return null;
    }

}
