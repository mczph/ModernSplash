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
together, independently of Minecraft's GUI scale. `1.0` uses the automatic size,
`0.5` halves it and `2.0` doubles it. The multiplier range is `0.25` to `4.0`;
non-finite values fall back to `1.0`. Multipliers above `1.0` can crop content.
For example, a 1708 x 960 window uses a final scale of `2.0` with `uiScale=1.0`,
or `1.5` with `uiScale=0.75`. The timer and
Forge logo stay anchored to their corners, and the memory bar stays near the top.
The logo keeps the same size when fading into the main menu.

The game window size and the main menu GUI scale are unchanged.

## Cleanroom 0.6.13 development

Use JDK 25 and the Gradle Wrapper. The loader version can be overridden without
changing the upstream default:

```shell
./gradlew build -Pcleanroom_version=0.6.13-alpha
./gradlew runClient -Pcleanroom_version=0.6.13-alpha
```

In PowerShell, quote the property argument:
`./gradlew.bat build '-Pcleanroom_version=0.6.13-alpha'`.
