package de.hsos.habiton.workers;

import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import de.hsos.habiton.HabitApp;
import de.hsos.habiton.R;

/**
 * Worker, der Benachrichtigungen sendet.
 * Erstellt und sendet eine Benachrichtigung über den Benachrichtigungskanal CHANNEL_1_ID.
 * @author Finn Garrels
 */
public class NotificationWorker extends Worker {

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String channelId = getInputData().getString("channelId");
        assert channelId != null;
        sendOnChannel(getApplicationContext(), channelId);
        return Result.success();
    }

    /**
     * Sendet eine Benachrichtigung über den Benachrichtigungskanal.
     *
     * @param context Der Kontext der Anwendung.
     */
    private void sendOnChannel(Context context, String channelId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        String contentTitle;
        String contentText;

        if (channelId.equals(HabitApp.CHANNEL_MORNING)) {
            contentTitle = "Morning Reminder";
            contentText = "Time for your morning routine.";
        } else if (channelId.equals(HabitApp.CHANNEL_EVENING)) {
            contentTitle = "Evening Reminder";
            contentText = "Time to wind down for the evening.";
        } else {
            contentTitle = "Reminder";
            contentText = "You have a new reminder.";
        }

        // Erstelle und sende die Benachrichtigung
        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        int notificationId = channelId.equals(HabitApp.CHANNEL_MORNING) ? 1 : 2;
        notificationManager.notify(notificationId, notification);
    }
}