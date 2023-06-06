package com.neatlogic;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.hints.ParameterHintsPassFactory;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jetbrains.annotations.NotNull;

public class I18nCurrentLangAction extends ToggleAction {
    private final String name;
    private final String lang;


    I18nCurrentLangAction(String name, String lang) {
        this.name = name;
        this.lang = lang;
    }

    @Override
    public void update(AnActionEvent e) {
        // 设置动作的名称
        e.getPresentation().setText(name);
        I18nConfig config = I18nConfigFactory.getInstance(e.getProject());
        if (this.lang.equalsIgnoreCase(config.getCurrentLang())) {
            e.getPresentation().setIcon(AllIcons.General.ArrowRight);
        } else {
            e.getPresentation().setIcon(null);
        }
    }


    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        if (e.getProject() != null) {
            I18nConfig config = I18nConfigFactory.getInstance(e.getProject());
            return this.lang.equalsIgnoreCase(config.getCurrentLang());
        }
        return false;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        if (e.getProject() != null) {
            I18nConfig config = I18nConfigFactory.getInstance(e.getProject());
            config.setCurrentLang(lang);
            //ParameterHintsPassFactory.forceHintsUpdateOnNextPass();
        }
    }
}
