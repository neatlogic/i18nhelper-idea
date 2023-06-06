package com.neatlogic;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class I18nConfig {
    private final Project project;
    private JsonObject setting;


    public I18nConfig(Project project) {
        this.project = project;
        I18nhelperSetting settings = project.getService(I18nhelperSetting.class);
        String json = settings.json;
        Gson gson = new Gson();
        if (StringUtils.isNotBlank(json)) {
            try {
                setting = gson.fromJson(json, JsonObject.class);
            } catch (Exception ignored) {
            }
        }
    }

    public String getCurrentLang() {
        I18nhelperSetting settings = project.getService(I18nhelperSetting.class);
        return settings.currentLang;
    }

    public void setCurrentLang(String lang) {
        I18nhelperSetting settings = project.getService(I18nhelperSetting.class);
        settings.currentLang = lang;
    }


    public String getSource() {
        JsonObject translateObj = setting.getAsJsonObject("translate");
        if (translateObj != null && !translateObj.entrySet().isEmpty()) {
            return translateObj.get("source") != null ? translateObj.get("source").getAsString() : null;
        }
        return null;
    }

    public List<String> getTargetList() {
        JsonObject translateObj = setting.getAsJsonObject("translate");
        if (translateObj != null && !translateObj.entrySet().isEmpty()) {
            if (translateObj.get("target") != null) {
                JsonArray list = translateObj.getAsJsonArray("target");
                List<String> returnList = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    returnList.add(list.get(i).getAsString());
                }
                return returnList;
            }
        }
        return null;
    }

    public String getSecret() {
        JsonObject translateObj = setting.getAsJsonObject("translate");
        if (translateObj != null && !translateObj.entrySet().isEmpty()) {
            return translateObj.get("secret") != null ? translateObj.get("secret").getAsString() : null;
        }
        return null;
    }

    public String getAppId() {
        JsonObject translateObj = setting.getAsJsonObject("translate");
        if (translateObj != null && !translateObj.entrySet().isEmpty()) {
            return translateObj.get("appid") != null ? translateObj.get("appid").getAsString() : null;
        }
        return null;
    }

    Map<String, JsonObject> langPackObj = new HashMap<>();

    public JsonObject getLangPack(String lang) throws IOException {
        if (StringUtils.isNotBlank(lang)) {
            if (!this.langPackObj.containsKey(lang)) {
                VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(this.getPath(lang));
                if (virtualFile == null || !virtualFile.exists()) {
                    return null;
                }
                Gson gson = new Gson();
                String fileContent = StreamUtil.readText(virtualFile.getInputStream(), StandardCharsets.UTF_8);
                if (StringUtils.isBlank(fileContent)) {
                    fileContent = "{}";
                }
                return gson.fromJson(fileContent, JsonObject.class);
            }
            return this.langPackObj.get(lang);
        } else {
            return this.getLangPack();
        }
    }

    public JsonObject getLangPack() throws IOException {
        if (!this.langPackObj.containsKey("default")) {
            VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(this.getPath());
            if (virtualFile == null || !virtualFile.exists()) {
                return null;
            }
            Gson gson = new Gson();
            String fileContent = StreamUtil.readText(virtualFile.getInputStream(), StandardCharsets.UTF_8);
            if (StringUtils.isBlank(fileContent)) {
                fileContent = "{}";
            }
            return gson.fromJson(fileContent, JsonObject.class);
        }
        return this.langPackObj.get("default");
    }


    public String getPath(String lang) {
        if (StringUtils.isBlank(lang) || lang.equalsIgnoreCase(this.getSource())) {
            return this.getPath();
        } else {
            String basePath = project.getBasePath();
            if (setting != null && setting.get("path_" + lang) != null) {
                String p = setting.get("path_" + lang).getAsString();
                return StringUtils.isNotBlank(basePath) ? Paths.get(basePath, p).toString() : p;
            }
        }
        return null;
    }

    public String getPath() {
        String basePath = project.getBasePath();
        if (setting != null && setting.get("path") != null) {
            String p = setting.get("path").getAsString();
            return StringUtils.isNotBlank(basePath) ? Paths.get(basePath, p).toString() : p;
        }
        return null;
    }


}
