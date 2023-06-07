package com.neatlogic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;

/**
 * @author chenqiwei
 */
public class I18nhelperConfigurable implements SearchableConfigurable {
    private final Project project;
    private JTextArea jsonTextArea;

    public I18nhelperConfigurable(Project project) {
        this.project = project;
    }

    @Override
    public @NotNull String getId() {
        return "i18nhelper";
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "I18nHelper Settings";
    }

    @Override
    public JComponent createComponent() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonObject helpObj = new JsonObject();
        helpObj.addProperty("path", "module_dir/config/i18n/language_zh.json");
        helpObj.addProperty("path_en", "module_dir/config/i18n/language_en.json");
        JsonObject translateObj = new JsonObject();
        JsonArray targetList = new JsonArray();
        targetList.add("en");
        translateObj.addProperty("source", "zh");
        translateObj.add("target", targetList);
        translateObj.addProperty("appid", "百度翻译接口appid");
        translateObj.addProperty("secret", "百度翻译接口密码");
        helpObj.add("translate", translateObj);
        String help = "帮助：path是必填参数，path_xx是目标语言的配置文件，xx代表目标语言缩写，例如en、jp等，可以有多个。" +
                "<br>translate如果不配则不进行翻译，target代表目标语言列表，需要和path_xx的数量对应。<br>目前只支持百度翻译接口，请到<a href=\"http://api.fanyi.baidu.com/\">百度翻译</a>申请appid和secret。<br>";
        help += "范例：<pre>" + gson.toJson(helpObj) + "</pre>";
        JTextPane helpTextArea = new JTextPane();
        helpTextArea.setContentType("text/html");
        helpTextArea.setText(help);
        helpTextArea.setEditable(false);
        helpTextArea.setBackground(mainPanel.getBackground());
        helpTextArea.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        mainPanel.add(helpTextArea, BorderLayout.NORTH);
        jsonTextArea = new JTextArea(15, 60);
        JBScrollPane scrollPane = new JBScrollPane(jsonTextArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        I18nhelperSetting settings = project.getService(I18nhelperSetting.class);
        return !jsonTextArea.getText().equals(settings.json);
    }

    @Override
    public void apply() {
        I18nhelperSetting settings = project.getService(I18nhelperSetting.class);
        settings.json = jsonTextArea.getText();
    }

    @Override
    public void reset() {
        I18nhelperSetting settings = project.getService(I18nhelperSetting.class);
        jsonTextArea.setText(settings.json);
    }
}

