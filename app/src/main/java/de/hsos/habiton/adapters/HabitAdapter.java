package de.hsos.habiton.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.hsos.habiton.R;
import de.hsos.habiton.database.models.Habit;
import de.hsos.habiton.database.models.HabitTracking;
import de.hsos.habiton.databinding.HabitItemBinding;
import de.hsos.habiton.databinding.AnalyticsHabitItemBinding;

/**
 * Adapter für die RecyclerView, um Gewohnheiten anzuzeigen.
 * @author Finn Garrels
 */
public class HabitAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Habit> habits = new ArrayList<>();
    private List<HabitTracking> trackings = new ArrayList<>();
    private final int viewType;
    public static final int VIEW_TYPE_NORMAL = 0;
    public static final int VIEW_TYPE_ANALYTICS = 1;

    /**
     * Konstruktor für den HabitAdapter.
     *
     * @param viewType Der Typ der Ansicht, entweder normal oder Analyse.
     */
    public HabitAdapter(int viewType) {
        this.viewType = viewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_ANALYTICS) {
            AnalyticsHabitItemBinding binding = DataBindingUtil.inflate(
                    inflater, R.layout.analytics_habit_item, parent, false);
            return new AnalyticsHabitHolder(binding);
        } else {
            HabitItemBinding binding = DataBindingUtil.inflate(
                    inflater, R.layout.habit_item, parent, false);
            return new HabitHolder(binding);
        }
    }

    /**
     * Befüllt die Ansicht mit Daten für die angegebene Position.
     *
     * @param holder   Der ViewHolder, der aktualisiert werden soll.
     * @param position Die Position des Items in der Datenquelle.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Habit habit = habits.get(position);
        HabitTracking tracking = findTrackingForHabit(habit.getId());

        if (getItemViewType(position) == VIEW_TYPE_ANALYTICS) {
            ((AnalyticsHabitHolder) holder).bind(habit, tracking);
        } else {
            ((HabitHolder) holder).bind(habit, tracking);
        }
    }

    /**
     * Sucht nach Tracking-Daten für eine gegebene Gewohnheit.
     *
     * @param habitId Die ID der Gewohnheit, für die Tracking-Daten gesucht werden.
     * @return Die Tracking-Daten für die gegebene Gewohnheit oder null, wenn keine gefunden wurden.
     */
    private HabitTracking findTrackingForHabit(int habitId) {
        for (HabitTracking tracking : trackings) {
            if (tracking.getHabitID() == habitId) {
                return tracking;
            }
        }
        return null;
    }

    /**
     * Gibt die Anzahl der Elemente in der Datenquelle zurück.
     *
     * @return Die Anzahl der Elemente in der Datenquelle.
     */
    @Override
    public int getItemCount() {
        return habits.size();
    }


    /**
     * Setzt die Liste der Gewohnheiten und aktualisiert die RecyclerView.
     *
     * @param habits Die Liste der Gewohnheiten, die angezeigt werden sollen.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setHabits(List<Habit> habits) {
        this.habits = habits;
        notifyDataSetChanged();
    }

    /**
     * Setzt die Liste der Gewohnheitstrackings und aktualisiert die RecyclerView.
     *
     * @param trackings Die Liste der Gewohnheitstrackings.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setHabitTrackings(List<HabitTracking> trackings) {
        this.trackings = trackings;
        notifyDataSetChanged();
    }

    /**
     * Gibt den View-Typ für das angegebene Element zurück.
     *
     * @param position Die Position des Elements in der Datenquelle.
     * @return Der View-Typ für das angegebene Element.
     */
    @Override
    public int getItemViewType(int position) {
        return viewType;
    }


    /**
     * Gibt die Gewohnheit an der angegebenen Position zurück.
     *
     * @param adapterPosition Die Position der Gewohnheit im Adapter.
     * @return Die Gewohnheit an der angegebenen Position oder null, wenn nicht vorhanden.
     */
    public Habit getHabitAt(int adapterPosition) {
        if (adapterPosition >= 0 && adapterPosition < habits.size()) {
            return habits.get(adapterPosition);
        }
        return null;
    }

    /**
     * ViewHolder-Klasse für Gewohnheiten.
     */
    static class HabitHolder extends RecyclerView.ViewHolder {
        private final HabitItemBinding binding;

        /**
         * Konstruktor für den HabitHolder.
         *
         * @param binding Das Binding-Objekt für die Ansicht.
         */
        public HabitHolder(HabitItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Bindet die Daten an die ViewHolder für normale Gewohnheiten.
         *
         * @param habit    Die Gewohnheit, die angezeigt werden soll.
         * @param tracking Die zugehörige Tracking-Information der Gewohnheit.
         */
        public void bind(Habit habit, HabitTracking tracking) {
            binding.setHabit(habit);
            binding.setTracking(tracking);
            binding.executePendingBindings();
        }
    }

    /**
     * ViewHolder-Klasse für Gewohnheiten mit Analyseinformationen.
     */
    public static class AnalyticsHabitHolder extends RecyclerView.ViewHolder {
        private final AnalyticsHabitItemBinding binding;

        /**
         * Konstruktor für den AnalyticsHabitHolder.
         *
         * @param binding Das Binding-Objekt für die Ansicht.
         */
        public AnalyticsHabitHolder(AnalyticsHabitItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Bindet die Daten an die ViewHolder für Gewohnheiten mit Analyseinformationen.
         *
         * @param habit    Die Gewohnheit, die angezeigt werden soll.
         * @param tracking Die zugehörige Tracking-Information der Gewohnheit.
         */
        public void bind(Habit habit, HabitTracking tracking) {
            binding.setHabit(habit);
            binding.setTracking(tracking);
            binding.longestStreakNumber.setText(String.valueOf(habit.getLongestStreak()));
            binding.executePendingBindings();
        }
    }
}
