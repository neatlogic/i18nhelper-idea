package com.neatlogic;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class I18nCurrentLangActionGroup extends ActionGroup {

    private List<AnAction> actionList = new ArrayList<>();

    @Override
    public AnAction @NotNull [] getChildren(AnActionEvent e) {
        if (CollectionUtils.isEmpty(actionList)) {
            if (e != null && e.getProject() != null) {
                I18nConfig config = I18nConfigFactory.getInstance(e.getProject());
                List<String> targetList = config.getTargetList();
                actionList = new ArrayList<>();
                String source = config.getSource();
                if (StringUtils.isNotBlank(source)) {
                    actionList.add(new I18nCurrentLangAction(source, source));
                }
                for (String s : targetList) {
                    actionList.add(new I18nCurrentLangAction(s, s));
                }
            }
        }
        return actionList.toArray(new AnAction[0]);
    }
}
