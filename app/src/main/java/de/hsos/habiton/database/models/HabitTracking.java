package de.hsos.habiton.database.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

/**
 * Eine Entitätsklasse, die das Tracking einer Gewohnheit in der Datenbank darstellt.
 * @author Finn Garrels
 */
@Entity(tableName = "habit_tracking_table",
        foreignKeys = @ForeignKey(entity = Habit.class,
                parentColumns = "id",
                childColumns = "habitID",
                onDelete = ForeignKey.CASCADE))
public class HabitTracking {
    @PrimaryKey(autoGenerate = true)
    private int trackID;
    private final int habitID;
    private String date;
    private boolean status;

    /**
     * Konstruktor für die HabitTracking-Klasse.
     *
     * @param habitID Die ID der Gewohnheit, die verfolgt wird.
     * @param date    Das Datum des Trackings.
     * @param status  Der Status des Trackings (erledigt/nicht erledigt).
     */
    public HabitTracking(int habitID, String date, boolean status) {
        this.habitID = habitID;
        this.date = date;
        this.status = status;
    }

    /**
     * Legt die ID des Trackings fest.
     *
     * @param trackID Die zu setzende ID des Trackings.
     */
    public void setTrackID(int trackID) {
        this.trackID = trackID;
    }

    /**
     * Legt das Datum des Trackings fest.
     *
     * @param date Das zu setzende Datum des Trackings.
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Legt den Status des Trackings fest.
     *
     * @param status Der zu setzende Status des Trackings.
     */
    public void setStatus(boolean status) {
        this.status = status;
    }

    /**
     * Gibt die ID des Trackings zurück.
     *
     * @return Die ID des Trackings.
     */
    public int getTrackID() {
        return trackID;
    }

    /**
     * Gibt die ID der Gewohnheit zurück, die verfolgt wird.
     *
     * @return Die ID der Gewohnheit.
     */
    public int getHabitID() {
        return habitID;
    }

    /**
     * Gibt das Datum des Trackings zurück.
     *
     * @return Das Datum des Trackings.
     */
    public String getDate() {
        return date;
    }

    /**
     * Gibt den Status des Trackings zurück.
     *
     * @return Der Status des Trackings.
     */
    public boolean isStatus() {
        return status;
    }
}
