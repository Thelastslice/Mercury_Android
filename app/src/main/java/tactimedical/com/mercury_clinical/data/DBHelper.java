package tactimedical.com.mercury_clinical.data;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import tactimedical.com.mercury_clinical.app.App;
import tactimedical.com.mercury_clinical.data.model.Placement;
import tactimedical.com.mercury_clinical.data.model.Room;
import tactimedical.com.mercury_clinical.data.model.Patient;
import tactimedical.com.mercury_clinical.data.model.PatientPlacement;
import tactimedical.com.mercury_clinical.data.repo.PlacementRepo;
import tactimedical.com.mercury_clinical.data.repo.RoomRepo;
import tactimedical.com.mercury_clinical.data.repo.PatientPlacementRepo;
import tactimedical.com.mercury_clinical.data.repo.PatientRepo;


public class DBHelper  extends SQLiteOpenHelper {
    //version number to upgrade database version
    //each time if you Add, Edit table, you need to change the
    //version number.
    private static final int DATABASE_VERSION =8;
    // Database Name
    private static final String DATABASE_NAME = "Mercury_clinical.db";
    private static final String TAG = DBHelper.class.getSimpleName().toString();

    public DBHelper( ) {
        super(App.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //All necessary tables you like to create will create here
        db.execSQL(PlacementRepo.createTable());
        db.execSQL(PatientRepo.createTable());
        db.execSQL(RoomRepo.createTable());
        db.execSQL(PatientPlacementRepo.createTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, String.format("SQLiteDatabase.onUpgrade(%d -> %d)", oldVersion, newVersion));

        // Drop table if existed, all data will be gone!!!
        db.execSQL("DROP TABLE IF EXISTS " + Placement.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Patient.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Room.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PatientPlacement.TABLE);
        onCreate(db);
    }

}