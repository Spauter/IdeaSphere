package com.spauter.extra.baseentity.utils;

import org.apache.commons.collections.list.UnmodifiableList;

import java.time.LocalDateTime;
import java.util.*;

public class ValueUtil {


    /**
     * 从对象中获取整数值
     * <p>
     * 该方法尝试将输入对象转换为整数，支持处理 null、Number 类型和数字格式的字符串
     * 如果转换失败或输入无效，则返回默认值 0
     *
     * @param o 输入对象，可以是 null、Number 类型或数字格式的字符串
     * @return 转换后的整数值，转换失败返回 0
     */
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

    /**
     * 从对象中获取长整型数值
     * <p>
     * 该方法尝试将输入对象转换为长整型，支持处理 null、Number 类型和数字格式的字符串。
     * 如果转换失败或输入无效，则返回默认值 0L
     *
     * @param o 输入对象，可以是 null、Number 类型或数字格式的字符串
     * @return 转换后的长整型数值，转换失败返回 0L
     */
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

    /**
     * 从对象中获取双精度浮点数值
     * <p>
     * 该方法尝试将输入对象转换为双精度浮点数，支持处理 null、Number 类型和浮点数字符串。
     * 字符串格式必须符合"数字.数字"的格式(如"123.45")才能被成功转换。
     * 如果转换失败或输入无效，则返回默认值 0.0
     *
     * @param o 输入对象，可以是 null、Number 类型或浮点数字符串
     * @return 转换后的双精度浮点数值，转换失败返回 0.0
     */
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

    /**
     * 从对象中获取浮点数值
     * <p>
     * 该方法尝试将输入对象转换为浮点数，支持处理 null、Number 类型和浮点数字符串。
     * 字符串格式必须符合"数字.数字"的格式(如"123.45")才能被成功转换。
     * 如果转换失败或输入无效，则返回默认值 0.0
     *
     * @param o 输入对象，可以是 null、Number 类型或浮点数字符串
     * @return 转换后的浮点数值，转换失败返回 0.0
     */
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

    /**
     * 将字符串转换为 LocalDateTime
     *
     * @param date 字符串
     * @return 转换后的 LocalDateTime
     */
    public static LocalDateTime parseLocalDateTime(String date) {
        return LocalDateTime.parse(date);
    }

    /**
     * 判断字符串是否为空
     *
     * @param strings 字符串数组
     * @return 是否为空
     */
    public static boolean isBlank(String... strings) {
        return strings == null || strings.length == 0;
    }


    /**
     * 判断集合是否为空
     */
    public static boolean isBlank(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 判断集合是否为空
     */
    public static boolean isBlank(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断数组是否为空
     */
    public static boolean isBlank(Object[] objects) {
        return objects == null || objects.length == 0;
    }


    /**
     * 安全地将两个集合合并为一个集合
     * <p>
     * 该方法处理各种边界情况，包括null集合、空集合和不可修改集合，
     * 确保合并操作不会抛出异常，并返回合理的合并结果
     *
     * @param <E> 集合元素类型
     * @param a   第一个集合，可以为null
     * @param b   第二个集合，可以为null
     * @return 合并后的集合。如果两个集合都为null则返回空集合；
     * 如果任一集合为null则返回非null集合；
     * 如果任一集合为空则返回非空集合；
     * 如果任一集合不可修改则返回新的可修改集合,并把两个集合合并到新集合中；
     * 否则将b合并到a中并返回a
     */
    @SuppressWarnings("unchecked")
    public static <E> Collection<E> safeAddAll(Collection<E> a, Collection<E> b) {
        if (a == null && b == null) {
            return Collections.EMPTY_LIST;
        }
        if (a == null || b == null) {
            return Objects.requireNonNullElse(a, b);
        }
        if (isBlank(a) || isBlank(b)) {
            return a.isEmpty() ? b : a;
        }
        if (a.getClass().getName().contains("Unmodifiable") || b.getClass().getName().contains("Unmodifiable")) {
            Collection<E> c = new ArrayList<>();
            c.addAll(a);
            c.addAll(b);
            return c;
        }
        a.addAll(b);
        return a;
    }


    /**
     * 安全地将元素数组添加到集合中
     *
     * @param <E> 集合元素类型
     * @param a   目标集合，可以为null（会自动创建新集合）
     * @param b   要添加的元素数组，可以为null或空
     * @return 合并后的集合。如果集合为null则返回新创建的空集合；
     * 如果元素数组为null或空则返回原集合；
     * 如果目标集合不可修改则返回新的可修改集合；
     * 否则将元素数组添加到目标集合并返回
     */
    @SuppressWarnings("unchecked")
    public static <E> Collection<E> safeAddAll(Collection<E> a, E... b) {
        if (a == null) {
            a = new ArrayList<>();
        }
        if (isBlank(b)) {
            return a;
        }
        if (a.getClass().getName().contains("Unmodifiable")) {
            Collection<E> c = new ArrayList<>();
            c.addAll(a);
            c.addAll(List.of(b));
            return c;
        }
        a.addAll(List.of(b));
        return a;
    }

    /**
     * 安全地将两个 Map 合并为一个 Map
     * <p>
     *
     * @param <K> Map 键类型
     * @param <V> Map 值类型
     * @param a 第一个 Map，可以为 null
     * @param b 第二个 Map，可以为 null
     * @return 合并后的 Map。如果两个 Map 都为 null 则返回空 Map；
     *         如果任一 Map 为 null 则返回非 null Map；
     *         如果任一 Map 为空则返回非空 Map；
     *         如果任一 Map 不可修改则返回新的可修改 Map，并把两个 Map 合并到新 Map 中；
     *         否则将 b 合并到 a 中并返回 a
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> safePutAll(Map<K, V> a, Map<K, V> b) {
        if (a == null && b == null) {
            return Collections.EMPTY_MAP;
        }
        if (a == null || b == null) {
            return Objects.requireNonNullElse(a, b);
        }
        if (isBlank(a) || isBlank(b)) {
            return a.isEmpty() ? b : a;
        }
        if (a.getClass().getName().contains("Unmodifiable") || b.getClass().getName().contains("Unmodifiable")) {
            Map<K, V> c = new HashMap<>();
            c.putAll(a);
            c.putAll(b);
            return c;
        }
        a.putAll(b);
        return a;
    }
}
