package pl.dailytasks.tasks;

import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import pl.dailytasks.DailyTasks;
import pl.dailytasks.data.DataHandler;
import pl.dailytasks.util.DateManager;
import pl.dailytasks.util.Messages;
import pl.dailytasks.util.RandomNumber;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {

    @Getter
    private final List<TaskObject> taskPool = new ArrayList<>();
    @Getter
    private final List<RewardObject> rewardPool = new ArrayList<>();
    private final HashMap<String, List<TaskObject>> taskList = new HashMap<>();
    private final HashMap<String, RewardObject> dayRewardList = new HashMap<>();
    private final HashMap<String, RewardObject> monthRewardList = new HashMap<>();
    private DataHandler dh;

    public void setDataHandler(DataHandler dh) {
        this.dh = dh;
    }
    
    public void Check(Player p, String checkable) {
        if(p == null || checkable == null) return;
        if(!p.hasPermission(DailyTasks.getInstance().getPermissionManager().getPermission("dt.use"))) return;
        int i = -1;
        PlayerTasks pt = PlayerTasks.Create(p);
        DateManager dm = DailyTasks.getInstance().getDateManager();
        if(pt.checkIfCompletedDay(dm.getDay())) return;
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
            dh.updatePlayerProgress(pt, i);
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
            Messages messages = DailyTasks.getInstance().getMessages();
            String[] splitMessage = messages.getMessage("complete-day").split("%nl%");
            DateManager dm = DailyTasks.getInstance().getDateManager();
            for(String message : splitMessage) {
                pt.getPlayer().sendMessage(MessageFormat.format(message, dm.getDay(), getTodayReward().initializedCommand));
            }
            if(pt.checkIfCompletedMonth()) {
                pt.getPlayer().playSound(pt.getPlayer().getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);
                DailyTasks.getInstance().getServer().dispatchCommand(DailyTasks.getInstance().getServer().getConsoleSender(),
                        getThisMonthReward().initializedCommand.replace("%player%", pt.getPlayer().getName()));
                splitMessage = messages.getMessage("complete-month").split("%nl%");
                for (String message : splitMessage) {
                    pt.getPlayer().sendMessage(MessageFormat.format(message, dm.getMonth(), getThisMonthReward().initializedCommand));
                }
            }
        }
    }

    public void RandomizeTasks(int numberOfTasks) {
        List<TaskObject> pool = new ArrayList<>(getTaskPool());
        if(pool.size() == 0 || getTodayTasks().size() >= numberOfTasks) return;
        for(int i = 0; i < numberOfTasks; i++) {
            if(pool.size() == 0) break;
            int index = RandomNumber.randomInt(0, pool.size()-1);
            TaskObject to = pool.get(index);
            getTodayTasks().add(new TaskObject(to.event, to.min, to.max));
            pool.remove(index);
        }
        DateManager dm = DailyTasks.getInstance().getDateManager();
        DailyTasks.getInstance().getLogger().info("Randomizing tasks! Last randomized: " +
                DailyTasks.getInstance().getLastRandomizedDate() + ", current date: " + dm.getFormattedDate("%Y/%M/%D"));
        RandomizeDayReward();
        dh.saveTodayTasks();
    }

    public void RandomizeDayReward() {
        List<RewardObject> allPool = new ArrayList<>(getRewardPool());
        List<RewardObject> pool = new ArrayList<>();
        for(RewardObject ro : allPool) {
            if(!ro.isMonthly) pool.add(ro);
        }
        if(pool.size() == 0 || getTodayReward() != null) return;
        DailyTasks.getInstance().getLogger().info("Randomizing daily reward!");
        RewardObject schema = pool.get(RandomNumber.randomInt(0, pool.size()-1));
        DateManager dm = DailyTasks.getInstance().getDateManager();
        dayRewardList.put(dm.getFormattedDate("%Y/%M/%D"), new RewardObject(schema.command, schema.min, schema.max, schema.isMonthly));
        dh.saveTodayReward();
    }

    public void RandomizeMonthReward() {
        List<RewardObject> allPool = new ArrayList<>(getRewardPool());
        List<RewardObject> pool = new ArrayList<>();
        for(RewardObject ro : allPool) {
            if(ro.isMonthly) pool.add(ro);
        }
        if(pool.size() == 0 || getThisMonthReward() != null) return;
        DailyTasks.getInstance().getLogger().info("Randomizing monthly reward!");
        RewardObject schema = pool.get(RandomNumber.randomInt(0, pool.size()-1));
        DateManager dm = DailyTasks.getInstance().getDateManager();
        monthRewardList.put(dm.getFormattedDate("%Y/%M"), new RewardObject(schema.command, schema.min, schema.max, schema.isMonthly));
        dh.saveMonthlyReward();
    }

    public List<TaskObject> getTodayTasks() {
        DateManager dm = DailyTasks.getInstance().getDateManager();
        if(!taskList.containsKey(dm.getFormattedDate("%Y/%M/%D"))) {
            taskList.put(dm.getFormattedDate("%Y/%M/%D"), new ArrayList<>());
        }
        return taskList.get(dm.getFormattedDate("%Y/%M/%D"));
    }

    public List<TaskObject> getTasks(int day) {
        DateManager dm = DailyTasks.getInstance().getDateManager();
        if(!taskList.containsKey(dm.getFormattedDate("%Y/%M/" + day))) {
            taskList.put(dm.getFormattedDate("%Y/%M/" + day), new ArrayList<>());
        }
        return taskList.get(dm.getFormattedDate("%Y/%M/" + day));
    }

    public RewardObject getTodayReward() {
        DateManager dm = DailyTasks.getInstance().getDateManager();
        if(!dayRewardList.containsKey(dm.getFormattedDate("%Y/%M/%D"))) {
            dayRewardList.put(dm.getFormattedDate("%Y/%M/%D"), null);
        }
        return dayRewardList.get(dm.getFormattedDate("%Y/%M/%D"));
    }

    public RewardObject getReward(int day) {
        DateManager dm = DailyTasks.getInstance().getDateManager();
        if(!dayRewardList.containsKey(dm.getFormattedDate("%Y/%M/" + day))) {
            dayRewardList.put(dm.getFormattedDate("%Y/%M/" + day), null);
        }
        return dayRewardList.get(dm.getFormattedDate("%Y/%M/" + day));
    }

    public RewardObject getThisMonthReward() {
        DateManager dm = DailyTasks.getInstance().getDateManager();
        if(!monthRewardList.containsKey(dm.getFormattedDate("%Y/%M"))) {
            monthRewardList.put(dm.getFormattedDate("%Y/%M"), null);
        }
        return monthRewardList.get(dm.getFormattedDate("%Y/%M"));
    }

    public RewardObject getMonthReward(int month) {
        DateManager dm = DailyTasks.getInstance().getDateManager();
        if(!monthRewardList.containsKey(dm.getFormattedDate("%Y/" + month))) {
            monthRewardList.put(dm.getFormattedDate("%Y/" + month), null);
        }
        return monthRewardList.get(dm.getFormattedDate("%Y/" + month));
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
