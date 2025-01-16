package de.hsos.habiton.views;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hsos.habiton.R;
import de.hsos.habiton.adapters.HabitAdapter;
import de.hsos.habiton.database.models.Habit;
import de.hsos.habiton.databinding.DashboardFragmentBinding;
import de.hsos.habiton.helpers.Utility;
import de.hsos.habiton.viewModels.DashboardViewModel;

/**
 * Fragment für das Dashboard, das die Liste der Gewohnheiten anzeigt und Aktionen wie das Bearbeiten und Löschen von Gewohnheiten ermöglicht.
 * @author Miikka Koensler, Finn Garrels
 */
public class DashboardFragment extends Fragment {
    private DashboardFragmentBinding binding;
    private DashboardViewModel dashboardViewModel;
    private HabitAdapter adapter;

    /**
     * Wird aufgerufen, um die View für dieses Fragment zu erstellen und zu konfigurieren.
     *
     * @param inflater           Der LayoutInflater, der verwendet wird, um XML-Layoutressourcen aufzublähen.
     * @param container          Wenn nicht null, wird die View, nachdem sie inflated wurde, diesem Eltern-View hinzugefügt.
     * @param savedInstanceState Wenn nicht null, wird diese Fragmentinstanz wiederhergestellt.
     * @return Die inflated View für dieses Fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DashboardFragmentBinding.inflate(inflater, container, false);
        setupRecyclerView();
        setupViewModel();
        binding.settingsicon.setOnClickListener(v -> navigateToSettings());
        return binding.getRoot();
    }

    /**
     * Konfiguriert das ViewModel für dieses Fragment und beobachtet Änderungen an den Habit-Daten.
     */
    private void setupViewModel() {
        dashboardViewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        dashboardViewModel.getAllHabits().observe(getViewLifecycleOwner(), habits -> {
            adapter.setHabits(habits);
            updateEmptyStateButton();
        });

        dashboardViewModel.getHabitTrackingsForDate(currentDate).observe(getViewLifecycleOwner(), trackings -> adapter.setHabitTrackings(trackings));
    }

    /**
     * Aktualisiert den Button für den leeren Zustand basierend auf dem aktuellen Status der Habit-Liste.
     */
    private void updateEmptyStateButton() {
        if (adapter.getItemCount() == 0) {
            addEmptyStateButton();
        } else {
            hideEmptyStateButton();
        }
    }

    /**
     * Fügt den Button für den leeren Zustand hinzu und weist ihm eine Aktion zum Hinzufügen einer neuen Gewohnheit zu.
     */
    private void addEmptyStateButton() {
        MaterialButton addButton = binding.getRoot().findViewById(R.id.add_habit_button);
        addButton.setVisibility(View.VISIBLE);
        addButton.setOnClickListener(view -> showAddHabitDialog());
    }

    /**
     * Versteckt den Button für den leeren Zustand.
     */
    private void hideEmptyStateButton() {
        MaterialButton addButton = binding.getRoot().findViewById(R.id.add_habit_button);
        addButton.setVisibility(View.GONE);
    }

    /**
     * Konfiguriert das RecyclerView für die Anzeige der Gewohnheitsliste.
     */
    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        adapter = new HabitAdapter(HabitAdapter.VIEW_TYPE_NORMAL);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(createItemTouchHelperCallback());
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * Erstellt den ItemTouchHelper, um das Swipen von Gewohnheiten zu ermöglichen.
     *
     * @return Der erstellte ItemTouchHelper.
     */
    private ItemTouchHelper.SimpleCallback createItemTouchHelperCallback() {
        return new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Habit selectedHabit = adapter.getHabitAt(position);

                if (direction == ItemTouchHelper.RIGHT) {
                    dashboardViewModel.toggleHabitDoneStatus(selectedHabit.getId());
                } else if (direction == ItemTouchHelper.LEFT) {
                    showEditHabitDialog(selectedHabit);
                }
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;
                int itemHeight = itemView.getBottom() - itemView.getTop();

                // Definieren von Hintergründen und Icons für beide Swipe-Richtungen
                Drawable rightSwipeBackground = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_check_background);
                Drawable rightSwipeIcon = ContextCompat.getDrawable(requireContext(), R.drawable.check);
                Drawable leftSwipeBackground = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_edit_background);
                Drawable leftSwipeIcon = ContextCompat.getDrawable(requireContext(), R.drawable.edit);

                assert rightSwipeIcon != null;
                int iconMargin = (itemHeight - rightSwipeIcon.getIntrinsicHeight()) / 2;
                int iconTop = itemView.getTop() + (itemHeight - rightSwipeIcon.getIntrinsicHeight()) / 2;
                int iconBottom = iconTop + rightSwipeIcon.getIntrinsicHeight();

                int padding = Utility.dpToPx();

                // Swipe nach rechts
                if (dX > 0) {
                    int backgroundRight = itemView.getLeft() + (int) dX - padding;
                    int iconLeft = itemView.getLeft() + iconMargin + padding;
                    int iconRight = iconLeft + rightSwipeIcon.getIntrinsicWidth();

                    assert rightSwipeBackground != null;
                    rightSwipeBackground.setBounds(itemView.getLeft() + padding, itemView.getTop(), backgroundRight, itemView.getBottom());
                    rightSwipeIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                    rightSwipeBackground.draw(c);
                    rightSwipeIcon.draw(c);
                }
                // Swipe nach links
                else if (dX < 0) {
                    int backgroundLeft = itemView.getRight() + (int) dX + padding;
                    int iconRight = itemView.getRight() - iconMargin - padding;
                    assert leftSwipeIcon != null;
                    int iconLeft = iconRight - leftSwipeIcon.getIntrinsicWidth();

                    assert leftSwipeBackground != null;
                    leftSwipeBackground.setBounds(backgroundLeft, itemView.getTop(), itemView.getRight() - padding, itemView.getBottom());
                    leftSwipeIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                    leftSwipeBackground.draw(c);
                    leftSwipeIcon.draw(c);
                }
            }
        };
    }

    /**
     * Zeigt das Dialogfragment zum Bearbeiten einer vorhandenen Gewohnheit an.
     *
     * @param habitToEdit Die zu bearbeitende Gewohnheit.
     */
    private void showEditHabitDialog(Habit habitToEdit) {
        EditHabitFragment editHabitFragment = new EditHabitFragment(habitToEdit);
        editHabitFragment.show(getParentFragmentManager(), "EditHabitFragment");
    }

    /**
     * Zeigt das Dialogfragment zum Hinzufügen einer neuen Gewohnheit an.
     */
    private void showAddHabitDialog() {
        AddHabitFragment addHabitFragment = new AddHabitFragment();
        addHabitFragment.show(getParentFragmentManager(), "AddHabitFragment");
    }

    /**
     * Navigiert zum Einstellungsfragment.
     */
    private void navigateToSettings() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_dashboardFragment_to_settingsFragment);
    }

    /**
     * Wird aufgerufen, wenn die View des Fragments erstellt wurde.
     *
     * @param view               Die erstellte View.
     * @param savedInstanceState Wenn nicht null, wird diese Fragmentinstanz wiederhergestellt.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new ViewModelProvider(this).get(DashboardViewModel.class);
    }

}