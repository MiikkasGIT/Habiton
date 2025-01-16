package de.hsos.habiton.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import de.hsos.habiton.database.models.Habit;

/**
 * Data Access Object (DAO) für die Habit-Entität.
 * @author Miikka Koensler, Finn Garrels
 */
@Dao
public interface HabitDao {

    /**
     * Fügt eine neue Gewohnheit in die Datenbank ein.
     *
     * @param habit Die einzufügende Gewohnheit.
     * @return Die ID der eingefügten Gewohnheit.
     */
    @Insert
    long insert(Habit habit);

    /**
     * Aktualisiert eine vorhandene Gewohnheit in der Datenbank.
     *
     * @param habit Die zu aktualisierende Gewohnheit.
     */
    @Update
    void update(Habit habit);

    /**
     * Löscht eine vorhandene Gewohnheit aus der Datenbank.
     *
     * @param habit Die zu löschende Gewohnheit.
     */
    @Delete
    void delete(Habit habit);

    /**
     * Gibt alle Gewohnheiten zurück, die an einem bestimmten Datum verfolgt wurden.
     *
     * @param date Das Datum, für das die Gewohnheiten abgerufen werden sollen.
     * @return Eine LiveData-Liste von Gewohnheiten.
     */
    @Query("SELECT habit_table.* FROM habit_table " +
            "LEFT JOIN habit_tracking_table ON habit_table.id = habit_tracking_table.habitId " +
            "AND habit_tracking_table.date = :date " +
            "ORDER BY habit_tracking_table.status DESC, habit_table.id ASC")
    LiveData<List<Habit>> getAllHabits(String date);

    /**
     * Überprüft, ob eine Gewohnheit mit einem bestimmten Namen bereits existiert.
     *
     * @param name Der Name der zu überprüfenden Gewohnheit.
     * @return true, wenn die Gewohnheit existiert, sonst false.
     */
    @Query("SELECT COUNT(id) > 0 FROM habit_table WHERE name = :name")
    boolean doesHabitExist(String name);

    /**
     * Gibt alle Titel der vorhandenen Gewohnheiten zurück.
     *
     * @return Eine LiveData-Liste von Gewohnheitstiteln.
     */
    @Query("SELECT name FROM habit_table")
    LiveData<List<String>> getAllHabitTitles();

    /**
     * Gibt eine Gewohnheit anhand ihrer ID zurück.
     *
     * @param habitId Die ID der abzurufenden Gewohnheit.
     * @return Die entsprechende Gewohnheit oder null, wenn keine gefunden wurde.
     */
    @Query("SELECT * FROM habit_table WHERE id = :habitId")
    Habit getHabitById(int habitId);

    /**
     * Gibt die ID einer Gewohnheit anhand ihres Titels zurück.
     *
     * @param name Der Titel der Gewohnheit.
     * @return Die ID der Gewohnheit oder -1, wenn keine gefunden wurde.
     */
    @Query("SELECT id FROM habit_table WHERE name = :name LIMIT 1")
    int getHabitIdByTitle(String name);

    /**
     * Inkrementiert den Streak für eine bestimmte Gewohnheit.
     *
     * @param habitId Die ID der Gewohnheit, deren Streak erhöht werden soll.
     */
    @Query("UPDATE habit_table SET streak = streak + 1 WHERE id = :habitId")
    void incrementStreak(int habitId);

    /**
     * Dekrementiert den Streak für eine bestimmte Gewohnheit.
     *
     * @param habitId Die ID der Gewohnheit, deren Streak verringert werden soll.
     */
    @Query("UPDATE habit_table SET streak = streak - 1 WHERE id = :habitId")
    void decrementStreak(int habitId);

    /**
     * Setzt den Streak einer bestimmten Gewohnheit auf 0 zurück.
     *
     * @param habitId Die ID der Gewohnheit, deren Streak zurückgesetzt werden soll.
     */
    @Query("UPDATE habit_table SET streak = 0 WHERE id = :habitId")
    void resetStreak(int habitId);

    /**
     * Gibt alle Gewohnheiten zurück.
     *
     * @return Eine Liste von Gewohnheiten.
     */
    @Query("SELECT * FROM habit_table")
    List<Habit> getAllHabits();

    /**
     * Gibt den Streak einer bestimmten Gewohnheit zurück.
     *
     * @param habitId Die ID der Gewohnheit.
     * @return Der Streak der Gewohnheit.
     */
    @Query("SELECT streak FROM habit_table WHERE id = :habitId")
    int getStreakForHabit(int habitId);

    /**
     * Aktualisiert den längsten Streak einer bestimmten Gewohnheit.
     *
     * @param habitId       Die ID der Gewohnheit.
     * @param longestStreak Der neue längste Streak.
     */
    @Query("UPDATE habit_table SET longestStreak = :longestStreak WHERE id = :habitId")
    void updateLongestStreak(int habitId, int longestStreak);

    /**
     * Gibt den längsten Streak aller Gewohnheiten zurück.
     *
     * @return Die längste Serienlänge.
     */
    @Query("SELECT MAX(longestStreak) FROM habit_table")
    LiveData<Integer> getMaxLongestStreak();

    /**
     * Gibt den längsten Streak einer bestimmten Gewohnheit zurück.
     *
     * @param habitId Die ID der Gewohnheit.
     * @return Der längste Streak der Gewohnheit.
     */
    @Query("SELECT longestStreak FROM habit_table WHERE id = :habitId")
    int getMaxLongestStreak(int habitId);

    /**
     * Gibt das Icon einer Gewohnheit anhand ihres Namens zurück.
     *
     * @param name Der Name der Gewohnheit.
     * @return Das Symbol der Gewohnheit.
     */
    @Query("SELECT icon FROM habit_table WHERE name = :name")
    String getIconByName(String name);
}
