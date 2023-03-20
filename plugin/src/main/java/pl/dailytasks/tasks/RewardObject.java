package pl.dailytasks.tasks;

import lombok.Getter;
import lombok.Setter;
import pl.dailytasks.util.RandomNumber;

@Getter
public class RewardObject {

    private final String id;
    private final String command;
    @Setter
    private String initializedCommand;
    private final int min;
    private final int max;
    private final boolean monthly;

    public RewardObject(String id, String command, int min, int max, boolean isMonthly) {
        this.id = id;
        this.command = command;
        this.min = min;
        this.max = max;
        this.monthly = isMonthly;
        this.initializedCommand = command.replace("%rdm%", String.valueOf(RandomNumber.randomInt(min, max)));
    }

}
