package de.hsos.habiton.views;

import static de.hsos.habiton.adapters.HabitAdapter.VIEW_TYPE_ANALYTICS;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hsos.habiton.adapters.BindingAdapters;
import de.hsos.habiton.adapters.HabitAdapter;
import de.hsos.habiton.R;
import de.hsos.habiton.database.models.Habit;
import de.hsos.habiton.databinding.AnalyticsFragmentBinding;
import de.hsos.habiton.helpers.Utility;
import de.hsos.habiton.viewModels.AnalyticsViewModel;

/**
 * Das Fragment zur Anzeige der Analyseübersicht der Gewohnheiten.
 * @author Miikka Koensler, Finn Garrels
 */
public class AnalyticsFragment extends Fragment {

    private AnalyticsFragmentBinding binding;
    private HabitAdapter habitAdapter;
    private PopupWindow popupWindow;
    private AnalyticsViewModel viewModel;
    private String selectedHabit;
    private Utility utility;

    /**
     * Erstellt das ViewModel für die Analyseübersicht.
     *
     * @param savedInstanceState Die gespeicherten Daten des Fragments, falls vorhanden.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AnalyticsViewModel.class);
        selectedHabit = "All Habits";

    }

    /**
     * Erstellt die Ansicht für die Analyseübersicht.
     *
     * @param inflater           Der LayoutInflater, der verwendet wird, um die Ansicht zu erstellen.
     * @param container          Der ViewGroup-Container, in den die Ansicht eingefügt wird.
     * @param savedInstanceState Die gespeicherten Daten des Fragments, falls vorhanden.
     * @return Die erstellte Ansicht für die Analyseübersicht.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AnalyticsFragmentBinding.inflate(inflater, container, false);
        setupRecyclerView();
        setupToggleButtonGroup();
        setupPopupMenu();
        binding.toggleButton.check(R.id.button1);
        createCardsBasedOnFilter("Woche");

        viewModel.getMaxLongestStreak().observe(getViewLifecycleOwner(), bestStreak -> {
            if (bestStreak != null) {
                TextView bestStreakView = binding.bestStreak;
                bestStreakView.setText(String.valueOf(bestStreak));
            }
        });

        return binding.getRoot();
    }

    /**
     * Zerstört die Ansicht für die Analyseübersicht.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Konfiguriert das RecyclerView für die Anzeige der Gewohnheiten.
     */
    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.analyticsHabitList;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        habitAdapter = new HabitAdapter(VIEW_TYPE_ANALYTICS);
        recyclerView.setAdapter(habitAdapter);

