# ModernSplash

适用于 Minecraft 1.12.2 / Cleanroom Loader 的加载界面模组，支持窗口自适应、自定义图片和绿色荧光暗色主题。

首次运行后，在游戏目录的 `config/modernsplash.cfg` 中修改配置，重启游戏生效。以下示例中的配置项都位于已有的 `splash { ... }` 内，修改对应项即可。

## 自定义标题 Logo

1. 准备一张 PNG 图片或 GIF 动图，推荐使用透明背景。
2. 在游戏目录中创建 `resources/assets/custom/textures/`，将图片放进去，例如 `title.png`。
3. 修改配置：

```text
splash {
    S:logoTexture=custom:textures/title.png
}
```

重启后，你的图片会完整替换加载界面中央的 Mojang Logo，保留图片本身的颜色、透明度和宽高比例。使用 GIF 时，将文件名和配置中的扩展名都改为 `.gif`，动图会循环播放。以后替换同一路径的图片并重启即可更新。

自定义图片不受 `logo` / `logoDark` 影响，即使旧配置为黑色，也会按图片原色显示。进入主菜单时的淡出效果使用同一张图片。

内置 Logo 的日间默认保持红色背景（`0xEF323D`）和白色 Logo，夜间使用深绿背景和白色 Logo。`logo` 和 `logoDark` 分别控制内置 Logo 的日间、夜间染色，默认均为白色 `0xFFFFFF`。已有自定义配色会保留。

默认标题路径为 `modernsplash:textures/gui/title/mojang.png`，改回该值可恢复内置图片。自定义图片缺失或无法读取时，也会使用内置图片。

上面的路径以游戏实际运行目录为准，使用版本隔离时通常是该版本的文件夹。若修改了 `resourcePackPath`，请将图片放到该目录下的 `assets/custom/textures/`。

## 自定义 Forge 图标

将图片放入同一个文件夹，例如 `resources/assets/custom/textures/forge.gif`，然后修改：

```text
splash {
    B:forgeLogo=true
    S:forgeTexture=custom:textures/forge.gif
    B:mimicModern=false
    B:rotate=false
}
```

支持 PNG 和 GIF，也兼容旧式竖向 PNG 帧图。`rotate=true` 会让整个图标旋转；关闭旋转仍会播放 GIF。图标按图片原色显示。

## 界面大小与位置

界面自动跟随窗口大小调整，`uiScale` 在此基础上控制整体缩放，独立于 Minecraft 的 GUI 大小设置：

```text
splash {
    D:uiScale=0.75
    B:swapBottomCorners=false
}
```

- `uiScale`：默认 `0.75`，可设为 `0.25`～`4.0`；数值越大，图片、进度条和文字越大，过大可能超出窗口。
- `swapBottomCorners=false`：计时器在左下角，Forge 图标在右下角；改为 `true` 交换位置。
- `logoOffset`：调整 Forge 图标到窗口边缘的距离。

NeoFontRender 提示与计时器使用一致的字体大小，并随界面一起缩放。

## 暗色主题

暗色模式使用深绿背景和荧光绿进度条，Logo 默认保留图片原色。

- `darkModeOnly=true`：全天启用暗色模式。
- `darkStartTime` / `darkEndTime`：设置启用时段，格式为 `HHmm`，例如 `2300` 表示 23:00。
- `backgroundDark`、`fontDark`、`barDark` 等选项可分别调整背景、文字和进度条颜色。

## 开发构建

使用 JDK 25，在项目目录运行：

```powershell
./gradlew.bat build '-Pcleanroom_version=0.6.13-alpha'
```

构建产物位于 `build/libs/`，游戏使用不带 `-dev` 或 `-sources` 后缀的 JAR。开发客户端的自定义图片放在 `run/client/resources/assets/custom/textures/`。

## 致谢

项目构建模板基于 [SizableShrimp 的 ForgeTemplate](https://github.com/SizableShrimp/ForgeTemplate/tree/1.12.x)。
