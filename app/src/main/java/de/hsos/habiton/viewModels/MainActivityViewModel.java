package de.hsos.habiton.viewModels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import de.hsos.habiton.R;
import de.hsos.habiton.workers.ResetHabitsWorker;

public class MainActivityViewModel extends AndroidViewModel {

    private MutableLiveData<Boolean> bottomNavigationVisibility = new MutableLiveData<>();

    /**
     * Konstruktor für MainActivityViewModel.
     * Initialisiert das ViewModel mit dem Anwendungskontext.
     * Setzt die initiale Sichtbarkeit der Bottom-Navigation auf 'true'.
     *
     * @param application Die Anwendungsklasse, die als Kontext dient.
     */
    public MainActivityViewModel(Application application) {
        super(application);
        bottomNavigationVisibility.setValue(true);
    }

    /**
     * Navigation wird versteckt, wenn das Ziel Settings oder Create-Button ist.
     * @param destinationId Die ID des aktuellen Navigationsziels.
     */
    public void updateBottomNavigationVisibility(int destinationId) {
        boolean isVisible = destinationId != R.id.settingsFragment && destinationId != R.id.createButton;
        bottomNavigationVisibility.setValue(isVisible);
    }

    /**
     * Returned das MutableLiveData-Objekt, um Sichtbarkeit der Navigation zu ändern.
     * @return MutableLiveData-Objekt, das den Sichtbarkeitszustand der Bottom-Navigation enthält.
     */
    public MutableLiveData<Boolean> getBottomNavigationVisibility() {
        return bottomNavigationVisibility;
    }

    /**
     * Plant die periodische Arbeit zum Zurücksetzen der Gewohnheiten.
     */
    public void scheduleResetHabitsWork() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build();

        PeriodicWorkRequest resetHabitsWorkRequest = new PeriodicWorkRequest.Builder(ResetHabitsWorker.class, 1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(getApplication()).enqueueUniquePeriodicWork("resetHabits", ExistingPeriodicWorkPolicy.KEEP, resetHabitsWorkRequest);
    }

    /**
     * Berechnet die initiale Verzögerung für das Zurücksetzen der Gewohnheiten.
     *
     * @return Die initiale Verzögerung in Millisekunden.
     */
    private long calculateInitialDelay() {
        Calendar calendar = Calendar.getInstance();
        Calendar nextMidnight = Calendar.getInstance();
        nextMidnight.add(Calendar.DAY_OF_YEAR, 1);
        nextMidnight.set(Calendar.HOUR_OF_DAY, 0);
        nextMidnight.set(Calendar.MINUTE, 0);
        nextMidnight.set(Calendar.SECOND, 0);
        nextMidnight.set(Calendar.MILLISECOND, 0);

        return nextMidnight.getTimeInMillis() - calendar.getTimeInMillis();
    }
}