        viewModel.getHabits().observe(getViewLifecycleOwner(), this::updateHabits);
    }

    /**
     * Konfiguriert die ToggleButton-Gruppe für die Auswahl des Zeitraums.
     */
    private void setupToggleButtonGroup() {
        MaterialButtonToggleGroup toggleButtonGroup = binding.toggleButton;

        toggleButtonGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                String viewOption = getViewOptionFromId(checkedId);
                createCardsBasedOnFilter(viewOption);
            }
        });
    }

    /**
     * Konfiguriert das Popup-Menü für die Auswahl der Gewohnheit.
     */
    private void setupPopupMenu() {
        Button menuButton = binding.menuButton;
        menuButton.setOnClickListener(this::showFullWidthPopup);
    }

    /**
     * Aktualisiert die Anzeige der Gewohnheiten basierend auf den aktualisierten Daten.
     *
     * @param habits Die Liste der Gewohnheiten, die aktualisiert werden sollen.
     */
    private void updateHabits(List<Habit> habits) {
        habitAdapter.setHabits(habits);
        for (int i = 0; i < habits.size(); i++) {
            updateHabitStreak(i, habits.get(i));
        }
    }

    /**
     * Aktualisiert die Anzeige des Gewohnheitsstreaks an der angegebenen Position.
     *
     * @param position Die Position der Gewohnheit in der Liste.
     * @param habit    Die Gewohnheit, deren Streak aktualisiert werden soll.
     */
    private void updateHabitStreak(int position, Habit habit) {
        viewModel.getHabitIdFromTitle(habit.getName(), habitId ->
                viewModel.calculateBestStreak(habitId).observe(getViewLifecycleOwner(), longestStreak -> {
                    habitAdapter.notifyItemChanged(position);
                })
        );
    }

    /**
     * Aktualisiert die Anzeige der Abschlussrate basierend auf dem ausgewählten Zeitraum und der ausgewählten Gewohnheit.
     */
    private void updateCompletionRate() {
        int days = getDaysFromViewOption(getViewOptionFromId(binding.toggleButton.getCheckedButtonId()));
        if (selectedHabit.equals("All Habits")) {
            viewModel.getOverallCompletionRate(days).observe(getViewLifecycleOwner(), rate -> {
                TextView completionView = binding.completion;
                completionView.setText(String.format(Locale.getDefault(), "%.2f%%", rate));
            });
        } else {
            viewModel.getHabitIdFromTitle(this.selectedHabit, habitId -> viewModel.getCompletionRateForHabit(habitId, days).observe(getViewLifecycleOwner(), rate -> {
                TextView completionView = binding.completion;
                completionView.setText(String.format(Locale.getDefault(), "%.2f%%", rate));
            }));
        }
    }

    /**
     * Erstellt die Karten basierend auf dem ausgewählten Filter.
     *
     * @param viewOption Der ausgewählte Filter für den Zeitraum.
     */
    private void createCardsBasedOnFilter(String viewOption) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        FrameLayout mainLayout = binding.historyFragmentContainer;
        mainLayout.removeAllViews();
        updateCompletionRate();

        View newContentView;
        int days = getDaysFromViewOption(viewOption);

        viewModel.getOverallCompletionRate(days).observe(getViewLifecycleOwner(), rate -> {
            TextView completionView = binding.completion;
            completionView.setText(String.format(Locale.getDefault(), "%.2f%%", rate));
        });

        switch (viewOption) {
            case "Woche":
            case "Monat":
                newContentView = inflater.inflate(R.layout.analytics_week_month_module, mainLayout, false);
                if (viewOption.equals("Woche")) {
                    addHabitPointsForWeek(newContentView);
                } else {
                    addHabitPointsForCurrentMonth(newContentView);
                }
                break;
            case "Jahr":
                newContentView = inflater.inflate(R.layout.analytics_year_module, mainLayout, false);
                addHabitPointsForCurrentYear(newContentView);
                break;
            default:
                newContentView = inflater.inflate(R.layout.analytics_week_month_module, mainLayout, false);
                break;
        }

        mainLayout.addView(newContentView);
    }

    /**
     * Fügt Datenpunkte für eine Woche zur GridLayout-Ansicht hinzu. Jeder Tag der Woche wird als Spalte dargestellt.
     *
     * @param layoutView Die Ansicht, zu der die Datenpunkte hinzugefügt werden sollen.
     */
    private void addHabitPointsForWeek(View layoutView) {
        GridLayout gridLayout = layoutView.findViewById(R.id.buttonGrid);
        LocalDate startDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        for (int day = 0; day < 7; day++) {
            LocalDate date = startDate.plusDays(day);
            addHabitDataToGridLayout(gridLayout, date, 1, day);
        }
    }
    /**
     * Fügt Datenpunkte für den aktuellen Monat zur GridLayout-Ansicht hinzu. Jeder Tag des Monats wird entsprechend
     * dem Wochentag und der Woche des Monats positioniert.
     *
     * @param layoutView Die Ansicht, zu der die Datenpunkte hinzugefügt werden sollen.
     */
    private void addHabitPointsForCurrentMonth(View layoutView) {
        GridLayout gridLayout = layoutView.findViewById(R.id.buttonGrid);
        gridLayout.removeAllViews();

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.withDayOfMonth(1); // Erster Tag des aktuellen Monats

        int totalDays = (int) ChronoUnit.DAYS.between(startDate, startDate.plusMonths(1)); // Anzahl der Tage im aktuellen Monat
        int firstDayOfWeekIndex = startDate.getDayOfWeek().getValue() - 1; // Montag = 0, Sonntag = 6
        int numberOfRows = (totalDays + firstDayOfWeekIndex) / 7 + ((totalDays + firstDayOfWeekIndex) % 7 == 0 ? 0 : 1);

        gridLayout.setRowCount(numberOfRows); // Zeilen für die Tage
        gridLayout.setColumnCount(7); // 7 Tage der Woche

        for (int i = 0; i < totalDays; i++) {
            LocalDate date = startDate.plusDays(i);
            int rowIndex = (i + firstDayOfWeekIndex) / 7; // Berechnung der Zeile
            int columnIndex = (i + firstDayOfWeekIndex) % 7; // Berechnung der Spalte
            addHabitDataToGridLayout(gridLayout, date, rowIndex, columnIndex);
        }
    }
    /**
     * Fügt einen Datenpunkt für das Jahr zur GridLayout-Ansicht hinzu, basierend auf der prozentualen Erfüllung der Habits.
     *
     * @param gridLayout Das GridLayout, zu dem der Datenpunkt hinzugefügt wird.
     * @param rowIndex Die Reihe im GridLayout, zu der der Datenpunkt hinzugefügt wird.
     * @param columnIndex Die Spalte im GridLayout, zu der der Datenpunkt hinzugefügt wird.
     * @param numberOfHabits Die Anzahl der Habits für diesen Tag.
     * @param totalHabits Die Gesamtanzahl der Habits.
     */
    private void addHabitPointsToDayForYear(GridLayout gridLayout, int rowIndex, int columnIndex, int numberOfHabits, int totalHabits) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        View pointView;
        int percentage = totalHabits > 0 ? (numberOfHabits * 100 / totalHabits) : 0;

        if (percentage == 100) {
            pointView = inflater.inflate(R.layout.analytics_year_100_item, gridLayout, false);
        } else if (percentage >= 75) {
            pointView = inflater.inflate(R.layout.analytics_year_75_item, gridLayout, false);
        } else if (percentage >= 50) {
            pointView = inflater.inflate(R.layout.analytics_year_50_item, gridLayout, false);
        } else if (percentage > 0) {
            pointView = inflater.inflate(R.layout.analytics_year_25_item, gridLayout, false);
        } else {
            pointView = inflater.inflate(R.layout.analytics_year_0_item, gridLayout, false);
        }

        GridLayout.Spec rowSpec = GridLayout.spec(rowIndex);
        GridLayout.Spec columnSpec = GridLayout.spec(columnIndex);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, columnSpec);
        params.setMargins(4, 4, 4, 4);
        pointView.setLayoutParams(params);
        gridLayout.addView(pointView);

    }
    /**
     * Fügt einen Datenpunkt für Woche oder Monat zur GridLayout-Ansicht hinzu, basierend auf der prozentualen Erfüllung der Habits.
     *
     * @param gridLayout Das GridLayout, zu dem der Datenpunkt hinzugefügt wird.
     * @param rowIndex Die Reihe im GridLayout für die Daten.
     * @param columnIndex Die Spalte im GridLayout für die Daten.
     * @param numberOfHabits Die Anzahl der Habits für diesen Tag.
     * @param totalHabits Die Gesamtanzahl der Habits.
     */
    private void addHabitPointsToDay(GridLayout gridLayout, int rowIndex, int columnIndex, int numberOfHabits, int totalHabits) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View pointView;
        int percentage = totalHabits > 0 ? (numberOfHabits * 100 / totalHabits) : 0;

        if (percentage == 100) {
            pointView = inflater.inflate(R.layout.analytics_week_month_100_item, gridLayout, false);
        } else if (percentage >= 75) {
            pointView = inflater.inflate(R.layout.analytics_week_month_75_item, gridLayout, false);
        } else if (percentage >= 50) {
            pointView = inflater.inflate(R.layout.analytics_week_month_50_item, gridLayout, false);
        } else if (percentage > 0) {
            pointView = inflater.inflate(R.layout.analytics_week_month_25_item, gridLayout, false);
        } else {
            pointView = inflater.inflate(R.layout.analytics_week_month_0_item, gridLayout, false);
        }

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.rowSpec = GridLayout.spec(rowIndex);
        params.columnSpec = GridLayout.spec(columnIndex);
        params.setMargins(10, 10, 10, 10);
        pointView.setLayoutParams(params);
        gridLayout.addView(pointView);
    }
    /**
     * Fügt Datenpunkte für das aktuelle Jahr zur GridLayout-Ansicht hinzu. Jede Woche des Jahres wird durch eine Zeile repräsentiert.
     *
     * @param layoutView Die Ansicht, zu der die Datenpunkte hinzugefügt werden sollen.
     */
    private void addHabitPointsForCurrentYear(View layoutView) {
        GridLayout gridLayout = layoutView.findViewById(R.id.buttonGridYear);


        LocalDate today = LocalDate.now();
        LocalDate startDate = today.with(TemporalAdjusters.firstDayOfYear());
        int totalWeeks = 53;


        for (int i = 0; i < totalWeeks; i++) {
            for (int j = 0; j < 7; j++) {
                LocalDate date = startDate.plusDays((i * 7) + j);
                addHabitDataToGridLayout(gridLayout, date, j, i);
            }
        }
    }
    /**
     * Fügt Daten zu einem GridLayout hinzu, basierend auf dem Datum. Zeigt leere Datenpunkte für zukünftige Tage und erfüllte/unerfüllte Gewohnheiten für vergangene Tage.
     *
     * @param gridLayout Das GridLayout, zu dem die Daten hinzugefügt werden.
     * @param date Das Datum, für das die Daten hinzugefügt werden.
     * @param rowIndex Die Reihe im GridLayout für die Daten.
     * @param columnIndex Die Spalte im GridLayout für die Daten.
     */
    private void addHabitDataToGridLayout(GridLayout gridLayout, LocalDate date, int rowIndex, int columnIndex) {
        String dateString = date.toString();
        LocalDate today = LocalDate.now();

        if (date.isAfter(today)) {
            View pointView;

            GridLayout.Spec rowSpec = GridLayout.spec(rowIndex);
            GridLayout.Spec columnSpec = GridLayout.spec(columnIndex);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, columnSpec);

            if (gridLayout.getId() == R.id.buttonGrid) {
                pointView = LayoutInflater.from(getContext()).inflate(R.layout.analytics_week_month_0_item, gridLayout, false);
                params.setMargins(10, 10, 10, 10);
                pointView.setLayoutParams(params);
            } else {
                pointView = LayoutInflater.from(getContext()).inflate(R.layout.analytics_year_0_item, gridLayout, false);
                params.setMargins(4, 4, 4, 4);
                pointView.setLayoutParams(params);
            }
            gridLayout.addView(pointView);
        } else {
            viewModel.getNumberOfHabitsForDay(dateString).observe(getViewLifecycleOwner(), numberOfHabits -> viewModel.getTotalHabitsForDate(dateString).observe(getViewLifecycleOwner(), totalHabits -> {
                if (gridLayout.getId() == R.id.buttonGridYear) {
                    addHabitPointsToDayForYear(gridLayout, rowIndex, columnIndex, numberOfHabits, totalHabits);
                } else {
                    addHabitPointsToDay(gridLayout, rowIndex, columnIndex, numberOfHabits, totalHabits);
                }
            }));
        }
    }

    /**
     * Fügt ein Menüelement zum angegebenen LinearLayout-Container hinzu. Das Menüelement wird mit einem Titel versehen,
     * und wenn ein zugehöriges Emoji-Icon vorhanden ist, wird dieses vor den Titel gesetzt.
     *
     * @param container Der LinearLayout-Container, zu dem das Menüelement hinzugefügt wird.
     * @param title Der Texttitel des Menüelements.
     */
    private void addMenuItem(LinearLayout container, String title) {
        Context context = getContext();
        if (context == null) return;

        LayoutInflater inflater = LayoutInflater.from(context);

        // Erstellt einen neuen MaterialButton als Menüelement.
        MaterialButton popupMenuButton = (MaterialButton) inflater.inflate(R.layout.popup_button, container, false);
        popupMenuButton.setText(title);

        // Beobachtet das zugehörige Icon zum Titel und setzt es, falls vorhanden.
        viewModel.getIconByName(title).observe(getViewLifecycleOwner(), icon -> {
            if (utility.isEmoji(icon)) {
                String titleWithEmoji = icon + "  " + title;
                popupMenuButton.setText(titleWithEmoji);
            }
            BindingAdapters.setIconSrc(popupMenuButton, icon);
        });

        // Setzt Layout-Parameter für das Menüelement.
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupMenuButton.setLayoutParams(layoutParams);

        // Definiert das Verhalten bei Klick auf das Menüelement.
        popupMenuButton.setOnClickListener(v -> {
            this.selectedHabit = title;
            updateCompletionRate();

            if ("All Habits".equals(title)) {
                viewModel.getMaxLongestStreak().observe(getViewLifecycleOwner(), bestStreak -> {
                    if (bestStreak != null) {
                        TextView bestStreakView = binding.bestStreak;
                        bestStreakView.setText(String.valueOf(bestStreak));
                    }
                });
            } else {
                viewModel.getHabitIdFromTitle(title, habitId -> viewModel.calculateBestStreak(habitId).observe(getViewLifecycleOwner(), bestStreak -> {
                    TextView bestStreakView = binding.bestStreak;
                    bestStreakView.setText(String.valueOf(bestStreak));
                }));
            }

            if (popupWindow != null) {
                popupWindow.dismiss();
            }

            // Aktualisiert das Icon des Menübuttons, um das ausgewählte Element widerzuspiegeln.
            viewModel.getIconByName(title).observe(getViewLifecycleOwner(), icon -> {
                BindingAdapters.setIconSrc(binding.menuButton, icon);
                binding.menuButton.setText(title);
            });
        });

        container.addView(popupMenuButton);
    }
    /**
     * Zeigt ein vollbreites Popup-Menü an, das es dem Benutzer ermöglicht, eine Gewohnheit auszuwählen.
     *
     * @param anchorView Die Ansicht, an der das Popup-Menü ausgerichtet wird.
     */
    private void showFullWidthPopup(View anchorView) {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            return;
        }

        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.popup_full_width, null);
        LinearLayout popupContainer = popupView.findViewById(R.id.popupContainer);

        addMenuItem(popupContainer, "All Habits");

        viewModel.getHabitTitles().observe(getViewLifecycleOwner(), habitTitles -> {
            if (habitTitles != null) {
                for (String title : habitTitles) {
                    addMenuItem(popupContainer, title);
                }
            }
        });

        initPopupWindow(popupView, anchorView);
    }

    /**
     * Initialisiert ein Popup-Fenster mit der gegebenen Ansicht und zeigt es an einer Ankeransicht an.
     *
     * @param popupView Die Ansicht, die im Popup-Fenster angezeigt wird.
     * @param anchorView Die Ankeransicht, relativ zu der das Popup-Fenster angezeigt wird.
     */
    private void initPopupWindow(View popupView, View anchorView) {
        int desiredWidth = getPopupWidth();
        popupWindow = new PopupWindow(popupView, desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT);

        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        View overlayView = createOverlayView();
        ((ViewGroup) Objects.requireNonNull(getActivity()).findViewById(android.R.id.content)).addView(overlayView);

        popupWindow.showAsDropDown(anchorView, 0, 0);
        popupWindow.setOnDismissListener(() -> {
            ((ViewGroup) getActivity().findViewById(android.R.id.content)).removeView(overlayView);
        });
    }

    /**
     * Berechnet die Breite des Popup-Fensters basierend auf der Breite des Bildschirms.
     *
     * @return Die berechnete Breite des Popup-Fensters.
     */
    private int getPopupWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels - 2 * utility.convertDpToPixel(getContext());
    }

    /**
     * Erstellt eine semi-transparente Overlay-Ansicht, die über den gesamten Bildschirm gelegt wird, wenn das Popup-Fenster angezeigt wird.
     *
     * @return Die erstellte Overlay-Ansicht.
     */
    private View createOverlayView() {
        View overlayView = new View(getContext());
        overlayView.setBackgroundColor(Color.parseColor("#20000000"));
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        overlayView.setLayoutParams(params);
        return overlayView;
    }

    /**
     * Gibt die Anzahl der Tage zurück, basierend auf der ausgewählten Ansichtsoption (Woche, Monat, Jahr).
     *
     * @param viewOption Die ausgewählte Ansichtsoption.
     * @return Die Anzahl der Tage entsprechend der Ansichtsoption.
     */
    private int getDaysFromViewOption(String viewOption) {
        switch (viewOption) {
            case "Monat":
                return 30;
            case "Jahr":
                return 365;
            default:
                return 7;
        }
    }

    /**
     * Ermittelt die Ansichtsoption (Woche, Monat, Jahr) basierend auf der ID der ausgewählten Schaltfläche.
     *
     * @param id Die ID der ausgewählten Schaltfläche.
     * @return Die entsprechende Ansichtsoption.
     */
    private String getViewOptionFromId(int id) {
        if (id == R.id.button1) return "Woche";
        if (id == R.id.button2) return "Monat";
        if (id == R.id.button3) return "Jahr";
        return "Woche";
    }
}