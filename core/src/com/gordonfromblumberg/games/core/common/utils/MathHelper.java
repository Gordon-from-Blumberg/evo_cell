package com.gordonfromblumberg.games.core.common.utils;

import com.badlogic.gdx.math.MathUtils;

public class MathHelper {
    public static boolean isSameSign(float n1, float n2) {
        if (MathUtils.isZero(n1) || MathUtils.isZero(n2)) {
            return false;
        }
        return Math.signum(n1) == Math.signum(n2);
    }

    public static float smoothStep(float f) {
        return f * f * (3.0f - 2.0f * f);
    }

    /**
     * Trim angle value to [-180, 180]
     * @param deg Angle in degrees
     * @return Angle value in [-180, 180] degrees
     */
    public static float clampAngleDeg(float deg) {
        deg %= 360;
        if (deg > 180) deg -= 360;
        else if (deg < -180) deg += 360;
        return deg;
    }
}
