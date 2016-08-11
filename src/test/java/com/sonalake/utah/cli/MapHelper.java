package com.sonalake.utah.cli;

import java.util.*;

/**
 * A little helper to create maps for tests
 *
 */
public class MapHelper{
    private final List<Map<String, String>> maps;
    public MapHelper() {
        maps = new ArrayList<Map<String, String>>();
    }
    public MapHelper newMap() {
        maps.add(new TreeMap<String, String>());
        return this;
    }

    public MapHelper put(String key, String value) {
        current().put(key, value);
        return this;
    }

    private Map<String, String> current() {
        if (maps.size() >= 1) {
            return maps.get(maps.size() - 1);
        } else {
            throw new IllegalArgumentException("Create a new map before putting values");
        }
    }

    public Map<String,String>[] toArray() {
        return maps.toArray(new Map[0]);
    }
}
