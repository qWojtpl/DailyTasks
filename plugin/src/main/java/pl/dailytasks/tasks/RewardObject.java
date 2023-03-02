package pl.dailytasks.tasks;

import pl.dailytasks.util.RandomNumber;

public class RewardObject {

    public String command;
    public String initializedCommand;
    public int min;
    public int max;
    public boolean isMonthly;

    public RewardObject(String command, int min, int max, boolean isMonthly) {
        this.command = command;
        this.min = min;
        this.max = max;
        this.isMonthly = isMonthly;
        this.initializedCommand = command.replace("%rdm%", String.valueOf(RandomNumber.randomInt(min, max)));
    }

}
