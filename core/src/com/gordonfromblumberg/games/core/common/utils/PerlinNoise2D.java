package com.gordonfromblumberg.games.core.common.utils;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;

/**
 * 2D Perlin noise generator with seed-based permutation table and octave support.
 * Two instances with the same seed and octaves will produce identical results.
 */
public class PerlinNoise2D {

    private static final int PERMUTATION_SIZE = 256;
    private static final int PERMUTATION_MASK = PERMUTATION_SIZE - 1;

    private final int[] permutation;
    private final int octaves;

    // Precomputed gradient vectors for 8 directions
    private static final float[] GRAD_X = {1, 1, 0, -1, -1, -1, 0, 1};
    private static final float[] GRAD_Y = {0, 1, 1, 1, 0, -1, -1, -1};

    /**
     * Creates a new Perlin noise generator.
     *
     * @param seed   the seed for deterministic noise generation
     * @param octaves number of octaves (must be >= 1)
     */
    public PerlinNoise2D(long seed, int octaves) {
        if (octaves < 1) {
            throw new IllegalArgumentException("Octaves must be >= 1, got: " + octaves);
        }
        this.octaves = octaves;
        this.permutation = generatePermutation(seed);
    }

    public PerlinNoise2D(long seed) {
        this(seed, 1);
    }

    /**
     * Generates the permutation table based on the given seed.
     */
    private int[] generatePermutation(long seed) {
        RandomXS128 random = new RandomXS128(seed);
        int[] perm = new int[PERMUTATION_SIZE];

        // Initialize with 0..255
        for (int i = 0; i < PERMUTATION_SIZE; ++i) {
            perm[i] = i;
        }

        // Fisher-Yates shuffle
        for (int i = PERMUTATION_SIZE - 1; i > 0; --i) {
            int j = random.nextInt(i + 1);
            int temp = perm[i];
            perm[i] = perm[j];
            perm[j] = temp;
        }

        // Duplicate the table to avoid bounds checking
        int[] result = new int[PERMUTATION_SIZE * 2];
        System.arraycopy(perm, 0, result, 0, PERMUTATION_SIZE);
        System.arraycopy(perm, 0, result, PERMUTATION_SIZE, PERMUTATION_SIZE);
        return result;
    }

    /**
     * Gets the noise value at the given coordinates.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @return noise value in range [-1, 1]
     */
    public float get(float x, float y) {
        float total = 0;
        float frequency = 1;
        float amplitude = 1;
        float maxValue = 0;

        for (int i = 0, o = octaves; i < o; ++i) {
            total += noise(x * frequency, y * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= 0.5f;
            frequency *= 2;
        }

        return total / maxValue;
    }

    /**
     * Single octave of Perlin noise.
     */
    private float noise(float x, float y) {
        int xi = (int) Math.floor(x) & PERMUTATION_MASK;
        int yi = (int) Math.floor(y) & PERMUTATION_MASK;

        float xf = x - (int) Math.floor(x);
        float yf = y - (int) Math.floor(y);

        float u = Interpolation.smooth.apply(xf);
        float v = Interpolation.smooth.apply(yf);

        int aa = permutation[permutation[xi] + yi];
        int ab = permutation[permutation[xi] + yi + 1];
        int ba = permutation[permutation[xi + 1] + yi];
        int bb = permutation[permutation[xi + 1] + yi + 1];

        float x1 = MathUtils.lerp(grad(aa, xf, yf),
                                  grad(ba, xf - 1, yf), u);
        float x2 = MathUtils.lerp(grad(ab, xf, yf - 1),
                                  grad(bb, xf - 1, yf - 1), u);

        return MathUtils.lerp(x1, x2, v);
    }

    /**
     * Calculates the dot product of the gradient vector and the distance vector.
     */
    private float grad(int hash, float x, float y) {
        int index = hash & 7;
        return GRAD_X[index] * x + GRAD_Y[index] * y;
    }
}
