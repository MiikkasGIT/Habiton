package de.hsos.habiton.views;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
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

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import de.hsos.habiton.R;
import de.hsos.habiton.databinding.SettingsFragmentBinding;
import de.hsos.habiton.viewModels.SettingsViewModel;


/**
 * Ein Fragment zur Darstellung und Konfiguration von Einstellungen.
 * @author finn garrels
 */
public class SettingsFragment extends Fragment {
    private SettingsFragmentBinding binding;
    private SettingsViewModel viewModel;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Erstellt die Benutzeroberfläche des Fragments.
     *
     * @param inflater           Der LayoutInflater, der verwendet wird, um die XML-Layout-Ressource aufzublasen.
     * @param container          Wenn nicht null, ist dies der Elternansicht, zu der das fragment hinzugefügt wird.
     * @param savedInstanceState Wenn nicht null, wird dies das zuvor gespeicherte Zustandsbündel sein.
     * @return Die aufgeblasene Ansicht des Fragments.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SettingsFragmentBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        return binding.getRoot();
    }

    /**
     * Wird aufgerufen, nachdem die zugehörige Ansicht des Fragments erstellt wurde.
     *
     * @param view               Die erstellte Ansicht.
     * @param savedInstanceState Wenn nicht null, ist dies das zuvor gespeicherte Zustandsbündel.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadTimePreferences();
        setupTimePickerButtons();
        setupNotificationToggle();
        binding.backicon.setOnClickListener(v -> navigateToDashboard());
    }

    /**
     * Lädt die gespeicherten Zeitpräferenzen und aktualisiert die Benutzeroberfläche entsprechend.
     */
    private void loadTimePreferences() {
        String morningTime = viewModel.loadTimeFromPreferences(requireContext(), SettingsViewModel.MORNING_TIME_KEY);
        String eveningTime = viewModel.loadTimeFromPreferences(requireContext(), SettingsViewModel.EVENING_TIME_KEY);
        if (!morningTime.isEmpty()) {
            binding.morningPicker.setText(morningTime);
        }
        if (!eveningTime.isEmpty()) {
            binding.eveningPicker.setText(eveningTime);
        }
    }

    /**
     * Konfiguriert die Aktionen beim Klicken auf die Zeitwähler.
     */
    private void setupTimePickerButtons() {
        binding.morningPicker.setOnClickListener(v -> showTimePicker(SettingsViewModel.MORNING_TIME_KEY));
        binding.eveningPicker.setOnClickListener(v -> showTimePicker(SettingsViewModel.EVENING_TIME_KEY));
    }



    /**
     * Konfiguriert den Benachrichtigungsumschalter und handhabt seine Änderungen.
     */
    private void setupNotificationToggle() {
        boolean notificationPermissionGranted = isNotificationPermissionGranted();
        SharedPreferences preferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        boolean savedToggleStatus = preferences.getBoolean("notificationToggle", notificationPermissionGranted);
        binding.notificationToggle.setChecked(savedToggleStatus);

        binding.notificationToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !notificationPermissionGranted) {
                requestNotificationPermission();
            }
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("notificationToggle", isChecked);
            editor.apply();

            if (!isChecked) {
                viewModel.saveTimeToPreferences(requireContext(), SettingsViewModel.MORNING_TIME_KEY, "");
                viewModel.saveTimeToPreferences(requireContext(), SettingsViewModel.EVENING_TIME_KEY, "");
                binding.morningPicker.setText(getString(R.string.select));
                binding.eveningPicker.setText(getString(R.string.select));
            }
        });
    }

    /**
     * Fordert die Benachrichtigungsberechtigung an.
     */
    private void requestNotificationPermission() {
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
        startActivityForResult(intent, NOTIFICATION_PERMISSION_REQUEST_CODE);
    }
    /**
     * Überprüft, ob die Benachrichtigungsberechtigung erteilt wurde.
     *
     * @return true, wenn die Berechtigung erteilt wurde, sonst false.
     */
    @SuppressLint("InlinedApi")
    private boolean isNotificationPermissionGranted() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Zeigt einen Zeitwählerdialog an und speichert die ausgewählte Zeit.
     *
     * @param channelID Die ID des Kanals, für den die Zeit ausgewählt wird.
     */
    private void showTimePicker(String channelID) {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .build();

        timePicker.addOnDismissListener(dialog -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            @SuppressLint("DefaultLocale") String selectedTime = String.format("%02d:%02d", hour, minute);
            viewModel.saveTimeToPreferences(requireContext(), channelID, selectedTime);

            if (channelID.equals(SettingsViewModel.MORNING_TIME_KEY)) {
                binding.morningPicker.setText(selectedTime);
            } else {
                binding.eveningPicker.setText(selectedTime);
            }
            long delay = viewModel.calculateDelayForNotification(selectedTime);
            viewModel.scheduleNotification(getContext(), channelID, delay);
        });

        timePicker.show(getChildFragmentManager(), "TimePicker");
    }
    /**
     * Navigiert zum Dashboard-Fragment.
     */
    private void navigateToDashboard() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_settingsFragment_to_dashboardFragment);
    }

    /**
     * Wird aufgerufen, wenn die Ansicht des Fragments zerstört wird.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
