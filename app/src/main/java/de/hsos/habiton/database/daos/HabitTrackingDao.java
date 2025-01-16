package de.hsos.habiton.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import de.hsos.habiton.database.models.HabitTracking;

/**
 * Data Access Object (DAO) für die Habit-Tracking-Entität.
 * @author Miikka Koensler
 */
@Dao
public interface HabitTrackingDao {

    /**
     * Fügt eine neue Gewohnheitsverfolgung in die Datenbank ein.
     *
     * @param habitTracking Die einzufügende Gewohnheitsverfolgung.
     */
    @Insert
    void insert(HabitTracking habitTracking);

    /**
     * Gibt die Anzahl der abgeschlossenen Gewohnheiten für ein bestimmtes Datum zurück.
     *
     * @param date Das Datum, für das die abgeschlossenen Gewohnheiten gezählt werden sollen.
     * @return Die Anzahl der abgeschlossenen Gewohnheiten als LiveData.
     */
    @Query("SELECT COUNT(*) FROM habit_tracking_table WHERE date = :date AND status = 1")
    LiveData<Integer> getCountOfCompletedHabitsOnDate(String date);

    /**
     * Zählt alle Gewohnheiten für ein bestimmtes Datum.
     *
     * @param date Das Datum, für das die Gewohnheiten gezählt werden sollen.
     * @return Die Anzahl der Gewohnheiten als LiveData.
     */
    @Query("SELECT COUNT(*) FROM habit_tracking_table WHERE date = :date")
    LiveData<Integer> countHabitsForDate(String date);

    /**
     * Sucht Gewohnheiten, die an einem bestimmten Datum nicht erledigt wurden.
     *
     * @param date Das Datum, für das die nicht erledigten Gewohnheiten gesucht werden sollen.
     * @return Eine Liste von IDs der nicht erledigten Gewohnheiten.
     */
    @Query("SELECT habitID FROM habit_tracking_table WHERE date = :date AND status = 0")
    List<Integer> findHabitsNotDoneOnDate(String date);

    /**
     * Löscht alle Verfolgungen für eine bestimmte Gewohnheit.
     *
     * @param habitId Die ID der Gewohnheit, deren Verfolgungen gelöscht werden sollen.
     */
    @Query("DELETE FROM habit_tracking_table WHERE habitID = :habitId")
    void deleteAllTrackingsForHabit(long habitId);

    /**
     * Aktualisiert den Status einer Gewohnheit für ein bestimmtes Datum.
     *
     * @param isDone   Der neue Status der Gewohnheit.
     * @param habitId  Die ID der Gewohnheit.
     * @param date     Das Datum, für das der Status aktualisiert werden soll.
     */
    @Query("UPDATE habit_tracking_table SET status = :isDone WHERE habitID = :habitId AND date = :date")
    void updateIsDone(boolean isDone, int habitId, String date);

    /**
     * Gibt die Verfolgung für eine bestimmte Gewohnheit und ein bestimmtes Datum zurück.
     *
     * @param habitId Die ID der Gewohnheit.
     * @param date    Das Datum.
     * @return Die Gewohnheitsverfolgung oder null, wenn keine gefunden wurde.
     */
    @Query("SELECT * FROM habit_tracking_table WHERE habitID = :habitId AND date = :date LIMIT 1")
    HabitTracking getTrackingForHabitAndDate(int habitId, String date);

    /**
     * Gibt alle Verfolgungen für ein bestimmtes Datum zurück.
     *
     * @param date Das Datum.
     * @return Eine LiveData-Liste von Gewohnheitsverfolgungen.
     */
    @Query("SELECT * FROM habit_tracking_table WHERE date = :date")
    LiveData<List<HabitTracking>> getHabitTrackingsForDate(String date);

    /**
     * Gibt alle Verfolgungen in der Datenbank zurück.
     *
     * @return Eine Liste von Gewohnheitsverfolgungen.
     */
    @Query("SELECT * FROM habit_tracking_table")
    List<HabitTracking> getAllTrackings();

    /**
     * Gibt alle Verfolgungen für eine bestimmte Gewohnheit zurück.
     *
     * @param habitId Die ID der Gewohnheit.
     * @return Eine Liste von Gewohnheitsverfolgungen.
     */
    @Query("SELECT * FROM habit_tracking_table WHERE habitId = :habitId")
    List<HabitTracking> getTrackingsForHabit(int habitId);

    /**
     * Gibt die IDs aller Gewohnheiten zurück.
     *
     * @return Eine Liste von Gewohnheits-IDs.
     */
    @Query("SELECT id FROM habit_table")
    List<Integer> getAllHabitIds();

    /**
     * Gibt den Status einer Gewohnheit für das aktuelle Datum zurück.
     *
     * @param habitId      Die ID der Gewohnheit.
     * @param currentDate  Das aktuelle Datum.
     * @return Der Status der Gewohnheit für das aktuelle Datum.
     */
    @Query("SELECT status FROM habit_tracking_table WHERE habitId = :habitId AND date = :currentDate")
    Integer getHabitStatusForCurrentDate(int habitId, String currentDate);
}
