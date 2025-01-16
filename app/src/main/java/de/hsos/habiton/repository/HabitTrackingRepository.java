package de.hsos.habiton.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import de.hsos.habiton.database.HabitDatabase;
import de.hsos.habiton.database.daos.HabitDao;
import de.hsos.habiton.database.daos.HabitTrackingDao;
import de.hsos.habiton.database.models.HabitTracking;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Ein Repository zur Verwaltung von Gewohnheitsverfolgungsdaten.
 * @author Miikka Koesnler, Finn Garrels
 */
public class HabitTrackingRepository {
    private final HabitDao habitDao;
    private final HabitTrackingDao habitTrackingDao;
    private final ExecutorService executorService;

    /**
     * Konstruktor für das HabitTrackingRepository.
     *
     * @param application Die Anwendungsinstanz.
     */
    public HabitTrackingRepository(Application application) {
        HabitDatabase database = HabitDatabase.getInstance(application);
        habitDao = database.habitDao();
        habitTrackingDao = database.habitTrackingDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Ruft die Gesamtzahl der Gewohnheiten für ein bestimmtes Datum ab.
     *
     * @param date Das Datum, für das die Gesamtzahl der Gewohnheiten abgerufen werden soll.
     * @return Eine LiveData-Integer der Gesamtzahl der Gewohnheiten für das angegebene Datum.
     */
    public LiveData<Integer> getTotalHabitsForDate(String date) {
        return habitTrackingDao.countHabitsForDate(date);
    }

    /**
     * Setzt die Streaks zurück, wenn gestern keine Gewohnheit abgeschlossen wurde.
     */
    public void resetStreakIfNotCompletedYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        executorService.execute(() -> {
            List<Integer> habitIdsNotCompletedYesterday = habitTrackingDao.findHabitsNotDoneOnDate(yesterday.toString());
            for (int habitId : habitIdsNotCompletedYesterday) {
                habitDao.resetStreak(habitId);
            }
        });
    }

    /**
     * Ruft die Anzahl der abgeschlossenen Gewohnheiten für ein bestimmtes Datum ab.
     *
     * @param date Das Datum, für das die Anzahl der abgeschlossenen Gewohnheiten abgerufen werden soll.
     * @return Eine LiveData-Integer der Anzahl der abgeschlossenen Gewohnheiten für das angegebene Datum.
     */
    public LiveData<Integer> getCountOfCompletedHabitsOnDate(String date) {
        return habitTrackingDao.getCountOfCompletedHabitsOnDate(date);
    }

    /**
     * Ändert den Status einer Gewohnheit für ein bestimmtes Datum.
     *
     * @param habitId Die ID der Gewohnheit.
     * @param date    Das Datum, für das der Status geändert werden soll.
     */
    public void toggleHabitDoneStatus(int habitId, String date) {
        executorService.execute(() -> {
            Integer currentStatusInteger = habitTrackingDao.getHabitStatusForCurrentDate(habitId, date);

            if (currentStatusInteger != null) {
                int currentStatus = currentStatusInteger;

                if (currentStatus == 0) {
                    habitDao.incrementStreak(habitId);
                    int currentStreak = habitDao.getStreakForHabit(habitId);
                    if (currentStreak > habitDao.getMaxLongestStreak(habitId)) {
                        habitDao.updateLongestStreak(habitId, currentStreak);
                    }
                } else {
                    habitDao.decrementStreak(habitId);
                }

                HabitTracking tracking = habitTrackingDao.getTrackingForHabitAndDate(habitId, date);
                if (tracking != null) {
                    habitTrackingDao.updateIsDone(!tracking.isStatus(), habitId, date);
                }
            }
        });
    }

    /**
     * Ruft alle Gewohnheitsverfolgungen für ein bestimmtes Datum ab.
     *
     * @param date Das Datum, für das die Gewohnheitsverfolgungen abgerufen werden sollen.
     * @return Eine LiveData-Liste aller Gewohnheitsverfolgungen für das angegebene Datum.
     */
    public LiveData<List<HabitTracking>> getHabitTrackingsForDate(String date) {
        return habitTrackingDao.getHabitTrackingsForDate(date);
    }
}
