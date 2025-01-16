package de.hsos.habiton.database.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Eine Entitätsklasse, die eine Gewohnheit in der Datenbank darstellt.
 * @author Miikka Koensler
 */
@Entity(tableName = "habit_table")
public class Habit {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String description;
    private int streak;
    private int longestStreak;
    private String icon;

    /**
     * Konstruktor für die Habit-Klasse.
     *
     * @param name        Der Name der Gewohnheit.
     * @param description Die Beschreibung der Gewohnheit.
     * @param icon        Das Symbol (Icon) der Gewohnheit.
     * @param streak      Die aktuelle Streak (Serienlänge) der Gewohnheit.
     */
    public Habit(String name, String description, String icon, int streak) {
        this.name = name;
        this.icon = icon;
        this.description = description;
        this.longestStreak = 0;
        this.streak = streak;
    }

    /**
     * Gibt das Symbol (Icon) der Gewohnheit zurück.
     *
     * @return Das Symbol der Gewohnheit.
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Legt das Symbol (Icon) der Gewohnheit fest.
     *
     * @param icon Das zu setzende Symbol der Gewohnheit.
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * Gibt die ID der Gewohnheit zurück.
     *
     * @return Die ID der Gewohnheit.
     */
    public int getId() {
        return id;
    }

    /**
     * Legt die ID der Gewohnheit fest.
     *
     * @param id Die zu setzende ID der Gewohnheit.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gibt den Namen der Gewohnheit zurück.
     *
     * @return Der Name der Gewohnheit.
     */
    public String getName() {
        return name;
    }

    /**
     * Legt den Namen der Gewohnheit fest.
     *
     * @param name Der zu setzende Name der Gewohnheit.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gibt die Beschreibung der Gewohnheit zurück.
     *
     * @return Die Beschreibung der Gewohnheit.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Legt die Beschreibung der Gewohnheit fest.
     *
     * @param description Die zu setzende Beschreibung der Gewohnheit.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gibt die aktuelle Streak (Serienlänge) der Gewohnheit zurück.
     *
     * @return Die aktuelle Streak der Gewohnheit.
     */
    public int getStreak() {
        return streak;
    }

    /**
     * Legt die aktuelle Streak (Serienlänge) der Gewohnheit fest.
     *
     * @param streak Die zu setzende Streak der Gewohnheit.
     */
    public void setStreak(int streak) {
        this.streak = streak;
    }

    /**
     * Legt die längste Streak (Serienlänge) der Gewohnheit fest.
     *
     * @param longestStreak Die zu setzende längste Streak der Gewohnheit.
     */
    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    /**
     * Gibt die längste Streak (Serienlänge) der Gewohnheit zurück.
     *
     * @return Die längste Streak der Gewohnheit.
     */
    public int getLongestStreak() {
        return longestStreak;
    }
}
