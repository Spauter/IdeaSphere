package com.spauter.extra.baseentity.utils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        for (String s : strings) {
            if (s == null || s.isEmpty()) {
                return false;
            }
        }
        return true;
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
     * 安全地将两个集合合并
     * <p>
     * 该方法会处理空集合的情况，将空集合替换为空的不可变列表后再进行合并操作。
     * 如果任一输入集合为 null 或空，将被替换为 {@link Collections#EMPTY_LIST}。
     * 最终返回合并后的第一个集合。
     *
     * @param <E> 集合元素类型
     * @param a   第一个集合，合并操作的目标集合
     * @param b   第二个集合，将被合并到第一个集合中的元素集合
     * @return 合并后的第一个集合
     * @throws UnsupportedOperationException 如果尝试修改不可变集合
     */
    @SuppressWarnings("unchecked")
    public static <E> Collection<E> safeAddAll(Collection<E> a, Collection<E> b) {
        if (a==null) {
            a = Collections.EMPTY_LIST;
        }
        if (b==null) {
            b = Collections.EMPTY_LIST;
        }
        a.addAll(b);
        return a;
    }

    /**
     * 安全地将可变参数元素添加到集合中
     * <p>
     * 该方法会处理空集合和空数组的情况，将空集合替换为空的不可变列表后再进行添加操作。
     * 如果输入数组为 null 或空，则直接返回原集合(或空集合)。
     * 使用 {@link List#of(Object[])} 将可变参数转换为列表后添加到目标集合。
     *
     * @param <E> 集合元素类型
     * @param a   目标集合，元素将被添加到此集合
     * @param b   要添加的可变参数元素数组
     * @return 添加元素后的目标集合
     * @throws UnsupportedOperationException 如果尝试修改不可变集合
     */
    @SuppressWarnings("unchecked")
    public static <E> Collection<E> safeAddAll(Collection<E> a, E... b) {
        if (a==null) {
            a = Collections.EMPTY_LIST;
        }
        if (isBlank(b)) {
            return a;
        }
        a.addAll(List.of(b));
        return a;
    }

    /**
     * 安全地将两个 Map 合并
     * <p>
     * 该方法会处理空 Map 的情况，将空 Map 替换为空的不可变 Map 后再进行合并操作。
     * 如果任一输入 Map 为 null 或空，将被替换为 {@link Collections#EMPTY_MAP}。
     * 最终返回合并后的第一个 Map。
     *
     * @param <K> Map 的键类型
     * @param <V> Map 的值类型
     * @param a   第一个 Map，合并操作的目标 Map
     * @param b   第二个 Map，将被合并到第一个 Map 中的键值对集合
     * @return 合并后的第一个 Map
     * @throws UnsupportedOperationException 如果尝试修改不可变 Map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> safePutAll(Map<K, V> a, Map<K, V> b) {
        if (a==null) {
            a = Collections.EMPTY_MAP;
        }
        if (b==null) {
            b = Collections.EMPTY_MAP;
        }
        a.putAll(b);
        return a;
    }
}
