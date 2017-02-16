package tactimedical.com.mercury_clinical.data.model;

public class Patient {
    public static final String TAG = Patient.class.getSimpleName();
    public static final String TABLE = "Patient";

    // Labels Table Columns names
    public static final String KEY_PatientID = "_id";
    public static final String KEY_Name = "Name";
    public static final String KEY_RoomID = "RoomID";

    private String ID ;
    private String name;
    private String roomID ;

    public String getPatientID() {
        return ID;
    }

    public void setPatientID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoom() {
        return roomID;
    }

    public void setRoom(String roomID) {
        this.roomID = roomID;
    }

}
