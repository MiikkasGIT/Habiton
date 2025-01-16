package de.hsos.habiton.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hsos.habiton.database.models.Habit;
import de.hsos.habiton.database.models.HabitTracking;
import de.hsos.habiton.repository.HabitRepository;
import de.hsos.habiton.repository.HabitTrackingRepository;

/**
 * ViewModel für das Dashboard. Verwaltet Daten und Geschäftslogik für das Dashboard.
 * @author Miikka Koensler
 */
public class DashboardViewModel extends AndroidViewModel {
    private final HabitRepository habitRepository;
    private final HabitTrackingRepository habitTrackingRepository;
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    private final ExecutorService executorService;

    /**
     * Konstruktor für das DashboardViewModel.
     *
     * @param application Die Instanz der Anwendung, die das ViewModel verwendet.
     */
    public DashboardViewModel(@NonNull Application application) {
        super(application);
        habitRepository = new HabitRepository(application);
        habitTrackingRepository = new HabitTrackingRepository(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Fügt einen neuen Habit hinzu, falls der Name und die Beschreibung gültig sind.
     *
     * @param name Der Name des Habits.
     * @param description Die Beschreibung des Habits.
     * @param icon Das Icon des Habits.
     * @param streak Der aktuelle Streak des Habits.
     */
    public void insertHabit(String name, String description, String icon, int streak) {
        if (isInputValid(name, description)) {
            habitRepository.insert(new Habit(name, description, icon, streak));
        } else {
            toastMessage.setValue("Please insert a name and description");
        }
    }

    /**
     * Überprüft, ob ein Habit bereits existiert.
     *
     * @param name Der Name des zu überprüfenden Habits.
     * @return LiveData, die angibt, ob der Habit existiert.
     */
    public LiveData<Boolean> doesHabitExist(String name) {
        return habitRepository.doesHabitExist(name);
    }

    /**
     * Überprüft, ob der eingegebene Name und die Beschreibung gültig sind.
     *
     * @param name Der Name des Habits.
     * @param description Die Beschreibung des Habits.
     * @return true, wenn beide Eingaben gültig sind, sonst false.
     */
    private boolean isInputValid(String name, String description) {
        return !name.trim().isEmpty() && !description.trim().isEmpty();
    }

    /**
     * Wechselt den Erfüllungsstatus eines Habits für das aktuelle Datum.
     *
     * @param habitId Die ID des Habits.
     */
    public void toggleHabitDoneStatus(int habitId) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        habitTrackingRepository.toggleHabitDoneStatus(habitId, currentDate);
        updateLongestStreakForHabit(habitId);
    }

    // Methoden für Habit Trackings und prozentuale Erfüllung hier implementieren

    /**
     * Aktualisiert einen vorhandenen Habit.
     *
     * @param habitId Die ID des zu aktualisierenden Habits.
     * @param name Der neue Name des Habits.
     * @param description Die neue Beschreibung des Habits.
     * @param icon Das neue Icon des Habits.
     * @param streak Der neue Streak des Habits.
     */
    public void updateHabit(int habitId, String name, String description, String icon, int streak) {
        Habit habitToUpdate = new Habit(name, description, icon, streak);
        habitToUpdate.setId(habitId);
        habitRepository.update(habitToUpdate);
    }

    /**
     * Löscht einen vorhandenen Habit.
     *
     * @param habit Der zu löschende Habit.
     */
    public void deleteHabit(Habit habit) {
        habitRepository.delete(habit);
    }

    /**
     * Holt alle Habits.
     *
     * @return LiveData-Liste aller Habits.
     */
    public LiveData<List<Habit>> getAllHabits() {
        return habitRepository.getAllHabits();
    }

    /**
     * Holt alle Habit-Trackings für ein bestimmtes Datum.
     *
     * @param date Das Datum, für das die Trackings geholt werden sollen.
     * @return LiveData-Liste aller Habit-Trackings für das gegebene Datum.
     */
    public LiveData<List<HabitTracking>> getHabitTrackingsForDate(String date) {
        return habitTrackingRepository.getHabitTrackingsForDate(date);
    }

    /**
     * Aktualisiert den längsten Streak für einen bestimmten Habit.
     *
     * @param habitId Die ID des Habits, für den der längste Streak aktualisiert werden soll.
     */
    public void updateLongestStreakForHabit(Integer habitId) {
        executorService.execute(() -> habitRepository.updateLongestStreakForHabit(habitId));
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
