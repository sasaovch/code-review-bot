package ru.max.codereviewbot.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImmutabilityUtils {

    /**
     * Add element to immutable list by creating a copy
     * @param list to be extended
     * @param value to be added to list
     * @return unmodifiable list containing desired element
     */
    public static <T> List<T> add(List<T> list, T value) {
        List<T> result = new ArrayList<>(list);
        result.add(value);
        return Collections.unmodifiableList(result);
    }
}
