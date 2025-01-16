package de.hsos.habiton;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;


/**
 * Die Anwendungsklasse für HabitApp.
 * @author Miikka Koensler
 */
public class HabitApp extends Application {
    public static final String CHANNEL_MORNING = "habit_morning";
    public static final String CHANNEL_EVENING = "habit_evening";

    /**
     * Wird aufgerufen, wenn die Anwendung gestartet wird. Erstellt die Benachrichtigungskanäle.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    /**
     * Erstellt die Benachrichtigungskanäle für die Anwendung.
     */
    private void createNotificationChannels() {
        NotificationChannel channel1 = new NotificationChannel(
                CHANNEL_MORNING,
                "Habit Morning",
                NotificationManager.IMPORTANCE_LOW
        );
        channel1.setDescription("Benachrichtigung über fällige Gewohnheiten am Morgen.");
        NotificationChannel channel2 = new NotificationChannel(
                CHANNEL_EVENING,
                "Habit Evening",
                NotificationManager.IMPORTANCE_LOW
        );
        channel2.setDescription("Benachrichtigung über ausstehende Gewohnheiten am Abend.");

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel1);
        manager.createNotificationChannel(channel2);
    }
}
