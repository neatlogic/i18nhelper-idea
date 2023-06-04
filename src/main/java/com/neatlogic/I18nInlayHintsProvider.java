package com.neatlogic;

import com.intellij.codeInsight.hints.*;
import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.codeInsight.hints.presentation.InlayTextMetricsStorage;
import com.intellij.codeInsight.hints.presentation.PresentationFactory;
import com.intellij.codeInsight.hints.presentation.TextInlayPresentation;
import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class I18nInlayHintsProvider implements InlayHintsProvider<NoSettings> {

    @Override
    public boolean isVisibleInSettings() {
        return false;
    }

    @NotNull
    @Override
    public SettingsKey<NoSettings> getKey() {
        return new SettingsKey<NoSettings>("i18nInlayHintsProvider");
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getName() {
        return "I18n hint";
    }

    @Nullable
    @Override
    public String getPreviewText() {
        return null;
    }

    @NotNull
    @Override
    public ImmediateConfigurable createConfigurable(@NotNull NoSettings noSettings) {
        return null;
    }

    @NotNull
    @Override
    public NoSettings createSettings() {
        return new NoSettings();
    }

    @Nullable
    @Override
    public InlayHintsCollector getCollectorFor(@NotNull PsiFile psiFile, @NotNull Editor editor, @NotNull NoSettings noSettings, @NotNull InlayHintsSink inlayHintsSink) {
        return new FactoryInlayHintsCollector(editor) {
            @Override
            public boolean collect(@NotNull PsiElement psiElement, @NotNull Editor editor, @NotNull InlayHintsSink inlayHintsSink) {
                return processElement(psiElement, editor, inlayHintsSink, getFactory());
            }
        };
    }

    private boolean processElement(@NotNull PsiElement element, @NotNull Editor editor, @NotNull InlayHintsSink inlayHintsSink, PresentationFactory presentFactory) {
        // process the current element
        //System.out.println(element);
        if (element instanceof PsiLiteralExpression) {
            if (((PsiLiteralExpression) element).getValue() instanceof String) {
                //处理返回值是字符串
                String value = (String) ((PsiLiteralExpression) element).getValue();
                I18nConfig config = I18nConfigFactory.getInstance(element.getProject());
                String path = config.getPath(config.getCurrentLang());
                if (StringUtils.isNotBlank(path)) {
                    try {
                        String result = Utils.findValueByKey(path, value);
                        if (StringUtils.isNotBlank(result)) {
                            inlayHintsSink.addInlineElement(element.getTextRange().getEndOffset(), true, presentFactory.roundWithBackground(presentFactory.smallText(result)));
                        }
                    } catch (Exception ex) {

                    }
                }
            }
        } /*else if (element instanceof PsiMethodCallExpression) {
            //处理$.t函数的变量
            PsiMethodCallExpression methodCall = (PsiMethodCallExpression) element;
            PsiReferenceExpressionImpl methodExpression = (PsiReferenceExpressionImpl) methodCall.getMethodExpression();
            if ("$.t".equals(methodExpression.getText())) {
                PsiLiteralExpression argument = (PsiLiteralExpression) methodCall.getArgumentList().getExpressions()[0];
                if (argument.getValue() != null && StringUtils.isNotBlank(argument.getValue().toString())) {
                    String value = argument.getValue().toString();
                    I18nConfig config = I18nConfigFactory.getInstance(element.getProject());
                    String path = config.getPath(config.getCurrentLang());
                    if (StringUtils.isNotBlank(path)) {
                        try {
                            String result = Utils.findValueByKey(path, value);
                            if (StringUtils.isNotBlank(result)) {
                                inlayHintsSink.addInlineElement(argument.getTextRange().getEndOffset(), true, presentFactory.roundWithBackground(presentFactory.text(result)));
                            }
                        } catch (Exception ex) {

                        }
                    }
                }
            }
        }*/
        // recursively process child elements
        for (PsiElement child : element.getChildren()) {
            if (processElement(child, editor, inlayHintsSink, presentFactory)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean isLanguageSupported(@NotNull Language language) {
        return language.isKindOf(JavaLanguage.INSTANCE);
    }
}

