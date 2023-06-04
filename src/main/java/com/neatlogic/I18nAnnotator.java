package com.neatlogic;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class I18nAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        try {
            processElement(element, holder);
        } catch (Exception ignored) {

        }
    }

    private final Map<PsiElement, List<String>> elementCheckMap = new HashMap<>();

    private boolean processElement(@NotNull PsiElement element, AnnotationHolder holder) throws IOException {
        if (element instanceof PsiLiteralExpression) {
            if (((PsiLiteralExpression) element).getValue() instanceof String) {
                String value = (String) ((PsiLiteralExpression) element).getValue();
                I18nConfig config = I18nConfigFactory.getInstance(element.getProject());
                List<String> targetList = config.getTargetList();
                if (targetList != null && targetList.size() > 0) {
                    for (String target : targetList) {
                        String path = config.getPath(target);
                        if (StringUtils.isNotBlank(path)) {
                            String newValue = Utils.findValueByKey(path, value);
                            if (StringUtils.isNotBlank(newValue) && (elementCheckMap.get(element) == null || !elementCheckMap.get(element).contains(target))) {
                                //目标语言
                                holder.newAnnotation(HighlightSeverity.INFORMATION, target + ":" + newValue).range(element).create();
                                elementCheckMap.computeIfAbsent(element, k -> new ArrayList<>());
                                elementCheckMap.get(element).add(target);
                            }
                        }
                    }
                }
                String path = config.getPath();
                if (StringUtils.isNotBlank(path)) {
                    String newValue = Utils.findValueByKey(path, value);
                    if (StringUtils.isNotBlank(newValue) && (elementCheckMap.get(element) == null || !elementCheckMap.get(element).contains("default"))) {
                        //源语言
                        if (StringUtils.isNotBlank(config.getSource())) {
                            newValue = config.getSource() + ":" + newValue;
                        }
                        elementCheckMap.computeIfAbsent(element, k -> new ArrayList<>());
                        holder.newAnnotation(HighlightSeverity.INFORMATION, newValue).range(element).create();
                        elementCheckMap.get(element).add("default");

                    }
                }
            }
        }
        // recursively process child elements
        for (PsiElement child : element.getChildren()) {
            if (processElement(child, holder)) {
                return true;
            }
        }
        return false;
    }

}
