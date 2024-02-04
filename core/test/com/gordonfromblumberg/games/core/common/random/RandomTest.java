package com.gordonfromblumberg.games.core.common.random;

import com.gordonfromblumberg.games.core.common.utils.RandomGen;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;

public class RandomTest {
    @Test
    void test() {
        int[] arr = new int[2048];
//        RandomGen.setSeed(5216505468L);
        for (int i = 0, n = arr.length; i < n; ++i) {
            arr[i] = RandomGen.INSTANCE.nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE);
        }
        TreeMap<Integer, Integer> intMap = new TreeMap<>();
        for (int value : arr) {
            if (intMap.containsKey(value)) {
                intMap.put(value, intMap.get(value) + 1);
            } else {
                intMap.put(value, 1);
            }
        }

        System.out.println("size = " + intMap.size());
        for (Map.Entry<Integer, Integer> e : intMap.entrySet()) {
            System.out.println(e.getKey() + ":\t" + e.getValue());
        }
    }
}
