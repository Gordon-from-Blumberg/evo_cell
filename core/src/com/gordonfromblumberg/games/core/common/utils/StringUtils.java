package com.gordonfromblumberg.games.core.common.utils;

public class StringUtils {
    static final String PLACE_HOLDER = "#";
    private static final short[] TEN_POWERS = new short[] {1, 10, 100, 1000};
    private static final StringBuilder sb = new StringBuilder();

    private StringUtils() {}

    public static String format(String template, Object... args) {
        if (args == null || args.length == 0) {
            if (template.contains(PLACE_HOLDER)) {
                throw new IllegalArgumentException("No arguments passed for template '" + template + "'");
            } else {
                return template;
            }
        }

        String[] parts = template.split(PLACE_HOLDER, -1);
        if (parts.length - 1 != args.length) {
            throw new IllegalArgumentException("Incorrect number of arguments: " + args.length
                    + " passed for template '" + template + "'");
        }

        StringBuilder result = new StringBuilder(parts[0]);
        for (int i = 0; i < args.length; i++) {
            result.append(args[i]).append(parts[i + 1]);
        }

        return result.toString();
    }

    public static String padLeft(long number, int pad) {
        try {
            String numberStr = String.valueOf(number);
            int length = Math.max(numberStr.length(), pad);
            sb.ensureCapacity(length);
            if (pad > numberStr.length()) {
                int zeros = pad - numberStr.length();
                while (zeros-- > 0)
                    sb.append('0');
            }
            sb.append(numberStr);
            return sb.toString();
        } finally {
            sb.delete(0, sb.length());
        }
    }

    /**
     * Round float number and convert it to string with specified digits after dot
     * @param value Float number
     * @param decimals Number of digits after dot
     * @return String representation
     */
    public static String floatToString(float value, int decimals) {
        try {
            int rounded = Math.round(value * TEN_POWERS[decimals]);
            if (rounded == 0) {
                switch (decimals) {
                    case 0: return "0";
                    case 1: return "0.0";
                    case 2: return "0.00";
                    case 3: return "0.000";
                }
            }
            int expectedDigits = decimals + 1;
            boolean isNegative = rounded < 0;
            if (isNegative)
                rounded = -rounded;
            sb.append(rounded);
            if (sb.length() < expectedDigits) {
                int n = expectedDigits - sb.length();
                while (n-- > 0) {
                    sb.insert(0, '0');
                }
            }
            if (decimals > 0)
                sb.insert(sb.length() - decimals, '.');
            if (isNegative)
                sb.insert(0, '-');
            return sb.toString();
        } finally {
            sb.delete(0, sb.length());
        }
    }

    public static boolean isBlank(String str) {
        if (str == null || str.isEmpty()) {
            return true;
        }
        for (int i = 0, len = str.length(); i < len; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
