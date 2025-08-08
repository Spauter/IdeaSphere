package com.spauter.extra.baseentity.utils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

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


    public static boolean isBlank(String... strings) {
        for (String s : strings) {
            if (s == null || s.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBlank(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isBlank(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isBlank(Object[] objects) {
        return objects == null || objects.length == 0;
    }


    public static <E> Collection<E> safeAddAll(Collection<E> a, Collection<E> b) {
        if(isBlank(a)){
            a=Collections.EMPTY_LIST;
        }
        if(isBlank(b)){
            b=Collections.EMPTY_LIST;
        }
        a.addAll(b);
        return a;
    }

    public static <K, V> Map<K, V> safePutAll(Map<K, V> a, Map<K, V> b) {
       if(isBlank(a)){
           a=Collections.EMPTY_MAP;
       }
       if(isBlank(b)){
           b=Collections.EMPTY_MAP;
       }
       a.putAll(b);
       return a;
    }
}
