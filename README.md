<!-- Plugin description -->
<p>
    <a href="https://opensource.org/license/gpl-3.0/" alt="License">
        <img src="https://img.shields.io/badge/License-GPL--3.0-green" /></a>
<a target="_blank" href="https://join.slack.com/t/neatlogichome/shared_invite/zt-1w037axf8-r_i2y4pPQ1Z8FxOkAbb64w">
<img src="https://img.shields.io/badge/Slack-Neatlogic-orange" /></a>
<a target="_blank" href="https://plugins.jetbrains.com/plugin/21921-i18nhelper"><img alt="JetBrains plugins" src="https://img.shields.io/jetbrains/plugin/d/21921"></a>
</p>

---

## Features 功能

Automatically convert selected text to a key in i18n. If the Chinese text does not exist, it will automatically write
the key and text content into the corresponding i18n configuration file, and complete the target language translation.

---


自动转换选中文本为i18n中的key，如果中文不存在，则自动往对应的i18n配置文件中写入key和文本内容，并完成目标语言翻译。

## Requirements 使用要求

This plugin requires that the i18n language file is in json format, for example:

language_zh.json:

``` json
{
    "common":{
        "name":"name",
        "age":"age"
    }
}
```

---

本插件要求i18n语言文件是json格式，例如

language_zh.json:

``` json
{
    "common":{
        "name":"名称",
        "age":"年龄"
    }
}
```

## Usage 使用方式

### Edit the language file path 编辑语言文件路径

![img.png](https://github.com/neatlogic/i18nhelper-idea/raw/main/IMAGES/img.png)

### Configure shortcut keys 配置快捷键

![img_1.png](https://github.com/neatlogic/i18nhelper-idea/raw/main/IMAGES/img_1.png)

### Select text and replace 选中文本并替换

Select any text in the editor, use the shortcut key to complete the replacement. If the key does not exist, you need to
input a new key.

---

在编辑器中选中任意文本，使用快捷键即可完成替换，如果key不存在，需要输入新的key

![img.png](https://github.com/neatlogic/i18nhelper-idea/raw/main/IMAGES/img3.png)

### Switch current language 切换当前语言

![img.png](https://github.com/neatlogic/i18nhelper-idea/raw/main/IMAGES/img4.png)

<!-- Plugin description end -->
