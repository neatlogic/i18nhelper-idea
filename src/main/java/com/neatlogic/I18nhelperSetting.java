package com.neatlogic;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import com.intellij.openapi.components.*;

@State(name = "I18nhelperSetting", storages = @Storage("I18nhelperSetting.xml"))
public class I18nhelperSetting implements PersistentStateComponent<I18nhelperSetting> {
    public String json = "";

    @Override
    public I18nhelperSetting getState() {
        return this;
    }

    @Override
    public void loadState(I18nhelperSetting state) {
        this.json = state.json;
    }
}

