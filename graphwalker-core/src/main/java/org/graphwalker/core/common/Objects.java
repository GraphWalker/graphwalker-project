package org.graphwalker.core.common;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Nils Olsson
 */
public class Objects {

    public static <K, V> boolean isNullOrEmpty(Map<K, V> map) {
        return !isNotNullOrEmpty(map);
    }

    public static <K, V> boolean isNotNullOrEmpty(Map<K, V> map) {
        return null != map && !map.isEmpty();
    }

    public static <V> boolean isNullOrEmpty(Set<V> set) {
        return !isNotNullOrEmpty(set);
    }

    public static <V> boolean isNotNullOrEmpty(Set<V> set) {
        return null != set && !set.isEmpty();
    }

    public static <V> boolean isNullOrEmpty(List<V> list) {
        return !isNotNullOrEmpty(list);
    }

    public static <V> boolean isNotNullOrEmpty(List<V> list) {
        return null != list && !list.isEmpty();
    }

    public static boolean isNullOrEmpty(String string) {
        return !isNotNullOrEmpty(string);
    }

    public static boolean isNotNullOrEmpty(String string) {
        return null != string && !"".equals(string);
    }

    public static boolean isNotNull(Object object) {
        return null != object;
    }

    public static boolean isNull(Object object) {
        return null == object;
    }

    public static boolean equals(Object a, Object b) {
        return (a == b) || (isNotNull(a) && a.equals(b));
    }

    public static int hash(Object... values) {
        return Arrays.hashCode(values);
    }
}
