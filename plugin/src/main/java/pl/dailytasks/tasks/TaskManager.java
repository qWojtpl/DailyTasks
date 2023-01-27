package pl.dailytasks.tasks;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import pl.dailytasks.DailyTasks;
import pl.dailytasks.data.DataHandler;
import pl.dailytasks.util.DateManager;
import pl.dailytasks.util.RandomNumber;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {

    private final HashMap<String, List<TaskObject>> taskList = new HashMap<>();
    private final HashMap<String, RewardObject> dayRewardList = new HashMap<>();
    private final HashMap<String, RewardObject> monthRewardList = new HashMap<>();

    public void Check(Player p, String checkable) {
        if(!p.hasPermission(DailyTasks.getInstance().getPermissionManager().getPermission("dt.use"))) return;
        int i = -1;
        PlayerTasks pt = PlayerTasks.Create(p);
        if(pt.checkIfCompletedDay(DateManager.getDay())) return;
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
            if(pt.checkIfCompletedDayTask(i)) {
                continue;
            }
            int progress = pt.getProgress().get(i);
            progress++;
            if(progress >= to.currentRandom) {
                to.Complete(pt, i);
            }
            pt.getProgress().set(i, progress);
            DataHandler.updatePlayerProgress(pt, i);
            CheckRewards(pt);
        }
    }

    public void CheckRewards(PlayerTasks pt) {
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
            DailyTasks.getInstance().getServer().dispatchCommand(DailyTasks.getInstance().getServer().getConsoleSender(),
                    getTodayReward().initializedCommand.replace("%player%", pt.getPlayer().getName()));
            String[] splitMessage = DailyTasks.getMessage("complete-day").split("%nl%");
            for(String message : splitMessage) {
                pt.getPlayer().sendMessage(MessageFormat.format(message, DateManager.getDay(), getTodayReward().initializedCommand));
            }
            if(pt.checkIfCompletedMonth()) {
                pt.getPlayer().playSound(pt.getPlayer().getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);
                DailyTasks.getInstance().getServer().dispatchCommand(DailyTasks.getInstance().getServer().getConsoleSender(),
                        getThisMonthReward().initializedCommand.replace("%player%", pt.getPlayer().getName()));
                splitMessage = DailyTasks.getMessage("complete-month").split("%nl%");
                for (String message : splitMessage) {
                    pt.getPlayer().sendMessage(MessageFormat.format(message, DateManager.getMonth(), getThisMonthReward().initializedCommand));
                }
            }
        }
    }

    public void RandomizeTasks(int numberOfTasks) {
        List<TaskObject> pool = new ArrayList<>(DailyTasks.TaskPool);
        if(pool.size() == 0 || getTodayTasks().size() >= numberOfTasks) return;
        for(int i = 0; i < numberOfTasks; i++) {
            int index = RandomNumber.randomInt(0, pool.size()-1);
            TaskObject to = pool.get(index);
            getTodayTasks().add(new TaskObject(to.event, to.min, to.max));
            pool.remove(index);
        }
        DailyTasks.getInstance().getLogger().info("Randomizing tasks! Last randomized: " +
                DailyTasks.lastRandomizedDate + ", current date: " + DateManager.getFormattedDate("%Y/%M/%D"));
        RandomizeDayReward();
        DataHandler.saveTodayTasks();
    }

    public void RandomizeDayReward() {
        List<RewardObject> allPool = new ArrayList<>(DailyTasks.RewardPool);
        List<RewardObject> pool = new ArrayList<>();
        for(RewardObject ro : allPool) {
            if(!ro.isMonthly) pool.add(ro);
        }
        if(pool.size() == 0 || getTodayReward() != null) return;
        DailyTasks.getInstance().getLogger().info("Randomizing daily reward!");
        RewardObject schema = pool.get(RandomNumber.randomInt(0, pool.size()-1));
        dayRewardList.put(DateManager.getFormattedDate("%Y/%M/%D"), new RewardObject(schema.command, schema.min, schema.max, schema.isMonthly));
        DataHandler.saveTodayReward();
    }

    public void RandomizeMonthReward() {
        List<RewardObject> allPool = new ArrayList<>(DailyTasks.RewardPool);
        List<RewardObject> pool = new ArrayList<>();
        for(RewardObject ro : allPool) {
            if(ro.isMonthly) pool.add(ro);
        }
        if(pool.size() == 0 || getThisMonthReward() != null) return;
        DailyTasks.getInstance().getLogger().info("Randomizing monthly reward!");
        RewardObject schema = pool.get(RandomNumber.randomInt(0, pool.size()-1));
        monthRewardList.put(DateManager.getFormattedDate("%Y/%M"), new RewardObject(schema.command, schema.min, schema.max, schema.isMonthly));
        DataHandler.saveMonthlyReward();
    }

    public List<TaskObject> getTodayTasks() {
        if(!taskList.containsKey(DateManager.getFormattedDate("%Y/%M/%D"))) {
            taskList.put(DateManager.getFormattedDate("%Y/%M/%D"), new ArrayList<>());
        }
        return taskList.get(DateManager.getFormattedDate("%Y/%M/%D"));
    }

    public List<TaskObject> getTasks(int day) {
        if(!taskList.containsKey(DateManager.getFormattedDate("%Y/%M/" + day))) {
            taskList.put(DateManager.getFormattedDate("%Y/%M/" + day), new ArrayList<>());
        }
        return taskList.get(DateManager.getFormattedDate("%Y/%M/" + day));
    }

    public RewardObject getTodayReward() {
        if(!dayRewardList.containsKey(DateManager.getFormattedDate("%Y/%M/%D"))) {
            dayRewardList.put(DateManager.getFormattedDate("%Y/%M/%D"), null);
        }
        return dayRewardList.get(DateManager.getFormattedDate("%Y/%M/%D"));
    }

    public RewardObject getReward(int day) {
        if(!dayRewardList.containsKey(DateManager.getFormattedDate("%Y/%M/" + day))) {
            dayRewardList.put(DateManager.getFormattedDate("%Y/%M/" + day), null);
        }
        return dayRewardList.get(DateManager.getFormattedDate("%Y/%M/" + day));
    }

    public RewardObject getThisMonthReward() {
        if(!monthRewardList.containsKey(DateManager.getFormattedDate("%Y/%M"))) {
            monthRewardList.put(DateManager.getFormattedDate("%Y/%M"), null);
        }
        return monthRewardList.get(DateManager.getFormattedDate("%Y/%M"));
    }

    public RewardObject getMonthReward(int month) {
        if(!monthRewardList.containsKey(DateManager.getFormattedDate("%Y/" + month))) {
            monthRewardList.put(DateManager.getFormattedDate("%Y/" + month), null);
        }
        return monthRewardList.get(DateManager.getFormattedDate("%Y/" + month));
    }

    public HashMap<String, List<TaskObject>> getSourceTaskList() {
        return this.taskList;
    }

    public HashMap<String, RewardObject> getSourceDayReward() {
        return this.dayRewardList;
    }

    public HashMap<String, RewardObject> getSourceMonthReward() {
        return this.monthRewardList;
    }

}
