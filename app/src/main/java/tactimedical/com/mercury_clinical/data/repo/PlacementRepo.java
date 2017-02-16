package tactimedical.com.mercury_clinical.data.repo;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import tactimedical.com.mercury_clinical.data.DatabaseManager;
import tactimedical.com.mercury_clinical.data.model.Placement;

public class PlacementRepo {

    private Placement placement;

    public PlacementRepo(){

        placement = new Placement();

    }


    public static String createTable(){
        return "CREATE TABLE " + Placement.TABLE  + "("
                + Placement.KEY_PlacementID  + "   PRIMARY KEY    ,"
                + Placement.KEY_Name + " TEXT )";
    }


    public int insert(Placement placement) {
        int placementID;
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues values = new ContentValues();
        values.put(Placement.KEY_PlacementID, placement.getPlacementID());
        values.put(Placement.KEY_Name, placement.getName());

        // Inserting Row
        placementID=(int)db.insert(Placement.TABLE, null, values);
        DatabaseManager.getInstance().closeDatabase();

        return placementID;
    }



    public void delete( ) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        db.delete(Placement.TABLE,null,null);
        DatabaseManager.getInstance().closeDatabase();
    }






}
