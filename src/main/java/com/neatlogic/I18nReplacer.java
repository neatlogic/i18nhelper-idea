package com.neatlogic;

import com.google.gson.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class I18nReplacer extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Gson gson = new Gson();
        JsonObject setting = new JsonObject();
        String basePath = null;
        if (project != null) {
            basePath = project.getBasePath();
            I18nhelperSetting settings = ServiceManager.getService(project, I18nhelperSetting.class);
            String json = settings.json;
            if (StringUtils.isNotBlank(json)) {
                try {
                    setting = gson.fromJson(json, JsonObject.class);
                } catch (Exception ignored) {
                    Messages.showErrorDialog("Configuration content does not comply with JSON format.", "Fatal Error");
                }
            } else {
                Messages.showInfoMessage("Configuration is Null", "Fatal Error");
                return;
            }
        } else {
            Messages.showInfoMessage("Project is Null", "Fatal Error");
        }
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        String selectedText;
        int start, end;
        if (editor != null) {
            final SelectionModel selectionModel = editor.getSelectionModel();
            selectedText = selectionModel.getSelectedText();
            // 获取选中的文本和它的范围
            start = selectionModel.getSelectionStart();
            end = selectionModel.getSelectionEnd();
        } else {
            Messages.showInfoMessage("Editor is Null", "Fatal Error");
            return;
        }
        if (setting == null || setting.entrySet().isEmpty()) {
            Messages.showInfoMessage("Configuration is Null", "Fatal Error");
        }
        if (StringUtils.isNotBlank(selectedText)) {
            String path = setting.get("path").getAsString();
            if (StringUtils.isNotBlank(path)) {
                String absolutePath = StringUtils.isNotBlank(basePath) ? Paths.get(basePath, path).toString() : path;
                VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(absolutePath);

                if (virtualFile != null && virtualFile.exists()) {
                    try {
                        String fileContent = StreamUtil.readText(virtualFile.getInputStream(), StandardCharsets.UTF_8);
                        JsonObject languagePack = gson.fromJson(fileContent, JsonObject.class);
                        String key = findKeyByValue(languagePack, selectedText, "");
                        if (StringUtils.isNotBlank(key)) {
                            // 执行一个写入命令，替换选中的文本
                            WriteCommandAction.runWriteCommandAction(project, () -> {
                                Document document = editor.getDocument();
                                document.replaceString(start, end, key);
                            });
                        } else {
                            String newKey = Messages.showInputDialog(project, "Please input new key, eg:common.name", "", Messages.getQuestionIcon());
                            if (StringUtils.isNotBlank(newKey)) {
                                try {
                                    JsonObject newLanguagePack = addCompoundKey(languagePack, newKey, selectedText);
                                    Document jsonDocument = FileDocumentManager.getInstance().getDocument(virtualFile);
                                    if (jsonDocument == null) {
                                        return;
                                    }
                                    Gson outputGson = new GsonBuilder().setPrettyPrinting().create();
                                    ApplicationManager.getApplication().runWriteAction(() -> jsonDocument.setText(outputGson.toJson(newLanguagePack)));

                                    // 必要时保存更改
                                    FileDocumentManager.getInstance().saveDocument(jsonDocument);
                                    WriteCommandAction.runWriteCommandAction(project, () -> {
                                        Document document = editor.getDocument();
                                        document.replaceString(start, end, newKey);
                                    });
                                    translate(selectedText, newKey, setting, e);
                                } catch (IllegalArgumentException ex) {
                                    Messages.showInfoMessage(project, ex.getMessage(), "");
                                } catch (NoSuchAlgorithmException ex) {
                                    //
                                } catch (IOException ex) {
                                    Messages.showInfoMessage(project, "Translate failed:" + ex.getMessage(), "Translate Error");
                                }
                            }
                        }
                        // 使用文件内容...
                    } catch (IOException ex) {
                        // 处理异常...
                    }
                } else {
                    Messages.showInfoMessage("i18n file:" + absolutePath + "is not exists.", "Fatal Error");
                }
            }


        }
    }

    private static String findKeyByValue(JsonElement element, String targetValue, String currentPath) {
        if (element.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                String newPath = currentPath.isEmpty() ? entry.getKey() : currentPath + "." + entry.getKey();
                String result = findKeyByValue(entry.getValue(), targetValue, newPath);
                if (result != null) {
                    return result;
                }
            }
        } else if (element.isJsonPrimitive()) {
            if (element.getAsJsonPrimitive().getAsString().equals(targetValue)) {
                return currentPath;
            }
        }

        return null;
    }

    public static JsonObject addCompoundKey(JsonObject jsonObject, String compoundKey, String newValue) {
        String[] keys = compoundKey.split("\\.");
        JsonObject currentObject = jsonObject;
        String currentKey = "";
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            if (StringUtils.isNotBlank(currentKey)) {
                currentKey += ".";
            }
            currentKey += key;
            if (i == keys.length - 1) {
                // 到达最后一个键，设置新值
                currentObject.addProperty(key, newValue);
            } else {
                // 还没有到达最后一个键，检查下一层 JsonObject 是否存在
                JsonElement nextElement = currentObject.get(key);
                if (nextElement == null) {
                    // 如果下一层 JsonObject 不存在，创建一个新的 JsonObject
                    JsonObject nextObject = new JsonObject();
                    currentObject.add(key, nextObject);
                    currentObject = nextObject;
                } else if (nextElement.isJsonObject()) {
                    // 如果下一层 JsonObject 存在，继续使用它
                    currentObject = nextElement.getAsJsonObject();
                } else {
                    // 如果下一层已经存在，但并非一个 JsonObject，抛出异常
                    throw new IllegalArgumentException("Key:" + currentKey + " already exists and is not a JsonObject");
                }
            }

        }

        return jsonObject;
    }

    /*
   "path":"",
   "path_en":"",
   "translate": {
 "source": "zh",
 "target": ["en"],
 "appid": "20230322001610272",
 "secret": "qUV8AOOK_CLU3hlb1YXa"
}
    */
    private static void translate(String content, String newKey, JsonObject setting, AnActionEvent e) throws IOException, NoSuchAlgorithmException {
        JsonObject translateObj = setting.getAsJsonObject("translate");
        Project project = e.getProject();
        if (project != null && translateObj != null && !translateObj.entrySet().isEmpty()) {
            String basePath = project.getBasePath();
            String source = translateObj.get("source") != null ? translateObj.get("source").getAsString() : "";
            JsonArray targetList = translateObj.getAsJsonArray("target");
            String appId = translateObj.get("appid") != null ? translateObj.get("appid").getAsString() : "";
            String secret = translateObj.get("secret") != null ? translateObj.get("secret").getAsString() : "";
            if (StringUtils.isNotBlank(source) && StringUtils.isNotBlank(appId) && StringUtils.isNotBlank(secret) && targetList != null && targetList.size() > 0) {
                // 创建 URL 对象
                String urlString = "http://api.fanyi.baidu.com/api/trans/vip/translate";
                for (int i = 0; i < targetList.size(); i++) {
                    String target = targetList.get(i).getAsString();
                    String path = setting.get("path_" + target.toLowerCase()) != null ? setting.get("path_" + target.toLowerCase()).getAsString() : null;
                    if (StringUtils.isBlank(path)) {
                        continue;
                    }
                    String absolutePath = StringUtils.isNotBlank(basePath) ? Paths.get(basePath, path).toString() : path;
                    VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(absolutePath);
                    if (!virtualFile.exists()) {
                        continue;
                    }
                    int salt = 123456;
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    byte[] hashBytes = md.digest((appId + content + salt + secret).getBytes(StandardCharsets.UTF_8));

                    StringBuilder sb = new StringBuilder();
                    for (byte b : hashBytes) {
                        sb.append(String.format("%02x", b));
                    }
                    String sign = sb.toString();

                    Map<String, String> params = new HashMap<>();
                    params.put("q", content);
                    params.put("from", source);
                    params.put("to", target);
                    params.put("appid", appId);
                    params.put("salt", Integer.toString(salt));
                    params.put("sign", sign);

                    StringBuilder query = new StringBuilder();
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        if (query.length() > 0) {
                            query.append("&");
                        }
                        query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                                .append("=")
                                .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                    }

                    URL url = new URL(urlString + "?" + query);
                    HttpURLConnection connection = null;
                    try {
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        // 发起请求并获取响应状态码
                        int responseCode = connection.getResponseCode();
                        if (responseCode == 200) {
                            // 读取响应内容
                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            StringBuilder response = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                            reader.close();
                            Gson gson = new Gson();
                            JsonObject result = gson.fromJson(response.toString(), JsonObject.class);
                            JsonArray resultList = result.getAsJsonArray("trans_result");
                            String translatedText;
                            if (resultList != null && resultList.size() > 0) {
                                translatedText = resultList.get(0).getAsJsonObject().get("dst").getAsString();
                                if (StringUtils.isNotBlank(translatedText)) {
                                    String fileContent = StreamUtil.readText(virtualFile.getInputStream(), StandardCharsets.UTF_8);
                                    JsonObject languagePack = gson.fromJson(fileContent, JsonObject.class);
                                    JsonObject newLanguagePack = addCompoundKey(languagePack, newKey, translatedText);
                                    Document jsonDocument = FileDocumentManager.getInstance().getDocument(virtualFile);
                                    if (jsonDocument == null) {
                                        continue;
                                    }
                                    Gson outputGson = new GsonBuilder().setPrettyPrinting().create();
                                    ApplicationManager.getApplication().runWriteAction(() -> jsonDocument.setText(outputGson.toJson(newLanguagePack)));
                                    FileDocumentManager.getInstance().saveDocument(jsonDocument);
                                }
                            }
                        }
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                }
            }
        }
    }
}
