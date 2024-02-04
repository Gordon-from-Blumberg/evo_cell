package com.gordonfromblumberg.games.core.common.utils;

import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

public class RandomGen {
    public static final RandomGen INSTANCE = new RandomGen();

    private final Random rand = new RandomXS128();

    public static void setSeed(long seed) {
        INSTANCE.rand.setSeed(seed);
    }

    /**
     * @param to Upper bound (exclusive)
     * @return random int from [0; to)
     */
    public int nextInt(int to) {
        return rand.nextInt(to);
    }

    /**
     * @param from Lower bound (inclusive)
     * @param to Upper bound (inclusive)
     * @return random int from [from; to]
     */
    public int nextInt(int from, int to) {
        return from == to ? from : from + rand.nextInt(to - from + 1);
    }

    public byte nextByte() {
        return (byte) nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    public float nextFloat() {
        return rand.nextFloat();
    }

    public float nextFloat(float max) {
        return max * rand.nextFloat();
    }

    public float nextFloat(float from, float to) {
        return from == to ? from : from + ((to - from) * rand.nextFloat());
    }

    public long nextLong() {
        return rand.nextLong();
    }

    public boolean nextBool() {
        return rand.nextBoolean();
    }

    public boolean nextBool(float chance) {
        return rand.nextFloat() < chance;
    }

    public <T> T getRandomItem(Array<T> array) {
        return array.get(nextInt(array.size));
    }
}
