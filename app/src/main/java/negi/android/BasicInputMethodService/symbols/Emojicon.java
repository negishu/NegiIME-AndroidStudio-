package negi.android.BasicInputMethodService.symbols;

public class Emojicon {
    private int icon;
    private int codePoint;
    private String emoji;
    private Emojicon() {
    }
    public static Emojicon fromCodePoint(final int codePoint) {
        Emojicon emoji = new Emojicon();
        emoji.emoji = newString(codePoint);
        emoji.codePoint = codePoint;
        return emoji;
    }
    public static Emojicon fromChar(final char ch) {
        Emojicon emoji = new Emojicon();
        emoji.emoji = Character.toString(ch);
        emoji.codePoint = ch;
        return emoji;
    }
    public static Emojicon fromChars(final String chars) {
        Emojicon emoji = new Emojicon();
        emoji.emoji = chars;
        emoji.codePoint = chars.codePointAt(0);
        return emoji;
    }

    public int getCodePoint() {
        return codePoint;
    }
    public int getIcon() {
        return icon;
    }
    public final String getEmoji() {
        return emoji;
    }
    public static final String newString(int codePoint) {
        if (Character.charCount(codePoint) == 1) {
            return String.valueOf(codePoint);
        }
        else {
            return new String(Character.toChars(codePoint));
        }
    }
}
