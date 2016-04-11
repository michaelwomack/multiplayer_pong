package main;

import java.util.Random;

/**
 * Created by michaelwomack on 4/9/16.
 */
public class RandomNumberGenerator {
    private static Random random = new Random();

    public static int getRandIntBetween(int lower, int upper) {
        return random.nextInt(upper - lower) + lower;
    }

    public static int getRandInt(int upper) {
        return random.nextInt(upper);
    }
}
