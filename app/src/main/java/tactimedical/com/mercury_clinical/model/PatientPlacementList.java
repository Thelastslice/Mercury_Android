package tactimedical.com.mercury_clinical.model;

public class PatientPlacementList {
    private String patientID;
    private String patientName;
    private String placementID;
    private String placementName;
    private String RoomID;
    private String RoomName;
    private String PatientPlacementSaveName;


    public String getPlacementName() {
        return placementName;
    }

    public void setPlacementName(String placementName) {
        this.placementName = placementName;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPlacementID() {
        return placementID;
    }

    public void setPlacementID(String placementID) {
        this.placementID = placementID;
    }

    public String getRoomID() {
        return RoomID;
    }

    public void setRoomID(String roomID) {
        RoomID = roomID;
    }

    public String getRoomName() {
        return RoomName;
    }

    public void setRoomName(String roomName) {
        RoomName = roomName;
    }

    public String getPatientPlacementSaveName() {
        return PatientPlacementSaveName;
    }

    public void setPatientPlacementSaveName(String PatientPlacementSaveName) {
        this.PatientPlacementSaveName = PatientPlacementSaveName;
    }

}
