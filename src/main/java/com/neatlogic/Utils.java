package com.neatlogic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class Utils {
    public static String findValueByKey(JsonObject json, String key) throws IOException {
        String[] keys = key.split("\\.");
        for (int i = 0; i < keys.length - 1; i++) {
            String subKey = keys[i];
            if (json.get(subKey) != null && json.get(subKey) instanceof JsonObject) {
                json = json.getAsJsonObject(subKey);
            } else {
                if (i < keys.length - 2) {
                    return null;
                }
                break;
            }
        }
        if (json.get(keys[keys.length - 1]) != null) {
            return json.get(keys[keys.length - 1]).getAsString();
        }
        return null;
    }

    public static JsonObject sortJsonObject(JsonObject unsortedJsonObject) {
        Map<String, JsonElement> sortedMap = new TreeMap<>();
        for (Map.Entry<String, JsonElement> entry : unsortedJsonObject.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                sortedMap.put(entry.getKey(), sortJsonObject(entry.getValue().getAsJsonObject()));
            } else {
                sortedMap.put(entry.getKey(), entry.getValue());
            }
        }

        JsonObject sortedJsonObject = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : sortedMap.entrySet()) {
            sortedJsonObject.add(entry.getKey(), entry.getValue());
        }
        return sortedJsonObject;
    }
}
