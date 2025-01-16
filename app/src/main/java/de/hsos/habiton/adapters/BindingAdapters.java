package de.hsos.habiton.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;

import com.google.android.material.button.MaterialButton;

import de.hsos.habiton.R;

/**
 * Binding-Adapter für die Datenbindung mit spezifischen Attributen in Android-Ansichten.
 * @author Finn Garrels
 */
public class BindingAdapters {

    /**
     * Setzt ein Icon als Drawable für ein TextView.
     * Wenn das Icon nicht gefunden wird, wird der Icon-Text stattdessen im TextView angezeigt.
     *
     * @param view Das TextView, für das das Icon gesetzt werden soll.
     * @param icon Der Name des Icons, das als Drawable gesetzt werden soll.
     */
    @BindingAdapter("iconSrc")
    public static void setIconSrc(TextView view, String icon) {
        Drawable drawable = getDrawableFromName(view.getContext(), icon);
        if (drawable != null) {
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            view.setCompoundDrawables(null, drawable, null, null);
        } else {
            view.setCompoundDrawables(null, null, null, null);
            view.setText(icon);
        }
    }

    /**
     * Setzt ein Icon für einen MaterialButton.
     * Wenn das Icon nicht gefunden wird, wird kein Icon gesetzt.
     *
     * @param button      Der MaterialButton, für den das Icon gesetzt werden soll.
     * @param iconResName Der Name des Icons, das als Drawable für den Button gesetzt werden soll.
     */
    @BindingAdapter("iconSrc")
    public static void setIconSrc(MaterialButton button, String iconResName) {
        Drawable drawable = getDrawableFromName(button.getContext(), iconResName);
        button.setIcon(drawable);
    }

    /**
     * Setzt die Hintergrundfarbe eines Views basierend auf einem Icon-Namen.
     * Wenn keine passende Farbe gefunden wird, wird eine Standardfarbe verwendet.
     *
     * @param view Das View-Element, für das die Hintergrundfarbe gesetzt werden soll.
     * @param icon Der Name des Icons, für das die Hintergrundfarbe ermittelt werden soll.
     */
    @BindingAdapter("backgroundTintByIcon")
    public static void setBackgroundTintByIcon(View view, String icon) {
        int colorId = getColorIdFromName(view.getContext(), icon + "_tint");
        int color = colorId != 0 ? ContextCompat.getColor(view.getContext(), colorId)
                : ContextCompat.getColor(view.getContext(), R.color.default_background_tint);
        view.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    /**
     * Hilfsmethode zum Abrufen eines Drawables anhand seines Namens.
     *
     * @param context Der Kontext der Anwendung.
     * @param name    Der Name des Drawable-Ressourcen.
     * @return Das Drawable-Objekt, das dem Namen entspricht, oder null, wenn nicht gefunden.
     */
    private static Drawable getDrawableFromName(Context context, String name) {
        if (name == null) return null;

        @SuppressLint("DiscouragedApi") int resId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        return resId != 0 ? ContextCompat.getDrawable(context, resId) : null;
    }

    /**
     * Hilfsmethode zum Abrufen einer Farb-ID anhand ihres Namens.
     *
     * @param context Der Kontext der Anwendung.
     * @param name    Der Name der Farb-Ressource.
     * @return Die Farb-ID, die dem Namen entspricht, oder 0, wenn nicht gefunden.
     */
    @SuppressLint("DiscouragedApi")
    private static int getColorIdFromName(Context context, String name) {
        return context.getResources().getIdentifier(name, "color", context.getPackageName());
    }
}
