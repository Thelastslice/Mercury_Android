package tactimedical.com.mercury_clinical.data.repo;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import tactimedical.com.mercury_clinical.data.DatabaseManager;
import tactimedical.com.mercury_clinical.data.model.Room;

public class RoomRepo {

    private Room room;

    public RoomRepo(){

        room = new Room();

    }


    public static String createTable(){
        return "CREATE TABLE " + Room.TABLE  + "("
                + Room.KEY_RoomID + " TEXT  PRIMARY KEY, "
                + Room.KEY_Name + " TEXT )";
    }



    public int insert(Room room) {
        int roomID;
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues values = new ContentValues();
        values.put(Room.KEY_RoomID, room.getRoomID());
        values.put(Room.KEY_Name, room.getName());

        // Inserting Row
        roomID=(int)db.insert(Room.TABLE, null, values);
        DatabaseManager.getInstance().closeDatabase();
        return roomID;

    }



    public void delete( ) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        db.delete(Room.TABLE, null,null);
        DatabaseManager.getInstance().closeDatabase();
    }








}
