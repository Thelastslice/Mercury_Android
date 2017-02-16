package tactimedical.com.mercury_clinical.data.repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import tactimedical.com.mercury_clinical.data.DatabaseManager;
import tactimedical.com.mercury_clinical.data.model.PatientPlacement;
import tactimedical.com.mercury_clinical.data.model.Placement;
import tactimedical.com.mercury_clinical.data.model.Room;
import tactimedical.com.mercury_clinical.data.model.Patient;
import tactimedical.com.mercury_clinical.model.PatientPlacementList;

import java.util.ArrayList;
import java.util.List;

public class PatientPlacementRepo {
    private final String TAG = PatientPlacementRepo.class.getSimpleName().toString();

    public PatientPlacementRepo() {

    }

    private PatientPlacement patientPlacement;



    public static String createTable(){
        return "CREATE TABLE " + PatientPlacement.TABLE  + "("
                + PatientPlacement.KEY_RunningID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + PatientPlacement.KEY_PatientID + " TEXT, "
                + PatientPlacement.KEY_PlacementID + " TEXT, "
                + PatientPlacement.KEY_PatientPlacementSaveName + " TEXT )";
    }



    public void insert(PatientPlacement patientPlacement) {

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues values = new ContentValues();
        values.put(PatientPlacement.KEY_PatientID, patientPlacement.getPatientID());
        values.put(PatientPlacement.KEY_PlacementID, patientPlacement.getPlacementID());
        values.put(PatientPlacement.KEY_PatientPlacementSaveName, patientPlacement.getPatientPlacementSaveName());

        // Inserting Row
        db.insert(PatientPlacement.TABLE, null, values);
        DatabaseManager.getInstance().closeDatabase();

    }

