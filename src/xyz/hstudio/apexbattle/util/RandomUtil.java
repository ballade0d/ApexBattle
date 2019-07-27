package xyz.hstudio.apexbattle.util;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {

    public static int nextInt(final int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }
}