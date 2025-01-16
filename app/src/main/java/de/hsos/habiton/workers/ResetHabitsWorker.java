package de.hsos.habiton.workers;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import de.hsos.habiton.repository.HabitRepository;
import de.hsos.habiton.repository.HabitTrackingRepository;

/**
 * Ein Worker, der täglich ausgeführt wird, um die Streaks von Habits zurückzusetzen und das Tracking für den neuen Tag zu initialisieren.
 */
public class ResetHabitsWorker extends Worker {

    /**
     * Konstruktor für ResetHabitsWorker.
     *
     * @param context Kontext, in dem der Worker ausgeführt wird. Wird für den Zugriff auf die Repositories benötigt.
     * @param params Parameter für den Worker, beinhalten verschiedene Daten und Konfigurationen für die Worker-Ausführung.
     */
    public ResetHabitsWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    /**
     * Führt die Hauptlogik des Workers aus. Wird automatisch von WorkManager aufgerufen, wenn der Worker ausgeführt werden soll.
     * Setzt Streaks zurück, falls sie gestern nicht erfüllt wurden, und initialisiert das Tracking für alle Habits für den heutigen Tag.
     *
     * @return Das Ergebnis der Arbeit, entweder Result.success(), Result.failure() oder Result.retry().
     */
    @NonNull
    @Override
    public Result doWork() {
        HabitTrackingRepository habitTrackingRepository = new HabitTrackingRepository((Application) getApplicationContext());
        HabitRepository habitRepository = new HabitRepository((Application) getApplicationContext());
        habitTrackingRepository.resetStreakIfNotCompletedYesterday();
        habitRepository.createTrackingForAllHabits();
        return Result.success();
    }
}