    public void delete( ) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        db.delete(PatientPlacement.TABLE, null, null);
        DatabaseManager.getInstance().closeDatabase();
    }

    public List<PatientPlacementList> getPatientPlacement(int id, String place){
        PatientPlacementList patientPlacementList = new PatientPlacementList();
        List<PatientPlacementList> patientPlacementLists = new ArrayList<>();

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        String selectQuery =  " SELECT Patient." + Patient.KEY_PatientID
                + ", Patient." + Patient.KEY_Name
                + ", PatientPlacement." + PatientPlacement.KEY_PlacementID
                + ", Room." + Room.KEY_RoomID
                + " FROM " + Patient.TABLE
                + " INNER JOIN " + PatientPlacement.TABLE + " ON PatientPlacement." + PatientPlacement.KEY_PatientID + "=Patient."+ Patient.KEY_PatientID
                + " INNER JOIN " + Room.TABLE + " Room ON Room." + Room.KEY_RoomID + "=Patient."+Patient.KEY_RoomID
                + " WHERE Patient." + Patient.KEY_PatientID+ "='"+ Integer.toString(id)+"' AND PatientPlacement." + PatientPlacement.KEY_PlacementID+"='"+place+"';"
                ;
        //StuPlacement." + PatientPlacement.KEY_PlacementID
        //Patient." + Patient.KEY_PatientID

        Log.d(TAG, selectQuery);
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                patientPlacementList = new PatientPlacementList();
                patientPlacementList.setPatientID(cursor.getString(cursor.getColumnIndex(Patient.KEY_PatientID)));
                patientPlacementList.setPatientName(cursor.getString(cursor.getColumnIndex(Patient.KEY_Name)));
                patientPlacementList.setPlacementID(cursor.getString(cursor.getColumnIndex(Placement.KEY_PlacementID)));
                patientPlacementList.setRoomID(cursor.getString(cursor.getColumnIndex(Room.KEY_RoomID)));
                patientPlacementLists.add(patientPlacementList);
            } while (cursor.moveToNext());
        }

        cursor.close();
        DatabaseManager.getInstance().closeDatabase();

        return patientPlacementLists;

    }


    public void failALLBUPatient(){

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        String selectQuery =  " UPDATE  PatientPlacement " +
                "SET Grade= (SELECT 'F' FROM Patient WHERE Patient.PatientID=PatientPlacement.PatientID) " +
                "WHERE EXISTS( " +
                "SELECT * " +
                "FROM Patient " +
                "WHERE PatientPlacement.PatientID=Patient.PatientID AND RoomID='BU' " +
                ") "
                ;

        try{
            db.beginTransaction();

            Log.d(TAG, selectQuery);
            db.execSQL(selectQuery);
            db.setTransactionSuccessful();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }finally {
            db.endTransaction();
        }

        DatabaseManager.getInstance().closeDatabase();
    }

    public void deleteAllPatient(){

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        String selectQuery1 =
                " DELETE FROM PatientPlacement WHERE _id IN (SELECT _id FROM Patient WHERE RoomID=''); "
                ;

        String selectQuery2 =
                " DELETE FROM Patient WHERE RoomID='';"
                ;

        try{
            db.beginTransaction();

            Log.d(TAG, selectQuery1);
            Log.d(TAG, selectQuery2);
            db.execSQL(selectQuery1);
            db.execSQL(selectQuery2);
            db.setTransactionSuccessful();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }finally {
            db.endTransaction();
        }

        DatabaseManager.getInstance().closeDatabase();

    }

    public void deletePatient(int id) {
        Log.e(TAG, Integer.toString(id));
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        String selectQuery1 =
                " DELETE FROM "+ PatientPlacement.TABLE+ " WHERE " +Patient.KEY_PatientID +
                        " IN (SELECT "+Patient.KEY_PatientID+ " FROM " +Patient.TABLE+ " WHERE "
                        +Patient.KEY_PatientID+ "='"+ Integer.toString(id)+"'); "
                ;

        String selectQuery2 =
                " DELETE FROM "+ Patient.TABLE +" WHERE "+Patient.KEY_PatientID+ "='"+ Integer.toString(id)+"';"
                ;

        try{
            db.beginTransaction();

            Log.d(TAG, selectQuery1);
            Log.d(TAG, selectQuery2);
            db.execSQL(selectQuery1);
            db.execSQL(selectQuery2);
            Log.e(TAG, "Success!");
            db.setTransactionSuccessful();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            db.endTransaction();
        }

        DatabaseManager.getInstance().closeDatabase();
    }

    public Cursor getAllPatients() {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Cursor res = null;
        String selectQuery =  " SELECT * FROM "+Patient.TABLE+
                " ORDER BY "+ PatientPlacement.KEY_PatientID +" asc; "
                ;

        try{
            db.beginTransaction();

            Log.d(TAG, selectQuery);
            res = db.rawQuery(selectQuery,null);
            db.setTransactionSuccessful();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }finally {
            db.endTransaction();

        }

        return res;
    }

    public Cursor getPatient(int id) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Cursor res = null;
        String selectQuery =  " SELECT * FROM "+Patient.TABLE+
                " WHERE "+ PatientPlacement.KEY_PatientID +" = "+ Patient.KEY_PatientID+
                " AND "+Patient.KEY_PatientID + "='"+Integer.toString(id)+"';"
                ;

        try{
            db.beginTransaction();

            Log.d(TAG, selectQuery);
            res = db.rawQuery(selectQuery, null);
            db.setTransactionSuccessful();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }finally {
            db.endTransaction();

        }

        return res;
    }

    public boolean updatePlacement(PatientPlacement patientPlacement) {

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues values = new ContentValues();
        values.put(PatientPlacement.KEY_PatientID, patientPlacement.getPatientID());
        values.put(PatientPlacement.KEY_PlacementID, patientPlacement.getPlacementID());
        values.put(PatientPlacement.KEY_PatientPlacementSaveName, patientPlacement.getPatientPlacementSaveName());

        String selectQuery =  PatientPlacement.KEY_PatientID + " ='"+patientPlacement.getPatientID()+"';";

        // Inserting Row
        db.update(PatientPlacement.TABLE,values,selectQuery,null);
        DatabaseManager.getInstance().closeDatabase();

        return true;
    }

    public boolean placementExists(int id, String place) {

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        String selectQuery =
                " SELECT * FROM "+ PatientPlacement.TABLE+ " WHERE " +Patient.KEY_PatientID +
                " IN (SELECT "+Patient.KEY_PatientID+ " FROM " +Patient.TABLE+ " WHERE "
                +Patient.KEY_PatientID+ "='"+ Integer.toString(id)+"') AND "+PatientPlacement.KEY_PlacementID+"='"+place+"'; "
                ;

        Cursor res = db.rawQuery(selectQuery,null);
        boolean exists = (res.getCount() > 0);
        res.close();
        DatabaseManager.getInstance().closeDatabase();
        return exists;
    }

    public boolean roomExists(String _id) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Cursor res =  db.rawQuery("SELECT * FROM " + Room.TABLE + " WHERE " +
                Room.KEY_Name + "='"+ _id+"';",null);
        boolean exists = (res.getCount() > 0);
        res.close();
        DatabaseManager.getInstance().closeDatabase();
        return exists;
    }

}
