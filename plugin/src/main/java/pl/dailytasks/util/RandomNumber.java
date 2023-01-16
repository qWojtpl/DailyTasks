package pl.dailytasks.util;

public class RandomNumber {

    public static int randomInt(int min, int max) {
        if(min == max) return min;
        return (int) Math.floor(Math.random() * (max - min + 1) + min);
    }

}
