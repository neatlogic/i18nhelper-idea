package com.neatlogic;

import com.intellij.codeInsight.hints.*;
import com.intellij.codeInsight.hints.presentation.PresentationFactory;
import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiLiteralExpression;
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
        return new SettingsKey<>("i18nInlayHintsProvider");
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getName() {
        return "I18n hint";
    }

    @Override
    public String getPreviewText() {
        return null;
    }

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
        final int[] count = {0};
        return new FactoryInlayHintsCollector(editor) {

            @Override
            public boolean collect(@NotNull PsiElement psiElement, @NotNull Editor editor, @NotNull InlayHintsSink inlayHintsSink) {
                if (count[0] == 0) {
                    count[0] += 1;
                    return processElement(psiFile, editor, inlayHintsSink, getFactory());
                }
                return false;
            }
        };
    }

    private boolean processElement(@NotNull PsiElement element, @NotNull Editor editor, @NotNull InlayHintsSink inlayHintsSink, PresentationFactory presentFactory) {
        // process the current element
        if (element instanceof PsiLiteralExpression) {
            if (((PsiLiteralExpression) element).getValue() instanceof String) {
                //处理返回值是字符串
                String value = (String) ((PsiLiteralExpression) element).getValue();
                I18nConfig config = I18nConfigFactory.getInstance(element.getProject());
                String path = config.getPath(config.getCurrentLang());
                if (StringUtils.isNotBlank(path)) {
                    try {
                        String result = Utils.findValueByKey(config.getLangPack(config.getCurrentLang()), value);
                        if (StringUtils.isNotBlank(result)) {
                            inlayHintsSink.addInlineElement(element.getTextRange().getEndOffset(), true, presentFactory.roundWithBackground(presentFactory.text(result)));
                        }
                    } catch (Exception ignored) {

                    }
                }
            }
        }
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

