package tactimedical.com.mercury_clinical.data.model;

public class Room {

    public static final String TAG = Room.class.getSimpleName();
    public static final String TABLE = "Room";
    // Labels Table Columns names
    public static final String KEY_RoomID = "RoomID";
    public static final String KEY_Name = "Name";

    private String roomID;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String ref) {
        this.roomID = ref;
    }
}
