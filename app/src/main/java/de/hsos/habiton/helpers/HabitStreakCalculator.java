package de.hsos.habiton.helpers;

import java.time.LocalDate;
import java.util.List;

import de.hsos.habiton.database.daos.HabitTrackingDao;
import de.hsos.habiton.database.models.HabitTracking;

/**
 * Hilfsklasse zur Berechnung von Gewinnserien und Abschlussraten von Gewohnheiten.
 */
public class HabitStreakCalculator {

    private final HabitTrackingDao habitTrackingDao;

    /**
     * Konstruktor für den HabitStreakCalculator.
     *
     * @param habitTrackingDao Das DAO-Objekt für die Gewohnheitsverfolgung.
     */
    public HabitStreakCalculator(HabitTrackingDao habitTrackingDao) {
        this.habitTrackingDao = habitTrackingDao;
    }

    /**
     * Berechnet die beste Gewinnserie für eine bestimmte Gewohnheit.
     *
     * @param habitId Die ID der Gewohnheit.
     * @return Die beste Gewinnserie.
     */
    public int calculateBestStreak(Integer habitId) {
        List<HabitTracking> trackings;
        if (habitId == null) {
            trackings = habitTrackingDao.getAllTrackings();
        } else {
            trackings = habitTrackingDao.getTrackingsForHabit(habitId);
        }
        return calculateStreakFromTrackings(trackings);
    }

    private int calculateStreakFromTrackings(List<HabitTracking> trackings) {
        int maxStreak = 0;
        int currentStreak = 0;
        LocalDate lastDate = null;

        for (HabitTracking tracking : trackings) {
            LocalDate currentDate = LocalDate.parse(tracking.getDate());

            // Überprüfen, ob es der erste Eintrag ist oder ob das aktuelle Datum direkt auf das letzte Datum folgt
            if (lastDate == null || currentDate.equals(lastDate.plusDays(1))) {
                if (tracking.isStatus()) {
                    currentStreak++;
                } else {
                    currentStreak = 0;
                }
            } else {
                maxStreak = Math.max(maxStreak, currentStreak);
                currentStreak = tracking.isStatus() ? 1 : 0;
            }
            lastDate = currentDate;
        }

        return Math.max(maxStreak, currentStreak);
    }

    /**
     * Berechnet die Abschlussrate für eine Gewohnheit über einen bestimmten Zeitraum.
     *
     * @param habitId Die ID der Gewohnheit.
     * @param days    Die Anzahl der Tage für die Berechnung.
     * @return Die Abschlussrate für die Gewohnheit.
     */
    public double calculateCompletionRateForHabit(int habitId, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        List<HabitTracking> trackings = habitTrackingDao.getTrackingsForHabit(habitId);

        int completedDays = (int) trackings.stream()
                .filter(tracking -> {
                    LocalDate trackingDate = LocalDate.parse(tracking.getDate());
                    return !trackingDate.isBefore(startDate) && !trackingDate.isAfter(endDate) && tracking.isStatus();
                })
                .count();

        return (double) completedDays / days * 100;
    }

    /**
     * Berechnet die Gesamtabschlussrate über einen bestimmten Zeitraum.
     *
     * @param days Die Anzahl der Tage für die Berechnung.
     * @return Die Gesamtabschlussrate.
     */
    public double calculateOverallCompletionRate(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        List<HabitTracking> allTrackings = habitTrackingDao.getAllTrackings();
        int totalCompleted = (int) allTrackings.stream()
                .filter(tracking -> {
                    LocalDate trackingDate = LocalDate.parse(tracking.getDate());
                    return !trackingDate.isBefore(startDate) && !trackingDate.isAfter(endDate) && tracking.isStatus();
                })
                .count();

        int totalHabits = habitTrackingDao.getAllHabitIds().size();
        return (double) totalCompleted / (totalHabits * days) * 100;
    }
}
