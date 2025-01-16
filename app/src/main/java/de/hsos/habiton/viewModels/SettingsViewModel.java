package de.hsos.habiton.viewModels;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.ViewModel;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import de.hsos.habiton.workers.NotificationWorker;

/**
 * ViewModel-Klasse für die Einstellungen.
 * @author Finn Garrels
 */
public class SettingsViewModel extends ViewModel {
    public static final String MORNING_TIME_KEY = "habit_morning";
    public static final String EVENING_TIME_KEY = "habit_evening";
    private static final String PREFERENCES_FILE_KEY = "AppPreferences";

    /**
     * Speichert die ausgewählte Zeit in den Anwendungseinstellungen.
     *
     * @param context Der Kontext der Anwendung.
     * @param key     Der Schlüssel für die Zeitpräferenz.
     * @param time    Die ausgewählte Zeit.
     */
    public void saveTimeToPreferences(Context context, String key, String time) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, time);
        editor.apply();
    }

    /**
     * Lädt die gespeicherte Zeit aus den Anwendungseinstellungen.
     *
     * @param context Der Kontext der Anwendung.
     * @param key     Der Schlüssel für die Zeitpräferenz.
     * @return Die gespeicherte Zeit oder ein leerer String, wenn keine Zeit gespeichert ist.
     */
    public String loadTimeFromPreferences(Context context, String key) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE_KEY, Context.MODE_PRIVATE);
        return preferences.getString(key, "");
    }

    /**
     * Berechnet die Verzögerung für die Benachrichtigungsauslösung basierend auf der ausgewählten Zeit.
     *
     * @param time Die ausgewählte Zeit.
     * @return Die Verzögerung bis zur Benachrichtigungsauslösung.
     */
    public long calculateDelayForNotification(String time) {
        String[] timeParts = time.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        long delay = calendar.getTimeInMillis() - System.currentTimeMillis();
        if (delay < 0) {
            delay += TimeUnit.DAYS.toMillis(1);
        }
        return delay;
    }

    /**
     * Plant eine einmalige Benachrichtigungsarbeit mit der angegebenen Verzögerung.
     *
     * @param context   Der Kontext der Anwendung.
     * @param channelID Die ID des Kanals für die Benachrichtigung.
     * @param delay     Die Verzögerung bis zur Ausführung der Benachrichtigung.
     */
    public void scheduleNotification(Context context, String channelID, long delay) {
        String channelId = channelID.equals(SettingsViewModel.MORNING_TIME_KEY) ? SettingsViewModel.MORNING_TIME_KEY : SettingsViewModel.EVENING_TIME_KEY;
        Data data = new Data.Builder()
                .putString("channelId", channelId)
                .build();

        OneTimeWorkRequest notificationRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build();

        WorkManager.getInstance(context).enqueue(notificationRequest);
    }
}
