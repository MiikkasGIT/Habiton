package de.hsos.habiton.helpers;

import android.content.Context;
import android.content.res.Resources;

/**
 * Eine Hilfsklasse für allgemeine Utility-Funktionen.
 * @author Miikka Koensler
 */
public class Utility {

    /**
     * Überprüft, ob ein gegebener Text Emojis enthält.
     *
     * @param text Der zu überprüfende Text.
     * @return true, wenn der Text Emojis enthält, ansonsten false.
     */
    public static boolean isEmoji(String text) {
        return text != null && text.codePoints().anyMatch(codePoint ->
                (codePoint >= 0x1F300 && codePoint <= 0x1F64F) ||
                        (codePoint >= 0x1F680 && codePoint <= 0x1F6FF) ||
                        (codePoint >= 0x2600 && codePoint <= 0x26FF));
    }

    /**
     * Konvertiert DP in Pixel basierend auf dem Gerätebildschirm.
     *
     * @param context Der Kontext der Anwendung.
     * @return Die Anzahl der Pixel, die dem angegebenen DP-Wert entsprechen.
     */
    public static int convertDpToPixel(Context context) {
        return (int) (10 * context.getResources().getDisplayMetrics().density);
    }

    /**
     * Konvertiert dp in Pixel.
     *
     * @return Die Anzahl der Pixel.
     */
    public static int dpToPx() {
        return (int) (10 * Resources.getSystem().getDisplayMetrics().density);
    }
}
