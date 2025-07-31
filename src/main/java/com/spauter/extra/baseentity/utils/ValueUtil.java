package com.spauter.extra.baseentity.utils;

import java.time.LocalDateTime;

public class ValueUtil {

    public static Integer getIntValue(Object o) {
        if (o == null) {
            return 0;
        }
        if (o instanceof Number number) {
            return number.intValue();
        }
        if (o instanceof String) {
            String match = "^\\d+$";
            if (((String) o).matches(match)) {
                return Integer.parseInt((String) o);
            }
        }
        return 0;
    }

    public static Long getLongValue(Object o) {
        if (o == null) {
            return 0L;
        }
        if (o instanceof Number number) {
            return number.longValue();
        }
        if (o instanceof String) {
            String match = "^\\d+$";
            if (((String) o).matches(match)) {
                return Long.parseLong((String) o);
            }
        }
        return 0L;
    }

    public static Double getDoubleValue(Object o) {
        if (o == null) {
            return 0.0;
        }
        if (o instanceof Number number) {
            return number.doubleValue();
        }
        if (o instanceof String) {
            String match = "^\\d+\\.\\d+$";
            if (((String) o).matches(match)) {
                return Double.parseDouble((String) o);
            }
        }
        return 0.0;
    }

    public static Float getFloatValue(Object o) {
        if (o == null) {
            return 0.0f;
        }
        if (o instanceof Number number) {
            return number.floatValue();
        }
        if (o instanceof String) {
            String match = "^\\d+\\.\\d+$";
            if (((String) o).matches(match)) {
                return Float.parseFloat((String) o);
            }
        }
        return 0.0f;
    }

    public static LocalDateTime parseLocalDateTime(String date) {
        return LocalDateTime.parse(date);
    }
}
