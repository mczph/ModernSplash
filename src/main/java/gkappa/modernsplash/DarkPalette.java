package gkappa.modernsplash;

/** Near-black green surfaces with bright phosphor accents. */
final class DarkPalette {
    static final int BACKGROUND = 0x06110C;
    static final int FONT = 0xB9FFD5;
    static final int LOGO = 0x000000;
    static final int BAR_BORDER = 0x2CFF88;
    static final int BAR = 0x39FF88;
    static final int BAR_BACKGROUND = 0x0B2518;
    static final int MEMORY_GOOD = 0x39FF88;
    static final int MEMORY_WARN = 0xD5FF45;
    static final int MEMORY_LOW = 0xFFBE55;

    private DarkPalette() {}

    static int upgradeLegacyColor(int configured, int legacy, int replacement) {
        return configured == legacy ? replacement : configured;
    }
}
