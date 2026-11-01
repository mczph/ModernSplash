# ForgeTemplate
Everything but the Java Classes are thanks and are credited to SizeableShrimps ForgeTemplates found [Here](https://github.com/SizableShrimp/ForgeTemplate/tree/1.12.x)

## Setting up
* Open up `gradle.properties` and change all the necessary properties
* Rename the main package to be the same as `mod_base_package` in the properties file, e.g. `io.github.examplemod`

Finally, import into your IDE of choice.

## Loading screen size

The loading screen automatically scales to the current window size, using
854 x 480 as the reference size and preserving the aspect ratio. The scale is
recomputed every frame, so resizing the window during loading takes effect
immediately. The smaller of the width and height ratios determines the scale.
This recalculation only uses scalar arithmetic and does not allocate objects or
reload textures.

`uiScale` is an additional multiplier for this automatic size. Edit
`config/modernsplash.cfg` after the first launch, then restart Minecraft:

```text
splash {
    D:uiScale=0.75
}
```

The final scale is `min(windowWidth / 854, windowHeight / 480) * uiScale`.
It scales the loading logo, progress bars, memory bar, timer and warning text
together, independently of Minecraft's GUI scale. The default multiplier is
`0.75`. `1.0` uses the automatic size,
`0.5` halves it and `2.0` doubles it. The multiplier range is `0.25` to `4.0`;
non-finite values fall back to `0.75`. Existing configuration values are preserved.
Multipliers above `1.0` can crop content.
For example, a 1708 x 960 window uses a final scale of `2.0` with `uiScale=1.0`,
or `1.5` with `uiScale=0.75`. The timer and
Forge logo stay anchored to their corners, and the memory bar stays near the top.
The logo keeps the same size when fading into the main menu.

The game window size and the main menu GUI scale are unchanged.

## Custom title and Forge images

Add images under the directory selected by `resourcePackPath` (default:
`resources` in the game directory). For example:

```text
resources/
  assets/
    custom/
      textures/
        title.png
        forge.gif
```

Then set the following in the `splash` section of `config/modernsplash.cfg`:

```text
splash {
    D:uiScale=0.75
    S:logoTexture=custom:textures/title.png
    B:forgeLogo=true
    S:forgeTexture=custom:textures/forge.gif
    B:rotate=false
    B:swapBottomCorners=true
    B:mimicModern=false
}
```

In the development client the files belong in `run/client/resources/assets/custom/textures/`.
The title defaults to `modernsplash:textures/gui/title/mojang.png`; its aspect ratio
is preserved within the original logo box, including during the main-menu fade.
The title is tinted by the existing `logoColor` setting (and `logoDark` at night).
Use white if the original image colors should be retained. PNG and GIF are
supported for both images. Missing or unreadable custom files fall back to the
built-in image.

GIFs loop using their individual frame delays, with transparent and partial
frames composited before upload. A zero-delay frame uses 100 ms. Legacy vertical
PNG strips remain supported for the Forge icon, at 50 ms per square frame; tall
title PNGs are displayed as a single image. Decoding and texture upload happen
once at startup. During the short main-menu fade the title keeps its last frame.

`rotate=true` rotates the entire Forge image around its center. GIF playback is
independent of this setting: `rotate=false` still plays the animation. Rotation
only appears when `forgeLogo=true` and `mimicModern=false`.

## Swapping the bottom corners

`swapBottomCorners=false` (default) places the timer at bottom left and the Forge
icon at bottom right. `swapBottomCorners=true` swaps them: timer at bottom right,
Forge icon at bottom left. `logoOffset` still controls the Forge icon's edge
padding. This affects the loading timer; the startup-time text on the main menu
retains its existing position. Restart after changing these settings.

## Neon green dark mode

Dark mode uses a near-black green background, mint-green text, a separately
configurable logo tint and bright phosphor-green progress bars. Memory warnings change to lime
and amber so they remain distinguishable.

| Setting | Default |
| --- | --- |
| `backgroundDark` | `0x06110C` |
| `fontDark` | `0xB9FFD5` |
| `logoDark` | `0x000000` |
| `barBorderDark` | `0x2CFF88` |
| `barDark` | `0x39FF88` |
| `barBackgroundDark` | `0x0B2518` |
| `memoryGoodDark` | `0x39FF88` |
| `memoryWarnDark` | `0xD5FF45` |
| `memoryLowDark` | `0xFFBE55` |

Old gray defaults upgrade automatically on the next launch; other custom colors
are preserved. `darkModeOnly=true` enables this palette all day, while the existing
`darkStartTime` and `darkEndTime` settings select the scheduled mode.

NeoFontRender loading tips use their original pixel coordinate system. The splash
restores that projection and resets the model-view before third-party overlays.
A transform anchored to the bottom center applies the same adaptive scale and
`uiScale` multiplier as the timer, keeping their font sizes and bottom baseline
aligned. Tips remain visible in large windows and are unaffected by Forge logo rotation.

## Cleanroom 0.6.13 development

NeoFontRender 0.8.0 patches the splash rendering thread using a fixed local
variable slot for the progress-bar opacity. The rendering loop preserves this
layout so its loading tips remain compatible with the animation clock.
`NeoFontRenderCompatTest` checks that slot and can also run the actual installed
transformer when `NEOFONT_COMPAT_JAR` points to its jar.

Use JDK 25 and the Gradle Wrapper. The loader version can be overridden without
changing the upstream default:

```shell
./gradlew build -Pcleanroom_version=0.6.13-alpha
./gradlew runClient -Pcleanroom_version=0.6.13-alpha
```

In PowerShell, quote the property argument:
`./gradlew.bat build '-Pcleanroom_version=0.6.13-alpha'`.
