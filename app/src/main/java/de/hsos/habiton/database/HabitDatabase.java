package de.hsos.habiton.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import de.hsos.habiton.database.daos.HabitDao;
import de.hsos.habiton.database.daos.HabitTrackingDao;
import de.hsos.habiton.database.models.Habit;
import de.hsos.habiton.database.models.HabitTracking;

/**
 * Die abstrakte Klasse für die Room-Datenbank.
 * @author Miikka Koensler
 */
@Database(entities = {Habit.class, HabitTracking.class}, version = 13)
public abstract class HabitDatabase extends RoomDatabase {

    private static HabitDatabase instance;

    /**
     * Gibt das DAO-Objekt für die Gewohnheitsdaten zurück.
     *
     * @return Das DAO-Objekt für die Gewohnheitsdaten.
     */
    public abstract HabitDao habitDao();

    /**
     * Gibt das DAO-Objekt für die Trackingdaten zurück.
     *
     * @return Das DAO-Objekt für die Trackingdaten.
     */
    public abstract HabitTrackingDao habitTrackingDao();

    /**
     * Stellt sicher, dass nur eine Instanz der Datenbank existiert.
     *
     * @param context Der Anwendungscontext.
     * @return Die Instanz der Datenbank.
     */
    public static synchronized HabitDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            HabitDatabase.class, "habit_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    /**
     * Callback-Funktion für die Erstellung der Datenbank.
     */
    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
        }
    };
}
