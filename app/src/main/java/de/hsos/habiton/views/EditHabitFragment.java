package de.hsos.habiton.views;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

import de.hsos.habiton.R;
import de.hsos.habiton.database.models.Habit;
import de.hsos.habiton.databinding.EditHabitFragmentBinding;
import de.hsos.habiton.helpers.Utility;
import de.hsos.habiton.viewModels.DashboardViewModel;

/**
 * Ein Fragment zum Bearbeiten eines bestehenden Habits.
 * Erstellt von Finn Garrels
 */
public class EditHabitFragment extends DialogFragment {

    private final Habit existingHabit;
    private final Map<Integer, String> buttonIconMap = new HashMap<>();
    private EditHabitFragmentBinding binding;
    private DashboardViewModel viewModel;
    private String selectedIcon;


    /**
     * Konstruktor für das EditHabitFragment.
     *
     * @param habitToEdit Der zu bearbeitende Habit.
     */
    public EditHabitFragment(Habit habitToEdit) {
        this.existingHabit = habitToEdit;
        this.selectedIcon = habitToEdit.getIcon();
    }

    /**
     * Erstellt und konfiguriert die View für dieses Fragment.
     *
     * @param inflater           Der LayoutInflater, der verwendet wird, um XML-Layoutressourcen aufzublähen.
     * @param container          Wenn nicht null, wird die View, nachdem sie aufgeblasen wurde, diesem Eltern-View hinzugefügt.
     * @param savedInstanceState Wenn nicht null, wird diese Fragmentinstanz wiederhergestellt.
     * @return Die aufgeblasene View für dieses Fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = EditHabitFragmentBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);
        setupUI();
        fillData();
        setupButtonListeners(); // Listener für die Habit-Icon-Buttons konfigurieren
        initializeButtonIconMap(); // Button-IDs zu Icon-Namen zuordnen
        return binding.getRoot();
    }

    /**
     * Wird aufgerufen, wenn das Fragment sichtbar ist und die Benutzerinteraktion akzeptiert.
     * Passt beim Starten des Fragments die Layoutparameter des Dialogs an.
     */
    @Override
    public void onStart() {
        super.onStart();
        adjustDialogLayout();
    }

    /**
     * Konfiguriert die UI und setzt die Listener für UI-Elemente.
     */
    private void setupUI() {
        binding.emojiinput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    selectedIcon = s.toString();
                    deselectAllButtons();
                }
            }
        });


        binding.closeicon.setOnClickListener(v -> dismiss());
        binding.btnDelete.setOnClickListener(v -> delete());
        binding.btnSave.setOnClickListener(view -> saveHabit());
    }

    /**
     * Konfiguriert die Listener für die Habit-Icon-Button.
     */
    private void setupButtonListeners() {
        GridLayout gridLayout = binding.iconButtonGrid;
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            if (child instanceof MaterialButton) {
                child.setOnClickListener(this::onIconButtonClicked);
            }
        }
    }

    /**
     * Initialisiert die Zuordnung von Button-IDs zu Icon-Namen.
     */
    private void initializeButtonIconMap() {
        int[] buttonIds = {
                R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5,
                R.id.button6, R.id.button7, R.id.button8, R.id.button9, R.id.button10,
                R.id.button11, R.id.button12, R.id.button13, R.id.button14, R.id.button15
        };
        String[] iconNames = {
                "workout", "walk", "book", "music", "sleep",
                "water", "clock", "cook", "learn", "meditate",
                "inbox", "laptop", "nophone", "todo", "food"
        };

        for (int i = 0; i < buttonIds.length; i++) {
            buttonIconMap.put(buttonIds[i], iconNames[i]);
        }
    }

    /**
     * Wird aufgerufen, wenn eine Habit-Icon-Button angeklickt wird.
     *
     * @param view Die angeklickte Button-Ansicht.
     */
    private void onIconButtonClicked(View view) {
        selectedIcon = buttonIconMap.get(view.getId());
        // Setze die Auswahl der anderen Buttons zurück
        deselectAllButtons();
        if (binding.emojiinput.getText() != null) {
            binding.emojiinput.getText().clear();
        }
        // Aktualisiere die Auswahl des ausgewählten Buttons
        updateSelectedButton((MaterialButton) view);
    }

    /**
     * Befüllt die Eingabefelder mit den vorhandenen Habit-Daten.
     */
    private void fillData() {
        binding.editName.setText(existingHabit.getName());
        binding.editDescription.setText(existingHabit.getDescription());
        updateSelectedIconView();
    }

    /**
     * Markiert den ausgewählten Button visuell und setzt die Auswahl der anderen Buttons zurück.
     */
    private void updateSelectedButton(MaterialButton selectedButton) {
        selectedButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.highlight_dark, null)));
    }

    /**
     * Setzt die visuelle Markierung aller Buttons zurück.
     */
    private void deselectAllButtons() {
        GridLayout gridLayout = binding.iconButtonGrid;
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            if (child instanceof MaterialButton) {
                ((MaterialButton) child).setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.highlight, null)));
            }
        }
    }


    /**
     * Aktualisiert die UI basierend auf dem ausgewählten Icon.
     */
    private void updateSelectedIconView() {
        if (Utility.isEmoji(selectedIcon)) {
            binding.emojiinput.setText(selectedIcon);
            deselectAllButtons();
        } else {
            for (Map.Entry<Integer, String> entry : buttonIconMap.entrySet()) {
                if (entry.getValue().equals(selectedIcon)) {
                    MaterialButton button = binding.getRoot().findViewById(entry.getKey());
                    if (button != null) {
                        updateSelectedButton(button);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Passt die Layoutparameter des Dialogs an.
     */
    private void adjustDialogLayout() {
        Dialog dialog = getDialog();

        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (getResources().getDisplayMetrics().heightPixels * 0.90));
                WindowManager.LayoutParams windowParams = window.getAttributes();
                windowParams.gravity = Gravity.BOTTOM;
                window.setAttributes(windowParams);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
    }

    /**
     * Speichert die Änderungen am Habit.
     */
    private void saveHabit() {
        String name = binding.editName.getText() != null ? binding.editName.getText().toString() : "";
        String description = binding.editDescription.getText() != null ? binding.editDescription.getText().toString() : "";


        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.updateHabit(existingHabit.getId(), name, description, selectedIcon, existingHabit.getStreak());
        Toast.makeText(getActivity(), "Habit updated", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    /**
     * Löscht den aktuellen Habit.
     */
    private void delete() {
        viewModel.deleteHabit(existingHabit);
        Toast.makeText(getActivity(), "Habit deleted", Toast.LENGTH_SHORT).show();
        dismiss();
    }
}
