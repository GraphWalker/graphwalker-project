package org.graphwalker.core.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Nils Olsson
 */
public class Objects {
    
    private Objects() {}

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

    public static <T> List<T> unmodifiableList(List<? extends T> list) {
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    public static <T> Set<T> unmodifiableSet(Set<? extends T> set) {
        return Collections.unmodifiableSet(new HashSet<>(set));
    }

    public static <K,V> Map<K,V> unmodifiableMap(Map<? extends K, ? extends V> map) {
        return Collections.unmodifiableMap(new HashMap<>(map));
    }
}
