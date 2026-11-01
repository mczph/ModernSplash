package gkappa.modernsplash;

import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Config {

    private static Configuration config;

    public static boolean enabled;
    public static boolean forgeLogo;
    public static boolean rotate;
    public static boolean mimicModern;
    public static boolean swapBottomCorners;
    public static float uiScale = SplashScale.DEFAULT;
    public static int logoOffset;
    public static int backgroundColor;
    public static int fontColor;
    public static int logoColor;
    public static int barBorderColor;
    public static int barColor;
    public static int barBackgroundColor;
    public static boolean showMemory;
    public static boolean showTotalMemoryLine;
    public static boolean displayStartupTimeOnMainMenu = true;
    public static boolean enableTimer = true;
    public static int memoryGoodColor;
    public static int memoryWarnColor;
    public static int memoryLowColor;

    public static String fontTexture;
    public static String forgeTexture;
    public static String logoTexture;
    public static String resourcePackPath;

    public static ResourceLocation fontLoc;
    public static ResourceLocation forgeLoc;
    public static ResourceLocation logoLoc;
    public static File miscPackFile;

    public static void load() {
        File configFile = new File(Minecraft.getMinecraft().gameDir, "config/modernsplash.cfg");
        config = new Configuration(configFile);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmm");
        int now = Integer.parseInt(formatter.format(LocalDateTime.now()));

        enabled = config.getBoolean("enabled", "splash", true, "Enable the custom splash screen")
            && ((!FMLClientHandler.instance().hasOptifine()) || Launch.blackboard.containsKey("optifine.ForgeSplashCompatible"));
        rotate = config.getBoolean("rotate", "splash", false, "Rotate the Forge logo");
        forgeLogo = config.getBoolean("forgeLogo", "splash", false, "Show the Forge logo");
        swapBottomCorners = config.getBoolean("swapBottomCorners", "splash", false,
            "Swap the bottom corners: timer on the right, Forge logo on the left. Restart to apply.");
        mimicModern = config.getBoolean("mimicModern", "splash", false, "Only show main progress bar and logo");
        Property scaleProperty = config.get("splash", "uiScale", (double) SplashScale.DEFAULT,
            "Multiplier for the window-adaptive loading screen scale: 1.0 = automatic size, "
                + "0.5 = half automatic size, 2.0 = double automatic size. Scales the logo, bars and text "
                + "independently of Minecraft GUI scale. Restart to apply changes to this multiplier.",
            (double) SplashScale.MIN, (double) SplashScale.MAX);
        uiScale = SplashScale.normalize((float) scaleProperty.getDouble(SplashScale.DEFAULT));
        scaleProperty.setValue((double) uiScale);
        showMemory = config.getBoolean("showMemory", "splash", true, "Show the memory usage bar");
        showTotalMemoryLine = config
            .getBoolean("showTotalMemoryLine", "splash", false, "Show the total memory line on the memory bar");
        enableTimer = config.getBoolean("enableTimer", "splash", true, "Show the startup timer");
        displayStartupTimeOnMainMenu = config
            .getBoolean("timeOnMainMenu", "splash", true, "Show startup time on the main menu");

        logoOffset = config.getInt("logoOffset", "splash", 0, -999, 999, "Logo offset in pixels");

        backgroundColor = getConfigColor("background", 0xEF323D, "Background color (hex RRGGBB)");
        fontColor = getConfigColor("font", 0xFFFFFF, "Font color (hex RRGGBB)");
        logoColor = getConfigColor("logo", 0xFFFFFF, "Logo color (hex RRGGBB)");
        barBorderColor = getConfigColor("barBorder", 0xFFFFFF, "Progress bar border color (hex RRGGBB)");
        barColor = getConfigColor("bar", 0xFFFFFF, "Progress bar fill color (hex RRGGBB)");
        barBackgroundColor = getConfigColor("barBackground", 0xEF323D, "Progress bar background color (hex RRGGBB)");
        memoryGoodColor = getConfigColor("memoryGood", 0xFFFFFF, "Memory bar color (good, hex RRGGBB)");
        memoryWarnColor = getConfigColor("memoryWarn", 0xFFFFFF, "Memory bar color (warn, hex RRGGBB)");
        memoryLowColor = getConfigColor("memoryLow", 0xFFFFFF, "Memory bar color (low, hex RRGGBB)");

        boolean darkModeOnly = config
            .getBoolean("darkModeOnly", "splash", false, "Force dark mode always (ignores time range)");

        int darkStartTime = config.getInt("darkStartTime", "splash", 2300, 0, 2359, "Dark mode start time (HHmm)");
        int darkEndTime = config.getInt("darkEndTime", "splash", 600, 0, 2359, "Dark mode end time (HHmm)");

        int backgroundColorNight = getNightConfigColor(
            "backgroundDark",
            DarkPalette.BACKGROUND, 0x202020,
            "Background color night mode (hex RRGGBB)");
        int fontColorNight = getNightConfigColor("fontDark", DarkPalette.FONT, 0x606060, "Font color night mode (hex RRGGBB)");
        int logoColorNight = getNightConfigColor("logoDark", DarkPalette.LOGO, 0x999999, "Logo color night mode (hex RRGGBB)");
        int barBorderColorNight = getNightConfigColor(
            "barBorderDark",
            DarkPalette.BAR_BORDER, 0x4E4E4E,
            "Progress bar border color night mode (hex RRGGBB)");
        int barColorNight = getNightConfigColor("barDark", DarkPalette.BAR, 0x4E4E4E, "Progress bar fill color night mode (hex RRGGBB)");
        int barBackgroundColorNight = getNightConfigColor(
            "barBackgroundDark",
            DarkPalette.BAR_BACKGROUND, 0x202020,
            "Progress bar background color night mode (hex RRGGBB)");
        int memoryGoodColorNight = getNightConfigColor(
            "memoryGoodDark",
            DarkPalette.MEMORY_GOOD, 0x4E4E4E,
            "Memory bar color night mode (good, hex RRGGBB)");
        int memoryWarnColorNight = getNightConfigColor(
            "memoryWarnDark",
            DarkPalette.MEMORY_WARN, 0x4E4E4E,
            "Memory bar color night mode (warn, hex RRGGBB)");
        int memoryLowColorNight = getNightConfigColor(
            "memoryLowDark",
            DarkPalette.MEMORY_LOW, 0x4E4E4E,
            "Memory bar color night mode (low, hex RRGGBB)");

        if (darkModeOnly || (darkEndTime >= darkStartTime ? (now >= darkStartTime && now < darkEndTime)
            : (now >= darkStartTime || now <= darkEndTime))) {
            backgroundColor = backgroundColorNight;
            fontColor = fontColorNight;
            logoColor = logoColorNight;
            barBorderColor = barBorderColorNight;
            barColor = barColorNight;
            barBackgroundColor = barBackgroundColorNight;
            memoryGoodColor = memoryGoodColorNight;
            memoryWarnColor = memoryWarnColorNight;
            memoryLowColor = memoryLowColorNight;
        }

        fontTexture = config
            .getString("fontTexture", "splash", "textures/font/ascii.png", "Font texture resource location");
        forgeTexture = config
            .getString("forgeTexture", "splash", "fml:textures/gui/forge.png", "Forge logo resource location (PNG, animated PNG strip or GIF)");
        logoTexture = config.getString("logoTexture", "splash", "modernsplash:textures/gui/title/mojang.png",
            "Title logo resource location (PNG or GIF). Its aspect ratio is preserved.");
        resourcePackPath = config.getString("resourcePackPath", "splash", "resources", "Resource pack directory path");

        fontLoc = new ResourceLocation(fontTexture);
        forgeLoc = new ResourceLocation(forgeTexture);
        logoLoc = new ResourceLocation(logoTexture);
        miscPackFile = new File(Minecraft.getMinecraft().gameDir, resourcePackPath);

        config.save();
    }

    public static boolean disableSplash() {
        enabled = false;
        config.get("splash", "enabled", true, "Enable the custom splash screen")
            .setValue(false);
        config.save();
        return true;
    }

    private static int getConfigColor(String name, int def, String comment) {
        Property prop = config.get(
            "splash",
            name,
            "0x" + Integer.toString(def, 16)
                .toUpperCase(),
            comment);
        return Integer.decode(prop.getString());
    }

    private static int getNightConfigColor(String name, int def, int legacy, String comment) {
        Property prop = config.get("splash", name, "0x" + Integer.toHexString(def).toUpperCase(), comment);
        int configured = Integer.decode(prop.getString());
        int upgraded = DarkPalette.upgradeLegacyColor(configured, legacy, def);
        if (upgraded != configured) prop.setValue("0x" + Integer.toHexString(upgraded).toUpperCase());
        return upgraded;
    }
}
