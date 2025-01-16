package de.hsos.habiton.views;

import android.app.Dialog;
import android.content.DialogInterface;
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
import de.hsos.habiton.databinding.CreateHabitFragmentBinding;
import de.hsos.habiton.viewModels.DashboardViewModel;

/**
 * Ein Fragment zum Hinzufügen eines neuen Habits.
 * @author Miikka Koensler
 */
public class AddHabitFragment extends DialogFragment {

    private final Map<Integer, String> buttonIconMap = new HashMap<>();
    private CreateHabitFragmentBinding binding;
    private DashboardViewModel viewModel;
    private String icon;
    private MaterialButton selectedButton = null;
    private OnDialogCloseListener listener;

    /**
     * Bereitet die UI vor, initialisiert das ViewModel und lädt vorhandene Habit-Daten.
     *
     * @param inflater           Der LayoutInflater, der verwendet wird, um die Ansicht zu erstellen.
     * @param container          Der ViewGroup-Container, in den die Ansicht eingefügt wird.
     * @param savedInstanceState Die gespeicherten Daten des Fragments, falls vorhanden.
     * @return Die erstellte Ansicht für das Fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = CreateHabitFragmentBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);
        setupUI();
        return binding.getRoot();
    }

    /**
     * Passt beim Starten des Fragments die Layoutparameter des Dialogs an.
     */
    @Override
    public void onStart() {
        super.onStart();
        adjustDialogLayout();
    }

    /**
     * Behandelt das Schließen des Dialogs.
     *
     * @param dialog Der Dialog, der geschlossen wird.
     */
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        dialogClosed();
    }

    /**
     * Bereitet die UI vor und initialisiert das ViewModel.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Setzt den Listener für den Dialogschließvorgang.
     *
     * @param listener Der Listener für den Dialogschließvorgang.
     */
    public void setOnDialogCloseListener(OnDialogCloseListener listener) {
        this.listener = listener;
    }

    /**
     * Konfiguriert die UI, einschließlich der Texteingabe- und Button-Listener.
     */
    private void setupUI() {
        initializeButtonIconMap();
        setupButtonListeners();
        setupEmojiInputListener();
        setupSaveButtonListener();
        setupCloseButtons();
    }

    /**
     * Initialisiert die Zuordnung von Button-IDs zu Habit-Icon-Namen.
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
     * Konfiguriert die Button zum Schließen des Dialogs.
     */
    private void setupCloseButtons() {
        // Vereinfachen Sie das Schließen des Dialogs
        View.OnClickListener closeListener = v -> dismiss();
        binding.closeicon.setOnClickListener(closeListener);
        binding.btnCancel.setOnClickListener(closeListener);
    }

    /**
     * Konfiguriert den Listener für die Button zum Speichern des Habits.
     */
    private void setupSaveButtonListener() {
        binding.btnSave.setOnClickListener(view -> {
            String name = binding.addName.getText() != null ? binding.addName.getText().toString() : "";
            String description = binding.addDescription.getText() != null ? binding.addDescription.getText().toString() : "";

            viewModel.doesHabitExist(name).observe(this, exists -> {
                if (!exists) {
                    viewModel.insertHabit(name, description, icon, 0);
                } else {
                    Toast.makeText(getActivity(), "Name already exists", Toast.LENGTH_SHORT).show();
                }
            });

            dismissDialogOnSave();
        });
    }

    /**
     * Konfiguriert den Listener für die Emoji-Eingabe.
     */
    private void setupEmojiInputListener() {
        binding.emojiinput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    icon = s.toString();
                    resetSelectedButton();
                }
            }

            // Leere Methodenimplementierungen
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
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
     * Wird aufgerufen, wenn eine Habit-Icon-Button angeklickt wird.
     *
     * @param view Die angeklickte Button-Ansicht.
     */
    private void onIconButtonClicked(View view) {
        icon = buttonIconMap.get(view.getId());
        // Setze die Auswahl der anderen Buttons zurück
        deselectAllButtons();

        if (binding.emojiinput.getText() != null) {
            binding.emojiinput.getText().clear();
        }
        // Aktualisiere die Auswahl des ausgewählten Buttons
        updateSelectedButton((MaterialButton) view);
    }

    /**
     * Setzt die visuelle Markierung der ausgewählten Button zurück.
     */
    private void resetSelectedButton() {
        if (selectedButton != null) {
            selectedButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.highlight, null)));
            selectedButton = null;
        }
    }

    /**
     * Aktualisiert die visuelle Markierung der ausgewählten Button.
     *
     * @param button Der ausgewählte Button.
     */
    private void updateSelectedButton(MaterialButton button) {
        resetSelectedButton();
        selectedButton = button;
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
     * Entlässt den Dialog, nachdem der Habit gespeichert wurde.
     */
    private void dismissDialogOnSave() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::dismiss);
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
     * Wird aufgerufen, wenn der Dialog geschlossen wird.
     */
    private void dialogClosed() {
        if (listener != null) {
            listener.onDialogClosed();
        }
    }

    /**
     * Schnittstelle für den Listener beim Schließen des Dialogs.
     */
    public interface OnDialogCloseListener {
        void onDialogClosed();
    }
}
