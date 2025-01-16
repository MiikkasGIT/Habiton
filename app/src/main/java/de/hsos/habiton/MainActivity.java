package de.hsos.habiton;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import de.hsos.habiton.viewModels.MainActivityViewModel;
import de.hsos.habiton.views.AddHabitFragment;

/**
 * Die Hauptaktivität der Anwendung.
 * @author Miikka Koensler
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private MainActivityViewModel mainActivityViewModel;

    /**
     * Wird aufgerufen, wenn die Aktivität erstellt wird.
     *
     * @param savedInstanceState Das Bundle, das den gespeicherten Zustand der Aktivität enthält.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        initBottomNavigation();
        mainActivityViewModel.scheduleResetHabitsWork();
    }
    /**
     * Initialisiert die Navigation und setzt  die notwendigen Event-Listener.
     */
    private void initBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_nav);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            setupBottomNavigationListeners(navController);
        }
    }
    /**
     * Hier wird festgelegt was passiert, wenn ein Element in der Navigation ausgewählt wird.
     * Navigation wird entsprechend aktualisiert.
     * @param navController Der NavController, der für die Navigation zwischen den Fragmenten verwendet wird.
     */
    private void setupBottomNavigationListeners(NavController navController) {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.createButton) {
                showAddHabitFragment();
                return true;
            } else {
                return NavigationUI.onNavDestinationSelected(item, navController);
            }
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> mainActivityViewModel.updateBottomNavigationVisibility(destination.getId()));
        mainActivityViewModel.getBottomNavigationVisibility().observe(this, isVisible -> bottomNavigationView.setVisibility(isVisible ? View.VISIBLE : View.GONE));
    }

    /**
     * Zeigt das AddHabitFragment an. Dieses Fragment wird verwendet, um neue Habits hinzuzufügen.
     */
    private void showAddHabitFragment() {
        AddHabitFragment addHabitFragment = new AddHabitFragment();
        addHabitFragment.setOnDialogCloseListener(() -> bottomNavigationView.setSelectedItemId(R.id.DashboardFragment));
        addHabitFragment.show(getSupportFragmentManager(), "AddHabitFragmentTag");
    }
}
