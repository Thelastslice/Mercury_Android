package tactimedical.com.mercury_clinical.data.repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import tactimedical.com.mercury_clinical.data.DatabaseManager;
import tactimedical.com.mercury_clinical.data.model.Patient;

public class PatientRepo {

    private Patient patient;

    public PatientRepo(){

        patient = new Patient();

    }


    public static String createTable(){
        return "CREATE TABLE " + Patient.TABLE  + "("
                + Patient.KEY_PatientID  + " TEXT PRIMARY KEY  ,"
                + Patient.KEY_Name + " TEXT, "
                + Patient.KEY_RoomID  + " TEXT )";
    }



    public boolean insert(Patient patient) {

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues values = new ContentValues();
        values.put(Patient.KEY_PatientID, patient.getPatientID());
        values.put(Patient.KEY_Name, patient.getName());
        values.put(Patient.KEY_RoomID, patient.getRoom());

        // Inserting Row
        db.insert(Patient.TABLE, null, values);
        DatabaseManager.getInstance().closeDatabase();

        return true;
    }

    public boolean updatePatient(Patient patient) {

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues values = new ContentValues();
        values.put(Patient.KEY_PatientID, patient.getPatientID());
        values.put(Patient.KEY_Name, patient.getName());
        values.put(Patient.KEY_RoomID, patient.getRoom());

        String selectQuery = Patient.KEY_PatientID + " ='"+patient.getPatientID()+"';";
        // Inserting Row
        db.update(Patient.TABLE,values,selectQuery,null);
        DatabaseManager.getInstance().closeDatabase();

        return true;
    }

    public void delete( ) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        db.delete(Patient.TABLE, null,null);
        DatabaseManager.getInstance().closeDatabase();
    }

    public boolean patientExists(String _id) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        String selectQuery = "SELECT * FROM " + Patient.TABLE + " WHERE " +
            Patient.KEY_Name + "='"+ _id+"';"
            ;

        Cursor res =  db.rawQuery(selectQuery,null);
        boolean exists = (res.getCount() > 0);
        res.close();
        DatabaseManager.getInstance().closeDatabase();
        return exists;
    }



}
