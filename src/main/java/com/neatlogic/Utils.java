package com.neatlogic;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public class Utils {
    public static String findValueByKey(String filePath, String key) throws IOException {
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath);
        if (virtualFile == null || !virtualFile.exists()) {
            return "";
        }
        Gson gson = new Gson();
        String fileContent = StreamUtil.readText(virtualFile.getInputStream(), StandardCharsets.UTF_8);
        JsonObject json = gson.fromJson(fileContent, JsonObject.class);
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
