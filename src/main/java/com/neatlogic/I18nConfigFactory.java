package com.neatlogic;

import com.intellij.openapi.project.Project;

import java.util.HashMap;
import java.util.Map;

public class I18nConfigFactory {
    private static final Map<Project, I18nConfig> projectConfig = new HashMap<>();

    public synchronized static I18nConfig getInstance(Project project) {
        if (projectConfig.get(project) == null) {
            projectConfig.put(project, new I18nConfig(project));
        }
        return projectConfig.get(project);
    }

}
