package de.hsos.habiton.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hsos.habiton.database.HabitDatabase;
import de.hsos.habiton.database.daos.HabitDao;
import de.hsos.habiton.database.daos.HabitTrackingDao;
import de.hsos.habiton.database.models.Habit;
import de.hsos.habiton.database.models.HabitTracking;
import de.hsos.habiton.helpers.HabitStreakCalculator;

/**
 * Ein Repository zur Verwaltung von Gewohnheitsdaten und zugehörigen Verfolgungsdaten.
 * @author Miikka Koesnler, Finn Garrels
 */
public class HabitRepository {
    private final HabitDao habitDao;
    private final HabitTrackingDao habitTrackingDao;
    private final HabitStreakCalculator streakCalculator;
    private final ExecutorService executorService;

    /**
     * Konstruktor für das HabitRepository.
     *
     * @param application Die Anwendungsinstanz.
     */
    public HabitRepository(Application application) {
        HabitDatabase database = HabitDatabase.getInstance(application);
        habitDao = database.habitDao();
        habitTrackingDao = database.habitTrackingDao();
        this.streakCalculator = new HabitStreakCalculator(habitTrackingDao);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Fügt eine neue Gewohnheit hinzu.
     *
     * @param habit Die hinzuzufügende Gewohnheit.
     */
    public void insert(Habit habit) {
        executorService.execute(() -> {
            long habitId = habitDao.insert(habit);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());

            HabitTracking habitTracking = new HabitTracking((int) habitId, currentDate, false);
            habitTrackingDao.insert(habitTracking);
        });
    }

    /**
     * Aktualisiert eine vorhandene Gewohnheit.
     *
     * @param habit Die zu aktualisierende Gewohnheit.
     */
    public void update(Habit habit) {
        executorService.execute(() -> habitDao.update(habit));
    }

    /**
     * Löscht eine Gewohnheit.
     *
     * @param habit Die zu löschende Gewohnheit.
     */
    public void delete(Habit habit) {
        executorService.execute(() -> {
            long habitId = habit.getId();
            habitDao.delete(habit);
            if (habitTrackingDao != null) {
                habitTrackingDao.deleteAllTrackingsForHabit(habitId);
            }
        });
    }

    /**
     * Ruft alle Gewohnheiten ab.
     *
     * @return Eine LiveData-Liste aller Gewohnheiten.
     */
    public LiveData<List<Habit>> getAllHabits() {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        return habitDao.getAllHabits(currentDate);
    }

    /**
     * Ruft alle Gewohnheitstitel ab.
     *
     * @return Eine LiveData-Liste aller Gewohnheitstitel.
     */
    public LiveData<List<String>> getHabitTitles() {
        return habitDao.getAllHabitTitles();
    }

    /**
     * Ruft das Symbol für eine Gewohnheit anhand des Namens ab.
     *
     * @param name Der Name der Gewohnheit.
     * @return Eine LiveData-Zeichenfolge des Symbols der Gewohnheit.
     */
    public LiveData<String> getIconByName(String name) {
        MutableLiveData<String> iconLiveData = new MutableLiveData<>();

        executorService.execute(() -> {
            String icon = habitDao.getIconByName(name);
            iconLiveData.postValue(icon);
        });

        return iconLiveData;
    }

    /**
     * Aktualisiert die längste Streak einer Gewohnheit.
     *
     * @param habitId Die ID der Gewohnheit.
     */
    public void updateLongestStreakForHabit(int habitId) {
        int streak = habitDao.getHabitById(habitId).getStreak();
        if (habitDao.getHabitById(habitId).getLongestStreak() < streak) {
            habitDao.updateLongestStreak(habitId, streak);
        }
    }

    /**
     * Berechnet die beste Streak einer Gewohnheit.
     *
     * @param habitId Die ID der Gewohnheit.
     * @return Die beste Streak der Gewohnheit.
     */
    public int calculateBestStreak(Integer habitId) {
        return streakCalculator.calculateBestStreak(habitId);
    }

    /**
     * Überprüft, ob eine Gewohnheit existiert.
     *
     * @param name Der Name der Gewohnheit.
     * @return Eine LiveData-Boolean, die angibt, ob die Gewohnheit existiert oder nicht.
     */
    public LiveData<Boolean> doesHabitExist(String name) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        executorService.execute(() -> {
            boolean exists = habitDao.doesHabitExist(name);
            result.postValue(exists);
        });
        return result;
    }

    /**
     * Ruft die maximale längste Streak ab.
     *
     * @return Eine LiveData-Integer der maximalen längsten Streak.
     */
    public LiveData<Integer> getMaxLongestStreak() {
        return habitDao.getMaxLongestStreak();
    }

    /**
     * Berechnet die Abschlussrate für eine Gewohnheit über einen bestimmten Zeitraum.
     *
     * @param habitId Die ID der Gewohnheit.
     * @param days    Die Anzahl der Tage, über die die Abschlussrate berechnet werden soll.
     * @return Eine LiveData-Double der Abschlussrate der Gewohnheit.
     */
    public LiveData<Double> calculateCompletionRateForHabit(int habitId, int days) {
        MutableLiveData<Double> result = new MutableLiveData<>();

        executorService.execute(() -> {
            double rate = streakCalculator.calculateCompletionRateForHabit(habitId, days);
            result.postValue(rate);
        });

        return result;
    }

    /**
     * Berechnet die Gesamtabschlussrate über einen bestimmten Zeitraum.
     *
     * @param days Die Anzahl der Tage, über die die Abschlussrate berechnet werden soll.
     * @return Eine LiveData-Double der Gesamtabschlussrate.
     */
    public LiveData<Double> calculateOverallCompletionRate(int days) {
        MutableLiveData<Double> result = new MutableLiveData<>();

        executorService.execute(() -> {
            double overallRate = streakCalculator.calculateOverallCompletionRate(days);
            result.postValue(overallRate);
        });

        return result;
    }

    /**
     * Erstellt Verfolgungen für alle Gewohnheiten.
     */
    public void createTrackingForAllHabits() {
        executorService.execute(() -> {
            List<Habit> allHabits = habitDao.getAllHabits();
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            for (Habit habit : allHabits) {
                HabitTracking newTracking = new HabitTracking(habit.getId(), currentDate, false);
                habitTrackingDao.insert(newTracking);
            }
        });
    }

    /**
     * Ruft die ID einer Gewohnheit anhand des Titels ab.
     *
     * @param title Der Titel der Gewohnheit.
     * @return Die ID der Gewohnheit.
     */
    public int getHabitIdFromTitle(String title) {
        return habitDao.getHabitIdByTitle(title);
    }
}
