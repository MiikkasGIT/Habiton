package de.hsos.habiton.viewModels;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import de.hsos.habiton.database.models.Habit;
import de.hsos.habiton.repository.HabitRepository;
import de.hsos.habiton.repository.HabitTrackingRepository;


/**
 * ViewModel für die Analytics-Funktionalität. Verwaltet Daten und Geschäftslogik für die Analyse von Habit-Daten.
 * @author Finn Garrels
 */
public class AnalyticsViewModel extends AndroidViewModel {
    private final HabitRepository habitRepository;
    private final HabitTrackingRepository habitTrackingRepository;
    private final ExecutorService executorService;

    /**
     * Konstruktor für das AnalyticsViewModel.
     *
     * @param application Die Instanz der Anwendung, die das ViewModel verwendet.
     */
    public AnalyticsViewModel(Application application) {
        super(application);
        habitRepository = new HabitRepository(application);
        habitTrackingRepository = new HabitTrackingRepository(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Holt die Anzahl der an einem bestimmten Tag abgeschlossenen Habits.
     *
     * @param date Das Datum, für das die Anzahl der Habits ermittelt werden soll.
     * @return LiveData mit der Anzahl der Habits.
     */
    public LiveData<Integer> getNumberOfHabitsForDay(String date) {
        return habitTrackingRepository.getCountOfCompletedHabitsOnDate(date);
    }

    /**
     * Holt die Gesamtanzahl der Habits für ein bestimmtes Datum.
     *
     * @param date Das Datum, für das die Gesamtanzahl der Habits ermittelt werden soll.
     * @return LiveData mit der Gesamtanzahl der Habits.
     */
    public LiveData<Integer> getTotalHabitsForDate(String date) {
        return habitTrackingRepository.getTotalHabitsForDate(date);
    }

    /**
     * Holt die Titel aller Habits.
     *
     * @return LiveData mit einer Liste der Habit-Titel.
     */
    public LiveData<List<String>> getHabitTitles() {
        return habitRepository.getHabitTitles();
    }

    /**
     * Berechnet den besten Streak für einen bestimmten Habit.
     *
     * @param habitId Die ID des Habits, für den der beste Streak berechnet werden soll.
     * @return LiveData mit dem besten Streak.
     */
    public LiveData<Integer> calculateBestStreak(Integer habitId) {
        MutableLiveData<Integer> bestStreakLiveData = new MutableLiveData<>();
        executorService.execute(() -> {
            int bestStreak = habitRepository.calculateBestStreak(habitId);
            bestStreakLiveData.postValue(bestStreak);
        });
        return bestStreakLiveData;
    }

    /**
     * Holt die ID eines Habits anhand seines Titels.
     *
     * @param title    Der Titel des Habits.
     * @param callback Ein Consumer, der die Habit-ID verarbeitet.
     */
    public void getHabitIdFromTitle(String title, Consumer<Integer> callback) {
        executorService.execute(() -> {
            int habitId = habitRepository.getHabitIdFromTitle(title);
            new Handler(Looper.getMainLooper()).post(() -> callback.accept(habitId));
        });
    }

    /**
     * Holt das Icon eines Habits anhand seines Namens.
     *
     * @param name Der Name des Habits.
     * @return LiveData mit dem Icon des Habits.
     */
    public LiveData<String> getIconByName(String name) {
        return habitRepository.getIconByName(name);
    }

    /**
     * Berechnet die Abschlussrate eines Habits über einen bestimmten Zeitraum.
     *
     * @param habitId Die ID des Habits.
     * @param days    Die Anzahl der Tage für die Berechnung.
     * @return LiveData mit der Abschlussrate.
     */
    public LiveData<Double> getCompletionRateForHabit(int habitId, int days) {
        return habitRepository.calculateCompletionRateForHabit(habitId, days);
    }

    /**
     * Berechnet die Gesamtabschlussrate aller Habits über einen bestimmten Zeitraum.
     *
     * @param days Die Anzahl der Tage für die Berechnung.
     * @return LiveData mit der Gesamtabschlussrate.
     */
    public LiveData<Double> getOverallCompletionRate(int days) {
        return habitRepository.calculateOverallCompletionRate(days);
    }

    /**
     * Holt den maximalen längsten Streak aller Habits.
     *
     * @return LiveData mit dem maximalen längsten Streak.
     */
    public LiveData<Integer> getMaxLongestStreak() {
        return habitRepository.getMaxLongestStreak();
    }

    /**
     * Holt alle Habits.
     *
     * @return LiveData-Liste aller Habits.
     */
    public LiveData<List<Habit>> getHabits() {
        return habitRepository.getAllHabits();
    }

    /**
     * Wird aufgerufen, wenn das ViewModel nicht mehr benötigt wird und zerstört wird.
     * Schließt den ExecutorService, um Ressourcen freizugeben.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
